package com.example.wavelengthlocationservices;


import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONObject;

public class MqttHelper {
    public MqttAndroidClient mqttAndroidClient;

    public String SubscriptionTopic = "";
    public String ServerUri = "";
    public String Username = "";
    public String Password = "";

    public MqttHelper(Context context, String serverUri, String clientId, String username, String password, String subscriptionTopic, Boolean clean){
        ServerUri = serverUri;
        Username = username;
        Password = password;
        SubscriptionTopic = subscriptionTopic;
        mqttAndroidClient = new MqttAndroidClient(context, serverUri, clientId);
        //mqttAndroidClient.setCallback(new MqttCallbackHandler(context));
        connect(clean);
    }

    public void setCallback(MqttCallbackExtended callback) {
        mqttAndroidClient.setCallback(callback);
    }

    public void disconnect() {
        if (mqttAndroidClient.isConnected()) {
            try {
                mqttAndroidClient.disconnect();
            } catch (MqttException e) {
                Log.e("TAG", e.toString());
            }
        }
    }

    private void connect(Boolean clean){
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(clean);
        mqttConnectOptions.setUserName(Username);
        mqttConnectOptions.setPassword(Password.toCharArray());

        try {

            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt", "Failed to connect to: " + ServerUri + exception.toString());
                }
            });

        } catch (MqttException ex){
            ex.printStackTrace();
        }
    }

    private void subscribeToTopic() {
        try {
            mqttAndroidClient.subscribe(SubscriptionTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.w("Mqtt","Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt", "Subscribed fail!");
                }
            });

        } catch (MqttException ex) {
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }
    }

    public void publishToTopic(String publishTopic, String content) {
        try {
            MqttMessage msg = new MqttMessage();
            msg.setPayload(StringEscapeUtils.unescapeJava(content).getBytes());
            mqttAndroidClient.publish(publishTopic, msg, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.w("Mqtt","Published to topic:" + publishTopic);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt", "Publish fail!");
                }
            });

        } catch (MqttException ex) {
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }
    }
}

