package jwbCrawler;

import jwbCrawler.Data.*;

import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class Crawler {
    private final static String HOST="jwbinfosys.zju.edu.cn";
    private final static String LOGIN_VIEW_STATE="dDwxNTc0MzA5MTU4Ozs+RGE82+DpWCQpVjFtEpHZ1UJYg8w=";
    private final static String DEFAULT_CHARSET="gb2312";


    private String id=""; //学号
    private String pwd=""; //密码
    private String view_state=""; //用于页面中的状态"__VIEWSTATE"值记录

    String SessionId="";
    List<Cookie> cookies;

    private CloseableHttpClient httpClient;
    private HttpGet httpGet;
    private HttpPost httpPost;
    private URI uri;
    private HttpClientContext context;
    private CookieStore cookieStore;
    private CloseableHttpResponse response;

    private int state_code; //状态码
    private String state=""; //当前状态提示
    private boolean logedin=false; //是否登录

    public Crawler(){
        view_state=LOGIN_VIEW_STATE;
    }

    public Crawler(String id, String pwd){
        this.id=id;
        this.pwd=pwd;
        view_state=LOGIN_VIEW_STATE;
    }

    boolean getCookie(){
        httpClient = HttpClients.createDefault();
        httpGet = new HttpGet();
        try {
            uri = new URIBuilder()
                    .setScheme("http")
                    .setHost(HOST)
                    .setPath("/default2.aspx")
                    .build();
            httpGet.setURI(uri);
            context=new HttpClientContext();
            cookieStore=new BasicCookieStore();
            context.setCookieStore(cookieStore);
            response = httpClient.execute(httpGet, context);
            state_code=response.getStatusLine().getStatusCode();
            httpClient.close();
            if(state_code>=400){
                state="Network Error";
                return false;
            }
            cookies=cookieStore.getCookies();
            cookies.stream().filter(cookie -> cookie.getName().equals("ASP.NET_SessionId")).forEach(cookie -> SessionId = cookie.getValue());
            response.close();
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }


    }

    /**
     * 用于登录验证，获得cookie
     * @return 返回自身
     */
    public Crawler login(){
        if(getCookie()){
            httpClient = HttpClients.createDefault();
            httpPost=new HttpPost();
            try {
                uri = new URIBuilder()
                        .setScheme("http")
                        .setHost(HOST)
                        .setPath("/default2.aspx")
                        .build();
                httpPost.setURI(uri);
                context=new HttpClientContext();
                context.setCookieStore(cookieStore);
                List<NameValuePair> formParams=new ArrayList<>();
                formParams.add(new BasicNameValuePair("TextBox1",id));
                formParams.add(new BasicNameValuePair("TextBox2",pwd));
                formParams.add(new BasicNameValuePair("TextBox3",""));
                formParams.add(new BasicNameValuePair("RadioButtonList1","学生"));
                formParams.add(new BasicNameValuePair("__EVENTTARGET","Button1"));
                formParams.add(new BasicNameValuePair("__EVENTARGUMENT",""));
                formParams.add(new BasicNameValuePair("__VIEWSTATE",view_state));
                formParams.add(new BasicNameValuePair("Text1",""));
                UrlEncodedFormEntity entity=new UrlEncodedFormEntity(formParams, DEFAULT_CHARSET);
                httpPost.setEntity(entity);
                response = httpClient.execute(httpPost, context);
                state_code=response.getStatusLine().getStatusCode();
                if(state_code>=400){
                    state="Network Error";
                    return this;
                }
                String res=EntityUtils.toString(response.getEntity(), DEFAULT_CHARSET).replaceAll("&nbsp;", " ");
                Pattern pattern=Pattern.compile("(?<=^<script language='javascript'>alert\\(')([^']+)(?=')");
                Matcher matcher=pattern.matcher(res);
                if(matcher.find()){
                    state=matcher.group();
                    return this;
                }
                if(res.contains("欢迎您来到现代教务管理系统")||res.contains("?xh="+id)) {
                    state="OK";
                    logedin=true;
                    return this;
                }
                else{
                    state="Unknown";
                    return this;
                }

            }
            catch (Exception e){
                e.printStackTrace();
                return this;
            }
        }
        else{
            return this;
        }
    }

    /**
     * 用于登录验证，获得cookie
     * @param id 用户学号
     * @param pwd 用户密码
     * @return 返回自身
     */
    public Crawler login(String id, String pwd){
        this.id=id;
        this.pwd=pwd;
        return login();
    }

    /**
     * 用于验证是否登录或登录是否成功
     * @return 返回是否登录
     */
    public boolean isLogedin(){return logedin;}

    /**
     * 获取最后一次抓取的服务器返回状态码
     * @return 状态码
     */
    public int getState_code(){return state_code;}

    /**
     * 获取状态提示信息
     * @return 状态提示
     */
    public String getState(){return state;}

    String parseViewState(String path){
        httpClient= HttpClients.createDefault();
        httpGet = new HttpGet();

        try {
            uri = new URIBuilder()
                    .setScheme("HTTP")
                    .setHost(HOST)
                    .setPath(path)
                    .setParameter("xh", id)
                    .build();
            httpGet.setURI(uri);
            context = new HttpClientContext();
            context.setCookieStore(cookieStore);

            response = httpClient.execute(httpGet, context);
            state_code = response.getStatusLine().getStatusCode();
            if (state_code >= 400) {
                state="Network Error";
                return null;
            }
            String str= EntityUtils.toString(response.getEntity(), DEFAULT_CHARSET).replaceAll("&nbsp;", " ");
            httpClient.close();
            response.close();

            Document document=Jsoup.parse(str);
            Elements elements=document.getElementsByAttributeValue("name", "__VIEWSTATE");
            if(elements.size()==0){
                state="Unknown";
                return null;
            }
            view_state=elements.first().val();
            return str;
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    String postForm(String path, List<NameValuePair> formParams){
        httpClient=HttpClients.createDefault();
        httpPost=new HttpPost();
        try{
            uri=new URIBuilder()
                    .setScheme("http")
                    .setHost(HOST)
                    .setPath(path)
                    .addParameter("xh", id)
                    .build();
            httpPost.setURI(uri);
            context = new HttpClientContext();
            context.setCookieStore(cookieStore);
            UrlEncodedFormEntity entity=new UrlEncodedFormEntity(formParams, DEFAULT_CHARSET);
            httpPost.setEntity(entity);
            response = httpClient.execute(httpPost, context);
            state_code=response.getStatusLine().getStatusCode();
            if(state_code>=400){
                state="Network Error";
                return null;
            }
            return EntityUtils.toString(response.getEntity(), DEFAULT_CHARSET).replaceAll("&nbsp;", " ");
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    Lessons parseLesson(String html){
        Document document=Jsoup.parse(html);
        Element table=document.getElementById("xsgrid");
        Elements rows=table.getElementsByTag("tr");
        rows.remove(0);
        Lessons lessons=new Lessons();
        for(Element row:rows){
            Elements elements=row.getElementsByTag("td");
            Element[] tds=new Element[elements.size()];
            elements.toArray(tds);
            String lessonCode=tds[0].text();
            String lessonName=tds[1].text();
            List<String> teachers=Arrays.asList(tds[2].html().replaceAll("</?a[^>]*>", "").split("<br[ ]*/?>"));
            String semester=tds[3].text();
            String[] times=tds[4].html().replaceAll("</?a[^>]*>", "").split("<br[ ]*/?>");
            List<Attend> attends=new ArrayList<>();
            if(times.length>1||!times[0].replaceAll(" ", "").equals("")) {
                for (String time : times) {
                    int week = Attend.WEEK.indexOf(time.substring(0, 2));
                    Pattern pattern = Pattern.compile("[\\d,，]+");
                    List<Integer> classes = new ArrayList<>();
                    Matcher matcher = pattern.matcher(time);
                    if (matcher.find())
                        Arrays.asList(matcher.group().split("[,，]")).forEach(e -> classes.add(Integer.parseInt(e)));
                    String interval = "";
                    pattern = Pattern.compile("(?<=\\{)([^\\}]+)(?=\\})");
                    matcher = pattern.matcher(time);
                    if (matcher.find()) interval = matcher.group();
                    Attend attend = new Attend(interval, week, classes, "");
                    attends.add(attend);
                }
            }
            List<String> locations=Arrays.asList(tds[5].html().replaceAll("</?a[^>]*>", "").split("<br[ ]*/?>"));
            Iterator<String> iterator=locations.iterator();
            if(locations.size()>=attends.size())
                attends.forEach(attend -> attend.setLocation(iterator.next()));
            else
                attends.forEach(attend -> attend.setLocation(locations.get(0)));
            Lesson lesson=new Lesson(lessonCode, lessonName, teachers, semester, attends);
            lessons.add(lesson);
        }

        return lessons;
    }

    Exams parseExam(String html){
        Exams exams=new Exams();

        Document document=Jsoup.parse(html);
        Element table=document.getElementById("DataGrid1");
        if(table==null)return null;
        Elements rows=table.getElementsByTag("tr");
        rows.remove(0);
        for(Element row:rows){
            Elements elements=row.getElementsByTag("td");
            Element[] tds=new Element[elements.size()];
            elements.toArray(tds);
            if(tds[6].text().replaceAll(" ","").equals(""))continue;
            String classCode=tds[0].text();
            String className=tds[1].text();
            double credit=Double.parseDouble(tds[2].text());
            boolean retake=!tds[3].text().replaceAll(" ","").equals("");
            String semester=tds[5].text();
            String[] times=tds[6].text().split("[-\\(\\)]");
            LocalDate examDate= LocalDate.parse(times[0], DateTimeFormatter.ofPattern("yyyy年MM月dd日"));
            LocalTime startTime=LocalTime.parse(times[1], DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime endTime=LocalTime.parse(times[2], DateTimeFormatter.ofPattern("HH:mm"));
            String location=tds[7].text().replaceAll(" ","");
            int seatNum=0;
            String numStr=tds[8].text().replaceAll(" ","");
            if(!numStr.equals(""))seatNum=Integer.parseInt(numStr);
            Exam exam=new Exam(classCode, className, credit, retake, semester, examDate, startTime, endTime, location, seatNum);
            exams.add(exam);
        }
        return exams;
    }

    Grades parseGrade(String html){
        Grades grades=new Grades();

        Document document=Jsoup.parse(html);
        Element table=document.getElementById("DataGrid1");
        Elements rows=table.getElementsByTag("tr");
        rows.remove(0);
        for(Element row:rows){
            Elements elements=row.getElementsByTag("td");
            Element[] tds=new Element[elements.size()];
            elements.toArray(tds);
            String classCode=tds[0].text();
            String className=tds[1].text();
            int gradeNum=Integer.parseInt(tds[2].text().replaceAll(" ",""));
            double credit=Double.parseDouble(tds[3].text().replaceAll(" ", ""));
            double gradePoint=Double.parseDouble(tds[4].text().replaceAll(" ", ""));
            Grade grade=new Grade(classCode, className, gradeNum, credit, gradePoint);
            grades.add(grade);
        }
        return grades;
    }

    /**
     * 获取当前默认显示的课程表
     * @return 课程表
     * @see jwbCrawler.Data.Lessons
     */
    public Lessons getLesson(){
        if(logedin){
            String html=parseViewState("/xskbcx.aspx");
            if(html!=null){
                return parseLesson(html);
            }
            else {
                return null;
            }
        }
        else {
            return null;
        }
    }

    /**
     * 获取任意学期的课程表
     *
     * @param year 学年，格式示例"2016-2017"
     * @param semester 学期，为“1|秋、冬”或“2|春、夏”
     * @return 课程表
     * @see jwbCrawler.Data.Lessons
     */
    public Lessons getLesson(String year, String semester){
        if(logedin){
            String html=parseViewState("/xskbcx.aspx");
            if(html!=null){
                List<NameValuePair> formParams=new ArrayList<>();
                formParams.add(new BasicNameValuePair("kcxx", ""));
                formParams.add(new BasicNameValuePair("xnd", year));
                formParams.add(new BasicNameValuePair("xqd", semester));
                formParams.add(new BasicNameValuePair("xxms", "列表"));
                formParams.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
                formParams.add(new BasicNameValuePair("__EVENTTARGET", "xnd"));
                formParams.add(new BasicNameValuePair("__VIEWSTATE",view_state));
                html=postForm("/xskbcx.aspx", formParams);
                if(html!=null)
                    return parseLesson(html);
            }
        }
        return null;
    }

    /**
     * 获取
     * @return
     */
    public Exams getExam(){
        if(logedin){
            String html=parseViewState("/xskscx.aspx");
            if(html!=null){
                return parseExam(html);
            }
        }
        return null;
    }

    public Exams getExam(String year, String semester){
        if(logedin){
            String html=parseViewState("/xskscx.aspx");
            if(html!=null){
                List<NameValuePair> formParams=new ArrayList<>();
                formParams.add(new BasicNameValuePair("xnd", year));
                formParams.add(new BasicNameValuePair("xqd", semester));
                formParams.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
                formParams.add(new BasicNameValuePair("__EVENTTARGET", "xqd"));
                formParams.add(new BasicNameValuePair("__VIEWSTATE", view_state));
                html=postForm("/xskscx.aspx", formParams);
                if(html!=null)
                    return parseExam(html);
            }
        }
        return null;
    }

    public Grades getAllGrade(){
        if(logedin){
            String html=parseViewState("/xscj.aspx");
            if(html!=null){
                List<NameValuePair> formParams=new ArrayList<>();
                formParams.add(new BasicNameValuePair("ddlXN", ""));
                formParams.add(new BasicNameValuePair("ddlXQ", ""));
                formParams.add(new BasicNameValuePair("txtQSCJ", ""));
                formParams.add(new BasicNameValuePair("txtZZCJ", ""));
                formParams.add(new BasicNameValuePair("Button2", "在校学习成绩查询"));
                formParams.add(new BasicNameValuePair("__VIEWSTATE", view_state));
                html=postForm("/xscj.aspx", formParams);
                if(html!=null){
                    return parseGrade(html);
                }
            }
        }
        return null;
    }

    public static void main(String[] args){
        String id, password;
        System.out.println("教务网爬虫");
        if(args.length<2){
            System.out.print("学号：");
            Scanner scanner=new Scanner(System.in);
            id=scanner.nextLine();
            System.out.print("密码：");
            password=scanner.nextLine();
        }
        else {
            id=args[0];
            password=args[1];
        }
        Crawler crawler=new Crawler(id, password);
        crawler.login();
        if(!crawler.isLogedin()){
            System.out.println("Error:"+crawler.getState());
        }
        else{
            Lessons lessons=crawler.getLesson();
            if(lessons!=null){
                System.out.println("课程表");
                System.out.print(lessons.toString());
            }
            else {
                System.out.println("Error:"+crawler.getState());
            }
            Exams exams=crawler.getExam("2016-2017", "1|秋");
            if(exams!=null){
                System.out.println("考试-秋");
                System.out.print(exams.toString());
            }
            else {
                System.out.println("Error:"+crawler.getState());
            }
            exams=crawler.getExam("2016-2017", "1|冬");
            if(exams!=null){
                System.out.println("考试-冬");
                System.out.print(exams.toString());
            }
            else {
                System.out.println("Error:"+crawler.getState());
            }
            Grades grades=crawler.getAllGrade();
            if(grades!=null){
                System.out.println("成绩");
                System.out.print(grades.toString());
            }
            else {
                System.out.println("Error:"+crawler.getState());
            }
        }

    }
}
