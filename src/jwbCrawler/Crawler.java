package jwbCrawler;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import jwbCrawler.Data.*;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
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

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by peng on 2016/7/20.
 */

public class Crawler {
    private final static String HOST="jwbinfosys.zju.edu.cn";
    private final static String LOGIN_VIEW_STATE="dDwxNTc0MzA5MTU4Ozs+RGE82+DpWCQpVjFtEpHZ1UJYg8w=";
    private final static String DEFAULT_CHARSET="gb2312";

    private String id="";
    private String pwd="";
    private String view_state="";

    String SessionId="";
    List<Cookie> cookies;

    private CloseableHttpClient httpClient;
    private HttpGet httpGet;
    private HttpPost httpPost;
    private URI uri;
    private HttpClientContext context;
    private CookieStore cookieStore;
    private CloseableHttpResponse response;

    private int state_code;
    private String state="";
    private boolean logedin=false;

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
     *
     * @return
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

    public Crawler login(String id, String pwd){
        this.id=id;
        this.pwd=pwd;
        return login();
    }

    public boolean isLogedin(){return logedin;}

    public int getState_code(){return state_code;}

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

    Lessons parserLesson(String html){
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
            attends.forEach(attend -> attend.setLocation(iterator.next()));
            Lesson lesson=new Lesson(lessonCode, lessonName, teachers, semester, attends);
            lessons.add(lesson);
        }

        return lessons;
    }

    Exams parserExam(String html){
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

    public Lessons getLesson(){
        if(logedin){
            String html=parseViewState("/xskbcx.aspx");
            if(html!=null){
                return parserLesson(html);
            }
            else {
                return null;
            }
        }
        else {
            return null;
        }
    }

    public Exams getExam(){
        if(logedin){
            String html=parseViewState("/xskscx.aspx");
            if(html!=null){
                return parserExam(html);
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
                return parserExam(html);
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
        }


    }
}
