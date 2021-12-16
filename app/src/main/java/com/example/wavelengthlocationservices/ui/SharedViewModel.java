package com.example.wavelengthlocationservices.ui;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.wavelengthlocationservices.MqttHelper;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import java.security.KeyStore;
import java.util.ArrayList;

public class SharedViewModel extends ViewModel {
    public MqttHelper mqttHelper;
    public MqttHelper mqttHelper2;
    public MutableLiveData<Boolean> IsConnected = new MutableLiveData<>();
    public MutableLiveData<Boolean> IsConnected2 = new MutableLiveData<>();
    public MutableLiveData<Boolean> IsIoTConnected = new MutableLiveData<>();
    public MutableLiveData<Boolean> IsNew = new MutableLiveData<>();
    public MutableLiveData<Boolean> IsNew2 = new MutableLiveData<>();
    public MutableLiveData<ArrayList<String>> results = new MutableLiveData<>();
    public MutableLiveData<ArrayList<String>> results2 = new MutableLiveData<>();
    public double totalRTLatency = 0;
    public double totalRTLatency2 = 0;
    public MutableLiveData<Integer> totaltimes = new MutableLiveData<>();
    public MutableLiveData<Integer> totaltimes2 = new MutableLiveData<>();
    public MutableLiveData<Integer> volume = new MutableLiveData<>();
    private ArrayList<String> diffs = new ArrayList<String>();
    private ArrayList<String> diffs2 = new ArrayList<String>();
    public String _serverUrl = "";
    public String _clientId = "";
    public String _username = "";
    public String _password = "";
    public String _subTopic = "";
    public String _broker = "";
    public String _serverUrl2 = "";
    public String _clientId2 = "";
    public String _username2 = "";
    public String _password2 = "";
    public String _subTopic2 = "";
    public String _broker2 = "";
    public String _az = "";
    public String _az2 = "";
    public Integer delay;
    public String message = "";
    public Boolean IsSendToIoTCore = false;
    public Boolean IsTrackLocation = false;
    private final MutableLiveData<String> mText;
    public AwsIoTMqttHelper awsIoT;
    public SharedViewModel() {
        mText = new MutableLiveData<>();
    }

    public LiveData<String> getText() {
        return mText;
    }

    public void startMqtt(Context ctx, String serverUri, String clientId, String username, String password, String subscriptionTopic, Boolean clean){
        _serverUrl = serverUri;
        _clientId = clientId;
        _username = username;
        _password = password;
        _subTopic = subscriptionTopic;

        mqttHelper = new MqttHelper(ctx, serverUri, clientId, username, password, subscriptionTopic, clean);
        _serverUrl = serverUri;
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.w("Mqtt", s);
                IsConnected.setValue(true);
                IsNew.setValue(true);
            }

            @Override
            public void connectionLost(Throwable throwable) {
                IsConnected.setValue(false);
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                totaltimes.setValue(totaltimes.getValue() + 1);
                String json = "{\"URI\": \"{uri}\",\"ClientId\": \"{latitude}\",\"Latitude\": \"{latitude}\",\"Longitude\": \"{longitude}\", \"SendTime\": {sendtime}, \"ArriveTime\": {arrivetime}, \"Difference\": {diff} }";
                JSONObject msg = new JSONObject(mqttMessage.toString());
                Long sendTime = Long.parseLong(msg.getString("SendTimeStamp"));
                Long arriveTime = System.currentTimeMillis();
                Long diff = arriveTime - sendTime;
                totalRTLatency = totalRTLatency + diff;
                json = json.replace("{uri}", _serverUrl)
                        .replace("{latitude}", msg.getJSONObject("GeoLocation").getString("Latitude"))
                        .replace("{longitude}", msg.getJSONObject("GeoLocation").getString("Longitude"))
                        .replace("{sendtime}", msg.getString("SendTimeStamp"))
                        .replace("{arrivetime}", Long.toString(arriveTime))
                        .replace("{diff}", Long.toString(diff));
                diffs.add(json);
                results.setValue(diffs);
                Log.w("Mqtt", Long.toString(diff));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                Log.w("Mqtt", iMqttDeliveryToken.toString());
            }
        });
    }

    public void startMqtt2(Context ctx, String serverUri, String clientId, String username, String password, String subscriptionTopic, Boolean clean){
        _serverUrl2 = serverUri;
        _clientId2 = clientId;
        _username2 = username;
        _password2 = password;
        _subTopic2 = subscriptionTopic;

        mqttHelper2 = new MqttHelper(ctx, serverUri, clientId, username, password, subscriptionTopic, clean);
        _serverUrl2 = serverUri;
        mqttHelper2.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.w("Mqtt", s);
                IsConnected2.setValue(true);
                IsNew2.setValue(true);
            }

            @Override
            public void connectionLost(Throwable throwable) {
                IsConnected2.setValue(false);
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                totaltimes2.setValue(totaltimes2.getValue() + 1);
                String json = "{\"URI\": \"{uri}\",\"ClientId\": \"{latitude}\",\"Latitude\": \"{latitude}\",\"Longitude\": \"{longitude}\", \"SendTime\": {sendtime}, \"ArriveTime\": {arrivetime}, \"Difference\": {diff} }";
                JSONObject msg = new JSONObject(mqttMessage.toString());
                Long sendTime = Long.parseLong(msg.getString("SendTimeStamp"));
                Long arriveTime = System.currentTimeMillis();
                Long diff = arriveTime - sendTime;
                totalRTLatency2 = totalRTLatency2 + diff;
                json = json.replace("{uri}", _serverUrl2)
                        .replace("{latitude}", msg.getJSONObject("GeoLocation").getString("Latitude"))
                        .replace("{longitude}", msg.getJSONObject("GeoLocation").getString("Longitude"))
                        .replace("{sendtime}", msg.getString("SendTimeStamp"))
                        .replace("{arrivetime}", Long.toString(arriveTime))
                        .replace("{diff}", Long.toString(diff));
                diffs2.add(json);
                results2.setValue(diffs2);
                Log.w("Mqtt", Long.toString(diff));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                Log.w("Mqtt", iMqttDeliveryToken.toString());
            }
        });
    }
}
