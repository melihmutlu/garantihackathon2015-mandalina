package com.example.melih.mandalina;


import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.mime.HttpMultipartMode;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.entity.mime.content.FileBody;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;

/**
 * Created by erkam on 21.11.2015.
 */
public class CloudSight {

    private static CloudSight cs;
    private File file;
    public static CloudSight getInstance(){
        if(cs==null)
            return new CloudSight();
        else
            return cs;
    }
    public JSONObject askResult(String token){
        JSONObject obj=null;
        String responseBody;
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet("https://api.cloudsightapi.com/image_responses/"+token);
        httpget.addHeader("Authorization", "CloudSight E81djyGM5pUSrh0FQii70w");
        try {
            responseBody = EntityUtils.toString(httpclient.execute(httpget).getEntity(), "UTF-8");

            obj = new JSONObject(responseBody);
            Log.d("INFO", obj.toString());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            httpclient.getConnectionManager().shutdown();
        }
        return obj;
    }
    public JSONObject postImage(File file){
        this.file = file;
        JSONObject object=null;
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet("https://api.cloudsightapi.com/image_responses/j3MUiPig2O-mMi8AayoYzg");
        HttpPost httppost = new HttpPost("https://api.cloudsightapi.com/image_requests");
        httppost.addHeader("Authorization", "CloudSight E81djyGM5pUSrh0FQii70w");
        JSONObject json=null;

        try {
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();

            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            FileBody fileBody = new FileBody(file,"image/jpeg"); //image should be a String
            builder.addBinaryBody("image_request[image]", file, ContentType.MULTIPART_FORM_DATA, file.getName());
            builder.addTextBody("image_request[language]", "tr-TR");
            builder.addTextBody("image_request[locale]","tr-TR");
            httppost.setEntity(builder.build());
           /* // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("image_request[remote_image_url]", "12345"));
            nameValuePairs.add(new BasicNameValuePair("image_request[language]", "tr-TR"));
            nameValuePairs.add(new BasicNameValuePair("image_request[locale]", "tr-TR"));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
*/
            String responseBody;

            try {
                responseBody = EntityUtils.toString(httpclient.execute(httppost).getEntity(), "UTF-8");

                 object = new JSONObject(responseBody);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                httpclient.getConnectionManager().shutdown();
            }


        } catch (Exception e) {
            // TODO Auto-generated catch block
        }
        return object;
    }
    private String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

}
