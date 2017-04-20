package yuan.ocean.Util;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * use http to post information and get SOS service
 * Created by Yuan on 2016/4/20.
 */
public class HttpRequestAndPost {
    /**
     * use http to get information
     * @param url the sos url
     * @param param the post xml
     * @return the return result xml
     */
    public static String sendPost(String url,String param){
        String result="";
        BufferedReader in=null;
        PrintWriter out=null;
        //http post
        try {
            URL realURL=new URL(url);//new URL
            URLConnection connection=realURL.openConnection();
            connection.setRequestProperty("accept","*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0(compatible;MSIE 6.0;Windows NT 5.1;SV1)");

            connection.setDoOutput(true);
            connection.setDoInput(true);

            //write xml into post stream
            out=new PrintWriter(new OutputStreamWriter(connection.getOutputStream(),"gb2312"));
            out.print(param);
            out.flush();//flush the operation

            //use bufferreader to read the returned result
            in=new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));
            String line;
            while ((line=in.readLine())!=null){
                result+=line;
            }
            String encode=connection.getContentEncoding();
            String contentType=connection.getContentType();
            return result;
        } catch (MalformedURLException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return  result;
    }
}
