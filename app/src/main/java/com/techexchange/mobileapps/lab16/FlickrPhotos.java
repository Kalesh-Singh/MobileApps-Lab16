package com.techexchange.mobileapps.lab16;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FlickrPhotos implements URLDownloadListener {

    private static final String API_KEY = "b8f6b20808803c132201a71552dd4b82";
    private static final String QUERY = Uri.parse("https://api.flickr.com/services/rest")
            .buildUpon()
            .appendQueryParameter("method", "flickr.photos.getRecent")
            .appendQueryParameter("api_key", API_KEY)
            .appendQueryParameter("format", "json")
            .appendQueryParameter("nojsoncallback", "1")
            .appendQueryParameter("extras", "url_s")
            .build()
            .toString();
    private final FetchURLsTask fetchURLsTask;
    private final URLParseListener urlParseListener;
    private List<URL> photosURLs;

    public FlickrPhotos(URLParseListener urlParseListener) {
        this.urlParseListener = urlParseListener;
        this.fetchURLsTask = new FetchURLsTask(this);
    }

    public void getPhotosURLs() {
        this.fetchURLsTask.execute(QUERY);
    }

    @Override
    public void onComplete(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            JSONObject photosObject = jsonResponse.getJSONObject("photos");
            JSONArray photosArray = photosObject.getJSONArray("photo");
            List<URL> photosURLs = new ArrayList<>();
            for (int i = 0; i < photosArray.length(); ++i) {
                JSONObject photoObject = photosArray.getJSONObject(i);
                if (photoObject.has("url_s")) {
                    String photoURLString = photoObject.getString("url_s");
                    URL photoURL = new URL(Uri.parse(photoURLString).toString());
                    photosURLs.add(photoURL);
                }
            }
            this.photosURLs = photosURLs;

            // Pass result back to main activity.
            urlParseListener.onComplete(photosURLs);

        } catch (JSONException e) {
            Log.d(MainActivity.TAG, e.getMessage());
        } catch (MalformedURLException e) {
            Log.d(MainActivity.TAG, e.getMessage());
        }
    }
}
