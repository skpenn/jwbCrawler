package jwbCrawler.Data;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by peng on 2016/7/23.
 */
public class Grades extends ArrayList<Grade> {
    public Grades(){super();}
    public Grades(int initialCapacity){super(initialCapacity);}
    public Grades(Collection<? extends Grade> e){super(e);}

    public String toString(){
        StringBuilder stringBuilder=new StringBuilder();
        this.forEach(grade -> stringBuilder.append(grade.toString()).append("\n"));
        return stringBuilder.toString();
    }
}
