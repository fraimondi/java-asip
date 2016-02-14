/*
 * Extensions to off-line Scratch 2.0 require an HTTP server, see online documentation 
 * for Scratch.
 *  
 * This is a bridge to a bridge... It forwards requests from Scratch to a TCP connection
 * to a remote Mirto robot. Make sure that the remote robot is running a TCP to serial
 * ASIP bridge (if you don't understand what this means, ask Franco)
 * 
 */

package uk.ac.mdx.cs.asip.scratch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Date;

import uk.ac.mdx.cs.asip.AsipClient;
import uk.ac.mdx.cs.asip.JMirtoRobotOverTCP;
import uk.ac.mdx.cs.asip.SimpleSerialBoard;



public class HTTPScratchToMirtoBridge implements Runnable {

	public static final int HTTP_PORT = 14275;
	
	public static boolean DEBUG=false;
	
	private InputStream sockIn;
    private OutputStream sockOut;

    Date moDate = new Date();

    JMirtoRobotOverTCP robot;
    String remoteIP;
    
    
    public HTTPScratchToMirtoBridge(String remoteIP) {
    	this.remoteIP = remoteIP;   	
    	System.out.println("Using address "+remoteIP);
    	robot = new JMirtoRobotOverTCP();
    	robot.initialize(remoteIP);
		try {
			Thread.sleep(500);
			robot.setup();
			Thread.sleep(200);	
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
	@Override
	public void run() {
		try {
            InetAddress addr = InetAddress.getLocalHost();
            System.out.println("HTTPScratchToMirtoBridge app started on " + addr.toString());
            System.out.println("Connected to remote address: " + this.remoteIP);
            
            @SuppressWarnings("resource")
			ServerSocket serverSock = new ServerSocket(HTTP_PORT);
            
            while (true) {
                Socket sock = serverSock.accept();
                
                sockIn = sock.getInputStream();
                sockOut = sock.getOutputStream();
                try {
                    handleRequest();
                } catch (Throwable e) {
                    e.printStackTrace();
                    try {
                        sendResponse("_problem "+e.toString());
                    } catch (Throwable e1) {}
                }
                sock.close();
            }
            
        } catch (Throwable  ex) {
            ex.printStackTrace();
        }
    }
	
    private  void handleRequest() throws Throwable {
        String httpBuf = "";
        int i;

        // read data until the first HTTP header line is complete (i.e. a '\n' is seen)
        while ((i = httpBuf.indexOf('\n')) < 0) {
            byte[] buf = new byte[5000];
            int bytes_read = sockIn.read(buf, 0, buf.length);
            if (bytes_read < 0) {
                System.out.println("Socket closed; no HTTP header.");
                return;
            }
            httpBuf += new String(Arrays.copyOf(buf, bytes_read));
        }

        String header = httpBuf.substring(0, i);
        if (header.indexOf("GET ") != 0) {
            System.err.println("Only GET connections are supported");
            return;
        }
        i = header.indexOf("HTTP/1");
        if (i < 0) {
            System.err.println("Wrong HTTP header.");
            return;
        }
        header = header.substring(5, i - 1);
        if (header.equals("favicon.ico")) {
            return; // igore browser favicon.ico requests
        } else if (header.equals("crossdomain.xml")) {
            sendPolicyFile();
        } else if (header.length() == 0) {
            doHelp();
        } else {
            doCommand(header);
        }
    }
    
    private  void sendPolicyFile() throws IOException {
    	// Send a Flash null-teriminated cross-domain policy file.
        String policyFile
                = "<cross-domain-policy>\n"
                + " <allow-access-from domain=\"*\" to-ports=\"" + HTTP_PORT + "\"/>\n"
                + "</cross-domain-policy>\n\0";
        sendResponse(policyFile);
    }
		
    private  void sendResponse(String s) throws IOException {
        String crlf = "\r\n";
        String httpResponse="";
        httpResponse = "HTTP/1.1 200 OK" + crlf;
        httpResponse += "Content-Type: text/html; charset=ISO-8859-1" + crlf;
        httpResponse += "Access-Control-Allow-Origin: *" + crlf;
        httpResponse += crlf;
        httpResponse += s + crlf;
        byte[] outBuf = httpResponse.getBytes();
        sockOut.write(outBuf, 0, outBuf.length); 
    }
    
    private void doHelp() throws IOException {
// Optional: return a list of commands understood by this server
        String help = "HTTP Server Extension for ASIP<br><br>";
        sendResponse(help);
    }
    
    
    /* This is where we translate HTTP requests to ASIP requests for the robot 
     * Commands accepted:
     *  - setMotors
     *  - stopMotors
     *  - playNote
     *  - clearLCD
     *  - writeLCDLine
     *  - readLeftBumper
     *  - readRightBumper
     *  - readIR
     *  - readPotentiometer
     *  - readButton
     */
    private void doCommand(String header) throws Throwable {
    	String response = "okay";
    	header = java.net.URLDecoder.decode(header, "UTF-8");
		String[] parts = header.split("/");
		String cmd = parts[0];
		
		//System.out.print(cmdAndArgs);
		if (cmd.equals("setMotors")) {
			// Making life easier here: I change the sign of the second motor at this point
			// so that they don't have to worry about direction.
			robot.setMotors(Integer.parseInt(parts[2]), - Integer.parseInt(parts[3]));
			if (DEBUG) {
				System.out.println("Setting motors to "+parts[2]+ ","+parts[3]);
			}
			Thread.sleep(100);
		} else if (cmd.equals("stopMotors")) {
			if (DEBUG) {
				System.out.println("Stopping motors");
			}
			robot.stopMotors();
			Thread.sleep(100);
		} else if (cmd.equals("playNote")) {
			if (DEBUG) {
				System.out.println("Playing note "+Integer.parseInt(parts[2])+ " with duration " + Integer.parseInt(parts[3]));
			}
			robot.playNote(Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
			Thread.sleep(Integer.parseInt(parts[2]));
		} else if (cmd.equals("clearLCD")) {
			if (DEBUG) {
				System.out.println("Clearing LCD screen");
			}
			robot.clearLCDScreen();
			Thread.sleep(100);
		} else if (cmd.equals("writeLCDLine")) {
			if (DEBUG) {
				System.out.println("Writing LCD line "+Integer.parseInt(parts[2])+ " with text " + parts[3]);
			}
			robot.writeLCDLine(parts[3],Integer.parseInt(parts[2]));
			Thread.sleep(100);
		} else if (cmd.equals("poll")) {
			// set response to a collection of sensor, value pairs, one pair per line
			// in this example there is only one sensor, "volume"
			//response = "volume " + volume + "\n";
			
			/*     
			 *  - readLeftBumper
			 *  - readRightBumper
			 *  - readIR
			 *  - readPotentiometer
			 *  - readButton
			 */
			response = "";
			response += "readLeftBumper" + " " +  robot.isPressed(0)  + "\n";
			if (DEBUG) {
				System.out.println("Returning: "+"readLeftBumper" + " " +  robot.isPressed(0));
			}
			response += "readRightBumper" + " " +  robot.isPressed(1)  + "\n";
			if (DEBUG) {
				System.out.println("Returning: "+"readRightBumper" + " " +  robot.isPressed(1));
			}
			
			for (int i = 0; i <= 2; i++) {
				response += "readIR/" + i + " " + robot.getIR(i) + "\n";
			}
			
			response += "readPotentiometer" + " " +  robot.getPotentiometer()  + "\n";
			
			response += "readButton" + " " +  robot.getPushButton()  + "\n";
			if (DEBUG) {
				System.out.println("Returning: "+"readButton" + " " +  robot.getPushButton());
			}
			
		} else {
			response = "unknown command: " + cmd;
		}
		//System.out.println(" " + response);
		sendResponse(response);
   
  }
    
  public static void main (String[] args) {
	  if ( args.length < 1 ) {
		  System.err.println("Error: please provide an IP address for the robot");
		  System.exit(1);
	  }
	  System.out.println("Starting the service...");
	  String port = args[0];
	  System.out.println("Using IP "+port);
	  HTTPScratchToMirtoBridge b = new HTTPScratchToMirtoBridge(port);
	  b.run();
  }
    
}
    
