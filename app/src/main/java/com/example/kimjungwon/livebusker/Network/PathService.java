package com.example.kimjungwon.livebusker.Network;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;


/**
 * Created by kimjungwon on 2017-10-27.
 */

public interface PathService {
    String API_URL = "https://apis.skplanetx.com/";
    String App_Key = "b6da5114-cc49-3f84-b895-6fef4206cd22";
    @GET("tmap/routes/pedestrian")
    Call<ResponseBody> GetRoute(
            @Query("version") String version,
            @Query("format") String format,
            @Query("appKey") String appkey,
            @Query("startX") String startx,
            @Query("startY") String starty,
            @Query("endX") String endx,
            @Query("endY") String endy,
            @Query("startName") String startname,
            @Query("endName") String endname,
            @Query("resCoordType") String resCoordtype,
            @Query("reqCoordType") String reqCoordtype
    );


    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
}
