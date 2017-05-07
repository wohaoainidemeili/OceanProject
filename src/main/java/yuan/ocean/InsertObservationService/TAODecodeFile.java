package yuan.ocean.InsertObservationService;

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
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Yuan on 2017/5/4.
 */
public class TAODecodeFile implements IDecodeFile {
    public void decode(Map<String, Integer> linkedProperty, String paltCode, Station station, String subFilePath) {
        System.out.println("reading file"+SensorConfigInfo.getDownloadpath()+"\\"+subFilePath+"\\"+paltCode+".csv");
        File file=new File(SensorConfigInfo.getDownloadpath()+"\\"+subFilePath+"\\"+paltCode+".csv");
        if (file.exists()){
            try {
                BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                String temp = null;
                String dateLatestStr = "";
                bufferedReader.readLine();
                bufferedReader.readLine();
                int timePos = linkedProperty.get("time");
                int latPos = 0;
                int lonPos = 0;
                if (linkedProperty.containsKey("lat")&&linkedProperty.containsKey("lon")){
                    latPos=linkedProperty.get("lat");
                    lonPos = linkedProperty.get("lon");
                    while ((temp = bufferedReader.readLine()) != null) {
                        //get the obervation and from soswrapper
                        String[] eles = temp.split(",");
                        for (Sensor sensor : station.getSensors()) {
                            //postion property conposed by lat lon
                            //others composed by

                            //justify all current sensor has all the property supplied in property
                            if (isSensorMatchProperty(linkedProperty,sensor)) {
                                SOSWrapper sosWrapper = new SOSWrapper();
                                sosWrapper.setLat(Double.valueOf(eles[latPos]));
                                sosWrapper.setLon(Double.valueOf(eles[lonPos]));
                                sosWrapper.setSensorID(sensor.getSensorID());
                                sosWrapper.setSrid(4326);
                                sosWrapper.setSimpleTime(eles[timePos]);

                                for (ObservedProperty property : sensor.getObservedProperties()) {
                                    //if it NaN then change it to -32768
                                    if (eles[linkedProperty.get(property.getPropertyID())].equals("NaN"))
                                        property.setDataValue("-32768");
                                    else
                                        property.setDataValue(eles[linkedProperty.get(property.getPropertyID())]);
                                }

                                sosWrapper.setProperties(sensor.getObservedProperties());

                                //encode xml and insert into sos
                                String insertXML = Encode.getInserObservationXML(sosWrapper);
                                String responseXML = HttpRequestAndPost.sendPost(SensorConfigInfo.getUrl(), insertXML);
//                        System.out.println(responseXML);
//                        //log info
//                    }
                                for (int i = 0; i < eles.length - 1; i++) {
                                    System.out.print(eles[i]);
                                }
                                System.out.println(eles[eles.length - 1]);
                                dateLatestStr = eles[timePos];
                            }
                        }
                    }
                }else {
                    while ((temp = bufferedReader.readLine()) != null) {
                        //get the obervation and from soswrapper
                        String[] eles = temp.split(",");
                        for (Sensor sensor : station.getSensors()) {
                            //postion property conposed by lat lon
                            //others composed by

                            //justify all current sensor has all the property supplied in property
                            if (isSensorMatchProperty(linkedProperty,sensor)) {
                                SOSWrapper sosWrapper = new SOSWrapper();
//                                sosWrapper.setLat(Double.valueOf(eles[latPos]));
//                                sosWrapper.setLon(Double.valueOf(eles[lonPos]));
                                sosWrapper.setSensorID(sensor.getSensorID());
                                sosWrapper.setSrid(4326);
                                sosWrapper.setSimpleTime(eles[timePos]);

                                for (ObservedProperty property : sensor.getObservedProperties()) {
                                    //if it NaN then change it to -32768
                                    if (eles[linkedProperty.get(property.getPropertyID())].equals("NaN"))
                                        property.setDataValue("-32768");
                                    else
                                        property.setDataValue(eles[linkedProperty.get(property.getPropertyID())]);
                                }

                                sosWrapper.setProperties(sensor.getObservedProperties());

                                //encode xml and insert into sos
                                String insertXML = Encode.getInserObservationXML(sosWrapper);
                                String responseXML = HttpRequestAndPost.sendPost(SensorConfigInfo.getUrl(), insertXML);
//                        System.out.println(responseXML);
//                        //log info
//                    }
                                for (int i = 0; i < eles.length - 1; i++) {
                                    System.out.print(eles[i]);
                                }
                                System.out.println(eles[eles.length - 1]);
                                dateLatestStr = eles[timePos];
                            }
                        }
                    }
                }


                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                try {
                    Date date = simpleDateFormat.parse(dateLatestStr);
                    Station station1 = new Station();
                    station1.setStationID(station.getStationID());
                    station1.setLastTime(date);
                    DataBaseOper.updateStation(station1);
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
