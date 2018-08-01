package com.aboutfuture.twittertest;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.AsyncTaskLoader;
import android.text.TextUtils;
import android.util.Log;

import com.aboutfuture.twittertest.retrofit.ApiClient;
import com.aboutfuture.twittertest.retrofit.ApiInterface;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class WebPageLoader extends AsyncTaskLoader<String> {

    public WebPageLoader(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public String loadInBackground() {
        ApiInterface pageApiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<ResponseBody> call = pageApiInterface.getPage();

        try {
            return extractYoutubeLink(call.execute().body().string());
        } catch (IOException | NullPointerException e) {
            Log.v("Page Loader", "Error: " + e.toString());
        }

        return "";
    }

    private static String extractYoutubeLink(String response) {
        String id = "";
        if (response.length() > 0) {
            id = TextUtils.substring(
                    response,
                    response.indexOf("https://www.youtube.com/embed/") + 30,
                    response.indexOf("https://www.youtube.com/embed/") + 41);
        }

        return TextUtils.concat("https://www.youtube.com/watch?v=", id).toString();
    }
}
