package se.tmeit.app.ui.uploadPhoto;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import se.tmeit.app.R;
import se.tmeit.app.model.Member;
import se.tmeit.app.ui.MainActivity;
import se.tmeit.app.ui.members.MembersSimpleListFragment;

/**
 * Fragment which displays the image captured and allows the user to select a member.
 */
public final class FinishUploadPhotoFragment extends Fragment implements MainActivity.HasTitle {
    private static final String CAPTURED_IMAGE_URI = "capturedImageUri";
    private static final String TAG = FinishUploadPhotoFragment.class.getSimpleName();
    private Button mFinishButton;
    private Member mMember;

    public static FinishUploadPhotoFragment createInstance(Uri capturedImageUri) {
        Bundle bundle = new Bundle();
        bundle.putString(CAPTURED_IMAGE_URI, capturedImageUri.toString());

        FinishUploadPhotoFragment instance = new FinishUploadPhotoFragment();
        instance.setArguments(bundle);
        return instance;
    }

    @Override
    public int getTitle() {
        return R.string.upload_photo_finish_nav_title;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload_photo_finish, container, false);

        Bundle args = getArguments();
        if (args.containsKey(CAPTURED_IMAGE_URI)) {
            Uri capturedImageUri = Uri.parse(args.getString(CAPTURED_IMAGE_URI));
            ImageView imageView = (ImageView) view.findViewById(R.id.upload_photo_image);
            imageView.setImageURI(capturedImageUri);
        }

        mFinishButton = (Button) view.findViewById(R.id.upload_photo_finish_button);
        mFinishButton.setEnabled(false);
        mFinishButton.setOnClickListener(new OnFinishClickedListener());

        MembersSimpleListFragment membersList = (MembersSimpleListFragment) getChildFragmentManager().findFragmentById(R.id.upload_photo_member_list);
        membersList.setOnMemberSelectedListener(new OnMemberSelectedListener());

        return view;
    }

    private final class OnFinishClickedListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (null == mMember) {
                return;
            }
        }
    }

    private final class OnMemberSelectedListener implements MembersSimpleListFragment.OnMemberSelectedListener {
        @Override
        public void onMemberSelected(Member member) {
            mFinishButton.setEnabled(true);
            mMember = member;
        }
    }
}
