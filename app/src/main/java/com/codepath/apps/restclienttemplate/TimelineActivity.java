package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.codepath.apps.restclienttemplate.adapters.TweetsAdapter;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

/**
 * This activity displays 25 tweets from the user's timeline, allows the user to refresh their timeline
 * by pulling down, and calls ComposeActivity.java if the user wants to publish a Tweet.
 *
 * If the user has already logged in via Twitter and the OAuth token hasn't expired, then the user
 * automatically sees this activity upon opening the app. Otherwise, this activity is started once
 * the user successfully logs in from LoginActivity.java.
 */

public class TimelineActivity extends AppCompatActivity {

    public static final String TAG = "TimelineActivity";
    private static final int REQUEST_CODE = 20;
    public static long max_id;
    private TwitterClient client;
    private RecyclerView rvTweets;
    private List<Tweet> tweets;
    private TweetsAdapter adapter;
    private SwipeRefreshLayout swipeContainer;
    private EndlessRecyclerViewScrollListener scrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_launcher_twitter_round);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        client = TwitterApp.getRestClient(this);

        // pull down to refresh timeline
        swipeContainer = findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchTimelineAsync();
            }
        });
        swipeContainer.setColorSchemeColors(
                getResources().getColor(android.R.color.holo_blue_bright),
                getResources().getColor(android.R.color.holo_green_light),
                getResources().getColor(android.R.color.holo_orange_light),
                getResources().getColor(android.R.color.holo_red_light));

        rvTweets = findViewById(R.id.rvTweets);
        tweets = new ArrayList<>();
        adapter = new TweetsAdapter(this, tweets);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvTweets.setLayoutManager(linearLayoutManager);

        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadNextDataFromApi(page);
            }
        };
        rvTweets.addOnScrollListener(scrollListener);


        rvTweets.setAdapter(adapter);

        rvTweets.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        populateHomeTimeline();

        Log.d("oldest ID", String.valueOf(max_id));
    }

    private void loadNextDataFromApi(int offset) {
        client.loadNextPage(max_id-1, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                try {
                    tweets.addAll(Tweet.fromJsonArray(json.jsonArray));
                    //adapter.notifyItemRangeInserted(tweets.size()-26, 25);
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    Toast.makeText(TimelineActivity.this, "Error: Unable to parse timeline", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Toast.makeText(TimelineActivity.this, "Error: Unable to refresh timeline", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Fetch timeline error: " + throwable.toString());
            }
        });
        Log.d("oldest ID", String.valueOf(max_id));
    }

    // when timeline is refreshed (pulled down), this method will send a new request to Twitter
    // and replace all the old data with the new Twitter response in the adapter
    private void fetchTimelineAsync() {
        max_id = 0;
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                try {
                    tweets.clear();
                    tweets.addAll(Tweet.fromJsonArray(json.jsonArray));
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    Toast.makeText(TimelineActivity.this, "Error: Unable to parse timeline", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
                swipeContainer.setRefreshing(false);
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Toast.makeText(TimelineActivity.this, "Error: Unable to refresh timeline", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Fetch timeline error: " + throwable.toString());
            }
        });
    }

    // format Action Bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // handles when a button from the Action Bar is clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int compose = R.id.compose;
        final int logout = R.id.logout;
        switch (item.getItemId()) {
            case compose:
                // when the edit/compose button is pressed, launches Compose Activity
                Intent intent = new Intent(this, ComposeActivity.class);
                intent.putExtra("ID", "");
                intent.putExtra("screenname", "");
                startActivityForResult(intent, REQUEST_CODE);
                return true;

            case logout:
                // when logout is clicked, user is taken back to login screen
                client.clearAccessToken();
                finish();
                break;
        }
        if (item.getItemId() == R.id.compose) {
            Intent intent = new Intent(this, ComposeActivity.class);
            startActivityForResult(intent, REQUEST_CODE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // if the user successfully published a tweet on the ComposeActivity screen, then the user has
    // been automatically returned back to the start of the timeline screen with the new tweet at
    // the top of the screen
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            Toast.makeText(this, "Tweet sent!", Toast.LENGTH_LONG).show();
            Tweet tweet = Parcels.unwrap(data.getParcelableExtra(String.valueOf(R.string.sent_tweet)));
            tweets.add(0, tweet);
            adapter.notifyItemInserted(0);
            rvTweets.smoothScrollToPosition(0);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // load 25 tweets from the user's Twitter timeline into the RecyclerView on this screen
    private void populateHomeTimeline() {
        max_id = 0;
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                JSONArray jsonArray = json.jsonArray;
                try {
                    tweets.addAll(Tweet.fromJsonArray(jsonArray));
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    Log.e(TAG, "Json exception", e);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Toast.makeText(TimelineActivity.this, "Unable to load timeline", Toast.LENGTH_LONG).show();
                Log.e(TAG, "onFailure" + response, throwable);
            }
        });
    }

}