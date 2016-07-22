package jwbCrawler.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by peng on 2016/7/21.
 */

public class Attend{
    private String interval="";
    private int week;
    private List<Integer> classes=null;
    private String location;

    public final static List<String> WEEK= Arrays.asList("周日", "周一", "周二", "周三", "周四", "周五", "周六");

    public Attend(){}
    public Attend(String interval, int week, List<Integer> classes, String location){
        set(interval, week, classes, location);
    }
    public Attend set(String interval, int week, List<Integer> classes, String location){
        this.interval=interval;
        this.week=week;
        this.classes=classes;
        this.location=location;

        return this;
    }
    public Attend setLocation(String location){
        this.location=location;
        return this;
    }
    public String getInterval(){return interval;}
    public int getWeek(){return week;}
    public List<Integer> getClasses(){return classes;}
    public String getLocation(){return location;}
    public String getAttendTime(){
        StringBuilder sb=new StringBuilder();
        sb.append(WEEK.get(week));
        if(classes!=null) {
            Iterator<Integer> iterator=classes.iterator();
            if(iterator.hasNext()) {
                sb.append("第");
                do {
                    sb.append(iterator.next().toString());
                } while (iterator.hasNext() && sb.append("，") != null);
                sb.append("节");
            }
        }
        if(!interval.equals("")){
            sb.append("[").append(interval).append("]");
        }
        return sb.toString();
    }

    @Override
    public String toString(){
        return getAttendTime()+"\t"+getLocation();
    }
}

