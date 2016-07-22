package jwbCrawler.Data;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by peng on 2016/7/21.
 */
public class Lessons extends ArrayList<Lesson> {
    public Lessons(){super();}
    public Lessons(int initialCapacity){super(initialCapacity);}
    public Lessons(Collection<? extends Lesson> lessons){super(lessons);}

    @Override
    public String toString(){
        StringBuilder sb=new StringBuilder();
        this.forEach(lesson -> sb.append(lesson.toString()).append("\n"));
        return sb.toString();
    }
}
