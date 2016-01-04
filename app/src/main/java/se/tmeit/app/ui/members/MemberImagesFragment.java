package se.tmeit.app.ui.members;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.List;

import se.tmeit.app.R;
import se.tmeit.app.model.Member;
import se.tmeit.app.ui.MainActivity;

/**
 * Fragment displaying all available images for a member as a full-screen grid.
 */
public final class MemberImagesFragment extends Fragment implements MainActivity.HasTitle, MainActivity.HasNavigationItem {
    private final static String TAG = MemberImagesFragment.class.getSimpleName();
    private MemberFaceHelper mFaceHelper;

    public static MemberImagesFragment createInstance(List<String> faces) {
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(Member.Keys.FACES, new ArrayList<>(faces));

        MemberImagesFragment instance = new MemberImagesFragment();
        instance.setArguments(bundle);
        return instance;
    }

    @Override
	public int getItemId() {
		return R.id.nav_members;
	}

    @Override
    public int getTitle() {
        return R.string.member_images_nav_title;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mFaceHelper = MemberFaceHelper.getInstance(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_member_images, container, false);

        Bundle args = getArguments();

        List<String> faces = args.getStringArrayList(Member.Keys.FACES);
        if (null != faces && !faces.isEmpty()) {
            GridView gridView = (GridView) view;
            gridView.setAdapter(new MemberImageAdapter(getActivity(), faces, mFaceHelper));
        }

        return view;
    }
}
