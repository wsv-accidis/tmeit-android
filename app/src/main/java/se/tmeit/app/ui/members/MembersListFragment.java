package se.tmeit.app.ui.members;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
public final class MembersListFragment extends ListFragment implements MainActivity.HasTitle, MainActivity.HasMenu {
    private static final String STATE_LISTVIEW = "membersListState";
    private static final String TAG = MembersListFragment.class.getSimpleName();
    private final Handler mHandler = new Handler();
    private MemberFaceHelper mFaceHelper;
    private Parcelable mListState;
    private Member.RepositoryData mMembers;
    private Preferences mPrefs;

    @Override
    public int getMenu() {
        return R.menu.menu_member_list;
    }

    @Override
    public int getTitle() {
        return R.string.members_title;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mListState = savedInstanceState.getParcelable(STATE_LISTVIEW);
        }
        registerForContextMenu(getListView());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mFaceHelper = MemberFaceHelper.getInstance(activity);
        mPrefs = new Preferences(activity);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (info.position < mMembers.getMembers().size()) {
            Member member = mMembers.getMembers().get(info.position);

            switch (item.getItemId()) {
                case R.id.member_action_call:
                    if (TextUtils.isEmpty(member.getPhone())) {
                        Toast toast = Toast.makeText(getActivity(), R.string.member_no_phone, Toast.LENGTH_SHORT);
                        toast.show();
                    } else {
                        MemberActions.makeCallTo(member.getPhone(), this);
                    }
                    return true;
                case R.id.member_action_email:
                    if (TextUtils.isEmpty(member.getEmail())) {
                        Toast toast = Toast.makeText(getActivity(), R.string.member_no_email, Toast.LENGTH_SHORT);
                        toast.show();
                    } else {
                        MemberActions.sendEmailTo(member.getEmail(), this);
                    }
                    return true;
                case R.id.member_action_message:
                    if (TextUtils.isEmpty(member.getPhone())) {
                        Toast toast = Toast.makeText(getActivity(), R.string.member_no_phone, Toast.LENGTH_SHORT);
                        toast.show();
                    } else {
                        MemberActions.sendSmsTo(member.getPhone(), this);
                    }
                    return true;
                case R.id.member_action_add_contact:
                    boolean succeeded = MemberActions.addAsContact(member.getRealName(), member.getPhone(), member.getEmail(), getActivity().getContentResolver());
                    Toast toast = Toast.makeText(getActivity(),
                            (succeeded ? R.string.member_contact_saved : R.string.member_contact_could_not_saved),
                            (succeeded ? Toast.LENGTH_LONG : Toast.LENGTH_LONG));
                    toast.show();
                    return true;
            }
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.menu_member_context, menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem filterItem = menu.findItem(R.id.member_filter_list);
        if (null != filterItem) {
            SubMenu subMenu = filterItem.getSubMenu();
            subMenu.add("Test");
            subMenu.add("Test2");
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (position < mMembers.getMembers().size()) {
            Fragment memberInfoFragment = MemberInfoFragment.createInstance(getActivity(), mMembers, position);
            Activity activity = getActivity();
            if (activity instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) activity;
                mainActivity.openFragment(memberInfoFragment, true);
            } else {
                Log.e(TAG, "Activity holding fragment is not MainActivity!");
            }
        }
    }

    @Override
    public boolean onMenuItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();

        String username = mPrefs.getAuthenticatedUser(), serviceAuth = mPrefs.getServiceAuthentication();
        Repository repository = new Repository(username, serviceAuth);

        if (null == mMembers) {
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
        } else {
            initializeList();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (null != getView()) {
            outState.putParcelable(STATE_LISTVIEW, getListView().onSaveInstanceState());
        }
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
            titleTextView.setText(member.getTitleText(getContext(), mMembers));

            TextView teamTextView = (TextView) view.findViewById(R.id.member_team);
            teamTextView.setText(member.getTeamText(getContext(), mMembers));

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
    }
}
