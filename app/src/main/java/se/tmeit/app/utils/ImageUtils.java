package se.tmeit.app.utils;

import android.net.Uri;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility methods for working with images.
 */
public final class ImageUtils {
    private static final String IMAGE_EXTENSION = ".jpg";
    private static final String IMAGE_PREFIX_SEPARATOR = "_";
    private static final String IMAGE_PREFIX_TMEIT = "TMEIT_";
    private static final SimpleDateFormat sImagePrefixDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());

    public static Uri createTemporaryImageFile(File storageDir) throws IOException {
        String timeStamp = sImagePrefixDateFormat.format(new Date());
        String prefix = IMAGE_PREFIX_TMEIT + timeStamp + IMAGE_PREFIX_SEPARATOR;
        return Uri.fromFile(File.createTempFile(prefix, IMAGE_EXTENSION, storageDir));
    }

    private ImageUtils() {
    }
}
