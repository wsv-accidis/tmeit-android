package se.tmeit.app.ui.members;

import android.content.Context;
import android.net.Uri;

import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.util.List;
import java.util.Random;

import se.tmeit.app.R;
import se.tmeit.app.services.TmeitHttpClient;
import se.tmeit.app.services.TmeitServiceConfig;

/**
 * Helper class for loading a random face for a member.
 */
public final class MemberFaceHelper {
	private final Picasso mPicasso;
	private final Random mRandom = new Random();
	private static MemberFaceHelper mInstance;

	private MemberFaceHelper(Context context) {
		mPicasso = new Picasso.Builder(context)
			.downloader(new OkHttp3Downloader(TmeitHttpClient.getInstance()))
			.build();
	}

	public synchronized static MemberFaceHelper getInstance(Context context) {
		if (null == mInstance) {
			mInstance = new MemberFaceHelper(context);
		}
		return mInstance;
	}

	public RequestCreator picasso(List<String> faces) {
		String face = faces.get(mRandom.nextInt(faces.size()));
		return mPicasso.load(Uri.parse(TmeitServiceConfig.ROOT_URL).buildUpon().path(face).build());
	}

	public RequestCreator picasso(List<String> faces, int index) {
		String face = faces.get(index);
		return mPicasso.load(Uri.parse(TmeitServiceConfig.ROOT_URL).buildUpon().path(face).build());
	}

	public RequestCreator placeholder() {
		return mPicasso.load(R.drawable.member_placeholder);
	}
}
