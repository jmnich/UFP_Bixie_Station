package GroundStationUtilities;

import java.io.BufferedWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by Jakub on 08.10.2016.
 *
 * Controls access to global data.
 */
public class GlobalDataManager {
    private String droneModel;              //< the model of a drone chosen for communication session
    private InetSocketAddress ipAddress;    //< set of information needed to connect to the drone (ip + port)
    private Socket droneSocket;             //< reference to established socket
    private BufferedWriter tx;              //< encapsulated station -> drone channel
    private Scanner rx;                     //< encapsulated drone -> station channel

    public GlobalDataManager() {
        droneModel = null;
        ipAddress = null;
        droneSocket = null;
        tx = null;
        rx = null;
    }

    // SETTERS

    public void setDroneSocketAddress(InetSocketAddress socketAddress) {
        this.ipAddress = socketAddress;
    }

    public void setDroneModel(String droneModel) {
        this.droneModel = droneModel;
    }

    public void setDroneSocket(Socket droneSocket) {
        this.droneSocket = droneSocket;
    }

    public void setTx(BufferedWriter tx) {
        this.tx = tx;
    }

    public void setRx(Scanner rx) {
        this.rx = rx;
    }

    // GETTERS

    public String getDroneModel() {
        return droneModel;
    }

    public InetSocketAddress getDroneSocketAddress() {
        return ipAddress;
    }

    public Socket getDroneSocket() {
        return droneSocket;
    }

    public BufferedWriter getTx() {
        return tx;
    }

    public Scanner getRx() {
        return rx;
    }
}
