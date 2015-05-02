package uk.ac.mdx.cs.asip.mqtt;

import java.io.IOException;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import uk.ac.mdx.cs.asip.AsipWriter;

// A TCP writer for the Asip class
public class SimpleMQTTWriter implements AsipWriter {

	MqttClient mqttClient;
	String pubTopic;
	int qos;
	
	public SimpleMQTTWriter(MqttClient mc, String t, int q) {
		this.mqttClient = mc;
		this.pubTopic = t;
		this.qos = q;
	}
	
	@Override
	public void write(String val) {
		// TODO Auto-generated method stub
		// FIXME: add better exception handling
		try {
			MqttMessage message = new MqttMessage(val.getBytes());
			message.setQos(qos);
			mqttClient.publish(pubTopic, message);		
		} catch (Exception e) {
			// Something went wrong! We remove this
			// listener and we close the serial port
			e.printStackTrace();
		}
		
	}

}
