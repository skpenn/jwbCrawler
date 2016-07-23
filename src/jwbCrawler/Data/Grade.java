package jwbCrawler.Data;

/**
 * Created by peng on 2016/7/23.
 */
public class Grade {
    private String classCode;
    private String lessonCode;
    private String year;
    private int semesterNum;
    private String className;
    private int grade;
    private Double credit;
    private Double gradePoint;

    public Grade(){}
    public Grade(String classCode, String className, int grade, Double credit, Double gradePoint){
        set(classCode, className, grade, credit, gradePoint);
    }

    public Grade set(String classCode, String className, int grade, Double credit, Double gradePoint){
        this.classCode=classCode;
        this.className=className;
        this.grade=grade;
        this.credit=credit;
        this.gradePoint=gradePoint;
        return parseClassCode(classCode);
    }

    Grade parseClassCode(String classCode){
        String[] strings=classCode.split("\\)-");
        String time=strings[0];
        int index=time.lastIndexOf("-");
        year=time.substring(1, index);
        semesterNum=Integer.parseInt(time.substring(index+1));
        lessonCode=strings[1].substring(0, strings[1].indexOf("-"));
        return this;
    }

    public Grade setClassCode(String classCode) {
        this.classCode = classCode;
        return parseClassCode(classCode);
    }

    public Grade setClassName(String className) {
        this.className = className;
        return this;
    }

    public Grade setGrade(int grade) {
        this.grade = grade;
        return this;
    }

    public Grade setCredit(Double credit) {
        this.credit = credit;
        return this;
    }

    public Grade setGradePoint(Double gradePoint) {
        this.gradePoint = gradePoint;
        return this;
    }

    public String getClassCode() {
        return classCode;
    }

    public String getLessonCode() {
        return lessonCode;
    }

    public String getYear() {
        return year;
    }

    public int getSemesterNum() {
        return semesterNum;
    }

    public String getClassName() {
        return className;
    }

    public int getGrade() {
        return grade;
    }

    public Double getCredit() {
        return credit;
    }

    public Double getGradePoint() {
        return gradePoint;
    }

    public static void main(String[] args){
        Grade grade=new Grade();
        grade.parseClassCode("(2013-2014-1)-011H0020-0085375-4");
        System.out.println(grade.getLessonCode());
        System.out.println(grade.getYear());
        System.out.println(grade.getSemesterNum());
    }

    @Override
    public String toString(){
        return classCode
                +"\t"
                +className
                +"\t"
                +grade
                +"\t"
                +credit
                +"\t"
                +gradePoint;
    }
}
