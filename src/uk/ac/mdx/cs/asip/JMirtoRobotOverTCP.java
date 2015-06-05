package uk.ac.mdx.cs.asip;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import uk.ac.mdx.cs.asip.services.BumpService;
import uk.ac.mdx.cs.asip.services.EncoderService;
import uk.ac.mdx.cs.asip.services.IRService;
import uk.ac.mdx.cs.asip.services.MotorService;
import uk.ac.mdx.cs.asip.tcpclient.SimpleTCPAsipListener;
import uk.ac.mdx.cs.asip.tcpclient.SimpleTCPWriter;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

public class JMirtoRobotOverTCP {
	
	// For debugging
	boolean DEBUG = false;

	public static int SERVERPORT = 6789;
	
	// The client for the aisp protocol
	AsipClient asip;

	// The IP address of the ASIP board.
	String boardIP;
	
	// The output stream to write messages to.
	DataOutputStream outputStream;
	DataInputStream inputStream;
	
	// The robot has 2 motors (wheels), 2 encoders, 3 IR sensors, 2 bump sensors
	private MotorService m0, m1;
	private EncoderService e0, e1;
	private IRService ir0,ir1,ir2;
	private BumpService b0,b1;
	
	
	
	// This constructor takes the name of the serial port and it
	// creates the serialPort object and the asip client.
	// We then attach a listener to the serial port with SerialPortReader; this
	// listener calls the aisp method to process input.
	public JMirtoRobotOverTCP(String boardIP) {
		this.boardIP = boardIP;
		Socket s = null;
		
		// FIXME: improve error handling
		try {
			s = new Socket(boardIP, SERVERPORT);
			outputStream = new DataOutputStream( s.getOutputStream());
			inputStream = new DataInputStream( s.getInputStream()); 
			asip = new AsipClient(new SimpleTCPWriter(outputStream));
			SimpleTCPAsipListener asipListener = new SimpleTCPAsipListener(inputStream, asip);
			
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
	
	// We set up things here, attaching services etc.
	public void setup() {

		// Adding two motors.
		m0 = new MotorService(0, this.asip);
		m1 = new MotorService(1, this.asip);
		this.asip.addService('M', m0);
		this.asip.addService('M', m1);		
		if (DEBUG) {
			System.out.println("DEBUG: 2 motor services added");
		}
		
		// Adding two encoders
		e0 = new EncoderService(0, this.asip);
		e1 = new EncoderService(1, this.asip);
		e0.setReportingInterval(25);
		e1.setReportingInterval(25);
		this.asip.addService('E', e0);
		this.asip.addService('E', e1);
		if (DEBUG) {
			System.out.println("DEBUG: 2 encoder services added");
		}
		
		// Adding 3 IR sensors
		ir0 = new IRService(0, this.asip);
		ir1 = new IRService(1, this.asip);
		ir2 = new IRService(2, this.asip);
		ir0.setReportingInterval(25);
		ir1.setReportingInterval(25);
		ir2.setReportingInterval(25);
		this.asip.addService('R', ir0);
		this.asip.addService('R', ir1);
		this.asip.addService('R', ir2);
		if (DEBUG) {
			System.out.println("DEBUG: 3 IR services added");
		}
		
		// Adding two bumpers
		b0 = new BumpService(0, this.asip);
		b1 = new BumpService(1, this.asip);
		b0.setReportingInterval(25);
		b1.setReportingInterval(25);
		this.asip.addService('B', b0);
		this.asip.addService('B', b1);
		if (DEBUG) {
			System.out.println("DEBUG: 2 bumper services added");
		}
		
	}
	
	// Setting the two motors speed
	public void setMotors(int s0, int s1) {
		m0.setMotor(s0);
		m1.setMotor(s1);
		if (DEBUG) {
			System.out.println("DEBUG: setting motors to ("+s0+","+s1+")");
		}
	}
	
	public void stopMotors() {
		m0.stopMotor();
		m1.stopMotor();
	}
	
	public int getIR(int i) {
		// Franco, this is horrible code, IR should be a list!
		// FIXME
		switch (i) {

		case 0: 
			return ir0.getIR();
			
		case 1:
			return ir1.getIR();
		
		case 2:
			return ir2.getIR();
		
		default: 
			return -1;	
		}
	}
	
	
	public int getCount(int i) {
		// As above, this is horrible code.
		// FIXME
		switch (i) {

		case 0: 
			return e0.getCount();
			
		case 1:
			return e1.getCount();

		default: 
			return -1;	
		}
	}
	
	public boolean isPressed(int i) {
		// As above, this is horrible code.
		// FIXME
		switch (i) {
		case 0: 
			return b0.isPressed();
			
		case 1:
			return b1.isPressed();

		default: 
			return false;	
		}			
	}


	// A main method for testing
	public static void main(String[] args) {
		
//		JMirtoRobot robot = new JMirtoRobot("/dev/tty.usbserial-A903VH1D");
		JMirtoRobotOverTCP robot = new JMirtoRobotOverTCP("192.168.0.100");

		
		try {
			Thread.sleep(500);
			robot.setup();
			Thread.sleep(500);	
			while (true) {
				System.out.println("IR: "+robot.getIR(0) + ","+robot.getIR(1)+","+robot.getIR(2));
				System.out.println("Encoders: "+robot.getCount(0) + ","+robot.getCount(1));
				System.out.println("Bumpers: "+robot.isPressed(0) + ","+robot.isPressed(1));
				System.out.println("Setting motors to 50,50");
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
/*			System.out.println("Setting motors to 50,50");
			robot.setMotors(50, 50);
			Thread.sleep(3000);
			System.out.println("Stopping motors");
			robot.stopMotors();
			Thread.sleep(500);
			System.out.println("Setting motors to 80,-80");
			robot.setMotors(80, -80);
			Thread.sleep(3000);
			System.out.println("Stopping motors");
			robot.stopMotors();
			Thread.sleep(3000);
			System.out.println("Setting motors to -100,100");
			robot.setMotors(-100, 100);
			Thread.sleep(3000);
			System.out.println("Stopping motors");
			robot.stopMotors();
			System.out.println("All done, see you soon!");
*/
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}

}
