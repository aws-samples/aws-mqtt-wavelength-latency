// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0
package com.example.wavelengthlocationservices.ui.notifications;

import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.regions.Regions;
import com.example.wavelengthlocationservices.R;
import com.example.wavelengthlocationservices.databinding.FragmentNotificationsBinding;
import com.example.wavelengthlocationservices.ui.AwsIoTMqttHelper;
import com.example.wavelengthlocationservices.ui.SharedViewModel;
import com.google.android.play.core.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class NotificationsFragment extends Fragment {

    private SharedViewModel sharedViewModel;
    private FragmentNotificationsBinding binding;
    private double mLongitude = 0;
    private double mLatitude = 0;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        sharedViewModel =
                new ViewModelProvider(getActivity()).get(SharedViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        TextView tv = binding.textNotifications;
        TextView tv2 = binding.textNotifications2;
        super.onCreate(savedInstanceState);

        sharedViewModel.totaltimes.setValue(0);
        sharedViewModel.totaltimes2.setValue(0);
        final int delay = sharedViewModel.delay;
        sharedViewModel.totalRTLatency = 0;
        final int volume = sharedViewModel.volume.getValue();

        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
        ScheduledThreadPoolExecutor executor2 = new ScheduledThreadPoolExecutor(1);

        if(sharedViewModel.IsTrackLocation)
            getLocation();

        executor.scheduleAtFixedRate(new Runnable() {
            int currentTime = 0;
            @Override
            public void run() {
                if(currentTime < volume - 1) {
                    currentTime = currentTime + 1;
                } else {
                    executor.shutdown();
                }
                publishMessage(mLatitude, mLongitude);
            }
        }, 0, delay, TimeUnit.SECONDS);
        if(sharedViewModel.mqttHelper2 != null) {
            executor2.scheduleAtFixedRate(new Runnable() {
                int currentTime2 = 0;
                @Override
                public void run() {
                    if(currentTime2 < volume - 1) {
                        currentTime2 = currentTime2 + 1;
                    } else {
                        executor2.shutdown();
                    }
                    publishMessage2(mLatitude, mLongitude);
                }
            }, 0, delay, TimeUnit.SECONDS);
        }

        sharedViewModel.results.observe(getViewLifecycleOwner(), new Observer<ArrayList<String>>() {
            @Override
            public void onChanged(ArrayList<String> strings) {
                StringBuilder builder = new StringBuilder();
                new JSONObject();
                JSONObject msg;
                Long count = strings.stream().count();
                Integer i = 1;
                for (String s: strings) {
                    try {
                        msg = new JSONObject(s);
                        builder.append("Attempt #{i} of {c}, Latency:{l}".replace("{c}", Long.toString(volume)).replace("{i}", Integer.toString(i)).replace("{l}", msg.get("Difference").toString()));
                        builder.append("\n");
                        i++;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Handler handler = new Handler();
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        if(sharedViewModel.IsSendToIoTCore) {
                            sharedViewModel.awsIoT.publishToTopic(getResources().getString(R.string.iot_topic_ingress), strings.get(count.intValue() - 1));
                        }
                    }
                };
                handler.post(r);
                tv.setText(builder.toString().trim()); // .trim to remove the trailing space.
            }
        });

        sharedViewModel.results2.observe(getViewLifecycleOwner(), new Observer<ArrayList<String>>() {
            @Override
            public void onChanged(ArrayList<String> strings) {
                StringBuilder builder = new StringBuilder();
                new JSONObject();
                JSONObject msg;
                Long count = strings.stream().count();
                Integer i = 1;
                for (String s: strings) {
                    try {
                        msg = new JSONObject(s);
                        builder.append("Attempt #{i} of {c}, Latency:{l}".replace("{c}", Long.toString(volume)).replace("{i}", Integer.toString(i)).replace("{l}", msg.get("Difference").toString()));
                        builder.append("\n");
                        i++;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Handler handler = new Handler();
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        if(sharedViewModel.IsSendToIoTCore) {
                            sharedViewModel.awsIoT.publishToTopic(getResources().getString(R.string.iot_topic_ingress), strings.get(count.intValue() - 1));
                        }
                    }
                };
                handler.post(r);
                tv2.setText(builder.toString().trim()); // .trim to remove the trailing space.

            }
        });

        sharedViewModel.totaltimes.observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer i) {
                if (i == sharedViewModel.volume.getValue() - 1) {
                    double avgLatency = sharedViewModel.totalRTLatency / sharedViewModel.volume.getValue();
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle(getResources().getString(R.string.sim_complete_msg));
                    builder.setMessage(getResources().getString(R.string.avg_latency_msg)+ avgLatency );
                    // create and show the alert dialog
                    try {
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    } catch (Exception e) {
                        Log.w("err", e.toString());
                    }
                    if(sharedViewModel.IsSendToIoTCore) {
                        //connect to IoT Core


                    }
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

    private String generateTelemetry(double latitude, double longitude) {
        String defaultMsg = sharedViewModel.message;

        defaultMsg = defaultMsg.replace("0.0000",  String.valueOf(latitude));
        defaultMsg = defaultMsg.replace("0.000",  String.valueOf(longitude));
        defaultMsg = defaultMsg.replace("{$TIMESTAMP}",  String.valueOf(System.currentTimeMillis()));
        return defaultMsg;
    }
    private void publishMessage(double lat, double lng){
        sharedViewModel.mqttHelper.publishToTopic(sharedViewModel._subTopic,generateTelemetry(lat, lng));
    }

    private void publishMessage2(double lat, double lng){
        sharedViewModel.mqttHelper2.publishToTopic(sharedViewModel._subTopic2,generateTelemetry(lat, lng));
    }

    private void getLocation() {
        boolean gps_enabled = false;
        boolean network_enabled = false;
        try {
            LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (gps_enabled) {
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        mLongitude = location.getLongitude();
                        mLatitude = location.getLatitude();
                    }
                });
            }

            if (network_enabled)
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        mLongitude = location.getLongitude();
                        mLatitude = location.getLatitude();
                    }
                });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}