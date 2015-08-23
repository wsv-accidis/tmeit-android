package se.tmeit.app.ui.uploadPhoto;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.camera.CropImageIntentBuilder;

import java.io.IOException;

import se.tmeit.app.R;
import se.tmeit.app.ui.MainActivity;
import se.tmeit.app.utils.ImageUtils;

/**
 * Fragment which allows the user to take/select a photo and upload it.
 */
public final class UploadPhotoFragment extends Fragment implements MainActivity.HasTitle {
    private static final int ACTIVITY_RESULT_CROPPED_PHOTO = 13;
    private static final int ACTIVITY_RESULT_SELECT_EXISTING = 12;
    private static final int ACTIVITY_RESULT_TAKE_PHOTO = 11;
    private static final int BASE_OUTPUT_HEIGHT = 120;
    private static final int BASE_OUTPUT_WIDTH = 110;
    private static final String IMAGES_TYPE = "image/*";
    private static final int OUTPUT_SCALE_FACTOR = 4;
    private static final String STATE_PENDING_IMAGE_CAPTURE_URI = "uploadPhotoPendingCaptureUri";
    private static final String STATE_PENDING_IMAGE_CROP_URI = "uploadPhotoPendingCropUri";
    private static final String TAG = UploadPhotoFragment.class.getSimpleName();
    private Uri mPendingImageCaptureUri;
    private Uri mPendingImageCropUri;

    @Override
    public int getTitle() {
        return R.string.upload_photo_nav_title;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Activity.RESULT_OK != resultCode) {
            return;
        }

        if (ACTIVITY_RESULT_TAKE_PHOTO == requestCode || ACTIVITY_RESULT_SELECT_EXISTING == requestCode) {
            handleUploadPhotoActivityResult(requestCode, data);
        } else if (ACTIVITY_RESULT_CROPPED_PHOTO == requestCode) {
            handleCropPhotoActivityResult();
        } else {
            Log.w(TAG, "Activity result with unknown requestCode = " + requestCode + ".");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload_photo, container, false);

        if (null != savedInstanceState && savedInstanceState.containsKey(STATE_PENDING_IMAGE_CAPTURE_URI)) {
            mPendingImageCaptureUri = Uri.parse(savedInstanceState.getString(STATE_PENDING_IMAGE_CAPTURE_URI));
            Log.d(TAG, "Pending image capture uri = \"" + mPendingImageCaptureUri + "\".");
        }

        if (null != savedInstanceState && savedInstanceState.containsKey(STATE_PENDING_IMAGE_CROP_URI)) {
            mPendingImageCropUri = Uri.parse(savedInstanceState.getString(STATE_PENDING_IMAGE_CROP_URI));
            Log.d(TAG, "Pending image crop uri = \"" + mPendingImageCropUri + "\".");
        }

        Button takePhotoButton = (Button) view.findViewById(R.id.upload_photo_use_camera);
        takePhotoButton.setOnClickListener(new TakePhotoClickListener());
        Button selectExistingButton = (Button) view.findViewById(R.id.upload_photo_select_photo);
        selectExistingButton.setOnClickListener(new SelectExistingClickListener());

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (null != mPendingImageCaptureUri) {
            outState.putString(STATE_PENDING_IMAGE_CAPTURE_URI, mPendingImageCaptureUri.toString());
        }
        if (null != mPendingImageCropUri) {
            outState.putString(STATE_PENDING_IMAGE_CROP_URI, mPendingImageCropUri.toString());
        }
    }

    private void broadcastMediaScanIntent(Uri sourceUri) {
        try {
            // This ensures that the photo we just took also shows up in the photo gallery and other apps.
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(sourceUri);
            getContext().sendBroadcast(mediaScanIntent);
        } catch (Exception ex) {
            Log.e(TAG, "Caught an exception while sending media scanner broadcast intent.");
        }
    }

    private Uri createTemporaryImageFile() throws IOException {
        return ImageUtils.createTemporaryImageFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM));
    }

    private void handleCropPhotoActivityResult() {
        Uri uri = mPendingImageCropUri;
        mPendingImageCropUri = null;

        if (null == uri) {
            Log.e(TAG, "Tried to handle photo crop but didn't get a uri.");
            return;
        }

        Fragment finishFragment = FinishUploadPhotoFragment.createInstance(uri);
        Activity activity = getActivity();
        if (activity instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) activity;
            mainActivity.openFragment(finishFragment, true);
        } else {
            Log.e(TAG, "Activity holding fragment is not MainActivity!");
        }
    }

    private void handleUploadPhotoActivityResult(int requestCode, Intent data) {
        Uri sourceUri = null;

        if (ACTIVITY_RESULT_TAKE_PHOTO == requestCode) {
            sourceUri = mPendingImageCaptureUri;
            mPendingImageCaptureUri = null;
            broadcastMediaScanIntent(sourceUri);
        } else if (ACTIVITY_RESULT_SELECT_EXISTING == requestCode) {
            sourceUri = data.getData();
        }

        if (null == sourceUri) {
            Log.e(TAG, "Tried to handle photo upload but didn't get a uri.");
            return;
        }

        try {
            Log.i(TAG, "Captured/selected image from uri = \"" + sourceUri + "\".");
            mPendingImageCropUri = ImageUtils.createTemporaryImageFile(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES));
            Log.d(TAG, "Created image crop uri = \"" + mPendingImageCropUri + "\".");

            // TODO Use own cropping lib instead, base it on https://github.com/jdamcd/android-crop

            CropImageIntentBuilder intentBuilder = new CropImageIntentBuilder(OUTPUT_SCALE_FACTOR * BASE_OUTPUT_WIDTH, OUTPUT_SCALE_FACTOR * BASE_OUTPUT_HEIGHT, mPendingImageCropUri);
            intentBuilder.setSourceImage(sourceUri);
            intentBuilder.setDoFaceDetection(false);
            intentBuilder.setScaleUpIfNeeded(false);
            startActivityForResult(intentBuilder.getIntent(getContext()), ACTIVITY_RESULT_CROPPED_PHOTO);
        } catch (Exception ex) {
            Log.e(TAG, "Caught an exception while attempting to crop image.", ex);
        }
    }

    private final class SelectExistingClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            try {
                Intent intent = new Intent();
                intent.setType(IMAGES_TYPE);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, getString(R.string.upload_photo_chooser_title)), ACTIVITY_RESULT_SELECT_EXISTING);
            } catch (Exception ex) {
                Log.e(TAG, "Caught an exception while attempting to start image selection.", ex);
            }
        }
    }

    private final class TakePhotoClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            try {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
                    mPendingImageCaptureUri = createTemporaryImageFile();
                    Log.d(TAG, "Created image capture uri = \"" + mPendingImageCaptureUri + "\".");
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPendingImageCaptureUri);
                    startActivityForResult(takePictureIntent, ACTIVITY_RESULT_TAKE_PHOTO);
                }
            } catch (Exception ex) {
                Log.e(TAG, "Caught an exception while attempting to start image capture.", ex);
            }
        }
    }
}
