package se.tmeit.app.ui.uploadPhoto;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import se.tmeit.app.R;
import se.tmeit.app.model.Member;
import se.tmeit.app.ui.MainActivity;
import se.tmeit.app.ui.members.MembersListFragment;
import se.tmeit.app.ui.members.MembersSimpleListFragment;

/**
 * Fragment which displays the image captured and allows the user to select a member.
 */
public final class FinishUploadPhotoFragment extends Fragment implements MainActivity.HasTitle, MainActivity.HasNavigationItem {
	private static final String CAPTURED_PHOTO_URI = "capturedPhotoUri";
	private static final String SELECTED_USER = "selectedUser";
	private static final String TAG = FinishUploadPhotoFragment.class.getSimpleName();
	private final UploadPhotoResultListener mUploadResultListener = new UploadPhotoResultListener();
	private Uri mCaptureUri;
	private Button mFinishButton;
	private View mFinishView;
	private ProgressBar mProgressBar;
	private View mProgressView;
	private String mSelectedUser;

	public static FinishUploadPhotoFragment createInstance(Uri capturedPhotoUri) {
		Bundle bundle = new Bundle();
		bundle.putString(CAPTURED_PHOTO_URI, capturedPhotoUri.toString());

		FinishUploadPhotoFragment instance = new FinishUploadPhotoFragment();
		instance.setArguments(bundle);
		return instance;
	}

	@Override
	public int getItemId() {
		return R.id.nav_upload_photo;
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
		if (args.containsKey(CAPTURED_PHOTO_URI)) {
			mCaptureUri = Uri.parse(args.getString(CAPTURED_PHOTO_URI));
			ImageView imageView = (ImageView) view.findViewById(R.id.upload_photo_image);
			imageView.setImageURI(mCaptureUri);
		}

		if (null != savedInstanceState && savedInstanceState.containsKey(SELECTED_USER)) {
			mSelectedUser = savedInstanceState.getString(SELECTED_USER);
		}

		mFinishView = view.findViewById(R.id.upload_photo_finish_layout);
		mProgressView = view.findViewById(R.id.upload_photo_progress_layout);
		mProgressBar = (ProgressBar) view.findViewById(R.id.upload_photo_progress_bar);

		mFinishButton = (Button) view.findViewById(R.id.upload_photo_finish_button);
		mFinishButton.setEnabled(!TextUtils.isEmpty(mSelectedUser));
		mFinishButton.setOnClickListener(new OnFinishClickedListener());

		MembersSimpleListFragment membersList = (MembersSimpleListFragment) getChildFragmentManager().findFragmentById(R.id.upload_photo_member_list);
		if (null != mCaptureUri) {
			membersList.setOnMemberSelectedListener(new OnMemberSelectedListener());
		}

		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(SELECTED_USER, mSelectedUser);
	}

	private final class OnFinishClickedListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			if (null == mSelectedUser || null == mCaptureUri) {
				return;
			}

			mFinishView.setVisibility(View.GONE);
			mProgressView.setVisibility(View.VISIBLE);
			mProgressBar.setIndeterminate(true);

			UploadPhotoTask uploadPhotoTask = new UploadPhotoTask(getContext(), mSelectedUser, mCaptureUri, mUploadResultListener);
			uploadPhotoTask.execute();
		}
	}

	private final class OnMemberSelectedListener implements MembersSimpleListFragment.OnMemberSelectedListener {
		@Override
		public void onMemberSelected(Member member) {
			mFinishButton.setEnabled(true);
			mSelectedUser = member.username();
		}
	}

	private final class UploadPhotoResultListener implements UploadPhotoTask.UploadPhotoResultListener {
		@Override
		public void onFailure() {
			if (isVisible()) {
				mProgressBar.setIndeterminate(false);
				mProgressView.setVisibility(View.GONE);
				mFinishView.setVisibility(View.VISIBLE);
			}

			Toast toast = Toast.makeText(getContext(), R.string.upload_photo_failed, Toast.LENGTH_LONG);
			toast.show();
		}

		@Override
		public void onSuccess() {
			Activity activity = getActivity();
			if (null != activity && isVisible() && activity instanceof MainActivity) {
				Bundle bundle = getArguments();
				bundle.remove(UploadPhotoFragment.PHOTO);

				MainActivity mainActivity = (MainActivity) activity;
				Fragment membersListFragment = new MembersListFragment();
				mainActivity.openFragment(membersListFragment, false);
			}

			Toast toast = Toast.makeText(getContext(), R.string.upload_photo_succeeded, Toast.LENGTH_LONG);
			toast.show();
		}
	}
}
