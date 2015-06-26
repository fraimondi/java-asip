package uk.ac.mdx.cs.asip.tests;

import uk.ac.mdx.cs.asip.AsipClient;
import uk.ac.mdx.cs.asip.mqtt.SimpleMQTTBoard;

/* 
 * @author Franco Raimondi
 * @author Gianluca Barbon
 * 
 * A simple board to measure the time difference between setting a pin
 * and receiving the notification that it has changed.
 * 
 */

public class MQTTLatencyTest extends SimpleMQTTBoard {

	public MQTTLatencyTest(String port, String boardID) {
		super(port, boardID);
	}

	public static void main(String[] args) {

		int buttonPin = 2; // the number for the detection pin on the Arduino
		int ledPin = 13; // the number for the output pin on the Arduino

		int buttonState = 0; // initialise the variable for when we press the
		// button

		// We could pass the port as an argument, for the moment
		MQTTLatencyTest tB = new MQTTLatencyTest("tcp://127.0.0.1","board4");

		AsipClient testBoard = tB.getAsipClient();

		testBoard.requestPortMapping();

		try {
			testBoard.requestPortMapping();
			Thread.sleep(500);
			testBoard.requestPortMapping();
			Thread.sleep(500);
			testBoard.setPinMode(ledPin, AsipClient.OUTPUT);
			Thread.sleep(100);
			testBoard.setPinMode(buttonPin, AsipClient.INPUT);
			Thread.sleep(100);
			testBoard.setAutoReportInterval(0);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// read the current state of the button
		int i = 0;
		double total = 0;
		while (i < 100) {

			// Setting up things

			buttonState = AsipClient.LOW;
			testBoard.digitalWrite(ledPin, AsipClient.LOW);

			try {
				// Wait a little bit before entering the loop
				System.out.println("Doing it...");
				Thread.sleep(500);

				// Setting the pin to high
				testBoard.digitalWrite(ledPin, AsipClient.HIGH);

				// read time
				long start = System.nanoTime();
				while (buttonState != AsipClient.HIGH) {
					buttonState = testBoard.digitalRead(buttonPin);
					System.out.print("");; // FIXME: not working without this interrupt, why??
				}
				long end = System.nanoTime();
				double diff = (end - start) / 1000000;
				System.out.println(diff);
				i += 1;
				total += diff;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		double avg = total / i;
		System.out.println("Number of iteration is " + i
				+ ". Average latency is " + avg);
		System.exit(0);

	}

}
