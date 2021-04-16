import java.io.*;
import java.awt.*;
import javax.sound.sampled.*;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.border.LineBorder;
import javax.swing.ImageIcon;
import javax.swing.border.TitledBorder;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.JFrame;

public class GUI {
  private int floorNum;
  private static final int DEFAULT_FLOOR_NUM = 22;
  private int elevNum;
  private static final int DEFAULT_ELEVATOR_NUM = 4;
  private static final int EXTRA_SPACE = 165;
  private JLabel[] floorTitles;
  private JLabel[][] floors;
  private JPanel[] displays;
  private JFrame frmElevators;
  private JLabel[][] elevInfos;
  private JPanel[] elevInfoPanels;
  private static final int DEFAULT_COLUMN_WIDTH = 70;
  private static final int DEFAULT_ROW_HEIGHT = 30;
  private static final int DEFAULT_FLOOR_ROW_HEIGHT = 50;
  private static final int EXTRA_GUI_WIDTH = 160;
  private static final double DEFAULT_ROW_WEIGHT = 1.0;
  public static final int OPEN = 0;
  public static final int CLOSED = 1;
  public static final int STUCK = 2;
  public static final int ODO = 3;
  public static final int MOVING = 5;
  public GUI() {
    String[] options = {
      "Use Defaults",
      "Use User Inputs"
    };
    int popUp = JOptionPane.showOptionDialog(null, "Would you like to input values?", "Confirmation", JOptionPane.INFORMATION_MESSAGE, 0, null, options, options[0]);
    switch (popUp) {
    case -1:
      System.exit(0);
    case 0:
      floorNum = DEFAULT_FLOOR_NUM;
      elevNum = DEFAULT_ELEVATOR_NUM;
      break;
    case 1:
      elevNum = Integer.parseInt(JOptionPane.showInputDialog("How many elevators?"));
      floorNum = Integer.parseInt(JOptionPane.showInputDialog("How many floors?"));
    }
    initialize();
  }
  public GUI(int floorNum, int elevNum) {
    this.floorNum = floorNum;
    this.elevNum = elevNum;
    initialize();
  }
  private void initialize() {
    int heightOfRows = 30 * floorNum;
    int widthOfGUI = 50 + (50 * elevNum);
    if (elevNum % 2 == 0) {
      widthOfGUI += (elevNum / 2) * 350;
    } else {
      widthOfGUI += ((elevNum / 2) + 1) * 350;
    }
    widthOfGUI += EXTRA_GUI_WIDTH;
    frmElevators = new JFrame();
    frmElevators.getContentPane().setBackground(UIManager.getColor("Button.background"));
    frmElevators.setTitle("Elevators");
    frmElevators.setIconImage(Toolkit.getDefaultToolkit().getImage("Assets\\favicon.png"));
    frmElevators.setBounds(100, 100, widthOfGUI, EXTRA_SPACE + heightOfRows);
    frmElevators.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    GridBagLayout gridBagLayout = new GridBagLayout();
    gridBagLayout.columnWidths = new int[] {
      widthOfGUI
    };
    gridBagLayout.rowHeights = new int[] {
      heightOfRows
    };
    gridBagLayout.columnWeights = new double[] {
      1.0
    };
    gridBagLayout.rowWeights = new double[] {
      0.0
    };
    frmElevators.getContentPane().setLayout(gridBagLayout);
    frmElevators.setResizable(false);
    JPanel displayPanel = new JPanel();
    displayPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
    GridBagConstraints gbc_displayPanel = new GridBagConstraints();
    gbc_displayPanel.insets = new Insets(0, 0, 5, 0);
    gbc_displayPanel.anchor = GridBagConstraints.WEST;
    gbc_displayPanel.fill = GridBagConstraints.VERTICAL;
    gbc_displayPanel.gridx = 0;
    gbc_displayPanel.gridy = 0;
    frmElevators.getContentPane().add(displayPanel, gbc_displayPanel);
    GridBagLayout gbl_displayPanel = new GridBagLayout();
    int columns = 1 + elevNum + 1;
    int[] columnWidthds = new int[columns];
    for (int i = 0; i < columns; i++) {
      if (i != 1 + elevNum) {
        columnWidthds[i] = DEFAULT_COLUMN_WIDTH;
      } else {
        if (elevNum % 2 == 0) {
          columnWidthds[i] = (elevNum / 2) * 350;
        } else {
          columnWidthds[i] = ((elevNum / 2) + 1) * 350;
        }
      }
    }
    gbl_displayPanel.columnWidths = columnWidthds;
    gbl_displayPanel.rowHeights = new int[] {
      heightOfRows
    };
    double[] gblColumnWeights = new double[columns];
    for (int i = 0; i < columns; i++) {
      gblColumnWeights[i] = 0.0;
    }
    gbl_displayPanel.columnWeights = gblColumnWeights;
    gbl_displayPanel.rowWeights = new double[] {
      1.0
    };
    displayPanel.setLayout(gbl_displayPanel);
    JPanel floorTitlePanel = new JPanel();
    floorTitlePanel.setBackground(UIManager.getColor("Button.background"));
    floorTitlePanel.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0)), "Floors", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
    GridBagConstraints gbc_floorTitlePanel = new GridBagConstraints();
    gbc_floorTitlePanel.fill = GridBagConstraints.VERTICAL;
    gbc_floorTitlePanel.insets = new Insets(0, 0, 0, 5);
    gbc_floorTitlePanel.gridx = 0;
    gbc_floorTitlePanel.gridy = 0;
    displayPanel.add(floorTitlePanel, gbc_floorTitlePanel);
    GridBagLayout gbl_floorTitlePanel = new GridBagLayout();
    gbl_floorTitlePanel.columnWidths = new int[] {
      DEFAULT_FLOOR_ROW_HEIGHT
    };
    int[] tempArr = new int[floorNum];
    for (int j = 0; j < floorNum; j++) {
      tempArr[j] = DEFAULT_ROW_HEIGHT;
    }
    gbl_floorTitlePanel.rowHeights = tempArr;
    gbl_floorTitlePanel.columnWeights = new double[] {
      0.0
    };
    double[] temp = new double[floorNum];
    for (int j = 0; j < floorNum; j++) {
      temp[j] = DEFAULT_ROW_WEIGHT;
    }
    gbl_floorTitlePanel.rowWeights = temp;
    floorTitlePanel.setLayout(gbl_floorTitlePanel);
    floorTitles = new JLabel[floorNum];
    for (int i = 1; i <= floorNum; i++) {
      floorTitles[i - 1] = new JLabel(Integer.toString(floorNum - i + 1));
      floorTitles[i - 1].setHorizontalAlignment(SwingConstants.CENTER);
      GridBagConstraints floorTitle = new GridBagConstraints();
      floorTitle.fill = GridBagConstraints.BOTH;
      floorTitle.insets = new Insets(0, 0, 5, 0);
      floorTitle.gridx = 0;
      floorTitle.gridy = i - 1;
      floorTitlePanel.add(floorTitles[i - 1], floorTitle);
    }
    displays = new JPanel[elevNum];
    floors = new JLabel[elevNum][floorNum];
    for (int i = 1; i <= elevNum; i++) {
      displays[i - 1] = new JPanel();
      displays[i - 1].setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0)), new String("Elevator " + Integer.toString(i - 1)), TitledBorder.LEADING, TitledBorder.TOP, null, null));
      GridBagConstraints gbc_elevatorDisplay = new GridBagConstraints();
      gbc_elevatorDisplay.fill = GridBagConstraints.BOTH;
      gbc_elevatorDisplay.insets = new Insets(0, 0, 0, 5);
      gbc_elevatorDisplay.gridx = i;
      gbc_elevatorDisplay.gridy = 0;
      displayPanel.add(displays[i - 1], gbc_elevatorDisplay);
      GridBagLayout gbl_elevatorDisplay = new GridBagLayout();
      gbl_elevatorDisplay.columnWidths = new int[] {
        DEFAULT_COLUMN_WIDTH
      };
      gbl_elevatorDisplay.rowHeights = tempArr;
      gbl_elevatorDisplay.columnWeights = new double[] {
        0.0
      };
      gbl_elevatorDisplay.rowWeights = temp;
      displays[i - 1].setLayout(gbl_elevatorDisplay);
      for (int j = 0; j < floorNum; j++) {
        floors[i - 1][floorNum - 1 - j] = new JLabel("");
        floors[i - 1][floorNum - 1 - j].setIcon(new ImageIcon("Assets\\Images\\Closed.png"));
        floors[i - 1][floorNum - 1 - j].setHorizontalAlignment(SwingConstants.CENTER);
        GridBagConstraints gbc_floor = new GridBagConstraints();
        gbc_floor.fill = GridBagConstraints.BOTH;
        gbc_floor.insets = new Insets(0, 0, 5, 0);
        gbc_floor.gridx = 0;
        gbc_floor.gridy = j;
        displays[i - 1].add(floors[i - 1][floorNum - 1 - j], gbc_floor);
      }
      floors[i - 1][0].setIcon(new ImageIcon("Assets\\Images\\Moving.jpg"));
    }
    JPanel panel = new JPanel();
    GridBagConstraints gbc_panel = new GridBagConstraints();
    gbc_panel.insets = new Insets(0, 0, 0, 5);
    gbc_panel.fill = GridBagConstraints.BOTH;
    gbc_panel.gridx = columns - 1;
    gbc_panel.gridy = 0;
    displayPanel.add(panel, gbc_panel);
    int x;
    if (elevNum % 2 == 0) {
      x = elevNum / 2;
    } else {
      x = (elevNum / 2) + 1;
    }
    panel.setLayout(new GridLayout(2, x, 0, 0));
    elevInfoPanels = new JPanel[elevNum];
    elevInfos = new JLabel[elevNum][4];
    for (int i = 0; i < elevNum; i++) {
      elevInfoPanels[i] = new JPanel();
      elevInfoPanels[i].setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0)), new String("Elevator " + Integer.toString(i) + " Info"), TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
      panel.add(elevInfoPanels[i]);
      elevInfoPanels[i].setLayout(new GridLayout(0, 1, 0, 0));
      elevInfos[i][0] = new JLabel("Current Floor: 1");
      elevInfos[i][0].setFont(new Font("Tahoma", Font.PLAIN, 17));
      elevInfoPanels[i].add(elevInfos[i][0]);
      elevInfos[i][1] = new JLabel("Direction: IDLE");
      elevInfos[i][1].setFont(new Font("Tahoma", Font.PLAIN, 17));
      elevInfoPanels[i].add(elevInfos[i][1]);
      elevInfos[i][2] = new JLabel("Requests: STANDING BY");
      elevInfos[i][2].setFont(new Font("Tahoma", Font.PLAIN, 17));
      elevInfoPanels[i].add(elevInfos[i][2]);
      elevInfos[i][3] = new JLabel("Doors: CLOSED");
      elevInfos[i][3].setFont(new Font("Tahoma", Font.PLAIN, 17));
      elevInfoPanels[i].add(elevInfos[i][3]);
    }
    File audioFile = new File("Assets\\m.wav");
    AudioInputStream audioStream = null;
    try {
      audioStream = AudioSystem.getAudioInputStream(audioFile);
    } catch (UnsupportedAudioFileException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    AudioFormat format = audioStream.getFormat();
    DataLine.Info info = new DataLine.Info(Clip.class, format);
    Clip audioClip = null;
    try {
      audioClip = (Clip) AudioSystem.getLine(info);
    } catch (LineUnavailableException e) {
      e.printStackTrace();
    }
    try {
      audioClip.open(audioStream);
    } catch (LineUnavailableException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    audioClip.start();
    audioClip.loop(2);
    frmElevators.setVisible(true);
    frmElevators.setLocation(150, 100);
  }
  public void setElevatorDoor(int elev, int floor, int status) {
    if (elev <= elevNum && floor <= floorNum) {
      if (status == OPEN) {
        floors[elev][floor - 1].setIcon(new ImageIcon("Assets\\Images\\Open.png"));
      } else if (status == STUCK) {
        floors[elev][floor - 1].setIcon(new ImageIcon("Assets\\Images\\Stuck.png"));
      } else if (status == CLOSED) {
        floors[elev][floor - 1].setIcon(new ImageIcon("Assets\\Images\\Closed.png"));
      } else if (status == MOVING) {
        floors[elev][floor - 1].setIcon(new ImageIcon("Assets\\Images\\Moving.jpg"));
        if (floor > 1)
          floors[elev][floor - 2].setIcon(new ImageIcon("Assets\\Images\\Closed.png"));
        if (floor < floorNum)
          floors[elev][floor].setIcon(new ImageIcon("Assets\\Images\\Closed.png"));
      }
    }
  }
  public void setCurrentFloorInfo(int elev, int i) {
    elevInfos[elev][0].setText("Current Floor: " + i);
  }
  public void setDirectionInfo(int elev, String s) {
    elevInfos[elev][1].setText("Direction: " + s);
  }
  public void setRequestsInfo(int elev, ArrayList < Integer > arr) {
    String temp = "Requests: ";
    if (arr.isEmpty()) {
      temp += "STANDING BY";
    } else {
      temp += arr.toString();
    }
    elevInfos[elev][2].setText(temp);
  }
  public void setDoorsInfo(int elev, int status) {
    if (status == OPEN) {
      elevInfos[elev][3].setText("Doors: OPEN");
    } else if (status == STUCK) {
      elevInfos[elev][3].setText("Doors: STUCK");
    } else if (status == CLOSED) {
      elevInfos[elev][3].setText("Doors: CLOSED");
    } else if (status == ODO) {
      elevInfos[elev][3].setText("Doors: SHUTDOWN");
    }
  }
  public void setShutdown(int elev) {
    for (int i = 0; i < floorNum; i++) {
      floors[elev][i].setIcon(new ImageIcon("Assets\\Images\\Shutdown.png"));
    }
    setDirectionInfo(elev, "SHUTDOWN");
    setDoorsInfo(elev, 3);
  }
  public int getNumElevators() {
    return elevNum;
  }
  public int getNumFloors() {
    return floorNum;
  }
}