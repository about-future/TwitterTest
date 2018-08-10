package com.aboutfuture.twittertest;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aboutfuture.twittertest.utils.DateUtils;
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
        holder.tweetTextView.setText(cleanMessage(mResults.data.get(position).text));
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

        ViewHolder(View itemView) {
            super(itemView);
            tweetTextView = itemView.findViewById(R.id.tweet_text_tv);
            tweetDateView = itemView.findViewById(R.id.tweet_date_tv);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mOnClickListener.onItemClickListener(cleanMessage(mResults.data.get(getAdapterPosition()).text));
        }
    }

    private String cleanMessage(String originalText) {
        // If original text contains an arrow, delete it
        if (originalText.contains("\u2192")) {
            originalText = originalText.replace("\u2192", "");
        }

        // If originalText contains one or more links, separate the message from the links
        if (originalText.contains("https://")) {
            String link = originalText.substring(originalText.indexOf("https://"), originalText.length());
            originalText = originalText.replace(link, "");
        }

        return originalText;
    }
}
