import java.io.*;
import java.util.*;
import java.time.*;

public class FloorParser extends Thread {
  public final int REQUEST_TIME = 0;
  public final int SOURCE_FLOOR = 1;
  public final int DIRECTION = 2;
  public final int DEST_FLOOR = 3;
  public final boolean UP = true;
  public final boolean DOWN = false;
  private final int NUM_INSTRUCTION_FIELDS = 4;
  private long startTime;
  private FloorSubsystem fSystem;
  private int floors;
  private String filename;
  private ArrayList < String[] > requestList;
  private Queue < String[] > [] upQueue;
  private Queue < String[] > [] downQueue;
  public FloorParser(FloorSubsystem fSystem, int floors, String filename) {
    this.fSystem = fSystem;
    this.floors = floors;
    startTime = System.currentTimeMillis();
    this.filename = filename;
    requestList = new ArrayList < String[] > ();
    upQueue = new Queue[floors];
    downQueue = new Queue[floors];
  }
  public void parse() throws FileNotFoundException {
    File events = new File("Assets\\Request Files\\" + filename);
    Scanner scan = new Scanner(events);
    while (scan.hasNext()) {
      String request[] = new String[NUM_INSTRUCTION_FIELDS];
      for (int i = 0; i < NUM_INSTRUCTION_FIELDS; i++) {
        request[i] = scan.next();
      }
      requestList.add(request);
    }
    scan.close();
  }
  public long getRequestTimeInMillis(String requestTime) {
    LocalTime localTime = LocalTime.parse(requestTime);
    return localTime.toSecondOfDay() * 1000;
  }
  public long getElapsedTime() {
    return System.currentTimeMillis() - startTime;
  }
  public void dispatch() {
    for (int i = 0; i < floors; i++) {
      upQueue[i] = new LinkedList < String[] > ();
      downQueue[i] = new LinkedList < String[] > ();
    }
    for (String[] request: requestList) {
      int sourceFloor = Integer.parseInt(request[SOURCE_FLOOR]) - 1;
      if (request[DIRECTION].equals("up")) {
        upQueue[sourceFloor].add(request);
      } else {
        downQueue[sourceFloor].add(request);
      }
    }
  }
  public void run() {
    try {
      parse();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    dispatch();
    int count = 0;
    while (count < requestList.size()) {
      for (int i = 0; i < floors; i++) {
        if (upQueue[i].peek() != null) {
          String requestTime = upQueue[i].peek()[REQUEST_TIME];
          if (getElapsedTime() >= getRequestTimeInMillis(requestTime)) {
            String currentRequest[] = upQueue[i].poll();
            int source = Integer.parseInt(currentRequest[SOURCE_FLOOR]);
            int destination = Integer.parseInt(currentRequest[DEST_FLOOR]);
            fSystem.goUp(source, destination);
            count++;
          }
        }
        if (downQueue[i].peek() != null) {
          String requestTime = downQueue[i].peek()[REQUEST_TIME];
          if (getElapsedTime() >= getRequestTimeInMillis(requestTime)) {
            String currentRequest[] = downQueue[i].poll();
            int source = Integer.parseInt(currentRequest[SOURCE_FLOOR]);
            int destination = Integer.parseInt(currentRequest[DEST_FLOOR]);
            fSystem.goDown(source, destination);
            count++;
          }
        }
      }
    }
  }
}