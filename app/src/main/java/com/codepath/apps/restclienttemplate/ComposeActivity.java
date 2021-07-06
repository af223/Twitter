package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.codepath.apps.restclienttemplate.databinding.ActivityComposeBinding;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.network.TwitterApp;
import com.codepath.apps.restclienttemplate.network.TwitterClient;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;

/**
 * This activity allows the user to write a message, then publish it onto Twitter. Once the tweet
 * has been published on Twitter, this activity should automatically finish, and the user should be
 * returned to the timeline (TimelineActivity.java) with the new tweet at the top of the page.
 *
 * This activity appears when the user has clicked the "Compose" icon in the Action Bar on the top
 * right. It's started from TimelineActivity.java.
 */

public class ComposeActivity extends AppCompatActivity {

    private static final String TAG = "ComposeActivity";
    private static final int MAX_TWEET_LENGTH = 280;
    private String inReplyTo;
    private String inReplyToID;
    private EditText etCompose;
    private Button btnTweet;
    private TwitterClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityComposeBinding binding = ActivityComposeBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_launcher_twitter_round);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        client = TwitterApp.getRestClient(this);

        etCompose = binding.etCompose;
        btnTweet = binding.btnTweet;

        // if tweet is written in reply, then the screen name of the user was passed in from intent
        // screen name is autofilled into compose
        inReplyTo = getIntent().getStringExtra(String.valueOf(R.string.screen_name));
        inReplyToID = getIntent().getStringExtra(String.valueOf(R.string.id));
        if (!inReplyTo.isEmpty()) {
            etCompose.setText(inReplyTo);
        }
        btnTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String tweetContent = etCompose.getText().toString();
                if (tweetContent.isEmpty()) {
                    Toast.makeText(ComposeActivity.this, "Sorry, your tweet cannot be empty", Toast.LENGTH_LONG).show();
                    return;
                }
                if (tweetContent.length() > MAX_TWEET_LENGTH) {
                    Toast.makeText(ComposeActivity.this, "Sorry, your tweet is too long", Toast.LENGTH_LONG).show();
                    return;
                }
                client.publishTweet(tweetContent, inReplyToID, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        try {
                            Tweet tweet = Tweet.fromJson(json.jsonObject);
                            // send message (the tweet) back to parent (TimelineActivity) that tweet was published on Twitter
                            Intent intent = new Intent();
                            intent.putExtra(String.valueOf(R.string.sent_tweet), Parcels.wrap(tweet));
                            setResult(RESULT_OK, intent);
                            finish();
                        } catch (JSONException e) {
                            Toast.makeText(ComposeActivity.this, "Error: Tweet not parsed", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Toast.makeText(ComposeActivity.this, "Error: Tweet not published", Toast.LENGTH_LONG).show();
                        Log.e(TAG, "onFailure to publish tweet: " + response, throwable);
                    }
                });
            }
        });
    }
}