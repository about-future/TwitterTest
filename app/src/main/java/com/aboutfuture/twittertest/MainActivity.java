package com.aboutfuture.twittertest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

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

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Twitter.initialize(this);

        // List of Tweets in a RecyclerView
//        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//
//        final UserTimeline userTimeline = new UserTimeline.Builder()
//                .screenName("spacex")
//                .maxItemsPerRequest(1)
//                .build();
//
//
//        final TweetTimelineRecyclerViewAdapter adapter =
//                new TweetTimelineRecyclerViewAdapter.Builder(this)
//                        .setTimeline(userTimeline)
//
//                        .build();
//
//        recyclerView.setAdapter(adapter);


        // Get first Tweet from SpaceX timeline
        TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
        StatusesService statusesService = twitterApiClient.getStatusesService();
        Call<List<Tweet>> call = statusesService.userTimeline(
                null,
                "spacex",
                2,
                null,
                null,
                true,
                true,
                false,
                false);

        call.enqueue(new Callback<List<Tweet>>() {
            @Override
            public void success(Result<List<Tweet>> result) {
                TextView resultTextView = findViewById(R.id.tweet_tv);
                resultTextView.setText(result.data.get(0).text + " \n\n " + result.data.get(0).createdAt);
                //if (result.data.text.contains("Liftoff")) {
                    //resultTextView.setText("Go go go!");
                //}
            }

            public void failure(TwitterException exception) {
                //Do something on failure
            }
        });
    }
}
