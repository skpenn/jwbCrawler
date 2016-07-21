package jwbCrawler;

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

import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by peng on 2016/7/20.
 */

public class Crawler {
    private final static String HOST="jwbinfosys.zju.edu.cn";
    private final static String LOGIN_VIEW_STATE="dDwxNTc0MzA5MTU4Ozs+RGE82+DpWCQpVjFtEpHZ1UJYg8w=";
    private final static String DEFAULT_CHARSET="gb2312";

    String id="";
    String pwd="";
    private String view_state="";
    private String SessionId="";
    List<Cookie> cookies;

    private int state_code;

    public Crawler(){
        view_state=LOGIN_VIEW_STATE;
    }

    public Crawler(String id, String pwd){
        this.id=id;
        this.pwd=pwd;
        view_state=LOGIN_VIEW_STATE;
    }

    boolean getCookie(){
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet();
        try {
            URI uri = new URIBuilder().setScheme("http").setHost(HOST).setPath("/default2.aspx").build();
            httpGet.setURI(uri);
            HttpClientContext context=new HttpClientContext();
            CookieStore cookieStore=new BasicCookieStore();
            context.setCookieStore(cookieStore);
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet, context);
            state_code=httpResponse.getStatusLine().getStatusCode();
            httpClient.close();
            if(state_code>=400){
                System.out.println("State Code:"+state_code);
                return false;
            }
            cookies=cookieStore.getCookies();
            cookies.stream().filter(cookie -> cookie.getName().equals("ASP.NET_SessionId")).forEach(cookie -> SessionId = cookie.getValue());
            httpResponse.close();
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }


    }

    public boolean login(){
        if(getCookie()){
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost=new HttpPost();
            try {
                URI uri = new URIBuilder().setScheme("http").setHost(HOST).setPath("/default2.aspx").build();
                httpPost.setURI(uri);
                HttpClientContext context = new HttpClientContext();
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
                CookieStore cookieStore = new BasicCookieStore();
                cookies.forEach(cookieStore::addCookie);
                context.setCookieStore(cookieStore);
                CloseableHttpResponse httpResponse = httpClient.execute(httpPost, context);
                state_code=httpResponse.getStatusLine().getStatusCode();
                if(state_code>=400){
                    System.out.println("State Code:"+state_code);
                    return false;
                }
                BufferedReader br=new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), DEFAULT_CHARSET));
                char[] buf=new char[1024];
                StringBuilder sb=new StringBuilder();
                while(br.read(buf)>=0){
                    sb.append(buf);
                }
                String res=sb.toString();
                System.out.println(res.substring(0, 1024));
                Pattern pattern=Pattern.compile("(?<=^<script language='javascript'>alert\\(')([^']+)(?=')");
                Matcher matcher=pattern.matcher(res);
                if(matcher.find()){
                    System.out.println(matcher.group());
                    return false;
                }
                if(res.contains("欢迎您来到现代教务管理系统")||res.contains("?xh="+id)) {
                    return true;
                }
                else{
                    System.out.println("Unknown");
                    return false;
                }

            }
            catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }
        else{
            return false;
        }
    }

    public boolean login(String id, String pwd){
        this.id=id;
        this.pwd=pwd;
        return login();
    }

    public boolean getLesson(){
        CloseableHttpClient httpClient= HttpClients.createDefault();
        HttpGet httpGet = new HttpGet();
        try{
            URI uri=new URIBuilder().setScheme("HTTP").setHost(HOST).setPath("/xskbcx.aspx").setParameter("xh", id).build();
            httpGet.setURI(uri);
            HttpClientContext context=new HttpClientContext();
            CookieStore cookieStore=new BasicCookieStore();
            cookies.forEach(cookieStore::addCookie);
            context.setCookieStore(cookieStore);
            CloseableHttpResponse response=httpClient.execute(httpGet,context);
            state_code=response.getStatusLine().getStatusCode();
            if(state_code>=400){
                System.out.println("State Code:"+state_code);
                return false;
            }
            OutputStreamWriter osw= new OutputStreamWriter(new FileOutputStream("test.html"));
            InputStreamReader isr=new InputStreamReader(response.getEntity().getContent(), DEFAULT_CHARSET);
            char[] buf=new char[1024];
            while(isr.read(buf)>=0)osw.write(buf);
            httpClient.close();
            osw.close();
            response.close();
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args){
        Crawler crawler=new Crawler("3130******", "******");
        if(crawler.login()&&crawler.getLesson()){
            System.out.println("OK!");
        }
    }
}
