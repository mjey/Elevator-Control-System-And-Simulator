import java.io.*;
import java.net.*;

public class ElevatorCommunicator extends Thread {
  DatagramPacket sendPacket, receivePacket;
  DatagramSocket sendSocket, receiveSocket;
  private boolean running;
  private Elevator elevator;
  private SchedulerData scheDat;
  private InetAddress schedulerAddress;
  private int port;
  public ElevatorCommunicator(int port, Elevator e) {
    try {
      sendSocket = new DatagramSocket();
      receiveSocket = new DatagramSocket(port);
    } catch (SocketException se) {
      se.printStackTrace();
      System.exit(1);
    }
    this.port = port;
    running = true;
    elevator = e;
  }
  public void send() {
    ElevatorData elevDat = elevator.getElevatorData();
    try {
      ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
      ObjectOutputStream ooStream = new ObjectOutputStream(new BufferedOutputStream(baoStream));
      ooStream.flush();
      ooStream.writeObject(elevDat);
      ooStream.flush();
      byte msg[] = baoStream.toByteArray();
      sendPacket = new DatagramPacket(msg, msg.length, schedulerAddress, 3000);
    } catch (IOException e1) {
      e1.printStackTrace();
    }
    try {
      sendSocket.send(sendPacket);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
    elevator.print("Sending to address: " + schedulerAddress);
    elevator.print("Sent packet to scheduler.\n Containing:\n	" + elevDat.getStatus() + "\n");
  }
  public void receive() {
    if (elevator.getElevatorData().isOperational()) {
      byte data[] = new byte[5000];
      receivePacket = new DatagramPacket(data, data.length);
      try {
        receiveSocket.receive(receivePacket);
        schedulerAddress = receivePacket.getAddress();
      } catch (IOException e) {
        elevator.print("IO Exception: likely:");
        elevator.print("Receive Socket Timed Out.\n" + e);
        e.printStackTrace();
        System.exit(1);
      }
      try {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
        ObjectInputStream is;
        is = new ObjectInputStream(new BufferedInputStream(byteStream));
        Object o = is.readObject();
        is.close();
        if (o == null) {
          closeSockets();
        }
        scheDat = (SchedulerData) o;
      } catch (IOException e) {
        e.printStackTrace();
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
      elevator.print("Received packet from address: " + schedulerAddress);
      elevator.processPacket(scheDat);
      elevator.wake();
    } else {
      closeSockets();
    }
  }
  public void wait(int ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
  public void freeSockets() {
    try {
      sendSocket.send(new DatagramPacket(null, 1, 1, InetAddress.getLocalHost(), port));
    } catch (UnknownHostException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  public void closeSockets() {
    running = false;
    receiveSocket.close();
    sendSocket.close();
  }
  public void run() {
    while (running) {
      receive();
      wait(200);
    }
  }
}