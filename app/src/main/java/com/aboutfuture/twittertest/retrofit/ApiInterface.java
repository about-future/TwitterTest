package com.aboutfuture.twittertest.retrofit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiInterface {
    @GET("webcast")
    Call<ResponseBody> getPage();
}
