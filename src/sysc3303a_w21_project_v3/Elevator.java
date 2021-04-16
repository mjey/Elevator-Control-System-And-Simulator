import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.util.*;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

public class Elevator extends Thread {
  public final static int TEST_MODE = 2, TIMING_MODE = 1, DEFAULT_MODE = 0;
  private int runMode, currDirection, currFloor;
  private final int elevatorNum, numFloors;
  public final int UP = 0, DOWN = 1, IDLE = 2;
  private boolean movingUp, movingDown, doorOpen, shutdown, doorStuck, actionReady, replyRequired;
  private GUI elevatorGUI;
  private SchedulerData scheDat;
  private Timer arrivalSensorsTimer, elevatorButtonsTimer;
  private ElevatorSubsystem eSystem;
  private ElevatorCommunicator communicator;
  private ArrayList < Integer > reqFloors, subReqFloors, destFloors[];
  private ArrayList < ErrorEvent > errorList;
  private long initializedTime;
  private JTextArea elevatorLog;
  public Elevator(int elevatorNum, int numFloors, ElevatorSubsystem eSystem, int port, GUI elevatorGUI, int runMode) {
    this.elevatorGUI = elevatorGUI;
    this.elevatorNum = elevatorNum;
    this.numFloors = numFloors;
    this.eSystem = eSystem;
    this.runMode = runMode;
    movingUp = false;
    movingDown = false;
    currDirection = IDLE;
    doorOpen = false;
    currFloor = 1;
    shutdown = false;
    doorStuck = false;
    reqFloors = new ArrayList < Integer > ();
    subReqFloors = new ArrayList < Integer > ();
    destFloors = new ArrayList[numFloors];
    errorList = new ArrayList < ErrorEvent > ();
    initializedTime = System.currentTimeMillis();
    actionReady = false;
    if (runMode == DEFAULT_MODE || runMode == TIMING_MODE)
      createAndShowGUI();
    communicator = new ElevatorCommunicator(port, this);
    communicator.start();
    if (runMode == TIMING_MODE) {
      arrivalSensorsTimer = new Timer("arrival_sensors.txt");
      elevatorButtonsTimer = new Timer("elevator_buttons.txt");
      arrivalSensorsTimer.start();
      elevatorButtonsTimer.start();
    }
    for (int i = 0; i < numFloors; i++) {
      destFloors[i] = new ArrayList < Integer > ();
    }
  }
  public void createAndShowGUI() {
    elevatorLog = new JTextArea();
    elevatorLog.setFont(new Font("Arial", Font.ROMAN_BASELINE, 14));
    elevatorLog.setLineWrap(true);
    elevatorLog.setWrapStyleWord(true);
    JScrollPane areaScrollPane = new JScrollPane(elevatorLog);
    areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    areaScrollPane.setPreferredSize(new Dimension(800, 500));
    areaScrollPane.setBorder(
      BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(),
        BorderFactory.createEmptyBorder(5, 5, 5, 5)), areaScrollPane.getBorder()));
    DefaultCaret caret = (DefaultCaret) elevatorLog.getCaret();
    caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    JPanel schedulerPanel = new JPanel(new BorderLayout());
    schedulerPanel.add(areaScrollPane, BorderLayout.CENTER);
    JFrame frame = new JFrame("Elevator " + elevatorNum + " Log");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    Container newContentPane = schedulerPanel;
    frame.setContentPane(newContentPane);
    frame.setPreferredSize(new Dimension(500, 300));
    frame.setLocation(100 + (425 * 3), 50);
    frame.pack();
    frame.setVisible(true);
  }
  public void addError(ErrorEvent err) {
    errorList.add(err);
  }
  public boolean isShutDown() {
    return shutdown;
  }
  public void processPacket(SchedulerData s) {
    scheDat = s;
    int mode = s.getMode();
    switch (mode) {
    case SchedulerData.CONTINUE_REQUEST:
      if (arrivalSensorsTimer != null)
        if (arrivalSensorsTimer.isTiming()) {
          arrivalSensorsTimer.endTime();
        }
      print("Received CONTINUE request.");
      actionReady = true;
      break;
    case SchedulerData.FLOOR_REQUEST:
      print("Received FLOOR request.");
      int floor = s.getReqFloor();
      if (!reqFloors.contains(floor)) {
        switch (currDirection) {
        case UP:
          if (currFloor > floor) {
            subReqFloors.add(floor);
          } else if (currFloor < floor) {
            reqFloors.add(floor);
            Collections.sort(reqFloors);
            Collections.sort(subReqFloors);
          }
          break;
        case DOWN:
          if (currFloor < floor) {
            subReqFloors.add(floor);
          } else if (currFloor > floor) {
            reqFloors.add(floor);
            Collections.sort(reqFloors);
            Collections.reverse(reqFloors);
            Collections.sort(subReqFloors);
            Collections.reverse(subReqFloors);
          }
          break;
        case IDLE:
          reqFloors.add(floor);
          break;
        }
        if (!subReqFloors.isEmpty()) {
          switch (currDirection) {
          case UP:
            if (currFloor < subReqFloors.get(0)) {
              reqFloors.addAll(subReqFloors);
              Collections.sort(reqFloors);
              subReqFloors.clear();
            }
            break;
          case DOWN:
            if (currFloor > subReqFloors.get(0)) {
              reqFloors.addAll(subReqFloors);
              Collections.sort(reqFloors);
              Collections.reverse(subReqFloors);
              subReqFloors.clear();
            }
            break;
          case IDLE:
            reqFloors.addAll(subReqFloors);
            subReqFloors.clear();
            break;
          }
        }
      }
      destFloors[floor - 1].add(s.getDestFloor());
      print("Current requests: " + reqFloors.toString());
      ArrayList < Integer > allRequests = new ArrayList < Integer > ();
      allRequests.addAll(reqFloors);
      allRequests.addAll(subReqFloors);
      elevatorGUI.setRequestsInfo(elevatorNum, allRequests);
      break;
    case SchedulerData.MOVE_REQUEST:
      print("Received MOVE request.");
      if (elevatorButtonsTimer != null)
        if (elevatorButtonsTimer.isTiming()) {
          elevatorButtonsTimer.endTime();
        }
      if (doorOpen)
        closeDoor();
      if (s.moveUp()) {
        moveUp();
        Collections.sort(reqFloors);
      } else {
        moveDown();
        Collections.sort(reqFloors);
        Collections.reverse(reqFloors);
      }
      actionReady = true;
      break;
    case SchedulerData.STOP_REQUEST:
      if (arrivalSensorsTimer != null)
        if (arrivalSensorsTimer.isTiming()) {
          arrivalSensorsTimer.endTime();
        }
      print("Received STOP request.");
      stopMotor();
      openDoor();
      print("Arrived at floor " + currFloor + ".\n");
      if (!reqFloors.isEmpty()) {
        if (reqFloors.contains(currFloor))
          reqFloors.remove(new Integer(currFloor));
        if (reqFloors.isEmpty()) {
          currDirection = IDLE;
          elevatorGUI.setDirectionInfo(elevatorNum, "IDLE");
        } else {
          if (currDirection == UP) {
            elevatorGUI.setDirectionInfo(elevatorNum, "UP");
          } else {
            elevatorGUI.setDirectionInfo(elevatorNum, "DOWN");
          }
        }
        if (!destFloors[currFloor - 1].isEmpty()) {
          switch (currDirection) {
          case UP:
            if (currFloor < destFloors[currFloor - 1].get(0)) {
              reqFloors.removeAll(destFloors[currFloor - 1]);
              reqFloors.addAll(destFloors[currFloor - 1]);
              Collections.sort(reqFloors);
              destFloors[currFloor - 1].clear();
            }
            break;
          case DOWN:
            if (currFloor > destFloors[currFloor - 1].get(0)) {
              reqFloors.removeAll(destFloors[currFloor - 1]);
              reqFloors.addAll(destFloors[currFloor - 1]);
              Collections.sort(reqFloors);
              Collections.reverse(reqFloors);
              destFloors[currFloor - 1].clear();
            }
            break;
          case IDLE:
            reqFloors.addAll(destFloors[currFloor - 1]);
            Collections.sort(reqFloors);
            destFloors[currFloor - 1].clear();
            if (currFloor < reqFloors.get(0)) {
              currDirection = UP;
              elevatorGUI.setDirectionInfo(elevatorNum, "UP");
            } else {
              currDirection = DOWN;
              elevatorGUI.setDirectionInfo(elevatorNum, "DOWN");
            }
            break;
          }
          if (elevatorButtonsTimer != null)
            elevatorButtonsTimer.startTime();
        }
      }
      elevatorGUI.setRequestsInfo(elevatorNum, reqFloors);
      break;
    case SchedulerData.DOOR_REQUEST:
      print("Received DOOR request.");
      break;
    }
    if (mode == SchedulerData.STOP_REQUEST) {
      replyRequired = true;
      communicator.send();
      actionReady = true;
      waitForInstruction();
    } else if (mode != SchedulerData.CONTINUE_REQUEST || mode != SchedulerData.DOOR_REQUEST) {
      replyRequired = false;
      communicator.send();
      actionReady = true;
    }
  }
  public void waitForInstruction() {
    print("Awaiting Instruction.\n");
    do {
      wait(50);
    } while (!actionReady);
  }
  public void moveOneFloor() {
    while (doorOpen && !isIdle()) {
      wait(50);
    }
    if (!isIdle()) {
      if (!errorList.isEmpty()) {
        long currentTime = System.currentTimeMillis() - initializedTime;
        Random rand = new Random();
        if ((currentTime / 1000) > (10 + rand.nextInt(10))) {
          ErrorEvent e = errorList.get(0);
          if (e.getType() == ErrorEvent.ELEVATOR_STUCK) {
            if (movingUp) {
              print("Stuck between floors " + currFloor + " and " + (currFloor + 1) + ".");
            } else {
              print("Stuck between floors " + currFloor + " and " + (currFloor - 1) + ".");
            }
            shutdown = true;
            communicator.send();
            print("SHUTTING DOWN...");
            elevatorGUI.setShutdown(elevatorNum);
          }
        }
      }
      if (!shutdown) {
        switch (currDirection) {
        case UP:
          if (currFloor != reqFloors.get(0)) {
            currFloor++;
          }
          if (currFloor > numFloors) {
            currFloor = numFloors;
          }
          print("Currently on floor " + currFloor + ", moving up.");
          elevatorGUI.setDirectionInfo(elevatorNum, "UP");
          break;
        case DOWN:
          if (currFloor != reqFloors.get(0)) {
            currFloor--;
          }
          if (currFloor <= 0) {
            currFloor = 1;
          }
          print("Currently on floor " + currFloor + ", moving down.");
          elevatorGUI.setDirectionInfo(elevatorNum, "DOWN");
          break;
        case IDLE:
          elevatorGUI.setDirectionInfo(elevatorNum, "IDLE");
          break;
        }
        elevatorGUI.setCurrentFloorInfo(elevatorNum, currFloor);
        elevatorGUI.setElevatorDoor(elevatorNum, currFloor, GUI.MOVING);
        wait(1000);
      }
    }
  }
  public void moveUp() {
    print("Now moving up.");
    movingUp = true;
    movingDown = false;
    currDirection = UP;
  }
  public void moveDown() {
    print("Now moving down.");
    movingUp = false;
    movingDown = true;
    currDirection = DOWN;
  }
  public void stopMotor() {
    print("Now stopping.");
    movingUp = false;
    movingDown = false;
  }
  public boolean isIdle() {
    if (currDirection == IDLE)
      return true;
    return false;
  }
  public void openDoor() {
    print("Opening doors.");
    actionReady = false;
    if (!errorList.isEmpty()) {
      long currentTime = System.currentTimeMillis() - initializedTime;
      Random rand = new Random();
      if ((currentTime / 1000) > (10 + rand.nextInt(10))) {
        ErrorEvent e = errorList.get(0);
        if (e.getType() == ErrorEvent.DOOR_STUCK) {
          doorStuck = true;
          wait(1000);
          print("Doors STUCK.");
          elevatorGUI.setElevatorDoor(elevatorNum, currFloor, GUI.STUCK);
          replyRequired = true;
          communicator.send();
          do {
            communicator.receive();
            wait(1000);
          } while (scheDat.getMode() != SchedulerData.DOOR_REQUEST);
          errorList.remove(0);
        }
      }
    }
    wait(1000);
    print("Doors opened.");
    elevatorGUI.setDoorsInfo(elevatorNum, GUI.OPEN);
    elevatorGUI.setElevatorDoor(elevatorNum, currFloor, GUI.OPEN);
    doorOpen = true;
    doorStuck = false;
    actionReady = true;
  }
  public void closeDoor() {
    print("Closing doors.");
    actionReady = false;
    if (!errorList.isEmpty()) {
      long currentTime = System.currentTimeMillis() - initializedTime;
      Random rand = new Random();
      if ((currentTime / 1000) > (10 + rand.nextInt(10))) {
        ErrorEvent e = errorList.get(0);
        if (e.getType() == ErrorEvent.DOOR_STUCK) {
          doorStuck = true;
          wait(1000);
          print("Doors STUCK.");
          elevatorGUI.setElevatorDoor(elevatorNum, currFloor, GUI.STUCK);
          replyRequired = true;
          communicator.send();
          communicator.receive();
          do {
            wait(1000);
          } while (scheDat.getMode() != SchedulerData.DOOR_REQUEST);
          errorList.remove(0);
        }
      }
    }
    wait(1000);
    print("Doors closed.");
    elevatorGUI.setDoorsInfo(elevatorNum, GUI.CLOSED);
    elevatorGUI.setElevatorDoor(elevatorNum, currFloor, GUI.CLOSED);
    doorOpen = false;
    doorStuck = false;
    actionReady = true;
  }
  public synchronized ElevatorData getElevatorData() {
    int errType;
    if (doorStuck) {
      errType = ElevatorData.DOOR_STUCK_ERROR;
    } else if (shutdown) {
      errType = ElevatorData.ELEVATOR_STUCK_ERROR;
    } else {
      errType = ElevatorData.NO_ERROR;
    }
    return new ElevatorData(elevatorNum, errType, currFloor, reqFloors, movingUp, movingDown, currDirection,
      doorOpen, shutdown, replyRequired);
  }
  public void closeSockets() {
    communicator.freeSockets();
  }
  public void wait(int ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
  public void print(String message) {
    if (runMode == TIMING_MODE || runMode == DEFAULT_MODE)
      elevatorLog.append(" Elevator " + elevatorNum + ": " + message + "\n");
  }
  public synchronized void pause() {
    try {
      this.wait();
    } catch (Exception e) {}
  }
  public synchronized void wake() {
    try {
      this.notify();
    } catch (Exception e) {}
  }
  @Override
  public void run() {
    print("Started.");
    while (true) {
      if (isIdle() && reqFloors.isEmpty()) {
        print("ON STANDBY");
        pause();
      }
      if (!reqFloors.isEmpty() && !shutdown && !doorStuck && actionReady) {
        moveOneFloor();
        if (!shutdown) {
          replyRequired = true;
          actionReady = false;
          communicator.send();
          if (arrivalSensorsTimer != null) {
            arrivalSensorsTimer.startTime();
          }
          waitForInstruction();
        }
      }
      wait(500);
    }
  }
}