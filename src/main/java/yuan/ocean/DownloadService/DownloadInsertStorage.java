package yuan.ocean.DownloadService;

import yuan.ocean.DataBase.DataBaseOper;
import yuan.ocean.DownloadService.HttpDownFileload.Download;
import yuan.ocean.Entity.ObservedProperty;
import yuan.ocean.Entity.SOSWrapper;
import yuan.ocean.Entity.Sensor;
import yuan.ocean.Entity.Station;
import yuan.ocean.SensorConfigInfo;
import yuan.ocean.Util.Encode;
import yuan.ocean.Util.HttpRequestAndPost;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created by Yuan on 2017/4/18.
 */
public class DownloadInsertStorage {
    private int attemptDownLoadFileCount;
    private volatile Integer currentDownLoadFileCount=0;
    public DownloadInsertStorage(int attemptDownLoadFileCount){
        this.attemptDownLoadFileCount=attemptDownLoadFileCount;
    }
    public void downLoadFile(String downloadUrl,String filename){

        try {
            Download.downLoadFromUrl(downloadUrl,filename,"E:\\download");
        } catch (IOException e) {
            e.printStackTrace();
        }
        //finished download
        synchronized(currentDownLoadFileCount) {
            currentDownLoadFileCount++;
            notifyAll();
        }
    }
    public synchronized void insertObservation(Station station,Map<String,Integer> linkedProperty){
        while (currentDownLoadFileCount!=attemptDownLoadFileCount){
            try {
              wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        String paltCode=station.getStationID().substring(station.getStationID().lastIndexOf("-")+1,station.getStationID().length());
        //if file exists read file and insert Observation
        File file=new File("E:\\download\\"+paltCode+".csv");
       if(file.exists()) {
           try {
               BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream("E:\\download\\" + paltCode + ".csv")));
               String temp = null;
               bufferedReader.readLine();
               bufferedReader.readLine();

               int timePos= linkedProperty.get("time");
               int latPos=linkedProperty.get("lat");
               int lonPos=linkedProperty.get("lon");
               String dateLatestStr="";

               while ((temp=bufferedReader.readLine())!=null){
                   //get the obervation and from soswrapper
                   String[] eles=temp.split(",");
                   for (Sensor sensor:station.getSensors()){
                       //postion property conposed by lat lon
                       //others composed by
                       SOSWrapper sosWrapper=new SOSWrapper();
                       sosWrapper.setLat(Double.valueOf(eles[latPos]));
                       sosWrapper.setLon(Double.valueOf(eles[lonPos]));
                       sosWrapper.setSensorID(sensor.getSensorID());
                       sosWrapper.setSrid(4326);
                       sosWrapper.setSimpleTime(eles[timePos]);
                       for (ObservedProperty property: sensor.getObservedProperties()){
                           if (!property.getPropertyID().equals("urn:ogc:def:phenomenon:OGC:1.0.30:position")){
                               //if it NaN then change it to -32768
                               if(eles[linkedProperty.get(property.getPropertyID())].equals("NaN"))
                                   property.setDataValue("-32768");
                               else
                                  property.setDataValue(eles[linkedProperty.get(property.getPropertyID())]);
                           }else {
                               property.setDataValue("POINT("+eles[latPos]+" "+eles[lonPos]+")#"+sensor.getSrsid());
                           }
                       }
                       sosWrapper.setProperties(sensor.getObservedProperties());

                       //encode xml and insert into sos
                       String insertXML= Encode.getInserObservationXML(sosWrapper);
                       String responseXML= HttpRequestAndPost.sendPost(SensorConfigInfo.getUrl(), insertXML);
                       //log info
                   }
                   for (int i=0;i<eles.length-1;i++){
                       System.out.print(eles[i]);
                   }
                   System.out.println(eles[eles.length-1]);
                   dateLatestStr=eles[timePos];
               }
               //update station time
               SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
               try {
                   Date date=simpleDateFormat.parse(dateLatestStr);
                   Station station1=new Station();
                   station1.setStationID(station.getStationID());
                   station1.setLastTime(date);
                   DataBaseOper.updateStation(station);
               } catch (ParseException e) {
                   e.printStackTrace();
               }
               bufferedReader.close();
               file.delete();

           } catch (FileNotFoundException e) {
               e.printStackTrace();
           } catch (IOException e) {
               e.printStackTrace();
           }
       }
    }
}
