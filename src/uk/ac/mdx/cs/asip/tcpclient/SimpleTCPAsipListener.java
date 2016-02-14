package uk.ac.mdx.cs.asip.tcpclient;

import java.io.DataInputStream;
import java.io.IOException;

import uk.ac.mdx.cs.asip.AsipClient;

public class SimpleTCPAsipListener extends Thread {
	
	boolean DEBUG = false;
	
	DataInputStream inputStream;
	AsipClient asip;
	
	private String buffer = "";
	
	public SimpleTCPAsipListener(DataInputStream is, AsipClient a) {
		this.inputStream = is;
		this.asip = a;
        this.start(); 
	}
	
	public void run() {
		while (true) {
			try {
				String val = inputStream.readUTF();
				if (DEBUG) {
					System.err.println(val);
				}
				asip.processInput(val);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				inputStream = null;
				break;
			}
		}
	}

}
