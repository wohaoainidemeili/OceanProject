package yuan.ocean;

import org.apache.xmlbeans.XmlException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.metamodel.MetadataSources;
import yuan.ocean.Entity.Station;
import yuan.ocean.Entity.TestMy;
import yuan.ocean.Util.Decode;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Yuan on 2017/4/17.
 */
public class test {
    public static void main(String[] args){
        SensorConfigReader.reader();
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
           // simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = simpleDateFormat.parse("2017-04-13T00:10:00Z");

            Decode.parseSensorML("urn:liesmars:insitusensor:platform:ArgoFloat-APEX-Profiling-Float-2900452");

        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }
}
