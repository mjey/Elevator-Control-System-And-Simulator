import java.io.Serializable;
import java.util.ArrayList;
public class ElevatorData implements Serializable {
  public final static int NO_ERROR = 0;
  public final static int DOOR_STUCK_ERROR = 1;
  public final static int ELEVATOR_STUCK_ERROR = 2;
  public final int UP = 0;
  public final int DOWN = 1;
  public final int IDLE = 2;
  private final int elevatorNum;
  private final int currFloor;
  private ArrayList < Integer > reqFloor;
  private boolean movingUp;
  private boolean movingDown;
  private int currDirection;
  private boolean doorOpened;
  private boolean shutdown;
  private int errorType;
  private boolean replyRequired;
  private String status;
  public ElevatorData(int elevatorNum, int errorType, int currFloor,
    ArrayList < Integer > reqFloor, boolean movingUp, boolean movingDown, int currDirection,
    boolean doorOpened, boolean shutdown, boolean replyRequired) {
    this.elevatorNum = elevatorNum;
    this.errorType = errorType;
    this.currFloor = currFloor;
    this.reqFloor = reqFloor;
    this.movingUp = movingUp;
    this.movingDown = movingDown;
    this.currDirection = currDirection;
    this.doorOpened = doorOpened;
    this.shutdown = shutdown;
    this.replyRequired = replyRequired;
    switch (errorType) {
    case NO_ERROR:
      status = "Elevator " + elevatorNum + ": Current Floor - " + currFloor + ", requests " + reqFloor.toString() + ", ";
      if (movingUp)
        status += "moving up";
      else if (movingDown)
        status += "moving down";
      else
        status += "idle";
      if (doorOpened)
        status += ", door - open.";
      else
        status += ", door - closed.";
      break;
    case DOOR_STUCK_ERROR:
      if (doorOpened)
        status = "Elevator " + elevatorNum + ": Current Floor - " + currFloor +
        ", requests " + reqFloor.toString() + ", DOORS STUCK OPEN.";
      else {
        status = "Elevator " + elevatorNum + ": Current Floor - " + currFloor +
          ", requests " + reqFloor.toString() + ", DOORS STUCK CLOSED.";
      }
      break;
    case ELEVATOR_STUCK_ERROR:
      if (movingUp)
        status = "Elevator " + elevatorNum + ": STUCK BETWEEN floors " + currFloor +
        " and " + (currFloor + 1) + ".";
      else {
        status = "Elevator " + elevatorNum + ": STUCK BETWEEN floors " + currFloor +
          " and " + (currFloor - 1) + ".";
      }
    }
  }
  public int getCurrentFloor() {
    return currFloor;
  }
  public ArrayList < Integer > getRequestedFloors() {
    return reqFloor;
  }
  public boolean isMovingUp() {
    if (currDirection == UP)
      return true;
    return false;
  }
  public boolean isMovingDown() {
    if (currDirection == DOWN)
      return true;
    return false;
  }
  public boolean isIdle() {
    if (currDirection == IDLE)
      return true;
    return false;
  }
  public boolean doorOpened() {
    return doorOpened;
  }
  public String getStatus() {
    return status;
  }
  public int getElevatorNumber() {
    return elevatorNum;
  }
  public int getErrorType() {
    return errorType;
  }
  public boolean isOperational() {
    return !shutdown;
  }
  public boolean replyRequired() {
    return replyRequired;
  }
}