package uk.ac.mdx.cs.asip.services;

import uk.ac.mdx.cs.asip.AsipClient;

// A service for LCD. 

public class LCDService implements AsipService {

	private char serviceID = 'L';
	
	// The service should be attached to a client
	private AsipClient asip; 
	
	// The constructor takes only the asip client.
	public LCDService(AsipClient c) {
		this.asip = c;
	}
	
	public char getServiceID() {
		return this.serviceID;
	}
	public void setServiceID(char id) {
		this.serviceID = id;		
	}

	public void setClient(AsipClient c) {
		this.asip = c;
	}
	public AsipClient getClient() {
		return this.asip;
	}
	
	
	public void processResponse(String message) {
		// Nothing to do here... (no response from the LCD service)		
	}
	
	// This method sets a line on the LCD screen
	public void setLine(String message, int line) {
		asip.getAsipWriter().write(serviceID+","+"W"+","+line+","+message);
	}
	
	public void clearLCD() {
		asip.getAsipWriter().write(serviceID+","+"C");
	}

}
