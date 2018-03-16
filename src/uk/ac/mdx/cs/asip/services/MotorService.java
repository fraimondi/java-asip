package uk.ac.mdx.cs.asip.services;

import uk.ac.mdx.cs.asip.AsipClient;

public class MotorService implements AsipService {

	private boolean DEBUG =  true;
	
	private char serviceID = 'M';
	
	// A motor has a unique ID (there may be more than one motor
	// attached, each one has a different motorID)
	private int motorID;

	private int count; // Count for the encoder
	private int pulse; // Pulse for the encoder
		
	// The service should be attached to a client
	private AsipClient asip; 
	
	private final char TAG_SET_MOTOR_SPEED = 'm';
	private final char TAG_ENCODER_RESPONSE = 'e';
	
	public MotorService(int id, AsipClient c) {
		this.motorID = id;
		this.asip = c;
	}
	
	// Standar getters and setters;
	public char getServiceID() {
		// TODO Auto-generated method stub
		return this.serviceID;
	}
	public void setServiceID(char id) {
		this.serviceID = id;
	}
	public int getMotorID() {
		return this.motorID;
	}
	public void setMotorID(int id) {
		this.motorID = id;
	}
	public void setClient(AsipClient c) {
		this.asip = c;
	}
	public AsipClient getClient() {
		return this.asip;
	}
	
	// Mirto 2017: encoders either on or off
	public void setReportingInterval(int t) {
		if ( t>0 ) {
			this.asip.getAsipWriter().write(this.serviceID+","+AsipService.AUTOEVENT_REQUEST+",1");
		} else {
			this.asip.getAsipWriter().write(this.serviceID+","+AsipService.AUTOEVENT_REQUEST+",0");
		}
	}
	
	
	public void processResponse(String message) {
		// FIXME
		// A response for a message is something like "“@E,e,2,{3000:110,3100:120}"
		if (DEBUG) {
			//System.out.println("Encoder service, received: "+message);
		}
		if (message.charAt(3) != TAG_ENCODER_RESPONSE) {
			// FIXME: improve error checking
			// We have received a message but it is not an encoder reporting event
			System.out.println("Encoder message received but I don't know how to process it: "+message);
		} else {
			
			String[] encValues = message.substring(message.indexOf("{")+1,
								message.indexOf("}")).split(",");
			int p = Integer.parseInt(encValues[this.motorID].split(":")[0]);
			int c = Integer.parseInt(encValues[this.motorID].split(":")[1]);
			
			this.count = c;
			this.pulse = p;
			if (DEBUG) {
				//System.out.println("Setting count and pulse to: "+c+" "+p+" "+this.count);
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
	
	public void setMotor(int speed) {
		// Speed should be between -100 and +100
		if (speed > 255 ) {
			speed = 255;
		}
		if (speed < -255 ) {
			speed = -255;
		}
		if (DEBUG) {
			System.out.println("Setting motor "+this.motorID+" to "+speed+"...");
		}
		// Motors have been mounted the other way around, so swapping IDs
		// 0 with 1 for id
		
		// From 2017 the motors take speed ranging -100 to +100:
		speed = (int)Math.round(speed/255.0*100.0);
		
		asip.getAsipWriter().write(serviceID+"," 
									+ TAG_SET_MOTOR_SPEED+
									"," + 
									this.motorID
									+ "," + speed);
		
		if (DEBUG) {
			System.out.println("Sending message: "+serviceID+"," 
					+ TAG_SET_MOTOR_SPEED+
					"," + 
					this.motorID
					+ "," + speed);
		}
	}
	
	// Stop the motor (just set speed to 0)
	public void stopMotor() {
		this.setMotor(0);
	}

}
