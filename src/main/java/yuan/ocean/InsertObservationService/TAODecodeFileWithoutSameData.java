package yuan.ocean.InsertObservationService;

import org.apache.log4j.Logger;
import yuan.ocean.DataBase.DataBaseOper;
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
 * Created by Yuan on 2017/5/17.
 */
public class TAODecodeFileWithoutSameData implements IDecodeFile{
    private final static Logger log=Logger.getLogger(TAODecodeFileWithoutSameData.class);
    public void decode(Map<String, Integer> linkedProperty, String paltCode, Station station, String subFilePath) {
        File file=new File(SensorConfigInfo.getDownloadpath()+"\\"+subFilePath+"\\"+paltCode+".csv");
        if (file.exists()){
            try {
                BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                String temp = null;
                String dateLatestStr = "";
                bufferedReader.readLine();
                bufferedReader.readLine();
                int timePos = linkedProperty.get("time");
                String lastTime = "";
                String currentTime = "";
                String[] eles =null;
                int count = 0;

                    while ((temp = bufferedReader.readLine()) != null) {
                        //get the obervation and from soswrapper
                       eles=temp.split(",");
                        if (count == 0) {
                            lastTime = eles[timePos];
                            count++;
                        }
                        currentTime = eles[timePos];
                        if (currentTime.equals(lastTime)) {
                            for (Sensor sensor : station.getSensors()) {
                                if (isSensorMatchProperty(linkedProperty, sensor)) {
                                    for (ObservedProperty property : sensor.getObservedProperties()) {
                                        if (!eles[linkedProperty.get(property.getPropertyID())].equals("NaN")) {
                                            property.tempSumValue = property.tempSumValue + Double.valueOf(eles[linkedProperty.get(property.getPropertyID())]);
                                            property.count++;
                                        }
                                    }
                                }
                            }
                        } else {
                            for (Sensor sensor : station.getSensors()) {
                                if (isSensorMatchProperty(linkedProperty, sensor)) {
                                    SOSWrapper sosWrapper = new SOSWrapper();
                                    sosWrapper.setSensorID(sensor.getSensorID());
                                    sosWrapper.setLat(sensor.getLat());
                                    sosWrapper.setLon(sensor.getLon());
                                    sosWrapper.setSrid(4326);
                                    sosWrapper.setSimpleTime(lastTime);
                                    for (ObservedProperty property : sensor.getObservedProperties()) {
                                        if (property.getCount() == 0) {
                                            property.setDataValue("-32768");
                                        } else
                                            property.setDataValue(String.valueOf(property.tempSumValue / property.count));
                                    }
                                    sosWrapper.setProperties(sensor.getObservedProperties());
                                    String insertXML = Encode.getInserObservationXML(sosWrapper);
                                    String responseXML = HttpRequestAndPost.sendPost(SensorConfigInfo.getUrl(), insertXML);
                                    System.out.println(responseXML);
                                    log.info(responseXML);
                                    for (ObservedProperty property : sensor.getObservedProperties()) {
                                        property.tempSumValue = 0;
                                        property.count = 0;
                                        if (!eles[linkedProperty.get(property.getPropertyID())].equals("NaN")) {
                                            property.tempSumValue = property.tempSumValue + Double.valueOf(eles[linkedProperty.get(property.getPropertyID())]);
                                            property.count++;
                                        }
                                    }
                                }
                            }
                        }
                        lastTime=currentTime;
                        dateLatestStr=eles[timePos];
                    }
                for (Sensor sensor : station.getSensors()) {
                    if (isSensorMatchProperty(linkedProperty, sensor)) {
                        SOSWrapper sosWrapper = new SOSWrapper();
                        sosWrapper.setSensorID(sensor.getSensorID());
                        sosWrapper.setLat(sensor.getLat());
                        sosWrapper.setLon(sensor.getLon());
                        sosWrapper.setSrid(4326);
                        sosWrapper.setSimpleTime(lastTime);
                        for (ObservedProperty property : sensor.getObservedProperties()) {
                            if (property.getCount() == 0) {
                                property.setDataValue("-32768");
                            } else
                                property.setDataValue(String.valueOf(property.tempSumValue / property.count));
                        }
                        sosWrapper.setProperties(sensor.getObservedProperties());
                        String insertXML = Encode.getInserObservationXML(sosWrapper);
                        String responseXML = HttpRequestAndPost.sendPost(SensorConfigInfo.getUrl(), insertXML);
                        System.out.println(responseXML);
                        log.info(responseXML);
                        for (ObservedProperty property : sensor.getObservedProperties()) {
                            property.tempSumValue = 0;
                            property.count = 0;
                        }
                    }
                }


                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                try {
                    Date date = simpleDateFormat.parse(dateLatestStr);
                    Date date1= DataBaseOper.getLatestTime(station.getStationID());
                    if (date1==null||date1.getTime()<date.getTime())
                    {
                        Station station1 = new Station();
                        station1.setStationID(station.getStationID());
                        station1.setLastTime(date);
                        DataBaseOper.updateStation(station1);
                    }
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
    public boolean isSensorMatchProperty(Map<String,Integer> linkedProperty,Sensor sensor){
        boolean isContain=true;
        for (ObservedProperty property:sensor.getObservedProperties()){
            if (!linkedProperty.containsKey(property.getPropertyID()))
                isContain=false;
        }
        return isContain;
    }
}
