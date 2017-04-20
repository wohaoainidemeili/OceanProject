package yuan.ocean.DataBase;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Created by Yuan on 2017/4/18.
 */
public class InitialSessionFactory {
    private static SessionFactory sessionFactory=null;
    public static synchronized SessionFactory getInstance(){
        if (sessionFactory==null) {
            Configuration configuration=new Configuration();
            configuration.configure(ClassLoader.getSystemResource("hibernate.cfg.xml"));
            sessionFactory=configuration.buildSessionFactory();
            return sessionFactory;
        }else return sessionFactory;
    }

}
