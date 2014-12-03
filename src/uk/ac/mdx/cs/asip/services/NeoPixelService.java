package uk.ac.mdx.cs.asip.services;

/*
 * @author Franco Raimondi
 * 
 * A class to implement NeoPixel strips.
 * Remember to upload to appropriate ASIP firmware, currently a branch
 * of the main ASIP git repository
 * 
 */
import uk.ac.mdx.cs.asip.AsipClient;
import uk.ac.mdx.cs.asip.AsipWriter;

// A service for servos. 

public class NeoPixelService implements AsipService {

	private char serviceID = 'N';
	
	// A servo has a unique ID (there may be more than one servo
	// attached, each one has a different servoID)
	private int stripID;
	
	// The service should be attached to a client
	private AsipClient asip; 
	
	private AsipWriter aw;
	
	// The constructor takes the id of the servo.
	public NeoPixelService(int id, AsipClient c) {
		this.stripID = id;
		this.asip = c;
		this.aw = this.asip.getAsipWriter();
	}
	
	public char getServiceID() {
		return this.serviceID;
	}
	public void setServiceID(char id) {
		this.serviceID = id;		
	}
	public int getStripID() {
		return this.stripID;
	}
	public void setStripID(int id) {
		this.stripID = id;
	}
	public void setClient(AsipClient c) {
		this.asip = c;
	}
	public AsipClient getClient() {
		return this.asip;
	}
	
	
	public void processResponse(String message) {
		// Nothing to do here... (no response from the strip)		
	}
	
	// This method sends the message to set the servo angle
	public void setPixelColor(int pixel, int red, int green, int blue) {
		aw.write(serviceID+","+"C"+","+this.stripID+","+pixel+","+red+","+green+","+blue+"\n");
	}
	
	public void setBrightness(int b) {
		aw.write(serviceID+","+"B"+","+this.stripID+","+b+"\n");
	}
	
	public void show() {
		aw.write(serviceID+","+"S"+","+this.stripID+"\n");
	}

}
