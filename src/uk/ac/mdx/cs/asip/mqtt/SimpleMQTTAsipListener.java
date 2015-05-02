package uk.ac.mdx.cs.asip.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import uk.ac.mdx.cs.asip.AsipClient;

public class SimpleMQTTAsipListener implements MqttCallback {
	
	boolean DEBUG = true;
	
	AsipClient asip;
	
	public SimpleMQTTAsipListener(AsipClient a) {
		this.asip = a; 
	}

	@Override
	public void connectionLost(Throwable arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws MqttException {
		// Called when a message arrives from the server that matches any
		// subscription made by the client
		if ( DEBUG ) {
		System.out.println("  MQTT Message received: \t" +
                           "  Topic:\t" + topic +
                           "  Message:\t" + new String(message.getPayload()) +
                           "  QoS:\t" + message.getQos());
		}
		try {
			asip.processInput(new String(message.getPayload()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
