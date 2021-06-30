package com.codepath.apps.restclienttemplate.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

@Parcel
public class Tweet {

    public String body;
    public String createdAt;
    public User user;
    public String mediaURL;

    // empty constructor for Parceler Library
    public Tweet() {}

    public static Tweet fromJson(JSONObject jsonObject) throws JSONException {
        Tweet tweet = new Tweet();
        tweet.body = jsonObject.getString("text");
        tweet.createdAt = jsonObject.getString("created_at");
        tweet.user = User.fromJson(jsonObject.getJSONObject("user"));

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
