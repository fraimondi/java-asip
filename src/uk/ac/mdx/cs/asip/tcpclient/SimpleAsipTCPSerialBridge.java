package uk.ac.mdx.cs.asip.tcpclient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

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

public class SimpleAsipTCPSerialBridge {

	static boolean DEBUG = true;

	public static int SERVERPORT = 6789;

	// This board uses serial communication (provided by jssc)
	SerialPort serialPort;

	// The client for the aisp protocol

	// TCP streams
	DataOutputStream outputStream;
	DataInputStream inputStream;

	public SimpleAsipTCPSerialBridge(String port) {

		ServerSocket listenSocket = null;

		try {
			listenSocket = new ServerSocket(SERVERPORT);
			if (DEBUG) {
				System.out.println("Waiting for connections");
			}
			while (true) {
				Socket clientSocket = listenSocket.accept();
				if (DEBUG) {
					System.out
							.println("Connection received, setting up the streams");
				}
				inputStream = new DataInputStream(clientSocket.getInputStream());
				outputStream = new DataOutputStream(
						clientSocket.getOutputStream());
				Bridge b = new Bridge(port);
				b.bridge();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			outputStream = null;
			inputStream = null;
		} finally {
			try {
				listenSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	// The Bridge opens the serial connection
	class Bridge {
		public Bridge(String port) {
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
					serialPort.addEventListener(new SerialPortReaderTCP());
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
				System.out.println("Starting the TCP listener");
			}
			String val = "";
			while (true) {
				try {					
					if ( inputStream != null) {
						val = inputStream.readUTF();
					}
					if (DEBUG) {
						System.out.println("Received in inputStream: " + val);
					}
					serialPort.writeString(val);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					inputStream = null;
					outputStream = null;
					try {
						serialPort.removeEventListener();
						serialPort.closePort();
					} catch (SerialPortException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					break;
				}
			}
		}

		// When an event is received on the serial port, it is forwarded to the
		// output TCP stream.
		private class SerialPortReaderTCP implements SerialPortEventListener {

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
										if (outputStream != null) {
											outputStream.writeUTF(buffer);
										}
									} catch (IOException e) {
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
	}

	public static void main(String args[]) {
		SimpleAsipTCPSerialBridge bridge = new SimpleAsipTCPSerialBridge(
				"/dev/cu.usbmodem1411");
	}
}
