package com.codepath.apps.restclienttemplate.models;

import android.util.Log;

import com.codepath.apps.restclienttemplate.TimelineActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

// All exceptions thrown by methods in this class are handled by the caller
@Parcel
public class Tweet {

    public String body;
    public String createdAt;
    public User user;
    public String mediaURL;
    public long ID;
    public int RTCount;
    public int likeCount;

    // empty constructor for Parceler Library
    public Tweet() {}

    public static Tweet fromJson(JSONObject jsonObject) throws JSONException {
        Tweet tweet = new Tweet();
        tweet.body = jsonObject.getString("text");
        tweet.createdAt = jsonObject.getString("created_at");
        tweet.user = User.fromJson(jsonObject.getJSONObject("user"));
        tweet.ID = jsonObject.getLong("id");
        tweet.RTCount = jsonObject.getInt("retweet_count");
        tweet.likeCount = jsonObject.getInt("favorite_count");
        TimelineActivity.max_id = tweet.ID;
        Log.d("TweetID", String.valueOf(tweet.ID));

        // checks if tweet contains a photo. If there is one, sets that URL. Empty URL means no photo
        if (jsonObject.getJSONObject("entities").has("media")) {
            JSONArray media = jsonObject.getJSONObject("extended_entities").getJSONArray("media");
            int i = 0;
            while (i < media.length()) {
                if (media.getJSONObject(i).getString("type").equals("photo")) {
                    tweet.mediaURL = media.getJSONObject(i).getString("media_url_https");
                    return tweet;
                }
                i++;
            }
        }
        tweet.mediaURL = "";
        return tweet;
    }

    public static List<Tweet> fromJsonArray(JSONArray jsonArray) throws JSONException {
        List<Tweet> tweets = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            tweets.add(fromJson(jsonArray.getJSONObject(i)));
        }
        return tweets;
    }
}
