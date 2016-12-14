package Frames;

import GroundStationUtilities.GlobalDataManager;

/**
 * Created by Jakub on 08.10.2016.
 *
 * Common interface for all windows used in ground station.
 */
public interface GroundStationWindowController {

    void close();

    void setDataBank(GlobalDataManager dataBank);
}
