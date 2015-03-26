package se.tmeit.app.ui.members;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.List;

import se.tmeit.app.R;
import se.tmeit.app.model.Member;
import se.tmeit.app.services.Repository;
import se.tmeit.app.storage.Preferences;
import se.tmeit.app.ui.MainActivity;

/**
 * Fragment for the list of members.
 */
public final class MembersListFragment extends ListFragment implements MainActivity.HasTitle {
    private static final String STATE_LISTVIEW = "membersListState";
    private final Handler mHandler = new Handler();
    private MemberFaceHelper mFaceHelper;
    private Parcelable mListState;
    private Member.RepositoryData mMembers;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mListState = savedInstanceState.getParcelable(STATE_LISTVIEW);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mFaceHelper = MemberFaceHelper.getInstance(activity);

        if (null == mMembers) {
            Preferences prefs = new Preferences(activity);
            String username = prefs.getAuthenticatedUser(), serviceAuth = prefs.getServiceAuthentication();
            Repository repository = new Repository(username, serviceAuth);

            repository.getMembers(new Repository.RepositoryResultHandler<Member.RepositoryData>() {
                @Override
                public void onError(final int errorMessage) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Activity activity = getActivity();
                            if (null != activity && isVisible()) {
                                mMembers = null;
                                initializeList();

                                Toast toast = Toast.makeText(activity, getString(errorMessage), Toast.LENGTH_LONG);
                                toast.show();
                            }
                        }
                    });
                }

                @Override
                public void onSuccess(final Member.RepositoryData result) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (null != getActivity() && isVisible()) {
                                mMembers = result;
                                initializeList();
                            }
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (position < mMembers.getMembers().size()) {
            Fragment memberInfoFragment = MemberInfoFragment.createInstance(mMembers, position);
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.container, memberInfoFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (getView() != null) {
            outState.putParcelable(STATE_LISTVIEW, getListView().onSaveInstanceState());
        }
    }

    @Override
    public int getTitle() {
        return R.string.members_title;
    }

    private void initializeList() {
        List<Member> members = (null != mMembers ? mMembers.getMembers() : Collections.<Member>emptyList());
        setListAdapter(new MembersListAdapter(getActivity(), R.layout.list_item_member, R.id.member_real_name, members));

        if (null != mListState) {
            getListView().onRestoreInstanceState(mListState);
            mListState = null;
        }
    }

    private class MembersListAdapter extends ArrayAdapter<Member> {
        public MembersListAdapter(Context context, int resource, int textViewResourceId, List<Member> objects) {
            super(context, resource, textViewResourceId, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            Member member = mMembers.getMembers().get(position);

            ImageView imageView = (ImageView) view.findViewById(R.id.member_face);
            List<String> faces = member.getFaces();
            if (!faces.isEmpty()) {
                mFaceHelper.picasso(faces)
                        .resizeDimen(R.dimen.tmeit_members_list_face_size, R.dimen.tmeit_members_list_face_size)
                        .centerInside()
                        .placeholder(R.drawable.member_placeholder)
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.member_placeholder);
            }

            TextView titleTextView = (TextView) view.findViewById(R.id.member_title);
            titleTextView.setText(getTitle(member));

            TextView teamTextView = (TextView) view.findViewById(R.id.member_team);
            teamTextView.setText(getTeam(member));

            TextView phoneTextView = (TextView) view.findViewById(R.id.member_phone);
            if (null != phoneTextView) {
                phoneTextView.setText(member.getPhone());
            }

            TextView emailTextView = (TextView) view.findViewById(R.id.member_email);
            if (null != emailTextView) {
                emailTextView.setText(member.getEmail());
            }

            return view;
        }

        private String getTeam(Member member) {
            if (member.getTeamId() > 0) {
                return mMembers.getTeams().get(member.getTeamId());
            } else {
                return getString(R.string.members_no_team_placeholder);
            }
        }

        private String getTitle(Member member) {
            if (member.getTitleId() > 0) {
                return mMembers.getTitles().get(member.getTitleId());
            } else if (member.getGroupId() > 0) {
                return mMembers.getGroups().get(member.getGroupId());
            } else {
                return getString(R.string.members_no_title_placeholder);
            }
        }
    }
}
