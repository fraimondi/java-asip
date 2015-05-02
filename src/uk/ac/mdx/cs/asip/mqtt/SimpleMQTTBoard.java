package uk.ac.mdx.cs.asip.mqtt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import uk.ac.mdx.cs.asip.AsipClient;
import uk.ac.mdx.cs.asip.tcpclient.SimpleTCPWriter;

public class SimpleMQTTBoard {

	static boolean DEBUG = true;

	// TODO: covert these to private fields, update constructors, getters, setters etc.
	public static int MQTT_SERVERPORT = 1883;
	public static String BROKER = "tcp://10.16.107.55";
	public static int QOS = 0;
	public static String clientID = "board1";
	public static String PUBTOPIC = "asip/"+clientID+"/out";
	public static String SUBTOPIC = "asip/"+clientID+"/in";
	
	// The client for the aisp protocol
	AsipClient asip;
	
	// The MQTT client
	MqttClient mqttClient = null;
	
	// The constructor opens the connection to the MQTT broker.
	// The ASIP writer is just a publisher; incoming messages
	// are intercepted by subscribing to the appropriate topic.
	public SimpleMQTTBoard(String boardIP) {
		
		MemoryPersistence persistence = new MemoryPersistence();
		
		try {
			String url = BROKER+":"+MQTT_SERVERPORT;
			mqttClient = new MqttClient(url, clientID, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            mqttClient.connect(connOpts);
            if (DEBUG) {
            	System.out.println("Connected to broker "+url);
            }
			asip = new AsipClient(new SimpleMQTTWriter(mqttClient,PUBTOPIC,QOS));
			mqttClient.subscribe(SUBTOPIC,QOS);
			mqttClient.setCallback(new SimpleMQTTAsipListener(asip));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 		
	}
	
	public AsipClient getAsipClient() {
		return this.asip;
	}
	
}
