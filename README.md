# Wavelength MQTT Latency Tester

This Android application is purpose-built to test 5G roundtrip latency to EC2 MQTT brokers in a Wavelength zone. This application will all the user to connect to two separate MQTT brokers (Mosquitto is supported). We recommend implementing a Wavelength instance and a Region Instance.  The application will then connect to both brokers and send threaded packets with the location of the 5G device included in the payload.  This data can then be sent to IoT Core to be displayed on a Grafana instance for latency over time or on a map.  This workshop provides that implementation setup.  

## Prerequisites

The following libraries are required to run this Android application:
* [AWS Android SDK](https://github.com/aws-amplify/aws-sdk-android)
* [AWS Android IoT SDK](https://github.com/aws-amplify/aws-sdk-android/tree/main/aws-android-sdk-iot)
* [AWS Amplify](https://docs.amplify.aws/start/q/integration/android/)
* [Paho MQTT Libaries](https://repo.eclipse.org/content/repositories/paho-releases/org/eclipse/paho/org.eclipse.paho.android.service/)

Make sure you have the following installed: 

* [Node.js](https://nodejs.org/) v12.x or later
* [npm](https://www.npmjs.com/) v5.x or later
*  [git](https://git-scm.com/) v2.14.1 or later
   
Install the following (if not already installed):

* Install [Android Studio](https://developer.android.com/studio/index.html#downloads) version 4.0 or higher
* Install the [Android SDK API](https://developer.android.com/studio/releases/platforms) level 29 (Android 10)
* Install [Amplify CLI](https://docs.amplify.aws/cli/) version 4.21.0 or later by running:

```npm install -g @aws-amplify/cli```

Now it's time to setup the Amplify CLI. Configure Amplify by running the following command:

```amplify configure```

amplify configure will ask you to sign into the AWS Console.

We will finalized setup of Amplify later when we want to add Amplify CLI Authentication to the Android application

## Setup MQTT Brokers

Installing and configuring your Mosquitto brokers is outside the scope of this guide, but this [AWS Wavelength Workshop](https://studio.us-east-1.prod.workshops.aws/workshops/903b6952-004e-4a3e-b267-5bf129cea9b4#builds) provides an overview of installation and configuration.

Once your EC2 Instances are setup, you will need the public DNS of each one to configure in the ```strings.xml``` resource file.  

**Please note two MQTT brokers are not required**

Edit the app -> res -> values -> strings.xml and replace your EC2 endpoint

```
<string name="iot_endpoint_mosquito">tcp://ec2-11-222-33-444.us-west-2.compute.amazonaws.com:1883</string>
<string name="iot_endpoint_mosquito_2">tcp://ec2-11-222-33-445.compute-1.amazonaws.com:1883</string>
```

Additionally, replace the username and password with the values you set for your MQTT implementation on the EC2 Instances

```
    <string name="iot_username">InsertMQTTUsernameHere</string>
    <string name="iot_password">InsertPasswordHere</string>
```