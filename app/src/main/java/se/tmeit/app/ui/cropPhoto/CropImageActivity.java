/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.tmeit.app.ui.cropPhoto;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;

import se.tmeit.app.R;

/*
 * Modified from original in AOSP.
 */
public final class CropImageActivity extends MonitoredActivity {
	public static final String EXTRA_ASPECT_X = "aspect_x";
	public static final String EXTRA_ASPECT_Y = "aspect_y";
	public static final String EXTRA_ERROR = "error";
	public static final String EXTRA_MAX_X = "max_x";
	public static final String EXTRA_MAX_Y = "max_y";
	public static final int RESULT_ERROR = 404;
	private static final String TAG = CropImageActivity.class.getSimpleName();
	private final Handler mHandler = new Handler();
	private int mAspectX;
	private int mAspectY;
	private HighlightView mCrop;
	private int mExifRotation;
	private CropImageView mImageView;
	private boolean mIsSaving;
	private int mMaxX;
	private int mMaxY;
	private RotateBitmap mRotateBitmap;
	private Uri mSaveUri;
	private Uri mSourceUri;

	public boolean isSaving() {
		return mIsSaving;
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_crop_photo);
		initViews();

		setupFromIntent();
		if (mRotateBitmap == null) {
			finish();
			return;
		}
		startCrop();
	}

	@Override
	public boolean onSearchRequested() {
		return false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mRotateBitmap != null) {
			mRotateBitmap.recycle();
		}
	}

	private void clearImageView() {
		mImageView.clear();
		if (mRotateBitmap != null) {
			mRotateBitmap.recycle();
		}
		System.gc();
	}

	private static void closeSilently(Closeable c) {
		if (c == null) {
			return;
		}
		try {
			c.close();
		} catch (Throwable t) {
			// Do nothing
		}
	}

	private Bitmap decodeRegionCrop(Bitmap croppedImage, Rect rect, int outWidth, int outHeight) {
		// Release memory now
		clearImageView();

		InputStream is = null;
		try {
			is = getContentResolver().openInputStream(mSourceUri);
			BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(is, false);
			final int width = decoder.getWidth();
			final int height = decoder.getHeight();

			if (mExifRotation != 0) {
				Log.d(TAG, "EXIF rotation: " + mExifRotation + " degrees.");

				// Adjust crop area to account for image rotation
				Matrix matrix = new Matrix();
				matrix.setRotate(-mExifRotation);

				RectF adjusted = new RectF();
				matrix.mapRect(adjusted, new RectF(rect));

				// Adjust to account for origin at 0,0
				adjusted.offset(adjusted.left < 0 ? width : 0, adjusted.top < 0 ? height : 0);
				rect = new Rect((int) adjusted.left, (int) adjusted.top, (int) adjusted.right, (int) adjusted.bottom);
			}

			try {
				croppedImage = decoder.decodeRegion(rect, new BitmapFactory.Options());
				croppedImage = rotateAndScaleImage(croppedImage, outWidth, outHeight);
				mExifRotation = 0; // image is now rotated straight
			} catch (IllegalArgumentException e) {
				// Rethrow with some extra information
				throw new IllegalArgumentException("Rectangle " + rect + " is outside of the image (" + width + "," + height + "," + mExifRotation + ")", e);
			}
		} catch (IOException e) {
			Log.e(TAG, "Error cropping picture: " + e.getMessage(), e);
			finish();
		} finally {
			closeSilently(is);
		}

		return croppedImage;
	}

	private void initViews() {
		mImageView = (CropImageView) findViewById(R.id.crop_image);
		mImageView.setContext(this);
		mImageView.setRecycler(new ImageViewTouchBase.Recycler() {
			@Override
			public void recycle(Bitmap b) {
				b.recycle();
				System.gc();
			}
		});

		findViewById(R.id.crop_cancel_button).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});

		findViewById(R.id.crop_done_button).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				onSaveClicked();
			}
		});
	}

	private void onSaveClicked() {
		if (mCrop == null || mIsSaving) {
			return;
		}

		mIsSaving = true;
		Bitmap croppedImage;
		Rect r = mCrop.getIntCropRect();
		int width = r.width();
		int height = r.height();

		int outWidth = width, outHeight = height;
		if (mMaxX > 0 && mMaxY > 0 && (width > mMaxX || height > mMaxY)) {
			float ratio = (float) width / (float) height;
			if ((float) mMaxX / (float) mMaxY > ratio) {
				outHeight = mMaxY;
				outWidth = (int) ((float) mMaxY * ratio + .5f);
			} else {
				outWidth = mMaxX;
				outHeight = (int) ((float) mMaxX / ratio + .5f);
			}
		}

		Log.d(TAG, "Width = " + width + ", height = " + height + ", outWidth = " + outWidth + ", outHeight = " + outHeight + ".");

		try {
			croppedImage = decodeRegionCrop(null, mCrop.getIntCropRect(), outWidth, outHeight);
		} catch (IllegalArgumentException e) {
			setResultException(e);
			finish();
			return;
		}

		if (croppedImage != null) {
			mImageView.setImageRotateBitmapResetBase(new RotateBitmap(croppedImage, mExifRotation), true);
			mImageView.center(true, true);
			mImageView.clear();
		}

		saveImage(croppedImage);
	}

	private Bitmap rotateAndScaleImage(Bitmap croppedImage, int outWidth, int outHeight) {
		Bitmap outputImage = Bitmap.createBitmap(outWidth, outHeight, Bitmap.Config.RGB_565);

		Canvas canvas = new Canvas(outputImage);
		RectF srcRect = new RectF(0, 0, croppedImage.getWidth(), croppedImage.getHeight());
		RectF dstRect = new RectF(0, 0, outWidth, outHeight);

		Matrix m = new Matrix();
		m.setRectToRect(srcRect, dstRect, Matrix.ScaleToFit.FILL);
		m.preConcat(new RotateBitmap(croppedImage, mExifRotation).getRotateMatrix());
		canvas.drawBitmap(croppedImage, m, null);

		croppedImage.recycle();
		return outputImage;
}

	private void saveImage(Bitmap croppedImage) {
		if (croppedImage != null) {
			final Bitmap b = croppedImage;
			CropUtil.startBackgroundJob(this, null, getResources().getString(R.string.crop_photo_saving),
				new Runnable() {
					public void run() {
						saveOutput(b);
					}
				}, mHandler
			);
		} else {
			finish();
		}
	}

	private void saveOutput(Bitmap croppedImage) {
		if (mSaveUri != null) {
			OutputStream outputStream = null;
			try {
				outputStream = getContentResolver().openOutputStream(mSaveUri);
				if (outputStream != null) {
					croppedImage.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
				}

			} catch (IOException e) {
				setResultException(e);
				Log.e(TAG, "Cannot open file: " + mSaveUri, e);
			} finally {
				closeSilently(outputStream);
			}

			setResultUri(mSaveUri);
		}

		final Bitmap b = croppedImage;
		mHandler.post(new Runnable() {
			public void run() {
				mImageView.clear();
				b.recycle();
			}
		});

		finish();
	}

	private void setResultException(Throwable throwable) {
		setResult(RESULT_ERROR, new Intent().putExtra(EXTRA_ERROR, throwable));
	}

	private void setResultUri(Uri uri) {
		setResult(RESULT_OK, new Intent().putExtra(MediaStore.EXTRA_OUTPUT, uri));
	}

	private void setupFromIntent() {
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();

		if (extras != null) {
			mAspectX = extras.getInt(EXTRA_ASPECT_X);
			mAspectY = extras.getInt(EXTRA_ASPECT_Y);
			mMaxX = extras.getInt(EXTRA_MAX_X);
			mMaxY = extras.getInt(EXTRA_MAX_Y);
			mSaveUri = extras.getParcelable(MediaStore.EXTRA_OUTPUT);
		}

		mSourceUri = intent.getData();
		if (mSourceUri != null) {
			try {
				mExifRotation = CropUtil.getExifRotation(CropUtil.getFromMediaUri(getContentResolver(), mSourceUri));
			} catch (IOException ex) {
				Log.e(TAG, "Caught an exception reading EXIF rotation.", ex);
				mExifRotation = 0;
			}

			InputStream is = null;
			try {
				is = getContentResolver().openInputStream(mSourceUri);
				mRotateBitmap = new RotateBitmap(BitmapFactory.decodeStream(is), mExifRotation);
			} catch (IOException e) {
				Log.e(TAG, "Error reading picture: " + e.getMessage(), e);
				setResultException(e);
			} catch (OutOfMemoryError e) {
				Log.e(TAG, "OOM while reading picture: " + e.getMessage(), e);
				setResultException(e);
			} finally {
				closeSilently(is);
			}
		}
	}

	private void startCrop() {
		if (isFinishing()) {
			return;
		}
		mImageView.setImageRotateBitmapResetBase(mRotateBitmap, true);
		CropUtil.startBackgroundJob(this, null, getResources().getString(R.string.crop_photo_please_wait),
			new Runnable() {
				public void run() {
					final CountDownLatch latch = new CountDownLatch(1);
					mHandler.post(new Runnable() {
						public void run() {
							if (mImageView.getScale() == 1F) {
								mImageView.center(true, true);
							}
							latch.countDown();
						}
					});
					try {
						latch.await();
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
					new Cropper().crop();
				}
			}, mHandler);
	}

	private final class Cropper {
		public void crop() {
			mHandler.post(new Runnable() {
				public void run() {
					makeDefault();
					mImageView.invalidate();
					if (mImageView.getHighlightViews().size() == 1) {
						mCrop = mImageView.getHighlightViews().get(0);
						mCrop.setFocus(true);
					}
				}
			});
		}

		private void makeDefault() {
			if (mRotateBitmap == null) return;

			HighlightView hv = new HighlightView(mImageView);
			final int width = mRotateBitmap.getWidth();
			final int height = mRotateBitmap.getHeight();

			Rect imageRect = new Rect(0, 0, width, height);

			// Make the default size about 4/5 of the width or height
			int cropWidth = Math.min(width, height) * 4 / 5;
			@SuppressWarnings("SuspiciousNameCombination")
			int cropHeight = cropWidth;

			if (mAspectX != 0 && mAspectY != 0) {
				if (mAspectX > mAspectY) {
					cropHeight = cropWidth * mAspectY / mAspectX;
				} else {
					cropWidth = cropHeight * mAspectX / mAspectY;
				}
			}

			int x = (width - cropWidth) / 2;
			int y = (height - cropHeight) / 2;

			RectF cropRect = new RectF(x, y, x + cropWidth, y + cropHeight);
			hv.setup(mImageView.getUnrotatedMatrix(), imageRect, cropRect, mAspectX != 0 && mAspectY != 0);
			mImageView.add(hv);
		}
	}

}

