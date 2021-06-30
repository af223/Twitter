package com.codepath.apps.restclienttemplate.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TimelineActivity;
import com.codepath.apps.restclienttemplate.models.Tweet;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * This adapter is for the Recycler View in TimelineActivity.java, where each view holder within the
 * Recycler View displays a tweet from the user's timeline. For each tweet, the profile picture,
 * screen name, tweet text, and relative time that the text was published is displayed.
 */

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder> {

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    public static String TAG = "TweetAdapter";
    private final Context context;
    private final List<Tweet> tweets;

    public TweetsAdapter(Context context, List<Tweet> tweets) {
        this.context = context;
        this.tweets = tweets;
    }

    @NonNull
    @NotNull
    @Override

    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tweet, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        Tweet tweet = tweets.get(position);
        holder.bind(tweet);
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }

    // get the time that the Tweet was made relative to the current time;
    // time is formatted using shorthand: d for days, h for hours
    public String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        try {
            long time = sf.parse(rawJsonDate).getTime();
            long now = System.currentTimeMillis();

            final long diff = now - time;
            if (diff < MINUTE_MILLIS) {
                return "just now";
            } else if (diff < 2 * MINUTE_MILLIS) {
                return "a minute ago";
            } else if (diff < 50 * MINUTE_MILLIS) {
                return diff / MINUTE_MILLIS + " m";
            } else if (diff < 90 * MINUTE_MILLIS) {
                return "an hour ago";
            } else if (diff < 24 * HOUR_MILLIS) {
                return diff / HOUR_MILLIS + " h";
            } else if (diff < 48 * HOUR_MILLIS) {
                return "yesterday";
            } else if (diff < 8 * DAY_MILLIS) {
                return diff / DAY_MILLIS + " d";
            } else {
                String relativeDate = "";
                relativeDate = DateUtils.getRelativeTimeSpanString(time,
                        System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
                return relativeDate;
            }
        } catch (ParseException e) {
            Toast.makeText(context, "Error: Unable to parse time of tweet", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        return "";
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivProfileImage;
        private final TextView tvBody;
        private final TextView tvScreenName;
        private final TextView tvHandle;
        private final TextView tvTime;
        private final ImageView ivTweetImage;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            tvHandle = itemView.findViewById(R.id.tvHandle);
            tvTime = itemView.findViewById(R.id.tvTime);
            ivTweetImage = itemView.findViewById(R.id.ivTweetImage);
        }

        public void bind(Tweet tweet) {
            tvBody.setText(tweet.body);
            tvScreenName.setText(tweet.user.screenName);
            tvHandle.setText(tweet.user.name);
            tvTime.setText(getRelativeTimeAgo(tweet.createdAt));
            Glide.with(context)
                    .load(tweet.user.profileImageUrl)
                    .circleCrop()
                    .into(ivProfileImage);
            // if the tweet contains an image/photo, then embed it
            if (!tweet.mediaURL.isEmpty()) {
                ivTweetImage.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(tweet.mediaURL)
                        .into(ivTweetImage);
            } else {
                // no image view if no image in tweet
                ivTweetImage.setVisibility(View.GONE);
            }
        }
    }
}
