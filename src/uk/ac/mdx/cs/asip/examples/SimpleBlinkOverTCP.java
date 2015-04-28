package uk.ac.mdx.cs.asip.examples;

import uk.ac.mdx.cs.asip.AsipClient;
import uk.ac.mdx.cs.asip.tcpclient.SimpleTCPBoard;

/* 
 * @author Franco Raimondi
 * 
 * A simple board with just the I/O services.
 * The main method does a standard blink test.
 * This one employs a TCP board.
 */
public class SimpleBlinkOverTCP extends SimpleTCPBoard {
	
	public SimpleBlinkOverTCP(String port) {
		super(port);
	}

	public static void main(String[] args) {
		// We could pass the IP address as an argument, for the moment
		// I hard-code it because I'm lazy.
		
		SimpleBlinkOverTCP testBoard = new SimpleBlinkOverTCP("192.168.0.73");
		
		try {
			Thread.sleep(2500);
			testBoard.getAsipClient().requestPortMapping();
			Thread.sleep(500);
			testBoard.getAsipClient().setPinMode(13, AsipClient.OUTPUT);
			Thread.sleep(500);
			testBoard.getAsipClient().setPinMode(2, AsipClient.INPUT_PULLUP);
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		while(true) {
			try {
				testBoard.getAsipClient().digitalWrite(13, AsipClient.HIGH);
				Thread.sleep(500);
				testBoard.getAsipClient().digitalWrite(13, AsipClient.LOW);
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
}
