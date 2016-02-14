/*
 * Extensions to off-line Scratch 2.0 require an HTTP server, see online documentation 
 * for Scratch.
 * 
 * The structure of the code is taken from https://github.com/damellis/A4S
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
import uk.ac.mdx.cs.asip.SimpleSerialBoard;



public class HTTPScratchToAsipBridge implements Runnable {

	public static final int HTTP_PORT = 14275;
	
	// A default value (very unlikely to work...)
	private String SERIAL_PORT;
	
	public static boolean DEBUG=true;
	
	private InputStream sockIn;
    private OutputStream sockOut;

    Date moDate = new Date();

    SimpleSerialBoard board;
    
    
    public HTTPScratchToAsipBridge(String port) {
    	this.SERIAL_PORT = port;   	
    	System.out.println("Using port "+port);
    	board = new SimpleSerialBoard(SERIAL_PORT);
    }
    
	@Override
	public void run() {
		try {
            InetAddress addr = InetAddress.getLocalHost();
            System.out.println("HTTPScratchToAsipBridge app started on " + addr.toString());
            
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
    
    
    /* This is where we translate HTTP requests to ASIP requests */
    private void doCommand(String header) throws Throwable {
    	String response = "okay";
    	header = java.net.URLDecoder.decode(header, "UTF-8");
		String[] parts = header.split("/");
		String cmd = parts[0];
		
		//System.out.print(cmdAndArgs);
		if (cmd.equals("pinMode")) {
			// "Digital Input", "Digital Output","Analog Input","Analog Output(PWM)","Servo"
			int asipMode = 0;
			// FIXME: this is a simplification, we assume input = input_pullup!
			if (DEBUG) {
				System.out.println("DEBUG: requested mode is "+parts[2]);
			}
			if (parts[2].equals("Digital Input")) {
				asipMode = AsipClient.INPUT_PULLUP;				
			} else if ((parts[2].equals("Digital Output")) ) {
				asipMode = AsipClient.OUTPUT;
			} else if ((parts[2].equals("Analog Output(PWM)")) ) {
				asipMode = AsipClient.PWM;
			} else {
				// FIXME: Add error!
			}
			board.setPinMode(Integer.parseInt(parts[1]), asipMode);
			if (DEBUG) {
				System.out.println("Setting pin "+Integer.parseInt(parts[1])+ " to mode " + asipMode);
			}
		} else if (cmd.equals("digitalWrite")) {
			if (DEBUG) {
				System.out.println("Setting pin "+Integer.parseInt(parts[1])+ " to "+ ("high".equals(parts[2]) ? AsipClient.HIGH : AsipClient.LOW ));
			}
			board.digitalWrite(Integer.parseInt(parts[1]), ("high".equals(parts[2]) ? AsipClient.HIGH : AsipClient.LOW ));
		} else if (cmd.equals("analogWrite")) {
			if (DEBUG) {
				System.out.println("Analog write pin "+Integer.parseInt(parts[1])+ " to value " + parts[2]);
			}
			board.analoglWrite(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
		} else if (cmd.equals("servoWrite")) {
			if (DEBUG) {
				System.out.println("Servo write pin "+Integer.parseInt(parts[1])+ " with value " + parts[2]);
			}
			// TODO: FIXME!! Add servo support
			
		} else if (cmd.equals("poll")) {
			// set response to a collection of sensor, value pairs, one pair per line
			// in this example there is only one sensor, "volume"
			//response = "volume " + volume + "\n";
			response = "";
			for (int i = 2; i <= 13; i++) {
				// This should return the right value!
				response += "digitalRead/" + i + " " +  (board.digitalRead(i)==1 ? "true" : "false")  + "\n";
			}
			for (int i = 0; i <= 5; i++) {
				// This shohuld return the right value!
				response += "analogRead/" + i + " " + board.analogRead(i)+ "\n";
			}
		} else {
			response = "unknown command: " + cmd;
		}
		//System.out.println(" " + response);
		sendResponse(response);
   
  }
    
  public static void main (String[] args) {
	  if ( args.length < 1 ) {
		  System.err.println("Error: please provide a port name for Arduino");
		  System.exit(1);
	  }
	  System.out.println("Starting the service...");
	  String port = args[0];
	  System.out.println("Using port "+port);
	  HTTPScratchToAsipBridge b = new HTTPScratchToAsipBridge(port);
	  b.run();
  }
    
}
    
