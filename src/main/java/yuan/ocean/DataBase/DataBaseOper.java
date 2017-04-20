package yuan.ocean.DataBase;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import yuan.ocean.Entity.Station;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Yuan on 2017/4/18.
 */
public class DataBaseOper {

    public static synchronized List<String> getStationIDs(){
        List<String> stationIDs=new ArrayList<String>();
        Session session = InitialSessionFactory.getInstance().getCurrentSession();
        Transaction transaction = session.beginTransaction();
        Criteria crit = session.createCriteria(Station.class);
        List<Station> stations= crit.list();
        for (Station station:stations){
            stationIDs.add(station.getStationID());
        }
        transaction.commit();
        return stationIDs;
    }
    public static synchronized void saveStation(Station station){
        Session session = InitialSessionFactory.getInstance().getCurrentSession();
        Transaction transaction = session.beginTransaction();
        session.save(station);
        transaction.commit();
    }
    public static synchronized void updateStation(Station station){
        Session session = InitialSessionFactory.getInstance().getCurrentSession();
        Transaction transaction = session.beginTransaction();
        session.update(station);
        transaction.commit();
    }
    public static synchronized Date getLatestTime(String stationID){
        Date date=null;
        Session session = InitialSessionFactory.getInstance().getCurrentSession();
        Transaction transaction = session.beginTransaction();
        Criteria crit = session.createCriteria(Station.class);
        List<Station> stations= crit.add(Restrictions.eq("stationID", stationID)).list();
        date=stations.get(0).getLastTime();
        transaction.commit();
        return date;
    }
}
