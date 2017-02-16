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
import android.widget.Toast;

import java.io.IOException;

import se.tmeit.app.R;
import se.tmeit.app.ui.MainActivity;
import se.tmeit.app.ui.cropPhoto.CropImageActivity;
import se.tmeit.app.utils.ImageUtils;



/**
 * Fragment which allows the user to take/select a photo and upload it.
 */
public final class UploadPhotoFragment extends Fragment implements MainActivity.HasTitle, MainActivity.HasNavigationItem {
	private static final int ACTIVITY_RESULT_CROPPED_PHOTO = 13;
	private static final int ACTIVITY_RESULT_SELECT_EXISTING = 12;
	private static final int ACTIVITY_RESULT_TAKE_PHOTO = 11;
	private static final int BASE_OUTPUT_HEIGHT = 120;
	private static final int BASE_OUTPUT_WIDTH = 110;
	public static final String PHOTO = "photo";
	private static final String IMAGES_TYPE = "image/*";
	private static final int OUTPUT_SCALE_FACTOR = 4;
	private static final String STATE_PENDING_IMAGE_CAPTURE_URI = "uploadPhotoPendingCaptureUri";
	private static final String STATE_PENDING_IMAGE_CROP_URI = "uploadPhotoPendingCropUri";
	private static final String TAG = UploadPhotoFragment.class.getSimpleName();
	private Uri mPendingCaptureUri;
	private Uri mPendingCropUri;

	@Override
	public int getItemId() {
		return R.id.nav_upload_photo;
	}

	@Override
	public int getTitle() {
		return R.string.upload_photo_nav_title;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (Activity.RESULT_OK == resultCode) {
			if (ACTIVITY_RESULT_TAKE_PHOTO == requestCode || ACTIVITY_RESULT_SELECT_EXISTING == requestCode) {
				handleUploadPhotoActivityResult(requestCode, data);
			} else if (ACTIVITY_RESULT_CROPPED_PHOTO == requestCode) {
				handleCropPhotoActivityResult();
			}
		} else if (Activity.RESULT_CANCELED == resultCode) {
			if (ACTIVITY_RESULT_TAKE_PHOTO == requestCode) {
				ImageUtils.safelyDeleteTemporaryFile(mPendingCaptureUri);
				mPendingCaptureUri = null;
			} else if (ACTIVITY_RESULT_CROPPED_PHOTO == requestCode) {
				ImageUtils.safelyDeleteTemporaryFile(mPendingCropUri);
				mPendingCropUri = null;
			}
		}
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_upload_photo, container, false);

		if (null != savedInstanceState && savedInstanceState.containsKey(STATE_PENDING_IMAGE_CAPTURE_URI)) {
			mPendingCaptureUri = Uri.parse(savedInstanceState.getString(STATE_PENDING_IMAGE_CAPTURE_URI));
			Log.d(TAG, "Pending capture uri = \"" + mPendingCaptureUri + "\".");
		}

		if (null != savedInstanceState && savedInstanceState.containsKey(STATE_PENDING_IMAGE_CROP_URI)) {
			mPendingCropUri = Uri.parse(savedInstanceState.getString(STATE_PENDING_IMAGE_CROP_URI));
			Log.d(TAG, "Pending crop uri = \"" + mPendingCropUri + "\".");
		}

		Bundle bundle = getArguments();
		if(bundle != null)
		{
			String uri = getArguments().getString(PHOTO);
			if(bundle != null && uri!= null){
				Intent intent = new Intent();
				intent.setData(Uri.parse(uri));
				handleUploadPhotoActivityResult(ACTIVITY_RESULT_SELECT_EXISTING, intent);
			}
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
		if (null != mPendingCaptureUri) {
			outState.putString(STATE_PENDING_IMAGE_CAPTURE_URI, mPendingCaptureUri.toString());
		}
		if (null != mPendingCropUri) {
			outState.putString(STATE_PENDING_IMAGE_CROP_URI, mPendingCropUri.toString());
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
		return ImageUtils.createTemporaryImageFile(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES));
	}

	private void handleCropPhotoActivityResult() {
		Uri uri = mPendingCropUri;
		mPendingCropUri = null;

		if (null == uri) {
			Log.e(TAG, "Tried to handle photo crop but didn't get a uri.");
			return;
		}

		Fragment finishFragment = FinishUploadPhotoFragment.createInstance(uri);
		Activity activity = getActivity();
		if (activity instanceof MainActivity) {
			MainActivity mainActivity = (MainActivity) activity;
			mainActivity.openFragment(finishFragment, false);
		} else {
			Log.e(TAG, "Activity holding fragment is not MainActivity!");
		}
	}

	private void handleUploadPhotoActivityResult(int requestCode, Intent data) {
		Uri sourceUri = null;

		if (ACTIVITY_RESULT_TAKE_PHOTO == requestCode) {
			sourceUri = mPendingCaptureUri;
			mPendingCaptureUri = null;
			broadcastMediaScanIntent(sourceUri);
		} else if (ACTIVITY_RESULT_SELECT_EXISTING == requestCode) {
			sourceUri = data.getData();
		}

		if (null == sourceUri) {
			Log.e(TAG, "Tried to handle photo upload but didn't get a uri.");
			return;
		}

		try {
			Log.i(TAG, "Captured/selected photo from uri = \"" + sourceUri + "\".");
			mPendingCropUri = ImageUtils.createTemporaryImageFile(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES));
			Log.d(TAG, "Created crop uri = \"" + mPendingCropUri + "\".");

			Intent cropIntent = new Intent(getContext(), CropImageActivity.class);
			cropIntent.setData(sourceUri);
			cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPendingCropUri);
			cropIntent.putExtra(CropImageActivity.EXTRA_ASPECT_X, BASE_OUTPUT_WIDTH);
			cropIntent.putExtra(CropImageActivity.EXTRA_ASPECT_Y, BASE_OUTPUT_HEIGHT);
			cropIntent.putExtra(CropImageActivity.EXTRA_MAX_X, OUTPUT_SCALE_FACTOR * BASE_OUTPUT_WIDTH);
			cropIntent.putExtra(CropImageActivity.EXTRA_MAX_Y, OUTPUT_SCALE_FACTOR * BASE_OUTPUT_HEIGHT);
			startActivityForResult(cropIntent, ACTIVITY_RESULT_CROPPED_PHOTO);
		} catch (Exception ex) {
			ImageUtils.safelyDeleteTemporaryFile(mPendingCropUri);
			mPendingCropUri = null;

			Log.e(TAG, "Caught an exception while attempting to crop photo.", ex);
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
				Toast toast = Toast.makeText(getContext(), R.string.upload_photo_error_permissions, Toast.LENGTH_LONG);
				toast.show();
			}
		}
	}

	private final class TakePhotoClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			try {
				Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
					mPendingCaptureUri = createTemporaryImageFile();
					Log.d(TAG, "Created capture uri = \"" + mPendingCaptureUri + "\".");
					takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPendingCaptureUri);
					startActivityForResult(takePictureIntent, ACTIVITY_RESULT_TAKE_PHOTO);
				}
			} catch (Exception ex) {
				ImageUtils.safelyDeleteTemporaryFile(mPendingCaptureUri);
				mPendingCaptureUri = null;

				Log.e(TAG, "Caught an exception while attempting to start capture.", ex);
				Toast toast = Toast.makeText(getContext(), R.string.upload_photo_error_permissions, Toast.LENGTH_LONG);
				toast.show();
			}
		}
	}
}
