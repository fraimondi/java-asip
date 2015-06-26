package uk.ac.mdx.cs.asip.examples;

import uk.ac.mdx.cs.asip.AsipClient;
import uk.ac.mdx.cs.asip.mqtt.SimpleMQTTBoard;

/* 
 * @author Franco Raimondi
 * @author Gianluca Barbon
 * 
 * Three boards using MQTT:
 * - board1 has a digital input (pullup) pin connected to pin 2
 * - board2 and board3 have 2 output pins: pin 12 and pin 13.
 * - when board1.pin2 is LOW: board2.pin12=HIGH, board2.pin13=LOW
 *   (and vice-versa for board3).
 * - when board1.pin2 is HIGH: vice-versa
 */
public class TwoMQTTBoardsWithInput {

	public static void main(String args[]) {
		int buttonPin = 2; // the number for the pushbutton pin on the Arduino
		int ledPin = 13;  // the number for the LED pin on the Arduino
		
		int buttonState = 0; // initialise the variable for when we press the button
		String broker = "tcp://192.168.0.101";
		SimpleMQTTBoard board2 = new SimpleMQTTBoard(broker, "board2");
		SimpleMQTTBoard board4 = new SimpleMQTTBoard(broker, "board4");
		
		/* Setting up things */		
		try {
			board2.getAsipClient().requestPortMapping();
			board4.getAsipClient().requestPortMapping();
			Thread.sleep(500);
			board2.getAsipClient().requestPortMapping();
			board4.getAsipClient().requestPortMapping();
			Thread.sleep(500);
			board2.getAsipClient().requestPortMapping();
			board4.getAsipClient().requestPortMapping();
			Thread.sleep(500);
			board2.getAsipClient().setAutoReportInterval(0);
			board4.getAsipClient().setAutoReportInterval(0);
			board4.getAsipClient().setPinMode(ledPin, AsipClient.OUTPUT);
			board2.getAsipClient().setPinMode(buttonPin, AsipClient.INPUT_PULLUP);
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("All done, ready to go");
		/* Done with set-up */
		
		// read the current state of the button
		int oldstate = AsipClient.LOW;
		
		while (true) {
			
			buttonState = board2.getAsipClient().digitalRead(buttonPin);

			// check if the button is pressed and the corresponding state is
			// HIGH (1)
			// FIXME: we should check for state changed, otherwise we flood the
			// channel! For the moment I add a sleep(20) below.

			if (( buttonState != oldstate) && (buttonState == AsipClient.LOW)) {

				board4.getAsipClient().digitalWrite(ledPin, AsipClient.HIGH);
				// Thread.sleep(500); }
			} else if ( buttonState != oldstate ){
				board4.getAsipClient().digitalWrite(ledPin, AsipClient.LOW); // we turn it
																// off otherwise
			}
			
			oldstate = buttonState;

			}
		
	}
	
	
	
}
