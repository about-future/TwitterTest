package com.aboutfuture.twittertest;

import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;

public class MainActivity extends AppCompatActivity {

    private TextToSpeech tts;
    private int languageResult;
    private String resultedText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       // BuildConfig.TWITTER_KEY
        Twitter.initialize(this);

        // Get first Tweet from SpaceX timeline
        TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
        StatusesService statusesService = twitterApiClient.getStatusesService();
        Call<List<Tweet>> call = statusesService.userTimeline(
                null,
                "spacex",
                10,
                null,
                null,
                true,
                true,
                false,
                false);

        call.enqueue(new Callback<List<Tweet>>() {
            @Override
            public void success(final Result<List<Tweet>> result) {
                TextView resultTextView = findViewById(R.id.tweet_tv);

                resultedText = result.data.get(0).text;
                if (resultedText.contains("http")) {
                    resultedText = resultedText.substring(0, resultedText.indexOf("http")).concat(".");
                }
                resultTextView.setText(resultedText.concat("\n\n").concat(result.data.get(0).createdAt));

                tts = new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if (status == TextToSpeech.SUCCESS) {
                            languageResult = tts.setLanguage(Locale.US);
                            if (languageResult == TextToSpeech.LANG_MISSING_DATA ||
                                    languageResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                                Toast.makeText(getApplicationContext(), "This language is not supported.", Toast.LENGTH_SHORT).show();
                            } else {
                                speak(resultedText);
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Feature not supported on your device.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                //if (result.data.text.contains("Liftoff")) {
                    //resultTextView.setText("Go go go!");
                //}
            }

            public void failure(TwitterException exception) {
                //Do something on failure
            }
        });
    }

    private void speak(String textToSpeak) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    @Override
    protected void onPause() {
        if (tts != null) {
            tts.stop();
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        super.onDestroy();
    }
}
