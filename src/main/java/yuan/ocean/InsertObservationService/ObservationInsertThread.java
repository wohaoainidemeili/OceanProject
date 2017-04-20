package yuan.ocean.InsertObservationService;

import org.apache.xmlbeans.XmlException;
import yuan.ocean.DownloadService.DownloadInsertStorage;
import yuan.ocean.Entity.Station;
import yuan.ocean.Util.Decode;

import java.io.IOException;
import java.util.Map;

/**
 * Created by Yuan on 2017/4/17.
 */
public class ObservationInsertThread extends Thread {

    Station station=null;
    DownloadInsertStorage downloadInsertStorage;
    Map linkedProperty=null;
    public ObservationInsertThread(String stationID,Map linkedProperty,DownloadInsertStorage downloadInsertStorage){
        this.downloadInsertStorage=downloadInsertStorage;
        this.linkedProperty=linkedProperty;
        //get station data structure form sos
        try {
         station = Decode.parseSensorML(stationID);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void run() {
        downloadInsertStorage.insertObservation(station,linkedProperty);
    }
}
