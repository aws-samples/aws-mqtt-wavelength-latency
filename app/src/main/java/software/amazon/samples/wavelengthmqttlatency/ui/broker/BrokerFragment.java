// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0
package software.amazon.samples.wavelengthmqttlatency.ui.broker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import software.amazon.samples.wavelengthmqttlatency.R;
import software.amazon.samples.wavelengthmqttlatency.TextValidator;
import software.amazon.samples.wavelengthmqttlatency.databinding.FragmentBrokerBinding;
import software.amazon.samples.wavelengthmqttlatency.ui.SharedViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class BrokerFragment extends Fragment implements MqttCallbackExtended {

    private SharedViewModel sharedViewModel;
    private FragmentBrokerBinding binding;
    TextView textServerEndpoint = null;
    TextView textClientID= null;
    TextView textPassword= null;
    TextView textUsername= null;
    TextView textSubscriptionTopic = null;
    Button bConnect = null;
    ToggleButton clean = null;
    Button disconnect = null;
    Spinner az = null;
    Spinner broker = null;
    private ProgressBar spinner;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        sharedViewModel = new ViewModelProvider(getActivity()).get(SharedViewModel.class);

        binding = FragmentBrokerBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        textServerEndpoint = binding.textServerEndpoint;
        if(!sharedViewModel._serverUrl.equals("")) {
            textServerEndpoint.setText(sharedViewModel._serverUrl);
        }
        textClientID = binding.textClientID;
        if(!sharedViewModel._clientId.equals("")) {
            textClientID.setText(sharedViewModel._clientId);
        }
        textPassword= binding.textPassword;
        if(!sharedViewModel._password.equals("")) {
            textPassword.setText(sharedViewModel._password);
        }
        textUsername = binding.textUsername;
        if(!sharedViewModel._username.equals("")) {
            textUsername.setText(sharedViewModel._username);
        }
        textSubscriptionTopic = binding.textSubscriptionTopic;
        if(!sharedViewModel._subTopic.equals("")) {
            textSubscriptionTopic.setText(sharedViewModel._subTopic);
        }

        bConnect = binding.connect;
        clean = binding.clean;
        disconnect = binding.disconnect;
        az = binding.azArray;
        broker = binding.broker;

        spinner = binding.progressBar1;
        if(sharedViewModel.IsConnected.getValue() == null) sharedViewModel.IsConnected.setValue(false);
        if(!sharedViewModel.IsConnected.getValue()) {
            bConnect.setEnabled(true);
            bConnect.setVisibility(View.VISIBLE);
            clean.setVisibility(View.VISIBLE);
        } else {
            bConnect.setVisibility(View.GONE);
            bConnect.setEnabled(false);
            disconnect.setEnabled(true);
            disconnect.setVisibility(View.VISIBLE);
            clean.setVisibility(View.GONE);
        }
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.broker_array, android.R.layout.simple_spinner_dropdown_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        broker.setAdapter(adapter);
        if(!sharedViewModel._broker.equals("")) {
            broker.setSelection(adapter.getPosition(sharedViewModel._broker));
        }
        BottomNavigationView navView = getActivity().findViewById(R.id.nav_view);
        bConnect.setOnClickListener(view -> {
            spinner.setVisibility(View.VISIBLE);
            sharedViewModel._broker = broker.getSelectedItem().toString();
            sharedViewModel._az = az.getSelectedItem().toString();
            //Boolean isAwsIoT = ( val == "IoT Core" || val == "Greengrass") ? true : false;
            sharedViewModel.startMqtt(getActivity().getApplicationContext(),
                    textServerEndpoint.getText().toString(),
                    textClientID.getText().toString(),
                    textUsername.getText().toString(),
                    textPassword.getText().toString(),
                    textSubscriptionTopic.getText().toString(), clean.isChecked());
        });
        broker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(position == 1) {
                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                            R.array.az_mosquitto_array, android.R.layout.simple_spinner_dropdown_item);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    // Apply the adapter to the spinner
                    az.setAdapter(adapter);
                } else if (position == 2) {
                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                            R.array.az_core_array, android.R.layout.simple_spinner_dropdown_item);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    // Apply the adapter to the spinner
                    az.setAdapter(adapter);
                } else if (position == 3) {
                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                            R.array.az_gg_array, android.R.layout.simple_spinner_dropdown_item);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    // Apply the adapter to the spinner
                    az.setAdapter(adapter);
                }
                if(!sharedViewModel._az.equals("")) {
                    az.setSelection(((ArrayAdapter)az.getAdapter()).getPosition(sharedViewModel._az));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }

        });

        az.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String val = broker.getSelectedItem().toString();
                String clientId = getResources().getString(R.string.sample_client_id);
                String topic = getResources().getString(R.string.iot_topic_ingress);
                String endpoint_mosquitto = getResources().getString(R.string.iot_endpoint_mosquito);
                String username = getResources().getString(R.string.iot_username);
                String password = getResources().getString(R.string.iot_password);
                switch (val) {
                    case "Mosquitto":
                        if (position == 1) {
                            textServerEndpoint.setText(endpoint_mosquitto);
                            textClientID.setText(clientId);
                            textPassword.setText(password);
                            textUsername.setText(username);
                            textSubscriptionTopic.setText(topic);
                        } else if (position == 2) {
                            textServerEndpoint.setText("tcp://155.146.57.97:1883");
                            textClientID.setText(clientId);
                            textPassword.setText(password);
                            textUsername.setText(username);
                            textSubscriptionTopic.setText(topic);
                        } else if (position == 3) {
                            textServerEndpoint.setText("tcp://155.146.57.97:1883");
                            textClientID.setText(clientId);
                            textPassword.setText(password);
                            textUsername.setText(username);
                            textSubscriptionTopic.setText(topic);
                        }
                        break;
                    case "IoT Core":
                        if (position == 1) {
                            textServerEndpoint.setText("tcp:/" + getResources().getString(R.string.iot_endpoint));
                            textClientID.setText(clientId);
                            textPassword.setText("");
                            textUsername.setText("");
                            textSubscriptionTopic.setText(topic);
                        }
                        break;
                    case "Greengrass":
                        if (position == 1) {
                            textServerEndpoint.setText("tcp://a3m15yqfy6j3pe-ats.iot.us-east-1.amazonaws.com");
                            textClientID.setText(clientId);
                            textPassword.setText("");
                            textUsername.setText("");
                            textSubscriptionTopic.setText(topic);
                        }
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
        disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedViewModel.mqttHelper.disconnect();
                sharedViewModel.IsConnected.setValue(false);
                sharedViewModel.IsNew.setValue(true);
            }
        });
        textServerEndpoint.addTextChangedListener(new TextValidator(textServerEndpoint) {
            @Override public void validate(TextView textView, String text) {
                if (text.equals("") || text.length() <= 3 ) {
                    if (text.equals("")) {
                        textServerEndpoint.setError("Endpoint cannot be Blank");
                    } else {
                        textServerEndpoint.setError("Enter Valid Endpoint");
                    }
                }
                checkRequiredFields();
            }
        });
        textUsername.addTextChangedListener(new TextValidator(textUsername) {
            @Override public void validate(TextView textView, String text) {
                if (text.equals("") || text.length() <= 3 ) {
                    if (text.equals("")) {
                        textUsername.setError("Username cannot be Blank");
                    } else {
                        textUsername.setError("Enter Valid Username");
                    }
                }
                checkRequiredFields();
            }
        });
        textPassword.addTextChangedListener(new TextValidator(textPassword) {
            @Override public void validate(TextView textView, String text) {
                if (text.equals("") || text.length() <= 3 ) {
                    if (text.equals("")) {
                        textPassword.setError("Password cannot be Blank");
                    } else {
                        textPassword.setError("Enter Valid Password");
                    }
                }
                checkRequiredFields();
            }
        });
        textClientID.addTextChangedListener(new TextValidator(textClientID) {
            @Override public void validate(TextView textView, String text) {
                if (text.equals("") || text.length() <= 3 ) {
                    if (text.equals("")) {
                        textClientID.setError("ClientID cannot be Blank");
                    } else {
                        textClientID.setError("Enter Valid ClientID");
                    }
                }
                checkRequiredFields();
            }
        });
        textSubscriptionTopic.addTextChangedListener(new TextValidator(textSubscriptionTopic) {
            @Override public void validate(TextView textView, String text) {
                if (text.equals("") || text.length() <= 3 ) {
                    if (text.equals("")) {
                        textSubscriptionTopic.setError("Subscription Topic cannot be Blank");
                    } else {
                        textSubscriptionTopic.setError("Enter Valid Subscription Topic");
                    }
                }
                checkRequiredFields();
            }
        });

        // Create the observer which updates the UI.
        final Observer<Boolean> connectObserver = new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable final Boolean connected) {
                spinner.setVisibility(View.GONE);
                String msgC = "Connected.  Connect another?";
                String msgD = "Disconnected";
                if(sharedViewModel.IsNew.getValue() == null) { sharedViewModel.IsNew.setValue(false);}
                if(connected && !sharedViewModel.IsNew.getValue()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Connection Status");
                    builder.setMessage((connected) ? msgC : msgD);
                    // add a button
                    sharedViewModel.IsNew.setValue(true);
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialog, int which) {
                            navView.setSelectedItemId(R.id.navigation_broker2);
                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialog, int which) {
                            navView.setSelectedItemId(R.id.navigation_dashboard);
                            dialog.dismiss();
                        }
                    });
                    // create and show the alert dialog
                    AlertDialog dialog = builder.create();
                    dialog.show();

                } else if(!connected && sharedViewModel.IsNew.getValue()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Connection Status");
                    builder.setMessage("Disconnected");
                    // add a button
                    builder.setPositiveButton("OK", null);
                    // create and show the alert dialog
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    bConnect.setEnabled(true);
                    bConnect.setVisibility(View.VISIBLE);
                    clean.setVisibility(View.VISIBLE);
                    disconnect.setVisibility((View.GONE));

                }
            }
        };

        sharedViewModel.IsConnected.observe(getViewLifecycleOwner(), connectObserver);
        return root;
    }

    private void checkRequiredFields() {
        String val = broker.getSelectedItem().toString();
        if(val == "Mosquitto") {
            bConnect.setEnabled(!textServerEndpoint.getText().toString().isEmpty()
                    && !textClientID.getText().toString().isEmpty()
                    && !textPassword.getText().toString().isEmpty()
                    && !textUsername.getText().toString().isEmpty()
                    && !textSubscriptionTopic.getText().toString().isEmpty());
        } else {
            bConnect.setEnabled(!textServerEndpoint.getText().toString().isEmpty()
                    && !textClientID.getText().toString().isEmpty()
                    && !textSubscriptionTopic.getText().toString().isEmpty());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {

    }

    @Override
    public void connectionLost(Throwable cause) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }
}

