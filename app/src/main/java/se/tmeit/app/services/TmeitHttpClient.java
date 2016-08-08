package se.tmeit.app.services;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.security.ProviderInstaller;

import java.io.File;

import okhttp3.Cache;
import okhttp3.OkHttpClient;

/**
 * Static wrapper for the HTTP client. This is in accordance with documented
 * best practices for OkHttpClient, which suggests using the same instance everywhere.
 */
public final class TmeitHttpClient {
	private static final int CACHE_SIZE = 10 * 1024 * 1024;
	private static final String TAG = TmeitHttpClient.class.getSimpleName();
	private static OkHttpClient mInstance;

	private TmeitHttpClient() {
	}

	public static OkHttpClient getInstance() {
		return mInstance;
	}

	public static void initialize(Context context) {
		initializeProvider(context);

		OkHttpClient.Builder builder = new OkHttpClient.Builder();
		Cache cache = initializeCache(context);
		if (null != cache) {
			builder.cache(cache);
		}

		mInstance = builder.build();
	}

	private static Cache initializeCache(Context context) {
		try {
			File cacheDirectory = new File(context.getCacheDir().getAbsolutePath(), "HttpCache");
			Cache cache = new Cache(cacheDirectory, CACHE_SIZE);
			Log.d(TAG, "HTTP response cache was initialized.");
			return cache;
		} catch (Exception ex) {
			Log.w(TAG, "Failed to initialize HTTP response cache.", ex);
			return null;
		}
	}

	private static void initializeProvider(Context context) {
		try {
			ProviderInstaller.installIfNeeded(context);
			Log.d(TAG, "Google Play Services security provider was updated successfully.");
		} catch (Exception ex) {
			Log.w(TAG, "Google Play Services security provider failed to update automatically.", ex);
		}
	}
}
