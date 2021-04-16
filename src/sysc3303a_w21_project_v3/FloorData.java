import java.io.Serializable;
public class FloorData implements Serializable {
  private final int floorNum;
  private final boolean upPressed;
  private final int destFloor;
  private String status;
  public FloorData(int floorNum, boolean upPressed, int destFloor) {
    this.floorNum = floorNum;
    this.upPressed = upPressed;
    this.destFloor = destFloor;
    if (upPressed)
      status = "Floor " + floorNum + ": request to go up to floor " + destFloor;
    else
      status = "Floor " + floorNum + ": request to go down to floor " + destFloor;
  }
  public boolean upPressed() {
    if (upPressed)
      return upPressed;
    return false;
  }
  public boolean downPressed() {
    if (!upPressed)
      return true;
    return false;
  }
  public int getFloorNum() {
    return floorNum;
  }
  public int getDestFloor() {
    return destFloor;
  }
  public void setStatus(String status) {
    this.status = status;
  }
  public String getStatus() {
    return status;
  }
}