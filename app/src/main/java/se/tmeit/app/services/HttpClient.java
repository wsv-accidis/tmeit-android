package se.tmeit.app.services;

import android.util.Log;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;


/**
 * Singleton wrapper for the HTTP client. This is in accordance with documented
 * best practices for OkHttpClient, which suggests using the same instance everywhere.
 */
public final class HttpClient {
    private static final String TAG = HttpClient.class.getSimpleName();
    private static final OkHttpClient mInstance = new OkHttpClient();

    private HttpClient() {
    }

    static {
        String sslSocketFactory = mInstance.getSslSocketFactory().getClass().getName();
        Log.d(TAG, "SSL socket factory in use is " + sslSocketFactory);
    }

    public static Call enqueueRequest(Request request, Callback callback) {
        Call call = mInstance.newCall(request);
        call.enqueue(callback);
        return call;
    }
}
