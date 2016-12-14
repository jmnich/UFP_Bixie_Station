package GroundStationUtilities;

import javax.swing.*;
import java.io.File;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Jakub on 11.10.2016.
 *
 * Logs data for later analysis. Provides "save to file" service.
 */
public class Logger {

    List<List<String>>      dataStorage;
    int                     storedRecords;

    public Logger() {
        dataStorage = new LinkedList<>();
        storedRecords = 0;
    }

    public void appendNewRecord(List<String> newData) {
        storedRecords++;
        dataStorage.add(newData);
    }

    public int getNumberOfStoredRecords() {
        return storedRecords;
    }

    public void resetLogger() {
        storedRecords = 0;
        dataStorage.clear();
    }

    public void saveAllDataToFile(String pathToDirectory, String fileName, String fileFormat) {
        // prepare file
        String fullPath = pathToDirectory + File.separator + fileName + "." + fileFormat;
        File file = new File(fullPath);
        PrintWriter printWriter;
        try {
            printWriter = new PrintWriter(file, "UTF-8");

            for(List<String> record : dataStorage) {
                String line = "";
                for(String dataField : record) {
                    line += dataField + "\t";
                }
                printWriter.println(line);
            }
            printWriter.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Can't write to file.", "Kill me", JOptionPane.INFORMATION_MESSAGE);
            e.printStackTrace();
        }
    }
}
