package jwbCrawler.Data;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by peng on 2016/7/22.
 */
public class Exams extends ArrayList<Exam>{
    public Exams(){super();}
    public Exams(int initialCapacity){super(initialCapacity);}
    public Exams(Collection<? extends Exam> exams){super(exams);}

    @Override
    public String toString(){
        StringBuilder sb=new StringBuilder();
        this.forEach(exam -> sb.append(exam.toString()).append("\n"));
        return sb.toString();
    }
}
