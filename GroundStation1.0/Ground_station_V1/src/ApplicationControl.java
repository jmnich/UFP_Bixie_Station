import Frames.*;
import GroundStationUtilities.GlobalDataManager;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.*;
import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;

/**
 * Created by Jakub on 08.10.2016.
 *
 * Controls state of the application and launches the right frames.
 */
public class ApplicationControl implements Observer {

    private LoginWindow loginWindow;
    private LoadingWindow loadingWindow;
    private GroundStationWindowController telemetryWindow;
    private GlobalDataManager globalDataManager;

    private String applicationStatus = "login";
    public int bagno = 0;

    // ==========================================================

    public ApplicationControl() {
        loginWindow = new LoginWindow();
        loginWindow.addObserver(this);
        globalDataManager = new GlobalDataManager();
//        telemetryWindow = new PendulumBasicInterfaceWindow(globalDataManager);
    }

    // ==========================================================

    private void connectWithDrone() {
        // open loading window
        loadingWindow = new LoadingWindow();

        // get data from login window
        String model = loginWindow.getModel();
        String ip = loginWindow.getIP();
        String port = loginWindow.getPort();

        // connect here or return if failed
        int portAaInt = Integer.parseInt(port);
        SocketAddress droneAddress = new InetSocketAddress(ip, portAaInt);
        Socket socket = new Socket();
        //LoadingWindow loadingWindow = new LoadingWindow();
        try {
            socket.connect(droneAddress, 5000);
            //loadingWindow.close();
            loginWindow.close();
        }
        catch (SocketTimeoutException e) {
            loginWindow.close();
            telemetryWindow = new ErrorWindow();
            //loadingWindow.close();
            return;
        }
        catch (IOException e) {
            //loadingWindow.close();
            loginWindow.close();
            telemetryWindow = new ErrorWindow();
            return;
        }

        // now handle established connection
        globalDataManager.setDroneSocket(socket);
        try {
            globalDataManager.setRx(new Scanner(new InputStreamReader(socket.getInputStream())));
            globalDataManager.setTx(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
        } catch (IOException e) {
            telemetryWindow = new ErrorWindow();
            e.printStackTrace();
            return;
        }

        // launch main interface window of the chosen device
        System.out.println(model);

        switch(model){
            case("Enix"):
                telemetryWindow = new EnixBasicInterfaceWindow();
                break;
            case("Lyra"):
//                telemetryWindow = new LyraBasicInterfaceWindow();
                break;
            case("Pendulum"):
                telemetryWindow = new PendulumBasicInterfaceWindow(globalDataManager);
                break;
            default:
                telemetryWindow = new ErrorWindow();
                break;
        }

        loadingWindow.close();
        loginWindow.close();
    }

    // ==========================================================

    @Override
    public void update(Observable o, Object arg) {

        switch((String)arg) {
            case("connection_button_down"):
                connectWithDrone();
                break;
            default:
                break;
        }

    }
}
