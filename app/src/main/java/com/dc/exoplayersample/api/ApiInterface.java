package com.dc.exoplayersample.api;

import com.dc.exoplayersample.video.VideoResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiInterface {
    @GET("u8vz8")
    Call<VideoResponse> getVideo();
}
