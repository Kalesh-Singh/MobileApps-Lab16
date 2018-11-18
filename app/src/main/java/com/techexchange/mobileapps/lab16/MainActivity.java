package com.techexchange.mobileapps.lab16;

import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageView;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements URLParseListener {

    static final String TAG = "httptag";
    private List<ImageViewData> imageViewDataList;
    private RecyclerView imageRecyclerView;
    private ImageAdapter imageAdapter;
    private ThumbnailDownloader<ImageView> thumbnailThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TGet the image URLs when the app is started
        FlickrPhotos flickrPhotos = new FlickrPhotos(this);
        flickrPhotos.getPhotosURLs();

        // Set up the background thread
        thumbnailThread = new ThumbnailDownloader<>(new Handler());
        thumbnailThread.setListener(new ThumbnailDownloader.Listener<ImageView>() {
            @Override
            public void onThumbnailDownloaded(ImageView imageView, Bitmap thumbnail) {
                imageView.setImageBitmap(thumbnail);
            }
        });
        thumbnailThread.start();
        thumbnailThread.getLooper();
        Log.d(TAG, "Background thread started");

        imageRecyclerView = findViewById(R.id.image_recycler_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        imageRecyclerView.setLayoutManager(gridLayoutManager);

    }

    @Override
    public void onComplete(List<URL> photosURLs) {
        List<ImageViewData> imageViewDataList = new ArrayList<>();
        for (URL url : photosURLs) {
            imageViewDataList.add(new ImageViewData(url));
        }
        this.imageViewDataList = imageViewDataList;
        this.imageAdapter = new ImageAdapter(this, imageViewDataList, thumbnailThread);
        imageRecyclerView.setAdapter(imageAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        thumbnailThread.clearQueue();
        thumbnailThread.quit();
        Log.d(TAG, "Background thread destroyed");

    }
}
