package com.nhnent.exam.netlibrary;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.LruCache;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by gradler on 18/08/2017.
 */

public class ImageLoader {

    private final LruCache<String, Bitmap> mMemCache;

    public ImageLoader() {
        int cacheSize = ((int) (Runtime.getRuntime().maxMemory() / 1024)) / 8;
        mMemCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                // The cache size will be measured in kilobytes rather than number of items.
                return (value.getRowBytes() * value.getHeight()) / 1024;
            }

            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                if (oldValue != null && !oldValue.isRecycled()) {
                    oldValue.recycle();
                }
            }
        };
    }

    public void loadThumbnail(Context context, String url, OnResultListener listener) {
        new BitmapLoadThread(context, url, listener).start();
    }

    public void clear() {
        synchronized (mMemCache) {
            mMemCache.evictAll();
        }
    }

    public interface OnResultListener {
        void onResult(Bitmap bitmap);
    }

    /*
     * An InputStream that skips the exact number of bytes provided, unless it reaches EOF.
     */
    private static class FlushedInputStream extends FilterInputStream {
        public FlushedInputStream(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public long skip(long n) throws IOException {
            long totalBytesSkipped = 0L;
            while (totalBytesSkipped < n) {
                long bytesSkipped = in.skip(n - totalBytesSkipped);
                if (bytesSkipped == 0L) {
                    int b = read();
                    if (b < 0) {
                        break;  // we reached EOF
                    } else {
                        bytesSkipped = 1; // we read one byte
                    }
                }

                totalBytesSkipped += bytesSkipped;
            }
            return totalBytesSkipped;
        }
    }

    private void addBitmapToCache(String key, Bitmap bitmap) {
        synchronized (mMemCache) {
            if (getBitmapFromCache(key) == null) {
                mMemCache.put(key, bitmap);
            }
        }
    }

    private Bitmap getBitmapFromCache(String key) {
        synchronized (mMemCache) {
            return mMemCache.get(key);
        }
    }

    private void clear(String key) {
        synchronized (mMemCache) {
            mMemCache.remove(key);
        }
    }

    private Bitmap loadRemoteImage(String url) {
        try {
            URL reqUrl = new URL(url);

            HttpURLConnection conn = (HttpURLConnection) reqUrl.openConnection();
            int responseCode = conn.getResponseCode();
            System.out.println("[loadRemoteImage] responseCode: " + responseCode);
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return null;
            }

            InputStream inputStream = conn.getInputStream();
            if (conn.getInputStream() != null) {
                try {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 1;
                    return BitmapFactory.decodeStream(new FlushedInputStream(inputStream), null, options);

                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    class BitmapLoadThread extends Thread {
        final private String path;
        final private OnResultListener listener;
        final private Context context;

        public BitmapLoadThread(Context context, String path, OnResultListener listener) {
            this.context = context;
            this.path = path;
            this.listener = listener;
        }

        @Override
        public void run() {
            super.run();
            Bitmap bitmap = getBitmapFromCache(path);
            if (bitmap == null) {
                bitmap = loadRemoteImage(path);
            }

            if (bitmap != null) {
                addBitmapToCache(path, bitmap);
            }

            if (context != null) {
                final Bitmap finalBitmap = bitmap;
                new Handler(context.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onResult(finalBitmap);
                    }
                });
            } else {
                listener.onResult(bitmap);
            }
        }
    }
}