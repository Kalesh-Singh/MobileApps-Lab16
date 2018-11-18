package com.techexchange.mobileapps.lab16;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ThumbnailDownloader<Token> extends HandlerThread {
    private static final String TAG = "http";
    private static final int MESSAGE_DOWNLOAD = 0;
    private Handler handler;
    Map<Token, URL> requestMap
            = Collections.synchronizedMap(new HashMap<Token, URL>());

    private Handler responsehandler;        // A handler that was created on the main thread.
    Listener<Token> listener;

    public interface Listener<Token> {
        void onThumbnailDownloaded(Token token, Bitmap thumbnail);
    }

    public void setListener(Listener<Token> listener) {
        this.listener = listener;
    }

    public ThumbnailDownloader(Handler responsehandler) {
        super(TAG);
        this.responsehandler = responsehandler;
    }

    @SuppressLint("HandlerLeak")
    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == MESSAGE_DOWNLOAD) {
                    Token token = (Token) msg.obj;
                    Log.i(TAG, "Got a request for url: " + requestMap.get(token));
                    handleRequest(token);
                }
            }
        };
    }

    public void queueThumbnail(Token token, URL url) {
        Log.i(TAG, "Got an URL: " + url.toString());

        requestMap.put(token, url);

        handler.obtainMessage(MESSAGE_DOWNLOAD, token).sendToTarget();

    }

    private void handleRequest(final Token token) {
        final URL url = requestMap.get(token);
        if (url == null) {
            return;
        }
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = connection.getInputStream();
            final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            Log.d(TAG, "Bitmap created");

            responsehandler.post(new Runnable() {
                @Override
                public void run() {
                    if (requestMap.get(token) != url) {
                        return;
                    }
                    requestMap.remove(token);
                    listener.onThumbnailDownloaded(token, bitmap);
                }
            });
        } catch (IOException e) {
            Log.d(MainActivity.TAG, e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public void clearQueue() {
        handler.removeMessages(MESSAGE_DOWNLOAD);
        requestMap.clear();
    }
}
