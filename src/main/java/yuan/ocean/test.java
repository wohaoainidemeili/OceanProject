package yuan.ocean;

import org.apache.xmlbeans.XmlException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.metamodel.MetadataSources;
import yuan.ocean.Entity.Sensor;
import yuan.ocean.Entity.Station;
import yuan.ocean.Entity.TestMy;
import yuan.ocean.Util.Decode;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by Yuan on 2017/4/17.
 */
public class test {
    public static void main(String[] args){
        SensorConfigReader.reader();
        try {
            Map<String,Integer> linkedProperty=new HashMap<String, Integer>();
            String tempID=null;
            BufferedReader bufferedReader1=new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("propertyPos.csv")));
            try {
                while ((tempID=bufferedReader1.readLine())!=null){
                    String[] eles=tempID.split(",");
                    linkedProperty.put(eles[0],Integer.valueOf(eles[1]));
                }
                bufferedReader1.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Station station= Decode.parseSensorML("urn:liesmars:insitusensor:platform:ArgoFloat-APEX-Profiling-Float-5904033");
            String paltCode = station.getStationID().substring(station.getStationID().lastIndexOf("-") + 1, station.getStationID().length());

            Class<?> fileDecodeClass=Class.forName("yuan.ocean.InsertObservationService.ERDDAPDecodeFile");
            Object object=fileDecodeClass.newInstance();
            Method method= fileDecodeClass.getMethod("decode", Map.class, String.class, Station.class, String.class);
            System.out.println("load insert method"+SensorConfigInfo.getDownloadpath()+"\\"+paltCode);
            method.invoke(object, linkedProperty, paltCode, station, "FristKind");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        createStationCSV("C:\\Users\\Yuan\\Desktop\\TaoPirataRama\\Platform");
//        SensorConfigReader.reader();
//        try {
//            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
//           // simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
//            Date date = simpleDateFormat.parse("2017-04-13T00:10:00Z");
//
//            Decode.parseSensorML("urn:liesmars:insitusensor:platform:ArgoFloat-APEX-Profiling-Float-2900452");
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (XmlException e) {
//            e.printStackTrace();
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }

    }
    public static void createStationCSV(String path){
        File pathFile=new File(path);
        File[] files= pathFile.listFiles();
        for (int i=0;i<files.length;i++){
            try {
                BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(new FileInputStream(files[i])));
                StringBuffer stringBuffer=new StringBuffer();
                String temp=null;
                while ((temp=bufferedReader.readLine())!=null){
                    stringBuffer.append(temp);
                }
                String sml=stringBuffer.toString();
                Sensor sensor= Decode.decodeDescribeSensor(sml);
                String sensorID= sensor.getSensorID();
                System.out.println(sensorID);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlException e) {
                e.printStackTrace();
            }
        }
    }
}
