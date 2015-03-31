package se.tmeit.app.ui.members;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import se.tmeit.app.R;
import se.tmeit.app.model.Member;
import se.tmeit.app.ui.MainActivity;

/**
 * Fragment for an individual member.
 */
public final class MemberInfoFragment extends Fragment implements MainActivity.HasTitle, MainActivity.HasMenu {
    private final static String TAG = MemberInfoFragment.class.getSimpleName();
    private MemberFaceHelper mFaceHelper;

    public static MemberInfoFragment createInstance(Context context, Member.RepositoryData memberRepository, int position) {
        Member member = memberRepository.getMembers().get(position);

        Bundle bundle = new Bundle();
        bundle.putString(Member.Keys.USERNAME, member.getUsername());
        bundle.putString(Member.Keys.REAL_NAME, member.getRealName());
        bundle.putString(Member.Keys.TITLE_TEXT, member.getTitleText(context, memberRepository));
        bundle.putString(Member.Keys.TEAM_TEXT, member.getTeamText(context, memberRepository));
        bundle.putString(Member.Keys.PHONE, member.getPhone());
        bundle.putString(Member.Keys.EMAIL, member.getEmail());
        bundle.putStringArrayList(Member.Keys.FACES, new ArrayList<>(member.getFaces()));

        MemberInfoFragment instance = new MemberInfoFragment();
        instance.setArguments(bundle);
        return instance;
    }

    @Override
    public int getMenu() {
        return R.menu.menu_member_info;
    }

    @Override
    public int getTitle() {
        return R.string.member_title;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mFaceHelper = MemberFaceHelper.getInstance(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_member_info, container, false);

        Bundle args = getArguments();
        TextView realNameText = (TextView) view.findViewById(R.id.member_real_name);
        realNameText.setText(args.getString(Member.Keys.REAL_NAME));

        TextView usernameText = (TextView) view.findViewById(R.id.member_username);
        usernameText.setText(args.getString(Member.Keys.USERNAME));

        TextView titleText = (TextView) view.findViewById(R.id.member_title);
        titleText.setText(args.getString(Member.Keys.TITLE_TEXT));

        TextView teamText = (TextView) view.findViewById(R.id.member_team);
        teamText.setText(args.getString(Member.Keys.TEAM_TEXT));

        ImageView imageView = (ImageView) view.findViewById(R.id.member_face);
        List<String> faces = args.getStringArrayList(Member.Keys.FACES);
        if (!faces.isEmpty()) {
            mFaceHelper.picasso(faces)
                    .placeholder(R.drawable.member_placeholder)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.member_placeholder);
        }

        final String email = args.getString(Member.Keys.EMAIL);
        Button emailButton = (Button) view.findViewById(R.id.member_button_email);
        if (!TextUtils.isEmpty(email)) {
            emailButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MemberActions.sendEmailTo(email, MemberInfoFragment.this);
                }
            });
            emailButton.setEnabled(true);
        } else {
            emailButton.setEnabled(false);
        }

        final String phoneNo = args.getString(Member.Keys.PHONE);
        Button smsButton = (Button) view.findViewById(R.id.member_button_message);
        Button callButton = (Button) view.findViewById(R.id.member_button_call);

        if (!TextUtils.isEmpty(phoneNo)) {
            smsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MemberActions.sendSmsTo(phoneNo, MemberInfoFragment.this);
                }
            });
            callButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MemberActions.makeCallTo(phoneNo, MemberInfoFragment.this);
                }
            });

            smsButton.setEnabled(true);
            callButton.setEnabled(true);
        } else {
            smsButton.setEnabled(false);
            callButton.setEnabled(false);
        }

        return view;
    }

    @Override
    public boolean onMenuItemSelected(MenuItem item) {
        return false;
    }
}
