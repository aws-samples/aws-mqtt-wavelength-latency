// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0
package com.example.wavelengthlocationservices.ui;
import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;

import android.content.Context;
import android.util.Log;

import org.apache.commons.text.StringEscapeUtils;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.iot.AWSIotClient;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession;
import com.amplifyframework.core.Amplify;

import java.io.UnsupportedEncodingException;
import java.security.KeyStore;

public class AwsIoTMqttHelper {
    public AWSIotMqttManager mqttManager;
    private AWSCredentialsProvider clientCredentialsProvider;
    private AWSIotMqttClientStatusCallback userStatusCallback;
    private MqttManagerConnectionState connectionState;
    public String SubscriptionTopic = "";
    public String ServerUri = "";
    public String ClientId = "";
    public String Username = "";
    public String Password = "";
    public enum MqttManagerConnectionState {

        Connecting,

        Connected,

        Disconnected,

        Reconnecting

    }

    public MqttManagerConnectionState getConnectionState()
    {
        return connectionState;
    }

    public AwsIoTMqttHelper(String serverUri, String clientId, AWSIotMqttClientStatusCallback callback) {
        ServerUri = serverUri;
        ClientId = clientId;
        try {
            mqttManager = new AWSIotMqttManager(clientId, serverUri);
            mqttManager.connect(AWSMobileClient.getInstance(), callback);
        } catch (Exception e) {
            Log.d("exception", "Exception: " + String.valueOf(e));
        }
    }

    public void disconnect() {
            try {
                mqttManager.disconnect();
            } catch (Exception e) {
                Log.e("TAG", e.toString());
            }
    }

    public void connect(KeyStore keyStore,
                        Boolean clean
    ) {
        mqttManager.setAutoReconnect(true);
        mqttManager.setCleanSession(clean);
        //this.userStatusCallback = statusCallback;

        // Do nothing if Connecting, Connected or Reconnecting
        if (!MqttManagerConnectionState.Disconnected.equals(connectionState)) {
            this.userConnectionCallback(new Throwable());
            return;
        }

        try {
            mqttManager.connect(AWSMobileClient.getInstance(), new AWSIotMqttClientStatusCallback() {
                @Override
                public void onStatusChanged(final AWSIotMqttClientStatus status, final Throwable throwable) {
                    Log.d("AWSmqtt", "Connection Status: " + String.valueOf(status));
                }
            });
        } catch (final Exception e) {
            Log.e("AWSmqtt", "Connection error: ", e);
        }
    }

    private void subscribeToTopic(String subscriptionTopic) {
        mqttManager.subscribeToTopic(subscriptionTopic, AWSIotMqttQos.QOS0,
                new AWSIotMqttNewMessageCallback() {
                    @Override
                    public void onMessageArrived(final String topic, final byte[] data) {
                        try {
                            String message = new String(data, "UTF-8");
                            Log.d("AWSmqtt", "Message received: " + message);
                        } catch (UnsupportedEncodingException e) {
                            Log.e("AWSmqtt", "Message encoding error: ", e);
                        }
                    }
                });
    }

    public void publishToTopic(String publishTopic, String content) {
        try {
            mqttManager.publishString(content, publishTopic, AWSIotMqttQos.QOS0);
        } catch (Exception e) {
            Log.e("AWSmqtt", "Publish error: ", e);
        }
    }

    void userConnectionCallback(Throwable t) {
        if (userStatusCallback != null) {
            switch (connectionState) {
                case Connected:
                    userStatusCallback.onStatusChanged(
                            AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Connected, t);
                    break;
                case Connecting:
                    userStatusCallback.onStatusChanged(
                            AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Connecting, t);
                    break;
                case Reconnecting:
                    userStatusCallback.onStatusChanged(
                            AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Reconnecting, t);
                    break;
                case Disconnected:
                    userStatusCallback.onStatusChanged(
                            AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.ConnectionLost, t);
                    break;
                default:
                    throw new IllegalStateException("Unknown connection state.");
            }
        }
    }
}


