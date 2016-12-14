package Frames;

import java.awt.*;
import javax.swing.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.*;
import java.awt.geom.Arc2D;
import java.io.BufferedWriter;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.concurrent.*;
// JFree
import GroundStationUtilities.GlobalDataManager;
import GroundStationUtilities.Logger;
import com.sun.prism.paint.*;


/**
 * Created by Jakub on 08.10.2016.
 *
 * Displays basic telemetry of two-rotor pendulum.
 */
public class PendulumBasicInterfaceWindow extends Observable implements ActionListener, GroundStationWindowController{

    private GlobalDataManager               dataBank;
    private ScheduledExecutorService        scheduledExecutorService;

    private JFrame                          frame;
    private final int                       layoutRows      =   3;
    private final int                       layoutColumns   =   3;
    private JPanel[][]                      layoutControl;
    private JLabel                          angleOffsetLabel;
    private JTextArea                       logPathDisplayField;
    private JTextField                      logNameField;
    private JLabel                          logEnabledLabel;
    private GroundStationUtilities.Logger   logger;
    private JLabel                          deviceArmedLabel;
    private JComboBox                       algorithmChoiceBox;

    private final DynamicTimeChart3Series   accChart;
    private final DynamicTimeChart3Series   gyroChart;
    private final DynamicTimeChart3Series   magnetoChart;

    private final DynamicTimeChart          angularVelocityChart;
    private final DynamicTimeChart          angleErrorChart;
    private final DynamicTimeChart          angleChart;

    // PHYSICAL DEVICE STATE FIELDS
    private boolean         deviceArmed;
    private int             startAngle;
    private String          algorithmSet;
    private boolean         deviceLockedOnAngle;
    // END OF PHYSICAL DEVICE STATE FIELDS

    // APPLICATION UTILITY FIELDS
    private String          logDirPath;
    private String          logFileName;
    private boolean         logEnabled;
    // END OF APPLICATION UTILITY FIELDS

    public PendulumBasicInterfaceWindow(GlobalDataManager dataBank) {
        this.dataBank = dataBank;
        logger = new Logger();

        // run an object responsible for handling incoming data every 5[ms]
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(new IncomingMessagesHandler(), 10, 5, TimeUnit.MILLISECONDS);

        // Initialize state variables
        // physical
        deviceArmed         = false;
        startAngle          = 0;
        algorithmSet        = "PID";
        deviceLockedOnAngle = false;
        // application
        logDirPath      = "";
        logFileName     = "bagno";
        logger          = new Logger();
        logEnabled      = false;

        // Initialize GUI
        frame = new JFrame("Pendulum Interface");
        frame.setSize(new Dimension(1200, 1000));
        frame.setResizable(false);
        frame.setUndecorated(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        // prepare 2D array of JPanels to allow precise components placement
        frame.setLayout(new GridLayout(3, 3));

        layoutControl = new JPanel[layoutRows][layoutColumns];
        for(int r = 0; r < layoutRows; r++) {
            for(int c = 0; c < layoutColumns; c++) {
                layoutControl[r][c] = new JPanel();
                frame.add(layoutControl[r][c]);
            }
        }

        int width = 410;
        int height = 300;

        gyroChart = new DynamicTimeChart3Series("Gyro", "x", "y", "z", "Angular acceleration", width, height);
        accChart = new DynamicTimeChart3Series("Acc", "x", "y", "z", "Acceleration", width, height);
        magnetoChart = new DynamicTimeChart3Series("Magnet", "x", "y", "z", "Field", width, height);

        angularVelocityChart = new DynamicTimeChart("Angular velocity", "deg/s", width, height);
        angleErrorChart = new DynamicTimeChart("Angle Error", "deg", width, height);
        angleChart = new DynamicTimeChart("Angle", "deg", width, height);

        // prepare all controls
        setupControlPanel(0, 0);
        setupLoggingPanel(2, 0);
        setupGeneralInfoPanel(1, 0);
        setupAccPanel(0, 2);
        setupGyroPanel(1, 2);
        setupMagnetoPanel(2, 2);
        setupAngleErrorPanel(1, 1);
        setupAnglePanel(0, 1);
        setupAngularSpeedPanel(2, 1);

        frame.setVisible(true);
    }

    // =============================================================
    //                  GUI panels setup methods                   =
    // =============================================================

    private void setupGeneralInfoPanel(int row, int column) {
        // redundant
    }

    // ===========================================

    private void setupControlPanel(int row, int column) {
        JPanel thisPanel = layoutControl[row][column];
        thisPanel.setLayout(new GridLayout(4, 2));

        JPanel armPanel = new JPanel();
        JButton armButton = new JButton("ARM");
        armButton.setActionCommand("arm_button_down");
        armButton.addActionListener(this);
        armButton.setPreferredSize(new Dimension(140, 60));
        armButton.setHorizontalAlignment(SwingConstants.CENTER);
        armButton.setVerticalAlignment(SwingConstants.CENTER);
        armPanel.add(armButton);

        JPanel algorithmsChoicePanel = new JPanel();
        String[] algorithmsList = {"PID", "Fuzzy"};
        JComboBox algorithmChoiceBox = new JComboBox(algorithmsList);
        this.algorithmChoiceBox = algorithmChoiceBox;
        algorithmChoiceBox.setSelectedIndex(0);
        algorithmChoiceBox.setActionCommand("algorithm_selected");
        algorithmChoiceBox.addActionListener(this);
        algorithmChoiceBox.setPreferredSize(new Dimension(140, 60));
        algorithmsChoicePanel.add(algorithmChoiceBox);

        JPanel runPanel = new JPanel();
        JButton runButton = new JButton("RUN");
        runButton.setActionCommand("run_button_down");
        runButton.addActionListener(this);
        runButton.setEnabled(false);
        runButton.setPreferredSize(new Dimension(140, 60));
        runPanel.add(runButton);

        JPanel stopPanel = new JPanel();
        JButton stopButton = new JButton("STOP");
        stopButton.setActionCommand("stop_button_down");
        stopButton.addActionListener(this);
        stopButton.setEnabled(false);
        stopButton.setPreferredSize(new Dimension(140, 60));
        stopPanel.add(stopButton);

        JPanel lockAnglePanel = new JPanel();
        JButton lockAngleButton = new JButton("LOCK ON ANGLE");
        lockAngleButton.setActionCommand("lock_angle_button_down");
        lockAngleButton.addActionListener(this);
        lockAngleButton.setEnabled(false);
        lockAngleButton.setPreferredSize(new Dimension(140, 60));
        lockAnglePanel.add(lockAngleButton);

        JPanel armStatusPanel = new JPanel();
        JLabel armStatusLabel = new JLabel("Device DISARMED");
        this.deviceArmedLabel = armStatusLabel;
        armStatusLabel.setForeground(Color.GREEN);
        armStatusLabel.setFont(new Font(armStatusLabel.getName(), Font.BOLD, 22));
        armStatusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        armStatusLabel.setVerticalAlignment(SwingConstants.BOTTOM);
        armStatusPanel.add(armStatusLabel);

        JPanel startAngleLabelPanel = new JPanel();
        JLabel startAngleLabel = new JLabel("Start angle:  " + "0" + " deg");
        this.angleOffsetLabel = startAngleLabel;
        startAngleLabelPanel.add(startAngleLabel);

        JPanel startAnglePanel = new JPanel();
        JSlider startAngleSlider = new JSlider(-45, 45);
        startAngleSlider.setMajorTickSpacing(15);
        startAngleSlider.setMinorTickSpacing(5);
        startAngleSlider.setPaintLabels(true);
        startAngleSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                angleOffsetLabel.setText("Start angle:  " + String.valueOf(startAngleSlider.getValue() + " deg"));
                startAngle = startAngleSlider.getValue();
            }
        });
        startAnglePanel.add(startAngleSlider);

        thisPanel.add(armPanel);
        thisPanel.add(armStatusPanel);
        thisPanel.add(algorithmsChoicePanel);
        thisPanel.add(runPanel);
        thisPanel.add(stopPanel);
        thisPanel.add(lockAnglePanel);
        thisPanel.add(startAnglePanel);
        thisPanel.add(startAngleLabelPanel);
    }

    // ===========================================

    private void setupLoggingPanel(int row, int column) {
        JPanel thisPanel = layoutControl[row][column];
        thisPanel.setLayout(new GridLayout(4, 2));

        JPanel loggingEnabledPanel = new JPanel();
        JLabel loggingEnabledLabel = new JLabel("log DISABLED");
        this.logEnabledLabel = loggingEnabledLabel;
        loggingEnabledLabel.setFont(new Font(loggingEnabledLabel.getName(), Font.BOLD, 22));
        loggingEnabledLabel.setHorizontalAlignment(SwingConstants.CENTER);
        loggingEnabledLabel.setHorizontalAlignment(SwingConstants.CENTER);
        loggingEnabledLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        loggingEnabledLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
        loggingEnabledPanel.add(loggingEnabledLabel);

        JPanel startLogPanel = new JPanel();
        JButton startLogButton = new JButton("Start log");
        startLogButton.setActionCommand("start_log_button_down");
        startLogButton.addActionListener(this);
        startLogButton.setPreferredSize(new Dimension(140, 60));
        startLogPanel.add(startLogButton);

        JPanel stopLogPanel = new JPanel();
        JButton stopLogButton = new JButton("Stop log");
        stopLogButton.setActionCommand("stop_log_button_down");
        stopLogButton.addActionListener(this);
        stopLogButton.setPreferredSize(new Dimension(140, 60));
        stopLogPanel.add(stopLogButton);

        JPanel saveLogPanel = new JPanel();
        JButton saveLogButton = new JButton("Save log");
        saveLogButton.setActionCommand("save_log_button_down");
        saveLogButton.addActionListener(this);
        saveLogButton.setPreferredSize(new Dimension(140, 60));
        saveLogPanel.add(saveLogButton);

        JPanel directoryPanel = new JPanel();
        JButton directoryButton = new JButton("Set log dir");
        directoryButton.setActionCommand("log_directory_button_down");
        directoryButton.addActionListener(this);
        directoryButton.setPreferredSize(new Dimension(140, 60));
        directoryPanel.add(directoryButton);

        JPanel logPathDisplayPanel = new JPanel();
        JTextArea logPathField = new JTextArea();
        logPathField.setPreferredSize(new Dimension(140, 60));
        logPathField.setEnabled(true);
        logPathField.setEditable(false);
        logPathField.setLineWrap(true);
        logPathDisplayField = logPathField;
        logPathDisplayPanel.add(logPathField);

        JPanel logFileNamePanel = new JPanel();
        JTextField logFileNameField = new JTextField("set log file name");
        this.logNameField = logFileNameField;
        logFileNameField.setActionCommand("log_file_name_changed");
        logFileNameField.addActionListener(this);
        logFileNameField.setPreferredSize(new Dimension(140, 60));
        logFileNameField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                logFileNameField.setText("");
            }
        });
        logFileNamePanel.add(logFileNameField);

        JPanel logFileNameReminderPanel = new JPanel();
        JLabel logFileNameLabel = new JLabel("Log file name (no format):");
        logFileNameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        logFileNameReminderPanel.add(logFileNameLabel);

        thisPanel.add(loggingEnabledPanel);
        thisPanel.add(startLogPanel);
        thisPanel.add(saveLogPanel);
        thisPanel.add(stopLogPanel);
        thisPanel.add(directoryPanel);
        thisPanel.add(logPathDisplayPanel);
        thisPanel.add(logFileNameLabel);
        thisPanel.add(logFileNamePanel);

    }

    // ===========================================

    private void setupAnglePanel(int row, int column) {
        layoutControl[row][column].add(angleChart);
    }

    // ===========================================

    private void setupAngularSpeedPanel(int row, int column) {
        layoutControl[row][column].add(angularVelocityChart);
    }

    // ===========================================

    private void setupAngleErrorPanel(int row, int column) {
        layoutControl[row][column].add(angleErrorChart);
    }

    // ===========================================

    private void setupGyroPanel(int row, int column) {
        layoutControl[row][column].add(gyroChart);
    }

    // ===========================================

    private void setupAccPanel(int row, int column) {
        layoutControl[row][column].add(accChart);
    }

    // ===========================================

    private void setupMagnetoPanel(int row, int column) {
        layoutControl[row][column].add(magnetoChart);
    }

    // =============================================================
    //                  END OF GUI panels setup methods            =
    // =============================================================

    private void transmitMessageToPendulum(List<String> message) {
        BufferedWriter tx = dataBank.getTx();

        for(String singleLine : message) {
            try {
                tx.write(singleLine);
                tx.newLine();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Critical error while writing to the main communication socket. " +
                                "Reset the application.", "Error", JOptionPane.INFORMATION_MESSAGE);
                e.printStackTrace();
            }
        }

        try {
            tx.write("$");
            tx.newLine();
            tx.flush();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Critical error while writing to the main communication socket. " +
                    "Reset the application.", "Ełłoł", JOptionPane.INFORMATION_MESSAGE);
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    }

    // ===========================================

    @Override
    public void setDataBank(GlobalDataManager dataBank) {
        this.dataBank = dataBank;
    }

    // ===========================================

    @Override
    public void actionPerformed(ActionEvent e) {
        switch(e.getActionCommand()) {
            case("arm_button_down"):
                transmitMessageToPendulum(Arrays.asList("arm_motors"));
                deviceArmed = true;
                deviceArmedLabel.setText("Device ARMED");
                deviceArmedLabel.setForeground(Color.RED);
                break;
            case("algorithm_selected"):
                algorithmSet = (String)algorithmChoiceBox.getSelectedItem();
                break;
            case("run_button_down"):
                if(deviceArmed && deviceLockedOnAngle) {
                    transmitMessageToPendulum(Arrays.asList("launch_experiment", algorithmSet,
                            String.valueOf(startAngle)));
                }
                else if (deviceArmed && !deviceLockedOnAngle){
                    JOptionPane.showMessageDialog(null, "Lock the arm on any angle first", "RTFM!",
                            JOptionPane.INFORMATION_MESSAGE);
                }
                else {
                    JOptionPane.showMessageDialog(null, "Arm the device and lock it on any angle first", "RTFM!",
                            JOptionPane.INFORMATION_MESSAGE);
                }
                break;
            case("stop_button_down"):
                transmitMessageToPendulum(Arrays.asList("disarm_motors"));
                break;
            case("lock_angle_button_down"):
                transmitMessageToPendulum(Arrays.asList("lock_on_angle", String.valueOf(startAngle)));
                break;
            case("start_log_button_down"):
                if(logDirPath.equals("")){
                    JOptionPane.showMessageDialog(null, "Set log directory first", "RTFM!",
                            JOptionPane.INFORMATION_MESSAGE);
                    break;
                }

                if(logFileName.equals("")) {
                    JOptionPane.showMessageDialog(null, "Set log file name first", "Don't mess with me!",
                            JOptionPane.INFORMATION_MESSAGE);
                    break;
                }
                logEnabled = true;
                transmitMessageToPendulum(Arrays.asList("start_telemetry"));
                logEnabledLabel.setText("log ENABLED");
                break;
            case("log_directory_button_down"):
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                int chooserStatus = jFileChooser.showOpenDialog(frame);
                if(chooserStatus == JFileChooser.APPROVE_OPTION) {
                    File tempFile = jFileChooser.getSelectedFile();
                    logDirPath = tempFile.getAbsolutePath();
                    logPathDisplayField.setText(logDirPath);
                }
                break;
            case("save_log_button_down"):
                logger.saveAllDataToFile(logDirPath, logFileName, ".txt");
                logger.resetLogger();
                break;
            case("stop_log_button_down"):
                transmitMessageToPendulum(Arrays.asList("stop_telemetry"));
                logEnabled = false;
                logEnabledLabel.setText("log DISABLED");
                break;
            case("log_file_name_changed"):
                logFileName = logNameField.getText();
                break;
            default:
                JOptionPane.showMessageDialog(null, "Unrecognized action performed", "Serious bullshit",
                        JOptionPane.INFORMATION_MESSAGE);
                break;
        }
    }

    // =============================================================
    //                     UTILITY CLASSES                         =
    // =============================================================

    private class IncomingMessagesHandler implements Runnable{

        public IncomingMessagesHandler() {
            // unnecessary
        }

        @Override
        public void run() {
            Scanner rx = dataBank.getRx();

            // if anything has been received process it
            LinkedList<String> buf = new LinkedList<>();

            while(rx.hasNextLine()) {
                String nextLine = rx.nextLine();

                // if the line is the last in the message - "$" sign is used between Pendulum and Bixie as EOM
                if(nextLine.equals("$")) {
                    handleData(buf);
                    buf.clear();
                }
            }
        }

        private void handleData(List<String> data) {
            // extract identifier
            String id = data.get(0);

            switch(id) {
                case "telemetry_package":
                    // extract data from the message
                    String  nanoTime        = data.get(1);
                    String  angleFiltered   = data.get(2);
                    String  angularVelocity = data.get(3);
                    String  angleError      = data.get(4);
                    String  accX            = data.get(5);
                    String  accY            = data.get(6);
                    String  accZ            = data.get(7);
                    String  gyroX           = data.get(8);
                    String  gyroY           = data.get(9);
                    String  gyroZ           = data.get(10);
                    String  magX            = data.get(11);
                    String  magY            = data.get(12);
                    String  magZ            = data.get(13);
                    String  leftMotorRPS    = data.get(14);
                    String  rightMotorRPS   = data.get(15);
                    String  leftMotorCur    = data.get(16);
                    String  rightMotorCur   = data.get(17);

                    // log all data
                    LinkedList<String> dataForLog = new LinkedList<>(data.subList(1, 17));   //< only data without ID
                    logger.appendNewRecord(dataForLog);

                    // update GUI
                    float   filteredAngleAsFloat    = Float.parseFloat(angleFiltered);
                    float   angularVelocityAsFloat  = Float.parseFloat(angularVelocity);
                    float   angleErrorAsFloat       = Float.parseFloat(angleError);

                    float[] accReadings             = new float[3];
                    accReadings[0]                  = Float.parseFloat(accX);
                    accReadings[1]                  = Float.parseFloat(accY);
                    accReadings[2]                  = Float.parseFloat(accZ);

                    float[] gyroReadings            = new float[3];
                    gyroReadings[0]                 = Float.parseFloat(gyroX);
                    gyroReadings[1]                 = Float.parseFloat(gyroY);
                    gyroReadings[2]                 = Float.parseFloat(gyroZ);

                    float[] magReadings             = new float[3];
                    magReadings[0]                  = Float.parseFloat(magX);
                    magReadings[1]                  = Float.parseFloat(magY);
                    magReadings[2]                  = Float.parseFloat(magZ);

                    angleChart.update(filteredAngleAsFloat);
                    angularVelocityChart.update(angularVelocityAsFloat);
                    angleErrorChart.update(angleErrorAsFloat);

                    accChart.update(accReadings[0],     accReadings[1],     accReadings[2]);
                    gyroChart.update(gyroReadings[0],   gyroReadings[1],    gyroReadings[2]);
                    magnetoChart.update(magReadings[0], magReadings[1],     magReadings[2]);

                    break;

                default:
                    break;
            }
        }
    }
}
