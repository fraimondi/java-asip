package uk.ac.mdx.cs.asip.examples;

import uk.ac.mdx.cs.asip.AsipClient;
import uk.ac.mdx.cs.asip.SimpleSerialBoard;
import uk.ac.mdx.cs.asip.tcpclient.SimpleTCPBoard;

/* 
 * @author Franco Raimondi
 * 
 * A simple board to measure the time difference between setting a pin
 * and receiving the notification that it has changed.
 * 
 */

//public class LatencyTest extends SimpleSerialBoard {
public class LatencyTest extends SimpleTCPBoard {
	
	
	public LatencyTest(String port) {
		super(port);
	}
	
	public static void main(String[] args) {
		
		int buttonPin = 2; // the number for the detection pin on the Arduino
		int ledPin = 13;  // the number for the output pin on the Arduino
		
		int buttonState = 0; // initialise the variable for when we press the button
		
	
		// We could pass the port as an argument, for the moment
		// I hard-code it because I'm lazy.
	
		LatencyTest tB = new LatencyTest("192.168.2.8");
		
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
		
		while (true) {
			
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
				while ( buttonState != AsipClient.HIGH) {
					buttonState = testBoard.digitalRead(buttonPin);
				}
				long end = System.nanoTime();
				
				System.out.println((end-start)/1000000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
			
		
	}
		
	
	}


