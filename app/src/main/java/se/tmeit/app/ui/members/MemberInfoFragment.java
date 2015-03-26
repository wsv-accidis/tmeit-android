package se.tmeit.app.ui.members;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import se.tmeit.app.R;
import se.tmeit.app.model.Member;
import se.tmeit.app.ui.MainActivity;

/**
 * Fragment for an individual member.
 */
public final class MemberInfoFragment extends ListFragment implements MainActivity.HasTitle {
    private MemberFaceHelper mFaceHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_member_info, container, false);
        return view;
    }

    public static MemberInfoFragment createInstance(Member.RepositoryData memberRepository, int position) {
        Member member = memberRepository.getMembers().get(position);

        Bundle bundle = new Bundle();
        bundle.putString(Member.Keys.REAL_NAME, member.getRealName());

        MemberInfoFragment instance = new MemberInfoFragment();
        instance.setArguments(bundle);
        return instance;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mFaceHelper = MemberFaceHelper.getInstance(activity);
    }

    @Override
    public int getTitle() {
        return R.string.member_title;
    }
}
