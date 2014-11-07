package uk.ac.mdx.cs.asip.services;

import uk.ac.mdx.cs.asip.AsipClient;

public class EncoderService implements AsipService {
	
	private boolean DEBUG = false;
	
	private char serviceID = 'E';
	
	// An encoder has a unique ID (there may be more than one encoder
	// attached, each one has a different encoderID)
	private int encoderID;

	// The service should be attached to a client
	private AsipClient asip; 
	
	private final char TAG_ENCODER_RESPONSE = 'e';

	private int count; // Count for the encoder
	private int pulse; // Pulse for the encoder
	
	public EncoderService(int id, AsipClient c) {
		this.encoderID =id;
		this.asip = c;
	}
	
	// Standard getters and setters;
	public char getServiceID() {
		return this.serviceID;
	}
	public void setServiceID(char id) {
		this.serviceID = id;
	}
	public int getEncoderID() {
		return this.encoderID;
	}
	public void setEncoderID(int id) {
		this.encoderID = id;
	}
	public void setClient(AsipClient c) {
		this.asip = c;
	}
	public AsipClient getClient() {
		return this.asip;
	}
	
	// Set the reporting time to t milliseconds
	// (use t=0 to disable reporting)
	// Notice that this will affect all encoders
	public void setReportingInterval(int t) {
		this.asip.getAsipWriter().write(this.serviceID+","+AsipService.AUTOEVENT_REQUEST+","+t);
	}
	
	public void processResponse(String message) {
		// FIXME
		// A response for a message is something like "“@E,e,2,{3000:110,3100:120}"
		if (DEBUG) {
			System.out.println("Encoder service, received: "+message);
		}
		if (message.charAt(3) != TAG_ENCODER_RESPONSE) {
			// FIXME: improve error checking
			// We have received a message but it is not an encoder reporting event
			System.out.println("Encoder message received but I don't know how to process it: "+message);
		} else {
			
			String[] encValues = message.substring(message.indexOf("{")+1,
								message.indexOf("}")).split(",");
			int p = Integer.parseInt(encValues[this.encoderID].split(":")[0]);
			int c = Integer.parseInt(encValues[this.encoderID].split(":")[1]);
			
			this.count += c;
			this.pulse = p;
			if (DEBUG) {
				System.out.println("Setting count and pulse to: "+c+" "+p+" "+this.count);
			}
		}
	}
	
	public int getCount() {
		return this.count;
	}
	
	public int getPulse() {
		return this.pulse;
	}
	
	public void resetCount() {
		this.count = 0;
	}
	
}
