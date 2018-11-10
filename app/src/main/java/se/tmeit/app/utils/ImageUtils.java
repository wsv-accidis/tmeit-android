package se.tmeit.app.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import se.tmeit.app.BuildConfig;

/**
 * Utility methods for working with images.
 */
public final class ImageUtils {
	private static final String IMAGE_EXTENSION = ".jpg";
	private static final String IMAGE_PREFIX_SEPARATOR = "_";
	private static final String IMAGE_PREFIX_TMEIT = "TMEIT_";
	private static final SimpleDateFormat sImagePrefixDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());

	public static Uri createTemporaryImageFile(Context context) throws IOException {
		final String timeStamp = sImagePrefixDateFormat.format(new Date());
		final String prefix = IMAGE_PREFIX_TMEIT + timeStamp + IMAGE_PREFIX_SEPARATOR;
		return FileProvider.getUriForFile(
			context,
			BuildConfig.APPLICATION_ID + ".provider",
			File.createTempFile(prefix, IMAGE_EXTENSION, context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)));
	}

	public static void safelyDeleteTemporaryFile(Uri uri) {
		if (null == uri) {
			return;
		}
		try {
			File file = new File(uri.getPath());
			if (file.getName().startsWith(IMAGE_PREFIX_TMEIT) && file.exists()) {
				boolean ignored = file.delete();
			}
		} catch (Exception ignored) {
		}
	}

	private ImageUtils() {
	}
}
