package uk.ac.mdx.cs.asip;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

import uk.ac.mdx.cs.asip.tcpclient.SimpleTCPAsipListener;
import uk.ac.mdx.cs.asip.tcpclient.SimpleTCPWriter;

public class JMirtoRobotOverTCP extends JMirtoRobot {
	
	// For debugging
	boolean DEBUG = false;

	public static int SERVERPORT = 6789;
		// The IP address of the ASIP board.
	String boardIP;
	
	// The output stream to write messages to.
	DataOutputStream outputStream;
	DataInputStream inputStream;

	// This constructor takes the name of the serial port and it
	// creates the serialPort object and the asip client.
	// We then attach a listener to the serial port with SerialPortReader; this
	// listener calls the aisp method to process input.
	public void initialize(String boardIP) {
		this.boardIP = boardIP;
		Socket s = null;
		
		// FIXME: improve error handling
		try {
			s = new Socket(boardIP, SERVERPORT);
			outputStream = new DataOutputStream( s.getOutputStream());
			inputStream = new DataInputStream( s.getInputStream()); 
			asip = new AsipClient(new SimpleTCPWriter(outputStream));
			SimpleTCPAsipListener asipListener = new SimpleTCPAsipListener(inputStream, asip);
			Thread.sleep(800);
			this.asip.requestPortMapping();
			Thread.sleep(500);
			this.asip.requestPortMapping();
			Thread.sleep(500);
			this.asip.requestPortMapping();
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				s.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} 
	}
	


	// A main method for testing
	public static void main(String[] args) {
		
		JMirtoRobotOverTCP robot = new JMirtoRobotOverTCP();
		robot.initialize("192.168.42.1");
	
		
		try {
			Thread.sleep(500);
			robot.setup();
			Thread.sleep(200);	
			robot.clearLCDScreen();
			Thread.sleep(150);
			robot.writeLCDLine("Java Test Loop", 0);
			Thread.sleep(150);
			robot.writeLCDLine("  Line 1 ", 1);
			Thread.sleep(150);	
			robot.writeLCDLine("  Line 2 ", 2);
			Thread.sleep(150);		
			robot.writeLCDLine("  Line 3 ", 3);
			Thread.sleep(150);		
			robot.writeLCDLine("  Line 4 ", 4);
			Thread.sleep(150);		
			
			robot.playNote(262, 500);
			Thread.sleep(500);
			robot.playNote(294, 500);
			Thread.sleep(500);
			robot.playNote(330, 500);
			Thread.sleep(500);
			
			robot.setMotors(150,-150);
			while (true) {
				if (robot.isPressed(0) || robot.isPressed(1)  || (robot.getIR(1)>250)) {
					robot.stopMotors();
					Thread.sleep(10);
					robot.setMotors(-100,100);
					Thread.sleep(500);
					robot.stopMotors();
					Random rand = new Random();
					int rotationDuration = rand.nextInt(400);
					if ( rand.nextInt(2) == 1 ) {
						robot.setMotors(100, 100);
					} else {
						robot.setMotors(-100,-100);
					}
					Thread.sleep(200+rotationDuration);
					robot.stopMotors();
					Thread.sleep(10);
					robot.setMotors(150, -150);					
				}
				Thread.sleep(5);
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	} 
	

}
