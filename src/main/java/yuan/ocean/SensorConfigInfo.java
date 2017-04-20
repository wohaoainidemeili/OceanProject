package yuan.ocean;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.metamodel.MetadataSources;


import java.util.Properties;

/**
 * Created by Yuan on 2016/5/10.
 */
public class SensorConfigInfo {
    String URL="sos_url";

    static String url;

    public SensorConfigInfo(Properties properties){
        setUrl(properties.getProperty(URL));

    }
    public static void test(){

    }

    public static String getUrl() {
        return url;
    }

    public static void setUrl(String url) {
        SensorConfigInfo.url = url;
    }

}
