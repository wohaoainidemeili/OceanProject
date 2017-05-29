package yuan.ocean.DownloadService;

import org.apache.log4j.Logger;
import yuan.ocean.DownloadService.HttpDownFileload.Download;
import yuan.ocean.Entity.Station;
import yuan.ocean.SensorConfigInfo;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
    public void downLoadFile(String downloadUrl,String subFilePath,String filename){

        log.info("Start to download file "+filename+" from "+downloadUrl);
        try {
            System.out.println(SensorConfigInfo.getDownloadpath()+"\\"+subFilePath);
            Download.downLoadFromUrl(downloadUrl,filename,SensorConfigInfo.getDownloadpath()+"\\"+subFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //finished download
        synchronized(currentDownLoadFileCount) {
            int count= currentDownLoadFileCount.getAndAdd(1);
            System.out.println(SensorConfigInfo.getDownloadpath()+"\\"+subFilePath+"\\"+filename+":"+count);
            currentDownLoadFileCount.notifyAll();
        }
    }
    public  void insertObservation(Station station,String subFilePath,String fileDecodeClassName,Map<String,Integer> linkedProperty) {
        log.info("Start to insert Observation");
        synchronized (currentDownLoadFileCount) {
            while (currentDownLoadFileCount.get()!= attemptDownLoadFileCount) {
                try {
                    System.out.println(Thread.currentThread().getName()+"is waiting...");
                    currentDownLoadFileCount.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

           // System.out.println(station.getStationID());
            String paltCode = station.getStationID().substring(station.getStationID().lastIndexOf("-") + 1, station.getStationID().length());
            System.out.println("start to insert observation"+SensorConfigInfo.getDownloadpath()+"\\"+subFilePath+paltCode);
            // decode file
            //decode file using different method
            try {
                Class<?> fileDecodeClass=Class.forName(fileDecodeClassName);
                Object object=fileDecodeClass.newInstance();
                Method method= fileDecodeClass.getMethod("decode", Map.class, String.class, Station.class, String.class);
                System.out.println("load insert method"+SensorConfigInfo.getDownloadpath()+"\\"+subFilePath+paltCode);
                method.invoke(object,linkedProperty,paltCode,station,subFilePath);
            } catch (ClassNotFoundException e) {
                log.error("can not load class"+fileDecodeClassName);
            } catch (InstantiationException e) {
                log.error("can not instance class"+fileDecodeClassName);
            } catch (IllegalAccessException e) {
                log.error("illegal access class" + fileDecodeClassName);
            } catch (NoSuchMethodException e) {
                log.error("there is no decode method, please check your class"+fileDecodeClassName);
            } catch (InvocationTargetException e) {
                log.error("can not invoke to the method");
            }
//            IDecodeFile decodeFile=new ERDDAPDecodeFile();
//            decodeFile.decode(linkedProperty,paltCode,station,subFilePath);
        }
    }
}
