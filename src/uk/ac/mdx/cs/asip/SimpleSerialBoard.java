package uk.ac.mdx.cs.asip;

/* 
 * @author Franco Raimondi
 * 
 * A simple implementation with serial communication and only I/O services
 * 
 */
import uk.ac.mdx.cs.asip.AsipClient;
import uk.ac.mdx.cs.asip.AsipWriter;
import uk.ac.mdx.cs.asip.services.AsipService;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;


public class SimpleSerialBoard {

	// This board uses serial communication (provided by jssc)
	SerialPort serialPort;
	
	// The client for the aisp protocol
	AsipClient asip;

	// This constructor takes the name of the serial port and it
	// creates the serialPort object.
	// We attach a listener to the serial port with SerialPortReader; this
	// listener calls the aisp method to process input.
	public SimpleSerialBoard(String port) {
		
		serialPort = new SerialPort(port);

		asip = new AsipClient(new SimpleWriter());
		
		try {

			serialPort.openPort();// Open port
			serialPort.setParams(57600, 8, 1, 0);
			serialPort.setDTR(false);
			Thread.sleep(250);
			serialPort.setDTR(true);
			// Set params
			int mask = SerialPort.MASK_RXCHAR ; //+ SerialPort.MASK_CTS
					// + SerialPort.MASK_DSR;// Prepare mask
			serialPort.setEventsMask(mask);// Set mask
		} catch (Exception ex) {
			//System.out.println(ex);
			System.err.println("Could not open port "+port);
			System.err.println("Please check the Arduino connection and try again");
			System.exit(0);
		}
		
		
		if (!serialPort.isOpened()) {
			System.err.println("Could not open port "+port);
			System.err.println("Please check the Arduino connection and try again");
			System.exit(0);
		}

		try {
			Thread.sleep(1500);
			requestPortMapping();
			Thread.sleep(500);
			requestPortMapping();
			Thread.sleep(500);
			requestPortMapping();
			Thread.sleep(500);
			serialPort.addEventListener(new SerialPortReader());// Add
			// SerialPortEventListener
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	// The following methods are just a replica from the asip class.
	public int digitalRead(int pin) {
		return asip.digitalRead(pin);
	}	
	public int analogRead(int pin) {
		return asip.analogRead(pin);
	}
	public void setPinMode(int pin, int mode) {
		asip.setPinMode(pin, mode);
	}
	public void digitalWrite(int pin, int value) {
		asip.digitalWrite(pin, value);
	}
	public void analoglWrite(int pin, int value) {
		asip.analoglWrite(pin, value);
	}
	public void requestPortMapping() {
		asip.requestPortMapping();
	}
	public void setAutoReportInterval(int interval) {
		asip.setAutoReportInterval(interval);
	}
	
	// As described above, SimpleSerialBoard writes messages to
	// the serial port.
    private class SimpleWriter implements AsipWriter {
        public void write(String val) {
          try {	
			serialPort.writeString(val);
          } catch (SerialPortException e) {
        	  // TODO Auto-generated catch block
        	  e.printStackTrace();
          }	
        }
    }
	
	// A class for a listener that calls the processInput method of
	// the AispClient.
	private class SerialPortReader implements SerialPortEventListener {
		
		private String buffer = ""; // we store partial messages here.
		
        public void serialEvent(SerialPortEvent event) {
            if(event.isRXCHAR()){//If data is available
            	try {
            		String val = serialPort.readString();
            		if ( val != null ) { // Needed in win!
            			//System.out.println("DEBUG: received on serial: "+val);
            			if ( val.contains("\n")) {
            				// If there is at least one newline, we need to process
            				// the message (the buffer may contain previous characters).
            				while (val.contains("\n") && (val.length()>0)) {
            					// But remember that there could be more than one newline 
            					// in the buffer
            					buffer += val.substring(0,val.indexOf("\n"));
            					//System.out.println("DEBUG: processing "+buffer);
            					asip.processInput(buffer);
            					buffer = "";
            					val = val.substring(val.indexOf("\n")+1);
            				}
            				// If there is some leftover to process we add to buffer
            				if (val.length() > 0 ) {
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
	
	public void addService(char serviceID, AsipService s) {
		this.asip.addService(serviceID, s);
	}
	
	public AsipClient getAsipClient() {
		return this.asip;
	}
	
}
