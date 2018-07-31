package com.aboutfuture.twittertest;

import android.content.Intent;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.security.ProviderInstaller;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;

import java.util.List;

import retrofit2.Call;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {

    public static final int WEBPAGE_LOADER_ID = 236;
    public static final String ANONYMOUS = "anonymous";

    private String mUsername;
    private TextToSpeech tts;
    private int languageResult;

    private StatusesService statusesService;

    private String mResultedText;
    private TextView mResultTextView;
    private Button mSendNotificationButton;
    private ImageView mRefreshLastTweetImageView;

    //private TextView mWebcastLinkTextView;
    private Button mSendWebcastLinkButton;
    private ImageView mWebcastPreviewImageview;
    private ImageView mRefreshWecastImageView;
    private EditText mWebcastLinkEditText;

    private ProgressBar mTweetProgress;
    private ProgressBar mWebcastProgress;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMessegesDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mResultTextView = findViewById(R.id.tweet_tv);
        mSendNotificationButton = findViewById(R.id.notification_button);
        //mWebcastLinkTextView = findViewById(R.id.webcast_link_tv);
        mSendWebcastLinkButton = findViewById(R.id.webcast_button);
        mWebcastPreviewImageview = findViewById(R.id.webcast_preview);
        mWebcastLinkEditText = findViewById(R.id.webcast_edit_link);

        mRefreshLastTweetImageView = findViewById(R.id.refresh_last_tweet);
        mRefreshWecastImageView = findViewById(R.id.refresh_webcast_link);

        mTweetProgress = findViewById(R.id.tweet_progress);
        mWebcastProgress = findViewById(R.id.webcast_progress);

        mUsername = ANONYMOUS;

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mMessegesDatabaseReference = mFirebaseDatabase.getReference().child("messages");

        // Some devices have
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 22) {
            upgradeSecurityProvider();
        }

        // BuildConfig.TWITTER_KEY
        Twitter.initialize(this);

        // Get first Tweet from SpaceX timeline
        TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
        statusesService = twitterApiClient.getStatusesService();

        loadTweet();

        mRefreshLastTweetImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadTweet();
            }
        });

        // Send a message with the new status of the launch
        mSendNotificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FriendlyMessage friendlyMessage = new FriendlyMessage(
                        "news", mResultTextView.getText().toString());
                mMessegesDatabaseReference.push().setValue(friendlyMessage);
            }
        });

        // Send an update link for the upcoming mission
        mSendWebcastLinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FriendlyMessage friendlyUpdate = new FriendlyMessage(
                        "updates", mWebcastLinkEditText.getText().toString());
                mMessegesDatabaseReference.push().setValue(friendlyUpdate);
            }
        });

        initLoader();

        mRefreshWecastImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initLoader();
            }
        });
    }

    private void initLoader() {
        mWebcastProgress.setVisibility(View.VISIBLE);
        mWebcastPreviewImageview.setVisibility(View.INVISIBLE);
        mWebcastLinkEditText.setText("");

        getSupportLoaderManager().restartLoader(WEBPAGE_LOADER_ID, null, this);
    }

    private void speak(String textToSpeak) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    private void loadTweet() {
        mResultTextView.setText("");
        mResultTextView.setVisibility(View.INVISIBLE);
        mTweetProgress.setVisibility(View.VISIBLE);

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
                mResultedText = result.data.get(0).text;
                if (mResultedText.contains("http")) {
                    mResultedText = mResultedText.substring(0, mResultedText.indexOf("http")).concat(".");
                }
                mResultTextView.setText(mResultedText.concat("\n\n").concat(result.data.get(0).createdAt));
                mResultTextView.setVisibility(View.VISIBLE);
                mTweetProgress.setVisibility(View.GONE);

                // Text to speech
//                tts = new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {
//                    @Override
//                    public void onInit(int status) {
//                        if (status == TextToSpeech.SUCCESS) {
//                            languageResult = tts.setLanguage(Locale.US);
//                            if (languageResult == TextToSpeech.LANG_MISSING_DATA ||
//                                    languageResult == TextToSpeech.LANG_NOT_SUPPORTED) {
//                                Toast.makeText(getApplicationContext(), "This language is not supported.", Toast.LENGTH_SHORT).show();
//                            } else {
//                                //speak(resultedText);
//                            }
//                        } else {
//                            Toast.makeText(getApplicationContext(), "Feature not supported on your device.", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });

                //if (result.data.text.contains("Liftoff")) {
                //resultTextView.setText("Go go go!");
                //}
            }

            public void failure(TwitterException exception) {
                //Do something on failure
            }
        });
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

    @NonNull
    @Override
    public Loader<String> onCreateLoader(int loaderId, @Nullable Bundle args) {
        switch (loaderId) {
            case WEBPAGE_LOADER_ID:
                // If the loaded id matches webpage loader, return a new webpage loader
                return new WebPageLoader(this);
            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String data) {
        if (data != null) {
            mWebcastProgress.setVisibility(View.GONE);
            mWebcastPreviewImageview.setVisibility(View.VISIBLE);

            final String imageUrl = ImageUtils.buildSdVideoThumbnailUrl(
                    data.substring(data.indexOf("=") + 1, data.length()));

            Picasso.get()
                    .load(imageUrl)
                    .into(mWebcastPreviewImageview, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            // Yay!
                        }

                        @Override
                        public void onError(Exception e) {
                            // Try again online, if cache loading failed
                            Picasso.get()
                                    .load(imageUrl)
                                    .error(R.drawable.video)
                                    .into(mWebcastPreviewImageview);
                        }
                    });

            mWebcastLinkEditText.setText(data);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {
        mWebcastProgress.setVisibility(View.VISIBLE);
        mWebcastPreviewImageview.setVisibility(View.INVISIBLE);
        mWebcastLinkEditText.setText("");
    }

    private void upgradeSecurityProvider() {
        try{
            ProviderInstaller.installIfNeededAsync(this, new ProviderInstaller.ProviderInstallListener() {
                @Override
                public void onProviderInstalled() {
                    Log.e("SpaceXActivity", "New security provider installed.");
                }

                @Override
                public void onProviderInstallFailed(int errorCode, Intent recoveryIntent) {
                    Log.e("SpaceXActivity", "New security provider install failed.");
                }
            });
        }catch (Exception ex){
            Log.e("SpaceXActivity", "Unknown issue trying to install a new security provider", ex);
        }
    }
}
