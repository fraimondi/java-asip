package uk.ac.mdx.cs.asip.examples;

import uk.ac.mdx.cs.asip.AsipClient;
import uk.ac.mdx.cs.asip.SimpleSerialBoard;
import uk.ac.mdx.cs.asip.mqtt.SimpleMQTTBoard;

/* Three boards using MQTT:
 * - board1 has a digital input (pullup) pin connected to pin 2
 * - board2 and board3 have 2 output pins: pin 12 and pin 13.
 * - when board1.pin2 is LOW: board2.pin12=HIGH, board2.pin13=LOW
 *   (and vice-versa for board3).
 * - when board1.pin2 is HIGH: vice-versa
 */
public class ThreeMQTTBoardsWithInput {

	public static void main(String args[]) {
		String broker = "tcp://192.168.0.70";
		SimpleMQTTBoard board1 = new SimpleMQTTBoard(broker, "board1");
		SimpleSerialBoard board2 = new SimpleSerialBoard("/dev/cu.usbmodem1411");
		SimpleMQTTBoard board3 = new SimpleMQTTBoard(broker, "board2");
		
		/* Setting up things */		
		try {
			board1.getAsipClient().requestPortMapping();
			board2.getAsipClient().requestPortMapping();
			board3.getAsipClient().requestPortMapping();
			Thread.sleep(500);
			board1.getAsipClient().requestPortMapping();
			board2.getAsipClient().requestPortMapping();
			board3.getAsipClient().requestPortMapping();
			Thread.sleep(500);
			board1.getAsipClient().requestPortMapping();
			board2.getAsipClient().requestPortMapping();
			board3.getAsipClient().requestPortMapping();
			Thread.sleep(500);
			board1.getAsipClient().setPinMode(2, AsipClient.INPUT_PULLUP);
			board2.getAsipClient().setPinMode(12, AsipClient.OUTPUT);
			board3.getAsipClient().setPinMode(12, AsipClient.OUTPUT);
			Thread.sleep(100);
			board3.getAsipClient().setAutoReportInterval(0);
			board2.getAsipClient().setAutoReportInterval(0);
			board1.getAsipClient().setAutoReportInterval(0);
			board2.getAsipClient().setPinMode(13, AsipClient.OUTPUT);
			board3.getAsipClient().setPinMode(13, AsipClient.OUTPUT);
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("All done, ready to go");
		/* Done with set-up */
		
		int oldstate = AsipClient.LOW;
		try {
			while (true) {

				int buttonState = board1.getAsipClient().digitalRead(2);

				if (( buttonState != oldstate) && (buttonState == AsipClient.HIGH)) {
					board2.getAsipClient().digitalWrite(12, AsipClient.HIGH);
					board3.getAsipClient().digitalWrite(12, AsipClient.LOW);
					Thread.sleep(30); // Just for safety
					board2.getAsipClient().digitalWrite(13, AsipClient.LOW);
					board3.getAsipClient().digitalWrite(13, AsipClient.HIGH);
				} else if ( buttonState != oldstate ){
					board2.getAsipClient().digitalWrite(12, AsipClient.LOW);
					board3.getAsipClient().digitalWrite(12, AsipClient.HIGH);
					Thread.sleep(50);
					board2.getAsipClient().digitalWrite(13, AsipClient.HIGH);
					board3.getAsipClient().digitalWrite(13, AsipClient.LOW);
				}

				oldstate = buttonState;

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	
}
