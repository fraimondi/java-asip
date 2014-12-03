/**
 * 
 */
package uk.ac.mdx.cs.asip.examples;

import uk.ac.mdx.cs.asip.AsipClient;
import uk.ac.mdx.cs.asip.SimpleSerialBoard;
import uk.ac.mdx.cs.asip.services.DistanceService;
import uk.ac.mdx.cs.asip.services.NeoPixelService;

/**
 * @author Franco Raimondi
 * this class provides a simple example of a Distance sensor connected to pin 4
 * and 2 NeoPixel strips on pin 6 and 7, respectively.
 * (remember to upload the correct version of ASIP on the board. You will find it
 * in a branch of the main ASIP git repository)
 */

public class SimpleNeoPixelWithDistance extends SimpleSerialBoard {

	private DistanceService d0;
	private NeoPixelService s0;
	private NeoPixelService s1;
	
	public SimpleNeoPixelWithDistance(String port) {
		super(port);
		 try {
			Thread.sleep(300);
			d0 = new DistanceService(0, getAsipClient());
			Thread.sleep(300);	
			addService('D', d0);
			Thread.sleep(300);	
			d0.enableContinuousReporting(0);
			Thread.sleep(100);
			getAsipClient().setAutoReportInterval(0);
			s0 = new NeoPixelService(0, getAsipClient());
			s1 = new NeoPixelService(1, getAsipClient());
			addService('N', s0);
			Thread.sleep(300);
			s0.show();			
			Thread.sleep(100);
			addService('N', s1);
			Thread.sleep(100);
			s1.show();
			Thread.sleep(100);
			s0.setPixelColor(1, 0, 255, 0);
			Thread.sleep(500);
			s0.show();
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	public int getDistance() {
		return d0.getDistance();
	}
	
	public void setColor(NeoPixelService ns, int pixel, int red, int green, int blue) {
		ns.setPixelColor(pixel, red, green, blue);
	}
	
	public void setStripBrightness(NeoPixelService ns, int b) {
		ns.setBrightness(b);
	}
	
	public void showStrip(NeoPixelService ns) {
		ns.show();
	}
	
	// A utility code to retrieve the pointer to a strip.
	// Used in the main method below
	public NeoPixelService getStrip(int id) {
		if ( id == 0 ) {
			return this.s0;
		} else if ( id == 1 ) {
			return this.s1;
		} else {
			return null;
		}
	}
	
	public static void main(String[] args) {


		SimpleNeoPixelWithDistance testBoard = new SimpleNeoPixelWithDistance("/dev/tty.usbmodem1411");

		//testBoard.setPinMode(13, AsipClient.OUTPUT);
		//testBoard.digitalWrite(13, AsipClient.LOW);
		
		NeoPixelService strip0 = testBoard.getStrip(0);
		System.out.println("Service ID: "+strip0.getServiceID()+strip0.getStripID());
		NeoPixelService strip1 = testBoard.getStrip(1);
		System.out.println("Service ID: "+strip1.getServiceID()+strip1.getStripID());
			
		try {

			Thread.sleep(300);
			
			while (true) {

				int numPixels = 16;
				for (int i=0; i < numPixels; i++) {
					// setting a pixel to red and everything else to off
					for (int j = 1; j<numPixels; j++) {
						if ( i == j) {
							testBoard.setColor(strip1, j, 255, 0, 0);
						} else {
							testBoard.setColor(strip1, j, 0, 0, 0);
						}
						Thread.sleep(5);
					}
					testBoard.showStrip(strip1);
					Thread.sleep(50);
				}
				
				numPixels = 8;
				for (int i=0; i < numPixels; i++) {
					// setting a pixel to red and everything else to off
					for (int j = 0; j<numPixels; j++) {
						if ( i == j) {
							testBoard.setColor(strip0, j, 255, 0, 0);
						} else {
							testBoard.setColor(strip0, j, 0, 0, 0);
						}
						Thread.sleep(5);
					}
					testBoard.showStrip(strip0);
					Thread.sleep(50);
				}
				
				for (int i=numPixels-1; i>=0; i--) {
					// setting a pixel to red and everything else to off
					for (int j = 0; j<numPixels; j++) {
						if ( i == j) {
							testBoard.setColor(strip0, j, 255, 0, 0);
						} else {
							testBoard.setColor(strip0, j, 0, 0, 0);
						}
						Thread.sleep(5);
					}
					testBoard.showStrip(strip0);
					Thread.sleep(50);
				}
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}     		
	}
	
	
}
		
	
	