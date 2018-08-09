package com.aboutfuture.twittertest;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.models.Tweet;

import java.util.List;

public class TweetAdapter extends RecyclerView.Adapter<TweetAdapter.ViewHolder> {
    private Result<List<Tweet>> mResults;
    private Context mContext;
    private final ListItemClickListener mOnClickListener;

    public interface ListItemClickListener {
        void onItemClickListener(String tweetText);
    }

    public TweetAdapter(Context context, ListItemClickListener listener) {
        mContext = context;
        mOnClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.tweet_list_item, parent, false);
        view.setFocusable(false);
        return new TweetAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tweetDateView.setText(DateUtils.longDateFormat(mResults.data.get(position).createdAt));
        String[] textAndLink = cleanMessage(mResults.data.get(position).text);
        holder.tweetTextView.setText(cleanMessage(mResults.data.get(position).text)[0]);

//        if (textAndLink.length > 1 && !TextUtils.isEmpty(textAndLink[1])) {
//            holder.tweetImage.setVisibility(View.VISIBLE);
//            Picasso.get()
//                    .load(textAndLink[1])
//                    .into(holder.tweetImage);
//        } else {
//            holder.tweetImage.setVisibility(View.GONE);
//        }
    }

    @Override
    public int getItemCount() {
        return mResults != null ? mResults.data.size() : 0;
    }

    public void setResults(Result<List<Tweet>> results) {
        mResults = results;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements RecyclerView.OnClickListener {
        TextView tweetTextView;
        TextView tweetDateView;
        //ImageView tweetImage;

        ViewHolder(View itemView) {
            super(itemView);
            tweetTextView = itemView.findViewById(R.id.tweet_text_tv);
            tweetDateView = itemView.findViewById(R.id.tweet_date_tv);
            //tweetImage = itemView.findViewById(R.id.tweet_image);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mOnClickListener.onItemClickListener(mResults.data.get(getAdapterPosition()).text);
        }
    }

    private String[] cleanMessage(String originalText) {
        String[] textAndLink = new String[2];

        // If original text contains an arrow, delete it
        if (originalText.contains("\u2192")) {
            originalText = originalText.replace("\u2192", "");
        }

        // If originalText contains one or more links, separate the message from the links and save them
        // in different strings
        String links;
        if (originalText.contains("https://")) {
            links = originalText.substring(originalText.indexOf("https://"), originalText.length());
            textAndLink[0] = originalText.replace(links, "");

            // If there are more than one link, get only the first one and add it to the returned string array
//            if (links.contains(" ")) {
//                textAndLink[1] = links.substring(0, links.indexOf(" "));
//            } else {
//                textAndLink[1] = links;
//            }
        } else {
            textAndLink[0] = originalText;
        }

        return textAndLink;
    }
}
