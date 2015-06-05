package uk.ac.mdx.cs.asip.examples;

import uk.ac.mdx.cs.asip.AsipClient;
import uk.ac.mdx.cs.asip.mqtt.SimpleMQTTBoard;
import uk.ac.mdx.cs.asip.tcpclient.SimpleTCPBoard;

/* 
 * @author Mike Bottone
 * 
 * A simple board with just the I/O services on a fixed port.
 * The main method simulates a light switch.
 */

public class LightSwitchOverMQTT extends SimpleMQTTBoard {

	
	public LightSwitchOverMQTT(String port, String boardID) {
		super(port, boardID);
	}
	
	public static void main(String[] args) {
		
		int buttonPin = 2; // the number for the pushbutton pin on the Arduino
		int ledPin = 13;  // the number for the LED pin on the Arduino
		
		int buttonState = 0; // initialise the variable for when we press the button
		
	
		// We could pass the port as an argument, for the moment
		// I hard-code it because I'm lazy.
	
		LightSwitchOverMQTT testBoard = new LightSwitchOverMQTT("tcp://192.168.0.70","board1");
		
		testBoard.getAsipClient().requestPortMapping();

		try {
			Thread.sleep(2500);
			testBoard.getAsipClient().requestPortMapping();
			Thread.sleep(500);
			testBoard.getAsipClient().requestPortMapping();
			Thread.sleep(500);
			testBoard.getAsipClient().setPinMode(ledPin, AsipClient.OUTPUT);
			Thread.sleep(100);
			testBoard.getAsipClient().setPinMode(buttonPin, AsipClient.INPUT_PULLUP);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// read the current state of the button
		int oldstate = AsipClient.LOW;
		
		while (true) {
			
			buttonState = testBoard.getAsipClient().digitalRead(buttonPin);

			// check if the button is pressed and the corresponding state is
			// HIGH (1)
			// FIXME: we should check for state changed, otherwise we flood the
			// channel! For the moment I add a sleep(20) below.

			if (( buttonState != oldstate) && (buttonState == AsipClient.HIGH)) {

				testBoard.getAsipClient().digitalWrite(ledPin, AsipClient.HIGH);
				// Thread.sleep(500); }
			} else if ( buttonState != oldstate ){
				testBoard.getAsipClient().digitalWrite(ledPin, AsipClient.LOW); // we turn it
																// off otherwise
			}
			
			oldstate = buttonState;

			}
		
	}
		
	
	}


