package uk.ac.mdx.cs.asip.tcpclient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import uk.ac.mdx.cs.asip.AsipClient;

public class SimpleTCPBoard {

	public static int SERVERPORT = 6789;
	
	// The client for the aisp protocol
	AsipClient asip;

	// The IP address of the ASIP board.
	String boardIP;
	
	// The output stream to write messages to.
	DataOutputStream outputStream;
	DataInputStream inputStream;
	
	// The constructor opens the TCP connection to the ASIP board.
	// It sets and output stream to write ASIP messages to
	// and an input stream that is managed by a separate thread.
	// The asip object employs a SimpleTCPWriter that implements AsipWriter.	
	public SimpleTCPBoard(String boardIP) {
		this.boardIP = boardIP;
		Socket s;
		
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
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		
	}
	
	public AsipClient getAsipClient() {
		return this.asip;
	}
	
	public static void main (String args[]) {
		try {
			SimpleTCPBoard board = new SimpleTCPBoard("192.168.0.73");
			Thread.sleep(2500);
			board.getAsipClient().setAutoReportInterval(1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
