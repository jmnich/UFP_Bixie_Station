package Frames;

import GroundStationUtilities.GlobalDataManager;

import java.awt.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
import java.util.List;
import java.util.Observable;

/**
 * Created by Jakub on 08.10.2016.
 *
 * Allows to establish a connection with a particular drone server.
 */
public class LoginWindow extends Observable implements ActionListener, GroundStationWindowController {

    private JFrame              frame;
    private JLabel              headerLabel;
    private JPanel              connectionInputPanel;
    private JButton             connectionButton;
    private GlobalDataManager   dataBank;
    private JComboBox           modelSelectionBox;
    private JTextField          ipField;
    private JTextField          portField;

    public LoginWindow() {
        frame = new JFrame("Drone login");
        frame.setSize(350, 500);
        frame.setLayout(new GridLayout(3, 1));
        frame.setLocationRelativeTo(null);  //< center on screen
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        setupHeaderLabel();             //< prepare drone picture for a header
        setupConnectionPanel();         //< prepare IP input text field
        setupConnectionButton();        //< prepare connection button control

        frame.add(headerLabel);
        frame.add(connectionInputPanel);
        frame.add(connectionButton);

        frame.setVisible(true);

        // TODO notify application control when connection button is pressed

    }

    // ==========================================================

    private void setupConnectionButton() {

        try {
            BufferedImage conImage = ImageIO.read(new File("resources/connection.png"));
            connectionButton = new JButton(new ImageIcon(conImage));
        } catch (IOException e) {
            e.printStackTrace();
            connectionButton = new JButton("Connect  Err: couldn't load image");
        }

        connectionButton.setActionCommand("connection_button_down");
        connectionButton.addActionListener(this);
    }

    // ==========================================================

    private void setupHeaderLabel() {
        // load an image to a label
        BufferedImage drone_pic;
        try {
            drone_pic = ImageIO.read(new File("resources/enix_pic.png"));
            headerLabel = new JLabel(new ImageIcon(drone_pic));
        } catch (IOException e) {
            e.printStackTrace();
            headerLabel = new JLabel("error loading image: resources/enix_pic.png");
        }

    }

    // ==========================================================

    private void setupConnectionPanel() {
        connectionInputPanel = new JPanel();

        JLabel infoLabel = new JLabel("Drone server IP: ");
        infoLabel.setPreferredSize(new Dimension(110, 25));
        JTextField ipInput = new JTextField("192.168.1.");
        ipInput.setPreferredSize(new Dimension(200, 25));

        JLabel info2Label = new JLabel("Drone server port: ");
        info2Label.setPreferredSize(new Dimension(110, 25));
        JTextField portInput = new JTextField("6667");
        portInput.setPreferredSize(new Dimension(200, 25));

        // local info labels
        JLabel thisPcNetIPLabel;
        JLabel thisPcLocalIPLabel;
        List<String> localAddresses = new LinkedList<>();
        List<String> netAddresses = new LinkedList<>();

        // get PC's IP addresses and display them
        try {
            Enumeration interfaces = NetworkInterface.getNetworkInterfaces();

            while(interfaces.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) interfaces.nextElement();
                //System.out.println(ni.getDisplayName());
                Enumeration inetAdd = ni.getInetAddresses();

                while (inetAdd.hasMoreElements()) {
                    // sort local and net addresses, reject everything else
                    InetAddress i = (InetAddress) inetAdd.nextElement();
                    //System.out.println(i.getHostAddress());
                    String address = i.getHostAddress();
                    if(address.contains("192.168.")) {
                        localAddresses.add(address);
                    }
                    else if(address.charAt(3) == '.' && !address.startsWith("127.")) {
                        netAddresses.add(address);
                    }

                }
            }

            String localLabelContent = "";
            String netLabelContent = "";

            // prepare input strings for labels
            if(localAddresses.size() > 0) {
                localLabelContent = "Local addresses:   ";
                for(String add : localAddresses)
                    localLabelContent += add + "; ";
            }
            else
                localLabelContent = "No local addresses";

            if(netAddresses.size() > 0) {
                netLabelContent = "Net addresses:  ";
                for(String add : netAddresses)
                    netLabelContent += add + "; ";
            }
            else
                netLabelContent = "No valid non-local addresses";

            thisPcLocalIPLabel = new JLabel(localLabelContent);
            thisPcNetIPLabel = new JLabel(netLabelContent);

        } catch (SocketException e) {
            e.printStackTrace();
            thisPcLocalIPLabel = new JLabel("Network interface error");
            thisPcNetIPLabel = new JLabel("Network interface error");
        }

        thisPcLocalIPLabel.setPreferredSize(new Dimension(300, 15));
        thisPcLocalIPLabel.setHorizontalAlignment(JLabel.LEFT);
        thisPcNetIPLabel.setPreferredSize(new Dimension(300, 15));
        thisPcNetIPLabel.setHorizontalAlignment(JLabel.LEFT);

        // prepare combo box for choosing a drone model

        String[] models = {"Enix", "Lyra", "Pendulum"};
        JComboBox modelSelection = new JComboBox(models);
        modelSelection.setSelectedIndex(0);

        // a label for combo box
        JLabel modelLabel = new JLabel("Choose model   ");
        modelLabel.setPreferredSize(new Dimension(100, 15));
        JLabel lineLabel = new JLabel("_________________________________________");
        lineLabel.setPreferredSize(new Dimension(300, 15));

        // add action listener for switching drone pictures
        modelSelection.addActionListener(new ComboBoxListener());


        connectionInputPanel.add(infoLabel);
        connectionInputPanel.add(ipInput);
        connectionInputPanel.add(info2Label);
        connectionInputPanel.add(portInput);
        connectionInputPanel.add(thisPcLocalIPLabel);
        connectionInputPanel.add(thisPcNetIPLabel);
        connectionInputPanel.add(lineLabel);
        connectionInputPanel.add(modelLabel);
        connectionInputPanel.add(modelSelection);

        // save references to input controls for future access
        modelSelectionBox = modelSelection;
        ipField = ipInput;
        portField = portInput;
    }

    // ==========================================================

    public String getModel() {
        return (String)modelSelectionBox.getSelectedItem();
    }

    // ==========================================================

    public String getIP() {
        return ipField.getText();
    }

    // ==========================================================

    public String getPort() {
        return portField.getText();
    }

    // ==========================================================

    @Override
    public void close() {
        frame.setVisible(false);
    }

    // ==========================================================

    @Override
    public void setDataBank(GlobalDataManager dataBank) {
        this.dataBank = dataBank;
    }

    // ==========================================================

    @Override
    public void actionPerformed(ActionEvent e) {
        switch(e.getActionCommand()) {
            case("connection_button_down"):
                setChanged();
                notifyObservers("connection_button_down");
                connectionButton.setEnabled(false);
                break;
        }
    }

    // ==========================================================

    private class ComboBoxListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            JComboBox box = (JComboBox)e.getSource();
            String droneModel = (String)box.getSelectedItem();

            switch(droneModel) {
                case("Enix"):
                    try {
                        BufferedImage img = ImageIO.read(new File("resources/enix_pic.png"));
                        headerLabel.setIcon(new ImageIcon(img));
                        headerLabel.setText(null);
                        headerLabel.revalidate();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        headerLabel.setIcon(null);
                        headerLabel.setText("error loading image");
                        headerLabel.revalidate();
                    }
                    break;
                case("Lyra"):
                    try {
                        BufferedImage img = ImageIO.read(new File("resources/lyra_pic.png"));
                        headerLabel.setIcon(new ImageIcon(img));
                        headerLabel.setText(null);
                        headerLabel.revalidate();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        headerLabel.setIcon(null);
                        headerLabel.setText("error loading image");
                        headerLabel.revalidate();
                    }
                    break;
                case("Pendulum"):
                    try {
                        BufferedImage img = ImageIO.read(new File("resources/pendulum_pic.png"));
                        headerLabel.setIcon(new ImageIcon(img));
                        headerLabel.setText(null);
                        headerLabel.revalidate();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        headerLabel.setIcon(null);
                        headerLabel.setText("error loading image");
                        headerLabel.revalidate();
                    }
                    break;
                default:
                    headerLabel.setIcon(null);
                    headerLabel.setText("no image available");
                    headerLabel.revalidate();
            }
        }
    }
}
