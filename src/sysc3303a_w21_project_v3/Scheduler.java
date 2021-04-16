import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.awt.*;
import javax.swing.*;
import javax.swing.text.DefaultCaret;

public class Scheduler extends Thread {
	
  public final static int TEST_MODE = 2;
  public final static int TIMING_MODE = 1;
  public final static int DEFAULT_MODE = 0;
  private int runMode;
  private boolean running;
  DatagramPacket floorSendPacket, elevatorSendPacket, receivePacket;
  DatagramSocket sendSocket, receiveSocket;
  InetAddress elevatorAddress;
  InetAddress floorAddress;
  private int floorPort;
  ArrayList < DatagramPacket > receiveQueue;
  private boolean floorLamps[];
  private boolean arrivalSensors[];
  private final int numFloors;
  private ElevatorData elevatorList[];
  private boolean upToDate[];
  private ArrayList < ElevatorData > potentialRoutes;
  private int routedElevator;
  private SchedulerData scheDat;
  private FloorData floorDat;
  private ElevatorData elevDat;
  private ArrayList < FloorData > pendRequests;
  private JTextArea schedulerLog;
  public Scheduler(int numFloors, int numElevators, int runMode) {
    try {
      sendSocket = new DatagramSocket();
      receiveSocket = new DatagramSocket(3000);
    } catch (SocketException se) {
      se.printStackTrace();
      System.exit(1);
    }
    try {
      elevatorAddress = InetAddress.getLocalHost();
      floorAddress = InetAddress.getLocalHost();
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }
    this.runMode = runMode;
    running = true;
    receiveQueue = new ArrayList < DatagramPacket > ();
    this.numFloors = numFloors;
    floorLamps = new boolean[numFloors];
    arrivalSensors = new boolean[numFloors];
    pendRequests = new ArrayList < FloorData > ();
    elevatorList = new ElevatorData[numElevators];
    upToDate = new boolean[numElevators];
    for (int i = 0; i < numElevators; i++) {
      elevatorList[i] = new ElevatorData(i, ElevatorData.NO_ERROR,
        1, new ArrayList < Integer > (), false, false, 2, false, false, true);
      upToDate[i] = true;
    }
    if (runMode == DEFAULT_MODE || runMode == TIMING_MODE) {
      createAndShowGUI();
      requestAddress();
    }
    this.start();
  }
  public void requestAddress() {
    String[] options = {
      "Same Computer as Elevator Subsystem",
      "Separate Computer"
    };
    int popUp = JOptionPane.showOptionDialog(null, "Select Scheduler Run Configuration",
      "Confirmation", JOptionPane.INFORMATION_MESSAGE, 0, null, options, options[0]);
    switch (popUp) {
    case -1:
      System.exit(0);
    case 0:
      try {
        elevatorAddress = InetAddress.getLocalHost();
      } catch (UnknownHostException e1) {
        e1.printStackTrace();
      }
      break;
    case 1:
      try {
        elevatorAddress = InetAddress.getByName(JOptionPane.showInputDialog("Enter the IP address of the elevator subsystem:"));
      } catch (HeadlessException e) {
        e.printStackTrace();
      } catch (UnknownHostException e) {
        e.printStackTrace();
      }
    }
  }
  public void createAndShowGUI() {
    schedulerLog = new JTextArea();
    schedulerLog.setFont(new Font("Arial", Font.ROMAN_BASELINE, 14));
    schedulerLog.setLineWrap(true);
    schedulerLog.setWrapStyleWord(true);
    JScrollPane areaScrollPane = new JScrollPane(schedulerLog);
    areaScrollPane.setVerticalScrollBarPolicy(
      JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    areaScrollPane.setPreferredSize(new Dimension(800, 500));
    areaScrollPane.setBorder(
      BorderFactory.createCompoundBorder(
        BorderFactory.createCompoundBorder(
          BorderFactory.createEmptyBorder(),
          BorderFactory.createEmptyBorder(5, 5, 5, 5)),
        areaScrollPane.getBorder()));
    DefaultCaret caret = (DefaultCaret) schedulerLog.getCaret();
    caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    JPanel schedulerPanel = new JPanel(new BorderLayout());
    schedulerPanel.add(areaScrollPane, BorderLayout.CENTER);
    JFrame frame = new JFrame("Scheduler Log");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    Container newContentPane = schedulerPanel;
    frame.setContentPane(newContentPane);
    frame.setPreferredSize(new Dimension(500, 300));
    frame.setLocation(100 + (425 * 3), 350);
    frame.pack();
    frame.setVisible(true);
  }
  public void closeSockets() {
    sendSocket.close();
    receiveSocket.close();
    running = false;
  }
  public void floorSend(SchedulerFloorData data) {
    try {
      ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
      ObjectOutputStream ooStream;
      ooStream = new ObjectOutputStream(new BufferedOutputStream(baoStream));
      ooStream.flush();
      ooStream.writeObject(data);
      ooStream.flush();
      byte msg[] = baoStream.toByteArray();
      floorSendPacket = new DatagramPacket(msg, msg.length, floorAddress,
        floorPort);
    } catch (IOException e1) {
      e1.printStackTrace();
    }
    try {
      sendSocket.send(floorSendPacket);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
    print("Scheduler: Sent packet to FloorSubsystem.");
  }
  public void elevatorSend(SchedulerData scheDat) {
    this.scheDat = scheDat;
    int targetPort = 2000 + scheDat.getElevatorNumber();
    try {
      ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
      ObjectOutputStream ooStream;
      ooStream = new ObjectOutputStream(new BufferedOutputStream(baoStream));
      ooStream.flush();
      ooStream.writeObject(scheDat);
      ooStream.flush();
      byte msg[] = baoStream.toByteArray();
      elevatorSendPacket = new DatagramPacket(msg, msg.length, elevatorAddress, targetPort);
    } catch (IOException e1) {
      e1.printStackTrace();
    }
    try {
      sendSocket.send(elevatorSendPacket);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
    print("Scheduler: Sent packet to Elevator " + scheDat.getElevatorNumber() + ".");
    wait(100);
  }
  public void receive() {
    byte data[] = new byte[5000];
    receivePacket = new DatagramPacket(data, data.length);
    try {
      receiveSocket.receive(receivePacket);
      receiveQueue.add(receivePacket);
    } catch (IOException e) {
      print("IO Exception: likely:");
      print("Receive Socket Timed Out.\n" + e);
      e.printStackTrace();
      System.exit(1);
    }
  }
  public void processAndSend() {
    try {
      if (!receiveQueue.isEmpty()) {
        for (DatagramPacket dPacket: receiveQueue) {
          ByteArrayInputStream byteStream = new ByteArrayInputStream(dPacket.getData());
          ObjectInputStream is;
          is = new ObjectInputStream(new BufferedInputStream(byteStream));
          Object o = is.readObject();
          is.close();
          if (o instanceof FloorData) {
            floorDat = (FloorData) o;
            print("Scheduler: Packet received.");
            print("Containing:\n	" + floorDat.getStatus() + "\n");
            floorAddress = receivePacket.getAddress();
            floorPort = receivePacket.getPort();
            updateRequests();
            floorSend(new SchedulerFloorData(SchedulerFloorData.CONFIRM_MESSAGE));
          } else {
            elevDat = (ElevatorData) o;
            elevatorAddress = receivePacket.getAddress();
            elevatorList[elevDat.getElevatorNumber()] = elevDat;
            upToDate[elevDat.getElevatorNumber()] = true;
            displayElevatorStates();
            manageElevators();
          }
          if (!pendRequests.isEmpty())
            routeElevator();
        }
      }
      receiveQueue.clear();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }
  public void wait(int ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
  public void updateRequests() {
    if (!pendRequests.contains(floorDat))
      pendRequests.add(floorDat);
  }
  public void displayElevatorStates() {
    print("ELEVATOR STATUS:");
    for (ElevatorData e: elevatorList) {
      print("	" + e.getStatus());
    }
    print("\n");
  }
  public void manageElevators() {
    ElevatorData e = elevDat;
    SchedulerData s = null;
    int errType = e.getErrorType();
    int currentFloor = e.getCurrentFloor();
    if (e.replyRequired()) {
      switch (errType) {
      case ElevatorData.NO_ERROR:
        if (e.getRequestedFloors().contains(currentFloor)) {
          print("SIGNAL STOP to Elevator: " + e.getElevatorNumber() + ".");
          s = new SchedulerData(e.getElevatorNumber(), SchedulerData.STOP_REQUEST, false, false, true);
        } else if (!e.getRequestedFloors().isEmpty()) {
          if (currentFloor > e.getRequestedFloors().get(0) && (e.doorOpened() || e.isIdle())) {
            print("SIGNAL MOVE DOWN to elevator: " + e.getElevatorNumber());
            s = new SchedulerData(e.getElevatorNumber(), SchedulerData.MOVE_REQUEST, false, true,
              false);
          } else if (currentFloor < e.getRequestedFloors().get(0) && (e.doorOpened() || e.isIdle())) {
            print("SIGNAL MOVE UP to elevator: " + e.getElevatorNumber());
            s = new SchedulerData(e.getElevatorNumber(), SchedulerData.MOVE_REQUEST, true, false,
              false);
          } else {
            print("SIGNAL CONTINUE to elevator: " + e.getElevatorNumber());
            s = new SchedulerData(e.getElevatorNumber(), SchedulerData.CONTINUE_REQUEST);
          }
        }
        break;
      case ElevatorData.DOOR_STUCK_ERROR:
        if (e.doorOpened())
          print("SIGNAL CLOSE DOORS to elevator: " + e.getElevatorNumber());
        else
          print("SIGNAL OPEN DOORS to elevator: " + e.getElevatorNumber());
        s = new SchedulerData(e.getElevatorNumber(), SchedulerData.DOOR_REQUEST);
        break;
      case ElevatorData.ELEVATOR_STUCK_ERROR:
        print("Elevator " + e.getElevatorNumber() + ": SHUTDOWN.");
        break;
      }
      if (s != null)
        elevatorSend(s);
    }
  }
  public void updateLamps() {
    floorLamps[elevDat.getCurrentFloor() - 1] = true;
  }
  public boolean elevatorSameFloor(int floor) {
    potentialRoutes.clear();
    boolean caseTrue = false;
    for (int i = 0; i < elevatorList.length; i++) {
      if (floor == elevatorList[i].getCurrentFloor() && upToDate[i]) {
        caseTrue = true;
        potentialRoutes.add(elevatorList[i]);
      }
    }
    return caseTrue;
  }
  public boolean elevatorAboveFloor(int floor) {
    potentialRoutes.clear();
    boolean caseTrue = false;
    for (int i = 0; i < elevatorList.length; i++) {
      if (elevatorList[i].isOperational() && elevatorList[i].getCurrentFloor() > floor) {
        potentialRoutes.add(elevatorList[i]);
        caseTrue = true;
      }
    }
    return caseTrue;
  }
  public boolean elevatorBelowFloor(int floor) {
    potentialRoutes.clear();
    boolean caseTrue = false;
    for (int i = 0; i < elevatorList.length; i++) {
      if (elevatorList[i].isOperational() && elevatorList[i].getCurrentFloor() < floor) {
        potentialRoutes.add(elevatorList[i]);
        caseTrue = true;
      }
    }
    return caseTrue;
  }
  public boolean allElevatorsAboveFloor(int floor) {
    potentialRoutes.clear();
    for (int i = 0; i < elevatorList.length; i++) {
      if (elevatorList[i].isOperational() && elevatorList[i].getCurrentFloor() < floor) {
        potentialRoutes.add(elevatorList[i]);
        return false;
      }
    }
    return true;
  }
  public boolean allElevatorsBelowFloor(int floor) {
    potentialRoutes.clear();
    for (int i = 0; i < elevatorList.length; i++) {
      potentialRoutes.add(elevatorList[i]);
      if (elevatorList[i].isOperational() && elevatorList[i].getCurrentFloor() < floor) {
        return false;
      }
    }
    return true;
  }
  public int closestElevator() {
    if (!potentialRoutes.isEmpty()) {
      ElevatorData closest = potentialRoutes.get(0);
      for (ElevatorData e: potentialRoutes) {
        if (Math.abs((e.getCurrentFloor() - floorDat.getFloorNum())) < Math
          .abs((closest.getCurrentFloor() - floorDat.getFloorNum()))) {
          closest = e;
        }
      }
      return closest.getElevatorNumber();
    }
    return -1;
  }
  public void determineAbove(int floor) {
    ArrayList < ElevatorData > remove = new ArrayList < ElevatorData > ();
    for (ElevatorData ed: potentialRoutes) {
      if (ed.getCurrentFloor() >= floor) {
        remove.add(ed);
      }
    }
    potentialRoutes.removeAll(remove);
  }
  public void determineBelow(int floor) {
    ArrayList < ElevatorData > remove = new ArrayList < ElevatorData > ();
    for (ElevatorData ed: potentialRoutes) {
      if (ed.getCurrentFloor() <= floor) {
        remove.add(ed);
      }
    }
    potentialRoutes.removeAll(remove);
  }
  public void determineMovingUp() {
    ArrayList < ElevatorData > remove = new ArrayList < ElevatorData > ();
    for (ElevatorData ed: potentialRoutes) {
      if (!ed.isMovingUp()) {
        remove.add(ed);
      }
    }
    potentialRoutes.removeAll(remove);
  }
  public void determineMovingDown() {
    ArrayList < ElevatorData > remove = new ArrayList < ElevatorData > ();
    for (ElevatorData ed: potentialRoutes) {
      if (!ed.isMovingDown()) {
        remove.add(ed);
      }
    }
    potentialRoutes.removeAll(remove);
  }
  public void determineIdle() {
    ArrayList < ElevatorData > remove = new ArrayList < ElevatorData > ();
    for (ElevatorData ed: potentialRoutes) {
      if (!ed.isIdle()) {
        remove.add(ed);
      }
    }
    potentialRoutes.removeAll(remove);
  }
  public boolean isAnyMovingUp() {
    for (ElevatorData ed: potentialRoutes) {
      if (ed.isMovingUp()) {
        return true;
      }
    }
    return false;
  }
  public boolean isAnyMovingDown() {
    for (ElevatorData ed: potentialRoutes) {
      if (ed.isMovingDown()) {
        return true;
      }
    }
    return false;
  }
  public boolean isAnyIdle() {
    for (ElevatorData ed: potentialRoutes) {
      if (ed.isIdle()) {
        return true;
      }
    }
    return false;
  }
  public void routeElevator() {
    potentialRoutes = new ArrayList < ElevatorData > ();
    ArrayList < FloorData > completedRequests = new ArrayList < FloorData > ();
    routedElevator = -1;
    for (FloorData fd: pendRequests) {
      int floor = fd.getFloorNum();
      if (elevatorSameFloor(floor) && isAnyIdle()) {
        determineIdle();
        if (!potentialRoutes.isEmpty())
          routedElevator = potentialRoutes.get(0).getElevatorNumber();
        print("ROUTING CASE 0 - potential routes " + potentialRoutes.size());
      } else if (allElevatorsAboveFloor(floor)) {
        if (isAnyMovingDown() && fd.downPressed()) {
          print("ROUTING CASE 1 - potential routes " + potentialRoutes.size());
          determineMovingDown();
          routedElevator = closestElevator();
        } else if (isAnyIdle()) {
          determineIdle();
          routedElevator = potentialRoutes.get(0).getElevatorNumber();
          print("ROUTING CASE 2 - potential routes " + potentialRoutes.size());
        }
      } else if (allElevatorsBelowFloor(floor)) {
        if (isAnyMovingUp() && fd.upPressed()) {
          determineBelow(floor);
          determineMovingUp();
          routedElevator = closestElevator();
          print("ROUTING CASE 3 - potential routes " + potentialRoutes.size());
        } else if (isAnyIdle()) {
          determineIdle();
          routedElevator = potentialRoutes.get(0).getElevatorNumber();
          print("ROUTING CASE 4 - potential routes " + potentialRoutes.size());
        }
      } else {
        potentialRoutes.clear();
        for (ElevatorData e: elevatorList) {
          if (e.isOperational())
            potentialRoutes.add(e);
        }
        if (fd.upPressed() && isAnyMovingUp()) {
          determineMovingUp();
          determineBelow(floor);
          print("ROUTING CASE 5 - potential routes " + potentialRoutes.size());
        } else if (fd.downPressed() && isAnyMovingDown()) {
          determineMovingDown();
          determineAbove(floor);
          print("ROUTING CASE 6 - potential routes " + potentialRoutes.size());
        }
        if (!potentialRoutes.isEmpty()) {
          potentialRoutes.clear();
          for (ElevatorData e: elevatorList) {
            if (e.isOperational())
              potentialRoutes.add(e);
          }
          if (isAnyIdle()) {
            determineIdle();
            print("ROUTING CASE 7 - potential routes " + potentialRoutes.size());
            routedElevator = closestElevator();
          }
        } else {
          routedElevator = closestElevator();
        }
      }
      if (routedElevator != -1) {
        print("Sending request to Elevator " + routedElevator + ".\n");
        scheDat = new SchedulerData(routedElevator, SchedulerData.FLOOR_REQUEST, floorLamps, floor, floorDat.getDestFloor());
        elevatorSend(scheDat);
        completedRequests.add(fd);
        upToDate[routedElevator] = false;
      }
    }
    pendRequests.removeAll(completedRequests);
  }
  public ElevatorData getElevatorData() {
    return elevDat;
  }
  public SchedulerData getSchedulerData() {
    return scheDat;
  }
  public FloorData getFloorData() {
    return floorDat;
  }
  public void print(String message) {
    if (runMode == DEFAULT_MODE || runMode == TIMING_MODE)
      schedulerLog.append(" " + message + "\n");
  }
  public void run() {
    while (running) {
      receive();
      processAndSend();
      wait(50);
    }
  }
  public static void main(String args[]) {
    int numFloors = 0, numElevators = 0;
    String[] options = {
      "Use Defaults",
      "Use User Inputs"
    };
    int popUp = JOptionPane.showOptionDialog(null, "Enter Set Up Values For Scheduler",
      "Confirmation", JOptionPane.INFORMATION_MESSAGE, 0, null, options, options[0]);
    switch (popUp) {
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
    Scheduler c = new Scheduler(numFloors, numElevators, DEFAULT_MODE);
  }
}