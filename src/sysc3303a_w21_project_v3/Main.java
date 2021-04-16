import javax.swing.JOptionPane;

public class Main {
	
	
	public final static int TEST_MODE = 2;
	public final static int TIMING_MODE = 1;
	public final static int DEFAULT_MODE = 0;
	
	public static void main(String[] args) {
		int numFloors = 0, numElevators = 0;
		String[] options = {"Use Defaults", "Use User Inputs"};
		int popUp = JOptionPane.showOptionDialog(null, "Would you like to input values?", 
				"Confirmation", JOptionPane.INFORMATION_MESSAGE, 0, null, options, options[0]);
		switch(popUp) {
		case -1:
			System.exit(0);
		case 0:
			numFloors = 22; 
			numElevators = 4; 
			break;
		case 1:
			numElevators = Integer.parseInt(JOptionPane.showInputDialog("How many elevators?"));
			numFloors = Integer.parseInt(JOptionPane.showInputDialog("How many floors?"));
		}
		
		ElevatorSubsystem e = new ElevatorSubsystem(numFloors, numElevators, DEFAULT_MODE);
		Scheduler s = new Scheduler(numFloors, numElevators, DEFAULT_MODE);
		FloorSubsystem f = new FloorSubsystem(numFloors, DEFAULT_MODE);
		
	}

}
