package com.example.kimjungwon.livebusker.Network;

import android.app.DownloadManager;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by kimjungwon on 2017-09-06.
 */

public class PHPRequest {
    public static int Register_Stream = 1;
    public static int Delete_stream   = 2;
    public static int Select_stream   = 3;



    private URL url;

    public PHPRequest(String url) throws MalformedURLException {
        this.url = new URL(url);
    }

    private static final String TAG = DownloadManager.Request.class.getSimpleName();

    private String readStream(InputStream in) throws IOException {
        StringBuilder jsonHtml = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        String line = null;

        while ((line = reader.readLine()) != null)
            jsonHtml.append(line);

        reader.close();
        return jsonHtml.toString();
    }


    public String POSTJSON(final String json_string) {
        String postData = "json=" + json_string;
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(10000);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(postData.getBytes("UTF-8"));
            outputStream.flush();
            outputStream.close();
            String result = readStream(connection.getInputStream());
            connection.disconnect();
            return result;
        } catch (IOException e) {
            Log.i("PHPRequest", "request was failed");
            e.printStackTrace();
            return null;
        }
    }

}
