import java.io.Serializable;

public class SchedulerData implements Serializable {
    
  public final static int CONTINUE_REQUEST = 0;
  public final static int FLOOR_REQUEST = 1;
  public final static int MOVE_REQUEST = 2;
  public final static int STOP_REQUEST = 3;
  public final static int DOOR_REQUEST = 4;
  private final int mode;
  private final int elevatorNum;
  private boolean floorLamps[];
  private int floor;
  private int destFloor;
  private boolean moveUp, moveDown, doorOpen;
  public SchedulerData(int elevatorNum, int mode, boolean floorLamps[], int floor, int destFloor) {
    this.mode = mode;
    this.elevatorNum = elevatorNum;
    this.floorLamps = floorLamps;
    this.floor = floor;
    this.destFloor = destFloor;
  }
  public SchedulerData(int elevatorNum, int mode, boolean moveUp, boolean moveDown, boolean doorOpen) {
    this.mode = mode;
    this.elevatorNum = elevatorNum;
    this.moveUp = moveUp;
    this.moveDown = moveDown;
    this.doorOpen = doorOpen;
  }
  public SchedulerData(int elevatorNum, int mode) {
    this.elevatorNum = elevatorNum;
    this.mode = mode;
  }
  public int getElevatorNumber() {
    return elevatorNum;
  }
  public int getReqFloor() {
    return floor;
  }
  public int getDestFloor() {
    return destFloor;
  }
  public boolean[] getFloorLamps() {
    return floorLamps;
  }
  public boolean moveUp() {
    return moveUp;
  }
  public boolean moveDown() {
    return moveDown;
  }
  public boolean stop() {
    if (!moveUp && !moveDown)
      return true;
    return false;
  }
  public boolean doorOpen() {
    return doorOpen;
  }
  public int getMode() {
    return mode;
  }
}