package jwbCrawler.Data;

import java.util.Iterator;
import java.util.List;

/**
 * Created by peng on 2016/7/21.
 */
public class Lesson {
    private String lessonCode;
    private String lessonName;
    private List<String> teachers;
    private String semester;
    private List<Attend> attends;

    public Lesson(){
        lessonCode=lessonName=semester="";
        teachers=null;
        attends=null;
    }

    public Lesson(String lessonCode, String lessonName, List<String> teachers, String semester, List<Attend> attends){
        set(lessonCode, lessonName, teachers, semester, attends);
    }

    public Lesson set(String lessonCode, String lessonName, List<String> teachers, String semester, List<Attend> attends){
        this.lessonCode=lessonCode;
        this.lessonName=lessonName;
        this.teachers=teachers;
        this.semester=semester;
        this.attends=attends;

        return this;
    }

    public Lesson setLessonCode(String lessonCode){
        this.lessonCode=lessonCode;
        return this;
    }
    public Lesson setLessonName(String lessonName){
        this.lessonName=lessonName;
        return this;
    }
    public Lesson setTeachers(List<String> teachers){
        this.teachers=teachers;
        return this;
    }
    public Lesson addTeacher(String teacher){
        this.teachers.add(teacher);
        return this;
    }
    public Lesson setSemester(String semester){
        this.semester=semester;
        return this;
    }
    public Lesson setAttends(List<Attend> attends){
        this.attends=attends;
        return this;
    }
    public Lesson addAttend(Attend attend){
        this.attends.add(attend);
        return this;
    }
    public String getLessonCode(){return lessonCode;}
    public String getLessonName(){return lessonName;}
    public List<String> getTeachers(){return teachers;}
    public String getSemester(){return semester;}
    public List<Attend> getAttends(){return attends;}

    @Override
    public String toString(){
        StringBuilder sb=new StringBuilder();

        sb.append(lessonCode)
                .append("\t")
                .append(lessonName);

        Iterator<String> iterator=teachers.iterator();
        if(iterator.hasNext()) {
            do {
                sb.append(iterator.next());
            } while (iterator.hasNext() && sb.append("/") != null);
        }

        sb.append("\t")
                .append(semester)
                .append("\t");

        Iterator<Attend> iterator1=attends.iterator();
        if(iterator1.hasNext()) {
            do {
                sb.append(iterator1.next().getAttendTime());
            } while (iterator1.hasNext() && sb.append("/") != null);
            sb.append("\t");
        }
        iterator1=attends.iterator();
        if(iterator1.hasNext()) {
            do {
                sb.append(iterator1.next().getLocation());
            } while (iterator1.hasNext() && sb.append("/") != null);
        }

        return sb.toString();
    }
}
