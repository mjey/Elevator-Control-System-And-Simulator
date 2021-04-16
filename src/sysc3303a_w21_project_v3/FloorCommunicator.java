import java.io.*;
import java.net.*;

public class FloorCommunicator extends Thread {
  DatagramPacket sendPacket, receivePacket;
  DatagramSocket sendReceiveSocket;
  private SchedulerFloorData sfdata;
  private FloorSubsystem system;
  private InetAddress schedulerAddress;
  public FloorCommunicator(FloorSubsystem system) {
    try {
      sendReceiveSocket = new DatagramSocket();
    } catch (SocketException se) {
      se.printStackTrace();
      System.exit(1);
    }
    try {
      schedulerAddress = InetAddress.getLocalHost();
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }
    this.system = system;
  }
  public void send(FloorData floorDat) {
    try {
      ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
      ObjectOutputStream ooStream = new ObjectOutputStream(new BufferedOutputStream(baoStream));
      ooStream.flush();
      ooStream.writeObject(floorDat);
      ooStream.flush();
      byte msg[] = baoStream.toByteArray();
      sendPacket = new DatagramPacket(msg, msg.length, schedulerAddress, 3000);
    } catch (IOException e1) {
      e1.printStackTrace();
    }
    try {
      sendReceiveSocket.send(sendPacket);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
    system.print("Sending to address: " + schedulerAddress);
    system.print("Sent packet to scheduler.\n Containing:\n	" + floorDat.getStatus() + "\n");
  }
  public void receive() {
    byte data[] = new byte[5000];
    receivePacket = new DatagramPacket(data, data.length);
    try {
      sendReceiveSocket.receive(receivePacket);
      schedulerAddress = receivePacket.getAddress();
    } catch (IOException e) {
      system.print("IO Exception: likely:");
      system.print("Receive Socket Timed Out.\n" + e);
      e.printStackTrace();
      System.exit(1);
    }
    try {
      ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
      ObjectInputStream is;
      is = new ObjectInputStream(new BufferedInputStream(byteStream));
      Object o = is.readObject();
      is.close();
      sfdata = (SchedulerFloorData) o;
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    system.print("Received packet from address: " + schedulerAddress);
    system.processPacket(sfdata);
  }
  public void wait(int ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
  public void closeSockets() {
    sendReceiveSocket.close();
  }
  public void run() {
    while (true) {
      receive();
      wait(100);
    }
  }
}