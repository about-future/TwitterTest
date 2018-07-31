package com.aboutfuture.twittertest.retrofit;

import retrofit2.Retrofit;

public class ApiClient {
    private static final String BASE_URL = "http://www.spacex.com/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .build();
        }
        return retrofit;
    }
}
