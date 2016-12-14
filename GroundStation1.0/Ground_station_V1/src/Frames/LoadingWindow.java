package Frames;

import GroundStationUtilities.GlobalDataManager;

import java.awt.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.Color;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


/**
 * Created by Jakub on 08.10.2016.
 */
public class LoadingWindow implements GroundStationWindowController{

    JFrame frame;

    public LoadingWindow() {
        frame = new JFrame("Wait...");
        frame.setLayout(new GridLayout(2, 1));
        frame.setLocationRelativeTo(null);
        frame.setSize(new Dimension(250, 400));
        frame.setResizable(false);
        frame.setUndecorated(true);
        frame.setBackground(Color.BLACK);

        JLabel note = new JLabel("Doing things for you");
        note.setHorizontalAlignment(JLabel.CENTER);
        note.setVerticalAlignment(JLabel.CENTER);
        note.setFont(new Font(note.getName(), Font.PLAIN, 20));
        note.setForeground(Color.red);
        note.setBackground(Color.black);
        note.setOpaque(true);
        JLabel ufo;

        try {
            BufferedImage ufo_image = ImageIO.read(new File("resources/ufo_animation.gif"));
            Icon ufoico = new ImageIcon(ufo_image);
            ufo = new JLabel(ufoico);
        } catch (IOException e) {
            e.printStackTrace();
            ufo = new JLabel("Couldn't find any silly picture to entertain you");
            ufo.setForeground(Color.WHITE);
        }
        ufo.setBackground(Color.BLACK);
        ufo.setOpaque(true);

        frame.add(note);
        frame.add(ufo);

        frame.setVisible(true);
    }

    @Override
    public void close() {
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    }

    @Override
    public void setDataBank(GlobalDataManager dataBank) {
        // not required
    }
}
