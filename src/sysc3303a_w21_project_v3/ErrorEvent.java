public class ErrorEvent {
  public final static int ELEVATOR_STUCK = 0;
  public final static int DOOR_STUCK = 1;
  int type;
  long timeOccur;
  public ErrorEvent(int type, long timeOccur) {
    this.type = type;
    this.timeOccur = timeOccur;
  }
  public int getType() {
    return type;
  }
  public long getTimeOccur() {
    return timeOccur;
  }
}