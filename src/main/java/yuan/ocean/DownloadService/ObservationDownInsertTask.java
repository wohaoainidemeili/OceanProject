package yuan.ocean.DownloadService;

import yuan.ocean.DataBase.DataBaseOper;
import yuan.ocean.Entity.Station;
import yuan.ocean.InsertObservationService.ObservationInsertThread;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Yuan on 2017/4/17.
 */
public class ObservationDownInsertTask extends TimerTask {
    private List<String> stationIDs=new ArrayList<String>();
    private String url;
    private String property;
    private ExecutorService downloadExecutorService= Executors.newFixedThreadPool(10);
    private ExecutorService insertExecutorService=Executors.newFixedThreadPool(10);
    DownloadInsertStorage downloadInsertStorage=null;
    private java.util.Map<String,Integer> linkedProperty=new HashMap<String, Integer>();
    public ObservationDownInsertTask(String url,String property,String stationIDFile,String linkedFile){
        this.url=url;
        this.property=property;
        //read IDs
        BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(stationIDFile)));
        String tempID=null;
        try {
            while ((tempID=bufferedReader.readLine())!=null){
                stationIDs.add(tempID);
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //read linked properties and store it in hashmap
        BufferedReader bufferedReader1=new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(linkedFile)));
        try {
            while ((tempID=bufferedReader1.readLine())!=null){
                String[] eles=tempID.split(",");
                linkedProperty.put(eles[0],Integer.valueOf(eles[1]));
            }
            bufferedReader1.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //if stationIDs is not zero
        // then start 1.create the downloadinsertstroage for multithread download and insert
        // 2.check database has the stationID or not
        //check database if exist this stationID,
        // if not insert stationid without latesttime
        //else do nothing
        if (stationIDs.size()>=0){
            //database operation
            List<String> stationDataBase= DataBaseOper.getStationIDs();
            for (String staionID:stationIDs){
                if (!stationDataBase.contains(staionID)){
                    Station station=new Station();
                    station.setStationID(staionID);
                    DataBaseOper.saveStation(station);
                }
            }
        }

    }
    @Override
    public void run() {
        //download executor
        downloadInsertStorage=new DownloadInsertStorage(stationIDs.size());//initial downloadInsertStorage
        for (int i=0;i<stationIDs.size();i++){
            ObservationDownThread observationDownThread=new ObservationDownThread(stationIDs.get(i),url,property,downloadInsertStorage);
            if (!downloadExecutorService.isShutdown()){
                downloadExecutorService.execute(observationDownThread);
            }
        }
        //insert executor
        for (int i=0;i<stationIDs.size();i++){
            ObservationInsertThread observationInsertThread=new ObservationInsertThread(stationIDs.get(i),linkedProperty,downloadInsertStorage);
            if (!insertExecutorService.isShutdown())
                insertExecutorService.execute(observationInsertThread);
        }
    }
}
