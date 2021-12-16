package com.example.wavelengthlocationservices.ui.dashboard;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.example.wavelengthlocationservices.R;
import com.example.wavelengthlocationservices.databinding.FragmentDashboardBinding;
import com.example.wavelengthlocationservices.ui.AwsIoTMqttHelper;
import com.example.wavelengthlocationservices.ui.SharedViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
;
public class DashboardFragment extends Fragment {
    private SharedViewModel sharedViewModel;
    private FragmentDashboardBinding binding;
    TextView textTopic= null;
    TextView textMessage= null;
    TextView textFrequency= null;
    TextView textVolume = null;
    Spinner qos = null;
    Switch sIsIoTCore = null;
    Switch sTrackLocation = null;
    private ProgressBar progress;
    // Declare Context variable at class level in Fragment
    private Context mContext;

     public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        sharedViewModel =
                new ViewModelProvider(getActivity()).get(SharedViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        mContext = getActivity();
        Spinner spinner = binding.qos;
        textMessage = binding.textMessage;
        textFrequency= binding.textFrequency;
        textVolume = binding.textVolume;
        sIsIoTCore = binding.sIsIoTCore;
        sTrackLocation = binding.sIsTrackLocation;
        progress = binding.progressBar1;

        sharedViewModel.IsIoTConnected.setValue(false);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.qos_array, android.R.layout.simple_spinner_dropdown_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        BottomNavigationView navView = getActivity().findViewById(R.id.nav_view);

        Button bDefaults = binding.defaults;
        Button bPublish = binding.publish;
        bPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                bPublish.setEnabled(false);
                progress.setVisibility(View.VISIBLE);
                sharedViewModel.IsSendToIoTCore = sIsIoTCore.isChecked();
                sharedViewModel.IsTrackLocation = sTrackLocation.isChecked();
                int volume = Integer.parseInt(textVolume.getText().toString());
                sharedViewModel.volume.setValue(volume);
                sharedViewModel.delay = (Integer.parseInt(textFrequency.getText().toString()));
                sharedViewModel.message = textMessage.getText().toString();
                if (sharedViewModel.IsSendToIoTCore) {
                    sharedViewModel.awsIoT = new AwsIoTMqttHelper(getResources().getString(R.string.iot_endpoint),
                            Settings.Secure.getString(getContext().getContentResolver(),
                                    Settings.Secure.ANDROID_ID), new AWSIotMqttClientStatusCallback() {
                        @Override
                        public void onStatusChanged(final AWSIotMqttClientStatus status, final Throwable throwable) {
                            Log.d("mqtt", "Connection Status: " + String.valueOf(status));
                            if (status == AWSIotMqttClientStatus.Connected) {
                                sharedViewModel.IsIoTConnected.postValue(true);
                            } else {
                                if (sharedViewModel.IsIoTConnected.getValue())
                                    sharedViewModel.IsIoTConnected.postValue(false);
                            }
                        }
                    });
                } else sharedViewModel.IsIoTConnected.setValue(false);
            }
        });
        bDefaults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 String m = generateMessage();
                textFrequency.setText("1");
                textVolume.setText("10");
                textMessage.setText(m);
            }
        });
        // Create the observer which updates the UI.
         sharedViewModel.IsIoTConnected.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
             @Override
             public void onChanged(@Nullable final Boolean connected) {
                 if (!bPublish.isEnabled())
                 {
                     progress.setVisibility(View.GONE);
                     navView.setSelectedItemId(R.id.navigation_notifications);
                }
             }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private String generateMessage() {
        InputStream inputStream =  getResources().openRawResource(R.raw.defaultmessage);
        return new Scanner(inputStream).useDelimiter("\\A").next();
    }


}