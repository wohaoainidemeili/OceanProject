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
import java.util.List;
import java.util.Map;

/**
 * Created by Yuan on 2017/4/20.
 */
public class ERDDAPDecodeFile implements IDecodeFile {
    private static final Logger log=Logger.getLogger(ERDDAPDecodeFile.class);
    public void decode(Map<String, Integer> linkedProperty, String paltCode, Station station) {
        //if file exists read file and insert Observation
        File file = new File("E:\\download\\" + paltCode + ".csv");
        if (file.exists()) {
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream("E:\\download\\" + paltCode + ".csv")));
                String temp = null;
                bufferedReader.readLine();
                bufferedReader.readLine();
                int timePos = linkedProperty.get("time");
                int latPos = linkedProperty.get("lat");
                int lonPos = linkedProperty.get("lon");
                String dateLatestStr = "";
                String lastTime = "";
                String currentTime = "";
                int count = 0;
                String[] eles = null;
                while ((temp = bufferedReader.readLine()) != null) {
                  eles = temp.split(",");
                    if (count == 0) {
                        lastTime = eles[timePos];
                        count++;
                    }
                    currentTime = eles[timePos];
                    if (currentTime.equals(lastTime)) {
                        for (Sensor sensor : station.getSensors()) {
                            for (ObservedProperty property : sensor.getObservedProperties()) {
                                if (!property.getPropertyID().equals("urn:ogc:def:phenomenon:OGC:1.0.30:position")) {
                                   // log.info(temp);
                                   // System.out.println(temp);
                                   // log.info(property.getPropertyID());
                                   // System.out.println(property.getPropertyID());
                                    if (!eles[linkedProperty.get(property.getPropertyID())].equals("NaN"))
                                        property.tempSumValue = property.tempSumValue + Double.valueOf(eles[linkedProperty.get(property.getPropertyID())]);
                                    property.count++;
                                } else {
                                    property.setDataValue("POINT(" + eles[latPos] + " " + eles[lonPos] + ")#" + sensor.getSrsid());
                                }
                            }
                        }
                    }else {
                        for (Sensor sensor : station.getSensors()) {
                            //insert data
                            SOSWrapper sosWrapper = new SOSWrapper();
                            sosWrapper.setLat(sensor.getLat());
                            sosWrapper.setLon(sensor.getLon());
                            sosWrapper.setSensorID(sensor.getSensorID());
                            sosWrapper.setSrid(4326);
                            sosWrapper.setSimpleTime(lastTime);
                            //caculate average data
                            for (ObservedProperty property : sensor.getObservedProperties()) {
                                if (!property.getPropertyID().equals("urn:ogc:def:phenomenon:OGC:1.0.30:position")) {
                                    property.setDataValue(String.valueOf(property.tempSumValue / property.count));
                                }
                            }
                            sosWrapper.setProperties(sensor.getObservedProperties());
                            String insertXML = Encode.getInserObservationXML(sosWrapper);
                            String responseXML = HttpRequestAndPost.sendPost(SensorConfigInfo.getUrl(), insertXML);
                            log.info(responseXML);
                            System.out.println(responseXML);
                            ////change property to 0 after insert data
                            //and add this record for new observation
                            for (ObservedProperty property : sensor.getObservedProperties()) {
                                if (!property.getPropertyID().equals("urn:ogc:def:phenomenon:OGC:1.0.30:position")) {
                                    property.tempSumValue = 0;
                                    property.count = 0;
                                    property.tempSumValue = property.tempSumValue + Double.valueOf(eles[linkedProperty.get(property.getPropertyID())]);
                                    property.count++;
                                } else {
                                    property.setDataValue("POINT(" + eles[latPos] + " " + eles[lonPos] + ")#" + sensor.getSrsid());
                                }
                            }
                        }
                    }
                    lastTime = currentTime;
                    dateLatestStr=eles[timePos];

//                while ((temp = bufferedReader.readLine()) != null) {
//                    //get the obervation and from soswrapper
//                    String[] eles = temp.split(",");
//                    for (Sensor sensor : station.getSensors()) {
//                        //postion property conposed by lat lon
//                        //others composed by
//                        SOSWrapper sosWrapper = new SOSWrapper();
//                        sosWrapper.setLat(Double.valueOf(eles[latPos]));
//                        sosWrapper.setLon(Double.valueOf(eles[lonPos]));
//                        sosWrapper.setSensorID(sensor.getSensorID());
//                        sosWrapper.setSrid(4326);
//                        sosWrapper.setSimpleTime(eles[timePos]);
//                        for (ObservedProperty property : sensor.getObservedProperties()) {
//                            if (!property.getPropertyID().equals("urn:ogc:def:phenomenon:OGC:1.0.30:position")) {
//                                //if it NaN then change it to -32768
//                                if (eles[linkedProperty.get(property.getPropertyID())].equals("NaN"))
//                                    property.setDataValue("-32768");
//                                else
//                                    property.setDataValue(eles[linkedProperty.get(property.getPropertyID())]);
//                            } else {
//                                property.setDataValue("POINT(" + eles[latPos] + " " + eles[lonPos] + ")#" + sensor.getSrsid());
//                            }
//                        }
//                        sosWrapper.setProperties(sensor.getObservedProperties());
//
//                        //encode xml and insert into sos
//                        String insertXML = Encode.getInserObservationXML(sosWrapper);
//                        String responseXML = HttpRequestAndPost.sendPost(SensorConfigInfo.getUrl(), insertXML);
////                        System.out.println(responseXML);
////                        //log info
////                    }
//                    for (int i = 0; i < eles.length - 1; i++) {
//                        System.out.print(eles[i]);
//                    }
//                    System.out.println(eles[eles.length - 1]);
//                    dateLatestStr = eles[timePos];
//                }
                    //update station time

                }
                //insert last
                for (Sensor sensor : station.getSensors()) {
                    //insert data
                    SOSWrapper sosWrapper = new SOSWrapper();
                    sosWrapper.setLat(Double.valueOf(eles[latPos]));
                    sosWrapper.setLon(Double.valueOf(eles[lonPos]));
                    sosWrapper.setSensorID(sensor.getSensorID());
                    sosWrapper.setSrid(4326);
                    sosWrapper.setSimpleTime(eles[timePos]);
                    //caculate average data
                    for (ObservedProperty property : sensor.getObservedProperties()) {
                        if (!property.getPropertyID().equals("urn:ogc:def:phenomenon:OGC:1.0.30:position")) {
                            property.setDataValue(String.valueOf(property.tempSumValue / property.count));
                        }
                    }
                    sosWrapper.setProperties(sensor.getObservedProperties());
                    String insertXML = Encode.getInserObservationXML(sosWrapper);
                    String responseXML = HttpRequestAndPost.sendPost(SensorConfigInfo.getUrl(), insertXML);
                    log.info(responseXML);
                    ////change property to 0 after insert data
                    for (ObservedProperty property : sensor.getObservedProperties()) {
                        if (!property.getPropertyID().equals("urn:ogc:def:phenomenon:OGC:1.0.30:position")) {
                            property.tempSumValue = 0;
                            property.count = 0;
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
                log.error("There is no File."+file.getName()+e.getMessage());
            } catch (IOException e) {
                log.error("Read file"+file.getName()+" error. All info as follows:"+e.getMessage());
            }
        }
    }
}

