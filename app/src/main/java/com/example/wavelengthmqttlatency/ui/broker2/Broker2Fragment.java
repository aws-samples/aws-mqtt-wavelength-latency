// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0
package software.amazon.samples.wavelengthmqttlatency.ui.broker2;

import android.app.AlertDialog;
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
import software.amazon.samples.wavelengthmqttlatency.databinding.FragmentBroker2Binding;
import software.amazon.samples.wavelengthmqttlatency.ui.SharedViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class Broker2Fragment extends Fragment implements MqttCallbackExtended {

    private SharedViewModel sharedViewModel;
    private FragmentBroker2Binding binding;
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
    TextView text = null;
    private ProgressBar spinner;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //sharedViewModel =
        //        new ViewModelProvider(this).get(SharedViewModel.class);
        sharedViewModel = new ViewModelProvider(getActivity()).get(SharedViewModel.class);

        binding = FragmentBroker2Binding.inflate(inflater, container, false);
        View root = binding.getRoot();
        textServerEndpoint = binding.textServerEndpoint;
        if(sharedViewModel._serverUrl2.toString() != "") {
            textServerEndpoint.setText(sharedViewModel._serverUrl2.toString());
        }
        textClientID = binding.textClientID;
        if(sharedViewModel._clientId2.toString() != "") {
            textClientID.setText(sharedViewModel._clientId2.toString());
        }
        textPassword= binding.textPassword;
        if(sharedViewModel._password2.toString() != "") {
            textPassword.setText(sharedViewModel._password2.toString());
        }
        textUsername = binding.textUsername;
        if(sharedViewModel._username2.toString() != "") {
            textUsername.setText(sharedViewModel._username2.toString());
        }
        textSubscriptionTopic = binding.textSubscriptionTopic;
        if(sharedViewModel._subTopic2.toString() != "") {
            textSubscriptionTopic.setText(sharedViewModel._subTopic2.toString());
        }

        bConnect = binding.connect;
        clean = binding.clean;
        disconnect = binding.disconnect;
        az = binding.azArray;
        broker = binding.broker;

        spinner = binding.progressBar1;
        if(sharedViewModel.IsConnected2.getValue() == null) sharedViewModel.IsConnected2.setValue(false);
        if(!sharedViewModel.IsConnected2.getValue()) {
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
        if(sharedViewModel._broker2 != "") {
            broker.setSelection(adapter.getPosition(sharedViewModel._broker2));
        }
        BottomNavigationView navView = getActivity().findViewById(R.id.nav_view);
        bConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                spinner.setVisibility(View.VISIBLE);
                String val = broker.getSelectedItem().toString();
                sharedViewModel._broker2 = val;
                sharedViewModel._az2 = az.getSelectedItem().toString();
                //Boolean isAwsIoT = ( val == "IoT Core" || val == "Greengrass") ? true : false;
                sharedViewModel.startMqtt2(getActivity().getApplicationContext(),
                        textServerEndpoint.getText().toString(),
                        textClientID.getText().toString(),
                        textUsername.getText().toString(),
                        textPassword.getText().toString(),
                        textSubscriptionTopic.getText().toString(), clean.isChecked());
            }
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
                if(sharedViewModel._az2 != "") {
                    az.setSelection(((ArrayAdapter)az.getAdapter()).getPosition(sharedViewModel._az2));
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
                String clientId2 = getResources().getString(R.string.sample_client_id2);
                String topic = getResources().getString(R.string.iot_topic_ingress2);
                String endpoint_mosquitto = getResources().getString(R.string.iot_endpoint_mosquito_2);
                String username = getResources().getString(R.string.iot_username);
                String password = getResources().getString(R.string.iot_password);
                switch (val) {
                    case "Mosquitto":
                        if (position == 1) {
                            textServerEndpoint.setText(endpoint_mosquitto);
                            textClientID.setText(clientId2);
                            textPassword.setText(password);
                            textUsername.setText(username);
                            textSubscriptionTopic.setText(topic);
                        } else if (position == 2) {
                            textServerEndpoint.setText(endpoint_mosquitto);
                            textClientID.setText(clientId2);
                            textPassword.setText(password);
                            textUsername.setText(username);
                            textSubscriptionTopic.setText(topic);
                        } else if (position == 3) {
                            textServerEndpoint.setText("tcp://155.146.57.97:1883");
                            textClientID.setText(clientId2);
                            textPassword.setText(password);
                            textUsername.setText(username);
                            textSubscriptionTopic.setText(topic);
                        }
                        break;
                    case "IoT Core":
                        if (position == 1) {
                            textServerEndpoint.setText("tcp:/" + getResources().getString(R.string.iot_endpoint));
                            textClientID.setText(clientId2);
                            textPassword.setText("");
                            textUsername.setText("");
                            textSubscriptionTopic.setText(topic);
                        }
                        break;
                    case "Greengrass":
                        if (position == 1) {
                            textServerEndpoint.setText("tcp://a3m15yqfy6j3pe-ats.iot.us-east-1.amazonaws.com");
                            textClientID.setText(clientId2);
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
                sharedViewModel.mqttHelper2.disconnect();
                sharedViewModel.IsConnected2.setValue(false);
                sharedViewModel.IsNew2.setValue(true);
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
        final Observer<Boolean> connectObserver2 = new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable final Boolean connected) {
                spinner.setVisibility(View.GONE);
                String msgC = "Connected.";
                String msgD = "Disconnected";
                if(sharedViewModel.IsNew2.getValue() == null) { sharedViewModel.IsNew2.setValue(false);}
                if(connected && !sharedViewModel.IsNew2.getValue()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Connection Status");
                    builder.setMessage((connected) ? msgC : msgD);
                    // add a button
                    builder.setPositiveButton("OK", null);
                    // create and show the alert dialog
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    navView.setSelectedItemId(R.id.navigation_dashboard);
                    sharedViewModel.IsNew2.setValue(true);
                } else if(!connected && sharedViewModel.IsNew2.getValue()) {
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
        sharedViewModel.IsConnected2.observe(getViewLifecycleOwner(), connectObserver2);
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

