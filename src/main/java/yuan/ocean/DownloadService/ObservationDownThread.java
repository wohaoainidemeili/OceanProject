package yuan.ocean.DownloadService;

import yuan.ocean.DataBase.DataBaseOper;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Yuan on 2017/4/17.
 */
public class ObservationDownThread extends Thread {
    String stationID;
    String downloadUrl;
    String subFilePath;
    String restrictProperty;
    DownloadInsertStorage downloadInsertStorage;
    String platCode;
    public ObservationDownThread(String stationID,String url,String property,String subFilePath,String restrictProperty,DownloadInsertStorage downloadInsertStorage){
        //get the latesttime from database and create
        this.subFilePath=subFilePath;
        this.stationID=stationID;
        int index= stationID.lastIndexOf("-");
        platCode=stationID.substring(index+1,stationID.length());
        this.downloadInsertStorage=downloadInsertStorage;
        //get the latest time of the station
        Date date= DataBaseOper.getLatestTime(stationID);
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        String dateStr= "";
        if (date==null){
            date=new Date();
            dateStr= simpleDateFormat.format(date).replace("+0800","Z");
            if (restrictProperty.equals("platform_code"))
               // downloadUrl=url+property+"&"+restrictProperty+"=\""+platCode+"\"&time<="+dateStr+"&orderBy(\"time\")";
                downloadUrl=url+property+"&"+restrictProperty+"=\""+platCode+"\"&time%3C="+dateStr+"&orderBy(%22time%22)";
            else
               // downloadUrl=url+property+"&"+restrictProperty+"="+platCode+"&time<="+dateStr+"&orderBy(\"time\")";
                downloadUrl=url+property+"&"+restrictProperty+"="+platCode+"&time%3C="+dateStr+"&orderBy(%22time%22)";
        }else {
            dateStr = simpleDateFormat.format(date).replace("+0800", "Z");
            if (restrictProperty.equals("platform_code"))
                //downloadUrl = url + property + "&"+restrictProperty+"=\"" + platCode + "\"&time>" + dateStr + "&orderBy(\"time\")";
                downloadUrl = url + property + "&"+restrictProperty+"=\"" + platCode + "\"&time%3E" + dateStr + "&orderBy(%22time%22)";
            else
            // downloadUrl = url + property + "&"+restrictProperty+"=" + platCode + "&time>" + dateStr + "&orderBy(\"time\")";
                downloadUrl = url + property + "&"+restrictProperty+"=" + platCode + "&time%3E" + dateStr + "&orderBy(%22time%22)";
        }
    }
    @Override
    public void run() {
        //download file
        downloadInsertStorage.downLoadFile(downloadUrl,subFilePath,platCode+".csv");
    }
}
