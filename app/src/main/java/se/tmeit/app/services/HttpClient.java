package se.tmeit.app.services;

import android.content.Context;
import android.util.Log;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

/**
 * Static wrapper for the HTTP client. This is in accordance with documented
 * best practices for OkHttpClient, which suggests using the same instance everywhere.
 */
public final class HttpClient {
    private static final int CACHE_SIZE = 10 * 1024 * 1024;
    private static final String TAG = HttpClient.class.getSimpleName();
    private static final OkHttpClient mInstance;

    private HttpClient() {
    }

    static {
        mInstance = new OkHttpClient();
    }

    public static Call enqueueRequest(Request request, Callback callback) {
        Log.d(TAG, "Queueing request for URL = " + request.urlString());
        Call call = mInstance.newCall(request);
        call.enqueue(callback);
        return call;
    }

    public static Response executeRequest(Request request) throws IOException {
        Log.d(TAG, "Executing request for URL = " + request.urlString());
        return mInstance.newCall(request).execute();
    }

    public static void initializeCache(Context context) {
        try {
            File cacheDirectory = new File(context.getCacheDir().getAbsolutePath(), "HttpCache");
            Cache cache = new Cache(cacheDirectory, CACHE_SIZE);
            mInstance.setCache(cache);
            Log.d(TAG, "HTTP response cache was initialized.");
        } catch (IOException ex) {
            Log.w(TAG, "Failed to initialize HTTP response cache.", ex);
        }
    }

    public static void initializeSslSocketFactory() {
        SSLSocketFactory sslSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
        Log.d(TAG, "SSL socket factory is now " + sslSocketFactory.getClass().getName());
        mInstance.setSslSocketFactory(sslSocketFactory);
    }
}
