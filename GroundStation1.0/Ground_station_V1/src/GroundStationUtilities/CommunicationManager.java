package GroundStationUtilities;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by Jakub on 11.10.2016.
 *
 * Utility class for simple socket communication. Automatically notifies observers when full message is received.
 */
public class CommunicationManager extends Observable implements Runnable {

    private GlobalDataManager       dataBank;
    private Scanner                 rx;
    private BufferedWriter          tx;
    private List<String>            rxBuffer;

    public CommunicationManager(GlobalDataManager dataBank) {
        this.dataBank = dataBank;

        rx = dataBank.getRx();
        tx = dataBank.getTx();

        rxBuffer = new LinkedList<>();

        Thread self = new Thread(this);
        self.start();
    }

    public void transmitCommand(List<String> command) {
        for(String dummy : command) {
            try {
                tx.write(dummy);
                tx.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            tx.write("$");
            tx.newLine();
            tx.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while(rx.hasNext()) {
            String newLine = rx.nextLine();
            if(newLine.equals("$")) {
                setChanged();
                notifyObservers(new LinkedList<>(rxBuffer));
                rxBuffer.clear();
                continue;
            }
            else
                rxBuffer.add(newLine);
        }
    }
}
