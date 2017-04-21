package yuan.ocean.DownloadService;

import org.apache.log4j.Logger;
import yuan.ocean.DataBase.DataBaseOper;
import yuan.ocean.DownloadService.HttpDownFileload.Download;
import yuan.ocean.Entity.ObservedProperty;
import yuan.ocean.Entity.SOSWrapper;
import yuan.ocean.Entity.Sensor;
import yuan.ocean.Entity.Station;
import yuan.ocean.InsertObservationService.ERDDAPDecodeFile;
import yuan.ocean.InsertObservationService.IDecodeFile;
import yuan.ocean.SensorConfigInfo;
import yuan.ocean.Util.Encode;
import yuan.ocean.Util.HttpRequestAndPost;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Yuan on 2017/4/18.
 */
public class DownloadInsertStorage {
    private static final Logger log=Logger.getLogger(DownloadInsertStorage.class);
    private int attemptDownLoadFileCount;
    private volatile AtomicInteger currentDownLoadFileCount=new AtomicInteger(0);
    public DownloadInsertStorage(int attemptDownLoadFileCount){
        this.attemptDownLoadFileCount=attemptDownLoadFileCount;
    }
    public void downLoadFile(String downloadUrl,String filename){

        log.info("Start to download file "+filename+" from "+downloadUrl);
        try {
            Download.downLoadFromUrl(downloadUrl,filename,"E:\\download");
        } catch (IOException e) {
            e.printStackTrace();
        }
        //finished download
        synchronized(currentDownLoadFileCount) {
            currentDownLoadFileCount.getAndAdd(1);
            currentDownLoadFileCount.notifyAll();
        }
    }
    public  void insertObservation(Station station,Map<String,Integer> linkedProperty) {
        log.info("Start to insert Observation");
        synchronized (currentDownLoadFileCount) {
            while (currentDownLoadFileCount.get() != attemptDownLoadFileCount) {
                try {
                    System.out.println(Thread.currentThread().getName()+"is waiting...");
                    currentDownLoadFileCount.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            String paltCode = station.getStationID().substring(station.getStationID().lastIndexOf("-") + 1, station.getStationID().length());
            // decode file
            IDecodeFile decodeFile=new ERDDAPDecodeFile();
            decodeFile.decode(linkedProperty,paltCode,station);
        }
    }
}
