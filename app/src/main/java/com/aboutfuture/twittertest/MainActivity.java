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
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.aboutfuture.twittertest.utils.ImageUtils;
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
import java.util.Locale;

import retrofit2.Call;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<String>, TweetAdapter.ListItemClickListener {

    public static final int WEBPAGE_LOADER_ID = 236;
    private TextToSpeech tts;
    private int languageResult;

    private StatusesService statusesService;

    private EditText mSelectedEditTextView;
    private ImageView mClearImageView;
    private ImageView mSpeakImageView;
    private RecyclerView mTweetsRecyclerView;
    private TweetAdapter mTweetAdapter;
    private ImageView mWebcastPreviewImageview;
    private EditText mWebcastLinkEditText;
    private ProgressBar mTweetProgress;
    private View mEmptyView;
    private ProgressBar mWebcastProgress;

    private DatabaseReference mMessegesDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTweetsRecyclerView = findViewById(R.id.tweets_rv);
        mSelectedEditTextView = findViewById(R.id.selected_tweet_tv);
        mWebcastPreviewImageview = findViewById(R.id.webcast_preview);
        mWebcastLinkEditText = findViewById(R.id.webcast_edit_link);
        mTweetProgress = findViewById(R.id.tweet_progress);
        mEmptyView = findViewById(R.id.empty_view);
        mWebcastProgress = findViewById(R.id.webcast_progress);
        mSpeakImageView = findViewById(R.id.speak_iv);
        mClearImageView = findViewById(R.id.clear_iv);

        mClearImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSelectedEditTextView.setText("");
                mSpeakImageView.setVisibility(View.INVISIBLE);
                mClearImageView.setVisibility(View.GONE);
            }
        });

        mSelectedEditTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (TextUtils.isEmpty(charSequence)) {
                    mClearImageView.setVisibility(View.GONE);
                    mSpeakImageView.setVisibility(View.INVISIBLE);
                } else {
                    mClearImageView.setVisibility(View.VISIBLE);
                    mSpeakImageView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (TextUtils.isEmpty(editable.toString())) {
                    mClearImageView.setVisibility(View.GONE);
                    mSpeakImageView.setVisibility(View.INVISIBLE);
                } else {
                    mClearImageView.setVisibility(View.VISIBLE);
                    mSpeakImageView.setVisibility(View.VISIBLE);
                }
            }
        });

        mSpeakImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Text to speech
                tts = new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if (status == TextToSpeech.SUCCESS) {
                            languageResult = tts.setLanguage(Locale.US);
                            if (languageResult == TextToSpeech.LANG_MISSING_DATA ||
                                    languageResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                                Toast.makeText(getApplicationContext(), "This language is not supported.", Toast.LENGTH_SHORT).show();
                            } else {
                                if (!TextUtils.isEmpty(mSelectedEditTextView.getText().toString())) {
                                    speak(mSelectedEditTextView.getText().toString());
                                }
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Feature not supported on your device.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        mMessegesDatabaseReference = mFirebaseDatabase.getReference().child("messages");

        // Some devices have SSL protocol activated by default, this method will activate
        // the newer network protocols that are also installed on the device, but not active.
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 22) {
            upgradeSecurityProvider();
        }

        mTweetsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(
                mTweetsRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mTweetsRecyclerView.addItemDecoration(mDividerItemDecoration);
        mTweetsRecyclerView.setHasFixedSize(false);
        mTweetAdapter = new TweetAdapter(this, this);
        mTweetsRecyclerView.setAdapter(mTweetAdapter);

        // BuildConfig.TWITTER_KEY
        Twitter.initialize(this);

        // Get first Tweet from SpaceX timeline
        TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
        statusesService = twitterApiClient.getStatusesService();

        loadTweet();

        // Refresh tweets
        ImageView refreshLastTweetImageView = findViewById(R.id.refresh_last_tweet);
        refreshLastTweetImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadTweet();
            }
        });

        // Send a message with the new status of the launch
        Button sendNotificationButton = findViewById(R.id.notification_button);
        sendNotificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(mSelectedEditTextView.getText().toString())) {
                    FriendlyMessage friendlyMessage = new FriendlyMessage(
                            "news", mSelectedEditTextView.getText().toString());
                    mMessegesDatabaseReference.push().setValue(friendlyMessage);
                    mSelectedEditTextView.setText("");
                    mClearImageView.setVisibility(View.GONE);
                    mSpeakImageView.setVisibility(View.GONE);
                } else {
                    Toast.makeText(MainActivity.this, "No message to send!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Send an update link for the upcoming mission
        Button sendWebcastLinkButton = findViewById(R.id.webcast_button);
        sendWebcastLinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(mWebcastLinkEditText.getText().toString())) {
                    FriendlyMessage friendlyUpdate = new FriendlyMessage(
                            "updates", mWebcastLinkEditText.getText().toString());
                    mMessegesDatabaseReference.push().setValue(friendlyUpdate);
                } else {
                    Toast.makeText(MainActivity.this, "No link available!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        initLoader();

        // Refresh webcast link
        ImageView refreshWecastImageView = findViewById(R.id.refresh_webcast_link);
        refreshWecastImageView.setOnClickListener(new View.OnClickListener() {
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
        mSelectedEditTextView.setText("");
        mSpeakImageView.setVisibility(View.GONE);
        mClearImageView.setVisibility(View.GONE);
        mTweetsRecyclerView.setVisibility(View.GONE);
        mTweetProgress.setVisibility(View.VISIBLE);
        mEmptyView.setVisibility(View.VISIBLE);

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
//                mResultedText = result.data.get(0).text;
//                if (mResultedText.contains("http")) {
//                    mResultedText = mResultedText.substring(0, mResultedText.indexOf("http")).concat(".");
//                }
//                mResultTextView.append("\n\n" + result.data.get(0).createdAt);

                mTweetsRecyclerView.setVisibility(View.VISIBLE);
                mTweetProgress.setVisibility(View.GONE);
                mEmptyView.setVisibility(View.GONE);
                mTweetAdapter.setResults(result);
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
        try {
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
        } catch (Exception ex) {
            Log.e("SpaceXActivity", "Unknown issue trying to install a new security provider", ex);
        }
    }

    @Override
    public void onItemClickListener(String tweetText) {
        mSelectedEditTextView.setText(tweetText);
        mSpeakImageView.setVisibility(View.VISIBLE);
        mClearImageView.setVisibility(View.VISIBLE);

        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}
