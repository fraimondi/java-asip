package uk.ac.mdx.cs.asip.services;

import uk.ac.mdx.cs.asip.AsipClient;

// A service for tones. 

public class ToneService implements AsipService {

	private char serviceID = 'T';
	
	// The service should be attached to a client
	private AsipClient asip; 
	
	// The constructor takes only the asip client.
	public ToneService(AsipClient c) {
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
		// Nothing to do here... (no response from the tone service)		
	}
	
	// This method sends the message to play a tone (in Hz) for a certain duration (in ms)
	public void playNote(int frequency, int duration) {
		asip.getAsipWriter().write(serviceID+","+"P"+","+frequency+","+duration+"\n");
	}

}
