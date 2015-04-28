package uk.ac.mdx.cs.asip.tcpclient;

import java.io.DataOutputStream;
import java.io.IOException;

import uk.ac.mdx.cs.asip.AsipWriter;

// A TCP writer for the Asip class
public class SimpleTCPWriter implements AsipWriter {

	DataOutputStream outputStream;
	
	public SimpleTCPWriter(DataOutputStream os) {
		this.outputStream = os;
	}
	
	@Override
	public void write(String val) {
		// TODO Auto-generated method stub
		// FIXME: add better exception handling
		try {
			outputStream.writeUTF(val);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
