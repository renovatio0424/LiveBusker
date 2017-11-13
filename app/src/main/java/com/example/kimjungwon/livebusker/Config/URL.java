package com.example.kimjungwon.livebusker.Config;

/**
 * Created by kimjungwon on 2017-09-06.
 */

public class URL {
    public static String Server_IP         = "52.78.246.168";
    public static int    Chat_Port         = 8889;
    public static String RTMP_Addr         = "rtmp://" + Server_IP + "/live/";
    public static String HLS_Addr          = "http://" + Server_IP + "/hls/";
    public static String DASH_Addr         = "http://" + Server_IP + "/dash/";
    public static String LiveStream_Addr   = "http://" + Server_IP + "/LiveStreaming.php";
    public static String Thumbnail_Addr    = "http://" + Server_IP + "/thumbnail/";
    public static String TMAP_API_URL      = "https://apis.skplanetx.com/";

    public static String YouTube_Thumnail_URL = "https://i.ytimg.com/vi/";
}
