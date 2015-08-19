package se.tmeit.app.ui.uploadPhoto;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import se.tmeit.app.R;
import se.tmeit.app.ui.MainActivity;

/**
 * Fragment which allows the user to take/select a photo and upload it.
 */
public final class UploadPhotoFragment extends Fragment implements MainActivity.HasTitle {
    public static final String IMAGES_TYPE = "image/*";
    public static final int REQUEST_SELECT_EXISTING = 2;
    public static final int REQUEST_TAKE_PHOTO = 1;

    @Override
    public int getTitle() {
        return R.string.upload_photo_nav_title;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload_photo, container, false);

        // See: http://developer.android.com/training/camera/photobasics.html
        // See: http://codetheory.in/android-pick-select-image-from-gallery-with-intents/

        Button takePhotoButton = (Button) view.findViewById(R.id.upload_photo_use_camera);
        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                }
            }
        });

        Button selectExistingButton = (Button) view.findViewById(R.id.upload_photo_select_photo);
        selectExistingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType(IMAGES_TYPE);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, getString(R.string.upload_photo_chooser_title)), REQUEST_SELECT_EXISTING);
            }
        });

        return view;
    }
}
