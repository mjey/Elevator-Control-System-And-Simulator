import java.io.Serializable;

public class SchedulerFloorData implements Serializable {
  public final static int CONFIRM_MESSAGE = 0;
  public final static int UPDATE_MESSAGE = 1;
  private final int mode;
  private int floorLamps[];
  public SchedulerFloorData(int mode, int floorLamps[]) {
    this.mode = mode;
    this.floorLamps = floorLamps;
  }
  public SchedulerFloorData(int mode) {
    this.mode = mode;
  }
  public int[] getFloorLamps() {
    return floorLamps;
  }
  public int getMode() {
    return mode;
  }
}