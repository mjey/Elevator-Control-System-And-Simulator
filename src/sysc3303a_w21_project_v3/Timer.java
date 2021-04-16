import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Timer extends Thread {
  private long initialization;
  private long startTime;
  private long endTime;
  private boolean timing;
  private boolean recording;
  private ArrayList < String > measurements;
  private String filename;
  public Timer(String filename) {
    this.filename = filename;
    timing = false;
    recording = true;
    measurements = new ArrayList < String > ();
  }
  public void startTime() {
    startTime = System.currentTimeMillis();
    timing = true;
  }
  public void endTime() {
    endTime = System.currentTimeMillis();
    if (recording) {
      measurements.add("" + (endTime - startTime));
    }
    timing = false;
  }
  public boolean isTiming() {
    return timing;
  }
  public void saveMeasurements() throws IOException {
    recording = false;
    File file = new File("Assets\\Measurements\\" + filename);
    if (!file.exists()) {
      file.createNewFile();
    }
    FileWriter fileWriter = new FileWriter(file, true);
    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
    for (String measurement: measurements) {
      bufferedWriter.write(measurement + " ms\n");
    }
    bufferedWriter.close();
  }
  public void run() {
    initialization = System.currentTimeMillis();
    while (true) {
      long elapsedTime = System.currentTimeMillis() - initialization;
      if (elapsedTime > 60000) {
        try {
          saveMeasurements();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }
}