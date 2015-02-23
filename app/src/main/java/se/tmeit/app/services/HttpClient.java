package se.tmeit.app.services;

import android.util.Log;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import javax.net.ssl.HttpsURLConnection;

/**
 * Static wrapper for the HTTP client. This is in accordance with documented
 * best practices for OkHttpClient, which suggests using the same instance everywhere.
 */
public final class HttpClient {
    private static final String TAG = HttpClient.class.getSimpleName();
    private static final OkHttpClient mInstance;

    private HttpClient() {
    }

    static {
        mInstance = new OkHttpClient();
        mInstance.setSslSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory());
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
}
