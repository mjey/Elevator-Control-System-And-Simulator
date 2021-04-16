import java.util.ArrayList;
import junit.framework.TestCase;
public class TestCases extends TestCase {
  private Scheduler s;
  private FloorSubsystem f;
  private ElevatorSubsystem e;
  private long initialization;
  void waitToCheck(long waitTime) {
    boolean checkTest = false;
    while (!checkTest) {
      long elapsedTime = System.currentTimeMillis() - initialization;
      if (elapsedTime >= waitTime) {
        checkTest = true;
      }
    }
  }
  public void waitFor(long waitTime) {
    try {
      Thread.sleep(waitTime);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
  public void setUp() throws Exception {
    e = new ElevatorSubsystem(5, 2, Main.TEST_MODE);
    s = new Scheduler(5, 2, Main.TEST_MODE);
    f = new FloorSubsystem(5, Main.TEST_MODE);
    initialization = System.currentTimeMillis();
  }
  public void tearDown() throws Exception {}
  public void testBasicSystemFunctionality() {
    f.goUp(1, 5);
    waitToCheck(500);
    ArrayList < Integer > elev0Requests = e.getElevator(0).getElevatorData().getRequestedFloors();
    assertEquals(elev0Requests.size(), 1);
    waitFor(2000);
    f.goUp(1, 4);
    waitFor(500);
    ArrayList < Integer > elev1Requests = e.getElevator(1).getElevatorData().getRequestedFloors();
    assertEquals(elev1Requests.size(), 1);
    waitToCheck(12000);
    int elev0floor = e.getElevator(0).getElevatorData().getCurrentFloor();
    assertEquals(elev0floor, 5);
    int elev1floor = e.getElevator(1).getElevatorData().getCurrentFloor();
    assertEquals(elev1floor, 4);
    boolean elev0doorOpen = e.getElevator(0).getElevatorData().doorOpened();
    boolean elev1doorOpen = e.getElevator(1).getElevatorData().doorOpened();
    assertTrue(elev0doorOpen);
    assertTrue(elev1doorOpen);
    elev0Requests = e.getElevator(0).getElevatorData().getRequestedFloors();
    elev1Requests = e.getElevator(1).getElevatorData().getRequestedFloors();
    assertTrue(elev0Requests.isEmpty());
    assertTrue(elev1Requests.isEmpty());
  }
}