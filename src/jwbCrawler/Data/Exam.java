package jwbCrawler.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by peng on 2016/7/22.
 */
public class Exam {
    private String classCode;
    private String className;
    private double credit;
    private boolean retake=false;
    private String semester;
    private LocalDate examDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String location;
    private int seatNum;

    public Exam(){}
    public Exam(String classCode, String className, double credit, boolean retake, String semester, LocalDate examDate, LocalTime startTime, LocalTime endTime, String location, int seatNum){
        set(classCode, className, credit, retake, semester, examDate, startTime, endTime, location, seatNum);
    }

    public Exam set(String classCode, String className, double credit, boolean retake, String semester, LocalDate examDate, LocalTime startTime, LocalTime endTime, String location, int seatNum){
        this.classCode=classCode;
        this.className=className;
        this.credit=credit;
        this.retake=retake;
        this.semester=semester;
        this.examDate=examDate;
        this.startTime=startTime;
        this.endTime=endTime;
        this.location=location;
        this.seatNum=seatNum;
        return this;
    }

    public Exam setClassCode(String classCode){
        this.classCode=classCode;
        return this;
    }

    public Exam setClassName(String className){
        this.className=className;
        return this;
    }

    public Exam setCredit(double credit){
        this.credit=credit;
        return this;
    }

    public Exam setRetake(boolean retake){
        this.retake=retake;
        return this;
    }

    public Exam setSemester(String semester){
        this.semester=semester;
        return this;
    }

    public Exam setExamDate(LocalDate examDate){
        this.examDate=examDate;
        return this;
    }

    public Exam setStartTime(LocalTime startTime){
        this.startTime=startTime;
        return this;
    }

    public Exam setEndTime(LocalTime endTime){
        this.endTime=endTime;
        return this;
    }

    public Exam setLocation(String location){
        this.location=location;
        return this;
    }

    public Exam setSeatNum(int seatNum){
        this.seatNum=seatNum;
        return this;
    }

    public String getClassCode(){return classCode;}
    public String getClassName(){return className;}
    public String getSemester(){return semester;}
    public double getCredit(){return credit;}
    public boolean isRetake(){return retake;}

    public LocalDate getExamDate() {
        return examDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public String getLocation() {
        return location;
    }

    public int getSeatNum() {
        return seatNum;
    }

    @Override
    public String toString(){
        return classCode
                +"\t"
                +className
                +"\t"
                +Double.toString(credit)
                +"\t"
                +semester
                +"\t"
                +examDate.format(DateTimeFormatter.ofPattern("yyyy年M月d日"))
                +startTime.format(DateTimeFormatter.ofPattern("HH:mm"))
                +"-"
                +endTime.format(DateTimeFormatter.ofPattern("HH:mm"))
                +"\t"
                +location;
    }
}
