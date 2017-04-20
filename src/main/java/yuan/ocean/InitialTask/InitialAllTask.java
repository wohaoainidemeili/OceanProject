package yuan.ocean.InitialTask;

import yuan.ocean.DownloadService.ObservationDownInsertTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Timer;

/**
 * Created by Yuan on 2017/4/18.
 */
public class InitialAllTask {
    private static Timer timer = new Timer("observation-timer");
    public static void startTask(){
        BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("observationdownloadconfig.txt")));
        String tempStr;
        try {
            while ((tempStr=bufferedReader.readLine())!=null){
               String[] eles=tempStr.split("#");
                ObservationDownInsertTask downInsertTask=new ObservationDownInsertTask(eles[0],eles[1],eles[2],eles[3]);
                timer.schedule(downInsertTask,20000,24*3600*1000);
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
