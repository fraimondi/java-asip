package uk.ac.mdx.cs.asip;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

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
		robot.initialize("192.168.0.119");
	
		
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
			
			while (true) {
				System.out.println("IR: "+robot.getIR(0) + ","+robot.getIR(1)+","+robot.getIR(2));
				System.out.println("Encoders: "+robot.getCount(0) + ","+robot.getCount(1));
				System.out.println("Bumpers: "+robot.isPressed(0) + ","+robot.isPressed(1));
				System.out.println("Setting motors to 50,50");
				System.out.println("Pot value: "+robot.getPotentiometer());
				System.out.println("Push button value: " + robot.getPushButton());
				robot.setMotors(100, 0);
				Thread.sleep(1500);
				System.out.println("Stopping motors");
				robot.stopMotors();
				Thread.sleep(500);
				System.out.println("Setting motors to 100,100");
				robot.setMotors(0,-250);
				Thread.sleep(1500);
				System.out.println("Stopping motors");
				robot.stopMotors();
				Thread.sleep(500);
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}

}
