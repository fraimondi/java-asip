package uk.ac.mdx.cs.asip.examples;

import uk.ac.mdx.cs.asip.JMirtoRobot;


public class AsipMirtoPIDFollower {
	
	private String howTo = "AsipMirtoPIDFollower usage:\n"
			+ "You can invoke AsipMirtoPIDFollower with default parameter without providing \n"
			+ "arguments on the command line. Otherwise, you need to provide exactly 7 arguments:\n"
			+ " 1. power: this is an integer number between 0 and 100. Default 75\n"
			+ " 2. maxDelta: this is an integer providing the maximum correction value (0-100), default 75\n"
			+ " 3. Proportional correction constant: this is a double, default 0.05\n"
			+ " 4. Derivative correction constant: this is a double, default 1.6\n"
			+ " 5. Integral correction constant: this is a double, default 0.0001\n"
			+ " 6. Frequence of updates: this is an integer, default 30 (ms)\n"
			+ " 7. Cut-off IR value: this is the value under which we define black. Default 40.\n";
			
			
	public String getHowTo() {
		return howTo;
	}
	
	public void getHowTo(String s) {
		this.howTo = s;
	}
	
	public int getCutOffIR() {
		return cutOffIR;
	}

	public void setCutOffIR(int cutOffIR) {
		this.cutOffIR = cutOffIR;
	}

	public int getPWR() {
		return PWR;
	}

	public void setPWR(int pWR) {
		PWR = pWR;
	}

	public int getFreq() {
		return freq;
	}

	public void setFreq(int freq) {
		this.freq = freq;
	}

	public int getMaxDelta() {
		return maxDelta;
	}

	public void setMaxDelta(int maxDelta) {
		this.maxDelta = maxDelta;
	}

	public double getKp() {
		return Kp;
	}

	public void setKp(double kp) {
		Kp = kp;
	}

	public double getKd() {
		return Kd;
	}

	public void setKd(double kd) {
		Kd = kd;
	}

	public double getKi() {
		return Ki;
	}

	public void setKi(double ki) {
		Ki = ki;
	}

	private int cutOffIR = 40;
	
	private int PWR = 75;
	
	private int freq = 35; // frequency of updates;
	private int maxDelta = PWR; // max correction

	private double Kp = 0.050;
	private double Kd = 1.6;
	private double Ki = 0.0001;
	
	private double curError = 2000;
	private double prevError = 2000;

	
	private int cutIR(int in) {
		if ( in < cutOffIR ) {
			return 0;
		} else {
			return in;
		}
	}
	
	private double computeError(int left, int middle, int right, double previous) {
		if ( (left+right+middle) == 0 ) {
			return previous;
		} else {
			return ( (middle*2000 + right*4000) / (left + middle + right) );
		}
	}
	
	
	public void navigate() {
		
		JMirtoRobot robot = new JMirtoRobot("/dev/ttyAMA0");
//		JMirtoRobot robot = new JMirtoRobot("/dev/tty.usbmodem1411");
		
		try {
			System.out.println("Setting up in 2 seconds...");
			Thread.sleep(2000);
			System.out.println("Starting now");

			robot.setup();
			
			System.out.println("Robot set up completed");
			
			Thread.sleep(500);
			
			long timeNow = System.currentTimeMillis();
			long oldTime = 0;
			
			double proportional = 0;
			double integral = 0;
			double derivative = 0;
			
			int correction = 0;
			
			// print IR values every interval milliseconds
			while (true) {
				timeNow = System.currentTimeMillis();
				
				if (( timeNow - oldTime) > freq) {

					int leftIR = cutIR(robot.getIR(2));
					int middleIR = cutIR(robot.getIR(1));
					int rightIR = cutIR(robot.getIR(0));
				
					if ( (leftIR==0) && (middleIR==0) && (rightIR==0)) {
						// This means that we lost the track. We keep doing what we
						// were doing before.
						curError = prevError;
					} else {
						curError = computeError(leftIR,middleIR,rightIR,prevError);
					}
				
					proportional = curError - 2000;
				
					if (proportional == 0) {
						integral = 0;
					} else {
						integral += proportional;
					}
				
					derivative = proportional - ( prevError - 2000);
				
					prevError = curError;
				
					correction = (int) Math.floor(Kp*proportional + Ki*integral + Kd*derivative);
				
					int delta = correction;
				
					if (delta>maxDelta) {
						delta=maxDelta;
					} else if (delta < (-maxDelta)) {
						delta = (-maxDelta);
					}
				
					if (delta < 0) {
						robot.setMotors( (int) (2.55*(PWR+delta)), (int) (2.55*(-PWR)));
					} else {
						robot.setMotors( (int) (2.55*PWR), (int) (-(PWR-delta)*2.55) );
					}
					oldTime = timeNow;
				}
				// We do not want to flood the serial port if setting motors is
				// enabled
				Thread.sleep(10);
			}
		} catch (Exception e) {
			robot.stopMotors();
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		AsipMirtoPIDFollower mytest = new AsipMirtoPIDFollower();
		if (args.length == 0 ) {
			// No command line parameters provided
			mytest.setPWR(75);
			mytest.setMaxDelta(75);

			mytest.setKp(0.05);
			mytest.setKd(1.6);
			mytest.setKi(0.0001);

			mytest.setFreq(30);
			mytest.setCutOffIR(40);

			mytest.navigate();
			
		} else if (args.length == 7) {
			// the order is: power, maxDelta, Kp, Kd, Ki, freq, cutoffIR
			try {
			mytest.setPWR(Integer.parseInt(args[0]));
			mytest.setMaxDelta(Integer.parseInt(args[1]));

			mytest.setKp(Double.parseDouble(args[2]));
			mytest.setKd(Double.parseDouble(args[3]));
			mytest.setKi(Double.parseDouble(args[4]));

			mytest.setFreq(Integer.parseInt(args[5]));
			mytest.setCutOffIR(Integer.parseInt(args[6]));
			} catch (Exception e) {

				System.err.println("Error parsing command line parameters! The correct syntax is: ");
				System.err.println(mytest.getHowTo());
				e.printStackTrace();
			}
			
			mytest.navigate();
			
		} else {
			System.err.println("Error parsing command line parameters! The correct syntax is: ");
			System.err.println(mytest.getHowTo());
		}
	
		
	}
}
