package Frames;

import GroundStationUtilities.GlobalDataManager;

import javax.swing.*;
import java.awt.event.WindowEvent;
// jfree


/**
 * Created by Jakub on 08.10.2016.
 */
public class EnixBasicInterfaceWindow implements GroundStationWindowController {
    private JFrame frame;
    private GlobalDataManager dataBank;

    public EnixBasicInterfaceWindow() {
        frame = new JFrame("Enix telemetry");
        frame.setSize(1200, 1000);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        frame.setVisible(true);
    }

    @Override
    public void close() {
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    }

    @Override
    public void setDataBank(GlobalDataManager dataBank) {
        this.dataBank = dataBank;
    }
}
