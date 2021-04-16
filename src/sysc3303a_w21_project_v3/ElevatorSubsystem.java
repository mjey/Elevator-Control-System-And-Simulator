import java.io.*;
import java.net.*;
import java.util.ArrayList;
import javax.swing.JOptionPane;

public class ElevatorSubsystem extends Thread {
	public final static int TEST_MODE = 2;
	public final static int TIMING_MODE = 1;
	public final static int DEFAULT_MODE = 0;
	private int runMode;
	private GUI elevatorGUI;
	private Elevator elevatorList[];
	private ArrayList<ErrorEvent> errorList;
	public ElevatorSubsystem(int numFloors, int numElevators, int runMode) {
		elevatorList = new Elevator[numElevators];
		errorList = new ArrayList<ErrorEvent>();
		elevatorGUI = new GUI(numFloors, numElevators);
		for (int i = 0; i < numElevators; i ++) {
			elevatorList[i] = (new Elevator(i, numFloors, this, 2000 + i, elevatorGUI, runMode));
		}
		loadErrors();
		for (Elevator e: elevatorList) {
			e.start();
		}
	}
	public ElevatorSubsystem(boolean measureValues) {
		elevatorGUI = new GUI();
		elevatorList = new Elevator[elevatorGUI.getNumElevators()];
		errorList = new ArrayList<ErrorEvent>();
		for (int i = 0; i < elevatorGUI.getNumElevators(); i ++) {
			elevatorList[i] = (new Elevator(i, elevatorGUI.getNumFloors(), this, 2000 + i, elevatorGUI, runMode));
		}
		loadErrors();
		for (Elevator e: elevatorList) {
			e.start();
		}
	}
	public Elevator getElevator(int elevatorNum) {
		return elevatorList[elevatorNum];
	}
	public void loadErrors() {
		if (elevatorList.length >= 4) {
			elevatorList[0].addError(new ErrorEvent(ErrorEvent.DOOR_STUCK, 5000));
			elevatorList[1].addError(new ErrorEvent(ErrorEvent.ELEVATOR_STUCK, 100000));
			elevatorList[2].addError(new ErrorEvent(ErrorEvent.DOOR_STUCK, 5000));
			elevatorList[3].addError(new ErrorEvent(ErrorEvent.DOOR_STUCK, 5000));
		}
	}
	public void closeSockets() {
		for (Elevator e: elevatorList) {
			e.closeSockets();
		}
		System.exit(0);
	}
	public void print(String message) {
		System.out.println("ELEVATOR SUBSYSTEM: " + message);
	}
	public void wait(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	public static void main(String args[]) {
		int numFloors = 0, numElevators = 0;
		String[] options = {"Use Defaults", "Use User Inputs"};
		int popUp = JOptionPane.showOptionDialog(null, "Enter Set Up Values For Elevator Subsystem", 
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
		ElevatorSubsystem c = new ElevatorSubsystem(numFloors, numElevators, DEFAULT_MODE);
	}
}