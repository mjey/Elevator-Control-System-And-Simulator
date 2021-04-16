import java.util.ArrayList;
public class Floor {
  private final int floorNum;
  private final FloorSubsystem fSystem;
  private boolean upPressed;
  private boolean downPressed;
  private int destFloor;
  private ArrayList < Integer > floorLamps;
  public Floor(int floorNum, FloorSubsystem fSystem) {
    this.floorNum = floorNum;
    this.fSystem = fSystem;
    floorLamps = new ArrayList < Integer > ();
  }
  public void pressUp() {
    upPressed = true;
  }
  public void pressDown() {
    downPressed = true;
  }
  public boolean upPressed() {
    return upPressed;
  }
  public boolean downPressed() {
    return downPressed;
  }
  public void setDestination(int destFloor) {
    this.destFloor = destFloor;
  }
  public void setLamps(ArrayList < Integer > floorLamps) {
    this.floorLamps = floorLamps;
  }
  public FloorData getFloorData() {
    if (upPressed)
      return new FloorData(floorNum, true, destFloor);
    return new FloorData(floorNum, false, destFloor);
  }
}