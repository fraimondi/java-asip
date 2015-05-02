package uk.ac.mdx.cs.asip.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

/* This class acts as a bridge between a TCP connection
 * and a serial connection. The idea is that 
 * this could run on a machine (say a Raspberry Pi) to which
 * an Arduino ASIP is connected via USB.
 * Most of the code is repeated from SimpleSerialBoard, so maybe 
 * it could be re-engineered...
 */

public class SimpleAsipMQTTSerialBridge {

	static boolean DEBUG = true;

	public static int MQTT_SERVERPORT = 1883;
	public static String BROKER = "tcp://10.16.107.55";
	public static int QOS = 0;
	
	// This board uses serial communication (provided by jssc)
	SerialPort serialPort;


	String pubTopic = "asip/board1/out";
	String subTopic = "asip/board1/in";
	
	String clientID = "board1";
	
	MqttClient mqttClient;
	
	public SimpleAsipMQTTSerialBridge(String port) {

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
            MQTTBridge b = new MQTTBridge(port);
            b.bridge();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// The Bridge opens the serial connection
	class MQTTBridge implements MqttCallback {
		public MQTTBridge(String port) {
			serialPort = new SerialPort(port);
			if (DEBUG) {
				System.out.println("Setting up the serial port.");
			}
			try {

				serialPort.openPort();// Open port
				serialPort.setParams(57600, 8, 1, 0);
				serialPort.setDTR(false);
				Thread.sleep(250);
				serialPort.setDTR(true);
				// Set params
				int mask = SerialPort.MASK_RXCHAR; // + SerialPort.MASK_CTS
				// + SerialPort.MASK_DSR;// Prepare mask
				serialPort.setEventsMask(mask);// Set mask
			} catch (Exception ex) {
				System.out.println(ex);
			} finally {
				try {
					if (DEBUG) {
						System.out.println("Adding the serial port listener");
					}
					serialPort.addEventListener(new SerialPortReaderMQTT());
				} catch (SerialPortException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (DEBUG) {
				System.out.println("End of constructor");
			}

		}

		public void bridge() {
			// Everything that comes through the TCP connection is sent to the
			// serial
			// port.
			if (DEBUG) {
				System.out.println("Starting the MQTT subscriber");
			}
			try {
				mqttClient.subscribe(subTopic,QOS);
				mqttClient.setCallback(this);
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public void messageArrived(String topic, MqttMessage message) throws MqttException {
			// Called when a message arrives from the server that matches any
			// subscription made by the client
			if ( DEBUG ) {
			System.out.println("  MQTT Message received: \t" +
	                           "  Topic:\t" +
	                           "  Message:\t" + new String(message.getPayload()) +
	                           "  QoS:\t" + message.getQos());
			}
			try {
				serialPort.writeString(new String(message.getPayload())+"\n");
			} catch (SerialPortException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// When an event is received on the serial port, it is published
		// to the broker	
		class SerialPortReaderMQTT implements SerialPortEventListener {

			private String buffer = ""; // we store partial messages here.

			public void serialEvent(SerialPortEvent event) {
				if (event.isRXCHAR()) {// If data is available
					try {
						String val = serialPort.readString();
						if (val != null) { // Needed in win!
							// System.out.println("DEBUG: received on serial: "+val);
							if (val.contains("\n")) {
								// If there is at least one newline, we need to
								// process
								// the message (the buffer may contain previous
								// characters).
								while (val.contains("\n") && (val.length() > 0)) {
									// But remember that there could be more
									// than one newline
									// in the buffer
									buffer += val.substring(0,
											val.indexOf("\n"));
									// System.out.println("DEBUG: processing "+buffer);
									try {
										MqttMessage message = new MqttMessage(buffer.getBytes());
										message.setQos(QOS);
										mqttClient.publish(pubTopic, message);		
									} catch (Exception e) {
										// Something went wrong! We remove this
										// listener and we close the serial port
										e.printStackTrace();
										serialPort.removeEventListener();
										serialPort.closePort();
									}
									buffer = "";
									val = val.substring(val.indexOf("\n") + 1);
								}
								// If there is some leftover to process we add
								// tu buffer
								if (val.length() > 0) {
									buffer = val;
								}
							} else {
								buffer += val;
							}
						}
					} catch (SerialPortException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

		@Override
		public void connectionLost(Throwable arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void deliveryComplete(IMqttDeliveryToken arg0) {
			// TODO Auto-generated method stub
			
		}
	}

	public static void main(String args[]) {
		SimpleAsipMQTTSerialBridge bridge = new SimpleAsipMQTTSerialBridge(
				"/dev/cu.usbmodem1411");
	}
}
