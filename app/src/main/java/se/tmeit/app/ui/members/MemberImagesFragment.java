package se.tmeit.app.ui.members;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.List;

import se.tmeit.app.R;
import se.tmeit.app.model.Member;
import se.tmeit.app.ui.MainActivity;

public class MemberImagesFragment extends Fragment implements MainActivity.HasMenu, MainActivity.HasTitle {
    private final static String TAG = MemberImagesFragment.class.getSimpleName();
    private MemberFaceHelper mFaceHelper;

    public static MemberImagesFragment createInstance(Context context, List<String> faces) {
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(Member.Keys.FACES, new ArrayList<>(faces));

        MemberImagesFragment instance = new MemberImagesFragment();
        instance.setArguments(bundle);
        return instance;
    }

    @Override
    public int getMenu() {
        return R.menu.menu_member_info;
    }

    @Override
    public boolean onMenuItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public int getTitle() {
        return R.string.member_nav_title;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mFaceHelper = MemberFaceHelper.getInstance(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_member_images, container, false);

        Bundle args = getArguments();

        List<String> faces = args.getStringArrayList(Member.Keys.FACES);
        if (!faces.isEmpty()) {
            GridView gridView = (GridView) view;
            gridView.setAdapter(new MemberImageAdapter(getActivity(), faces, mFaceHelper));
        } else {

        }
        return view;
    }
}
