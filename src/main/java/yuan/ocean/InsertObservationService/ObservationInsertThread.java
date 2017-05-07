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
    String subFilePath;
    String fileDecodeClassName;
    DownloadInsertStorage downloadInsertStorage;
    Map linkedProperty=null;

    public ObservationInsertThread(String stationID,Map linkedProperty,String subFilePath,String fileDecodeClassName,DownloadInsertStorage downloadInsertStorage){
        this.downloadInsertStorage=downloadInsertStorage;
        this.linkedProperty=linkedProperty;
        this.subFilePath=subFilePath;
        this.fileDecodeClassName=fileDecodeClassName;
        //get station data structure form sos
        try {
         station = Decode.parseSensorML(stationID);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlException e) {
            System.out.println("This sensor has problem:"+stationID);
            e.printStackTrace();
        }
    }
    @Override
    public void run() {
        downloadInsertStorage.insertObservation(station,subFilePath,fileDecodeClassName,linkedProperty);
    }
}
