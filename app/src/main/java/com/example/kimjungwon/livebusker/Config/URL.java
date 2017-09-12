package com.example.kimjungwon.livebusker.Config;

/**
 * Created by kimjungwon on 2017-09-06.
 */

public class URL {
    public static String Server_IP         = "52.78.246.168";
    public static String RTMP_Addr         = "rtmp://" + Server_IP + "/live/";
    public static String HLS_Addr          = "http://" + Server_IP + "/hls/";
    public static String DASH_Addr         = "http://" + Server_IP + "/dash/";
    public static String LiveStream_Addr   = "http://" + Server_IP + "/LiveStreaming.php";
}
