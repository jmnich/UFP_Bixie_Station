package Frames;

import GroundStationUtilities.GlobalDataManager;

import java.awt.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by Jakub on 08.10.2016.
 *
 * Just another error window. DO NOT SHOW IT TO OFFICIALS!
 */
public class ErrorWindow implements GroundStationWindowController {
    private JFrame frame;

    public ErrorWindow() {
        frame = new JFrame("WHOPS!");
        frame.setLayout(new GridLayout(1, 1));
        frame.setSize(400, 400);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);

        JLabel pic;

        try {
            BufferedImage err = ImageIO.read(new File("resources/error_pic.png"));
            pic = new JLabel(new ImageIcon(err));
        } catch (IOException e) {
            e.printStackTrace();
            pic = new JLabel("Even error message does not work correctly.");
        }

        frame.add(pic);

        frame.setVisible(true);
    }

    @Override
    public void close() {

    }

    @Override
    public void setDataBank(GlobalDataManager dataBank) {

    }
}
