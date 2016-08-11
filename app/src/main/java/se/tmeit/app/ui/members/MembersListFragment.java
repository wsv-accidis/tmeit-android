package se.tmeit.app.ui.members;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import se.tmeit.app.R;
import se.tmeit.app.model.Member;
import se.tmeit.app.services.Repository;
import se.tmeit.app.services.RepositoryResultHandler;
import se.tmeit.app.ui.ListFragmentBase;
import se.tmeit.app.ui.MainActivity;
import se.tmeit.app.utils.AndroidUtils;

/**
 * Fragment for the list of members.
 */
public final class MembersListFragment extends ListFragmentBase implements MainActivity.HasMenu, MainActivity.HasTitle, MainActivity.HasNavigationItem {
	private static final int INTERNAL_LIST_CONTAINER_ID = 0x00ff0003; // from android.support.v4.app.ListFragment
	private static final String STATE_LIST_VIEW = "membersListState";
	private static final String STATE_SEARCH_QUERY = "membersSearchQueryState";
	private static final String TAG = MembersListFragment.class.getSimpleName();
	private final MembersListResultHandler mRepositoryResultHandler = new MembersListResultHandler();
	private ImageButton mClearSearchButton;
	private Set<Integer> mFilteredGroups = Collections.emptySet();
	private Set<Integer> mFilteredTeams = Collections.emptySet();
	private MembersListAdapter mListAdapter;
	private Member.RepositoryData mMembers;
	private String mSearchQuery;
	private EditText mSearchText;

	@Override
	public int getItemId() {
		return R.id.nav_members;
	}

	@Override
	public int getMenu() {
		return R.menu.menu_member_list;
	}

	@Override
	public int getTitle() {
		return R.string.members_nav_title;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		registerForContextMenu(getListView());
		setEmptyText(getString(R.string.members_no_results));
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		if (info.position < mMembers.getMembers().size()) {
			Member member = mMembers.getMembers().get(info.position);

			switch (item.getItemId()) {
				case R.id.member_action_call:
					onMemberCallSelected(member);
					return true;
				case R.id.member_action_email:
					onMemberEmailSelected(member);
					return true;
				case R.id.member_action_message:
					onMemberMessageSelected(member);
					return true;
				case R.id.member_action_add_contact:
					onMemberAddContactSelected(member);
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup root = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);
		assert (root != null);

		// Extract the internal list container from the root view
		@SuppressWarnings("ResourceType")
		View listContainer = root.findViewById(INTERNAL_LIST_CONTAINER_ID);
		root.removeView(listContainer);

		// Put the internal list container inside our custom container
		View outerContainer = inflater.inflate(R.layout.fragment_members_list, root, false);
		FrameLayout innerContainer = (FrameLayout) outerContainer.findViewById(R.id.members_list_container);
		innerContainer.addView(listContainer);

		// Put the custom container inside the root
		root.addView(outerContainer, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

		mSearchText = (EditText) outerContainer.findViewById(R.id.members_search);
		mSearchText.addTextChangedListener(new SearchChangedListener());
		mClearSearchButton = (ImageButton) outerContainer.findViewById(R.id.members_search_clear);
		mClearSearchButton.setOnClickListener(new ClearSearchClickedListener());

		if (null != savedInstanceState) {
			mSearchQuery = savedInstanceState.getString(STATE_SEARCH_QUERY);
		}

		return root;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if (position >= 0 && position < mListAdapter.getCount()) {
			Member member = (Member) mListAdapter.getItem(position);
			Fragment memberInfoFragment = MemberInfoFragment.createInstance(getActivity(), member, mMembers);
			Activity activity = getActivity();
			if (activity instanceof MainActivity) {
				AndroidUtils.hideSoftKeyboard(getContext(), getView());
				saveInstanceState();
				MainActivity mainActivity = (MainActivity) activity;
				mainActivity.openFragment(memberInfoFragment);
			} else {
				Log.e(TAG, "Activity holding fragment is not MainActivity!");
			}
		}
	}

	@Override
	public boolean onMenuItemSelected(MenuItem item) {
		if (R.id.member_filter_dialog == item.getItemId()) {
			if (null != mMembers) {
				Bundle args = new Bundle();
				args.putSerializable(MembersFilterDialogFragment.ARG_GROUPS, (Serializable) mMembers.getGroups());
				args.putSerializable(MembersFilterDialogFragment.ARG_GROUPS_FILTERED, (Serializable) mFilteredGroups);
				args.putSerializable(MembersFilterDialogFragment.ARG_TEAMS, (Serializable) mMembers.getTeams());
				args.putSerializable(MembersFilterDialogFragment.ARG_TEAMS_FILTERED, (Serializable) mFilteredTeams);

				MembersFilterDialogFragment dialog = new MembersFilterDialogFragment();
				dialog.setArguments(args);
				dialog.setDialogListener(new MembersFilterDialogListener());
				dialog.show(getFragmentManager(), MembersFilterDialogFragment.class.getSimpleName());
			}

			return true;
		}
		return false;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (null != mSearchQuery) {
			outState.putString(STATE_SEARCH_QUERY, mSearchQuery);
		}
	}

	@Override
	protected void getDataFromRepository(Repository repository) {
		mSearchText.setEnabled(false);
		mClearSearchButton.setEnabled(false);

		repository.getMembers(mRepositoryResultHandler, getPreferences().shouldRefreshMembers());
	}

	@Override
	protected String getStateKey() {
		return STATE_LIST_VIEW;
	}

	@Override
	protected void initializeList() {
		mSearchText.setEnabled(true);
		mClearSearchButton.setEnabled(true);

		if (null == mListAdapter) {
			mListAdapter = new MembersListAdapter(getActivity());
		}
		mListAdapter.setContent(mMembers);
		mFilteredGroups = getPreferences().getMembersListGroupsFilter();
		mFilteredTeams = getPreferences().getMembersListTeamsFilter();
		mListAdapter.setGroupTeamFilters(mFilteredGroups, mFilteredTeams);

		finishInitializeList(mListAdapter);
		refreshFilter();
	}

	private void onMemberAddContactSelected(Member member) {
		boolean succeeded = MemberActions.addAsContact(member.getRealName(), member.getPhone(), member.getEmail(), getActivity().getContentResolver());
		Toast toast = Toast.makeText(getActivity(),
			(succeeded ? R.string.member_contact_saved : R.string.member_contact_could_not_saved),
			(succeeded ? Toast.LENGTH_LONG : Toast.LENGTH_LONG));
		toast.show();
	}

	private void onMemberCallSelected(Member member) {
		if (TextUtils.isEmpty(member.getPhone())) {
			Toast toast = Toast.makeText(getActivity(), R.string.member_no_phone, Toast.LENGTH_SHORT);
			toast.show();
		} else {
			MemberActions.makeCallTo(member.getPhone(), this);
		}
	}

	private void onMemberEmailSelected(Member member) {
		if (TextUtils.isEmpty(member.getEmail())) {
			Toast toast = Toast.makeText(getActivity(), R.string.member_no_email, Toast.LENGTH_SHORT);
			toast.show();
		} else {
			MemberActions.sendEmailTo(member.getEmail(), this);
		}
	}

	private void onMemberMessageSelected(Member member) {
		if (TextUtils.isEmpty(member.getPhone())) {
			Toast toast = Toast.makeText(getActivity(), R.string.member_no_phone, Toast.LENGTH_SHORT);
			toast.show();
		} else {
			MemberActions.sendSmsTo(member.getPhone(), this);
		}
	}

	private void refreshFilter() {
		getPreferences().setMembersListFilters(mFilteredGroups, mFilteredTeams);
		if (null != mListAdapter) {
			mListAdapter.setGroupTeamFilters(mFilteredGroups, mFilteredTeams);
			mListAdapter.getFilter().filter(mSearchQuery);
		}
	}

	private final class ClearSearchClickedListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			if (!TextUtils.isEmpty(mSearchQuery)) {
				mSearchText.setText("");
			}
		}
	}

	private final class MembersFilterDialogListener implements MembersFilterDialogFragment.MembersFilterDialogListener {
		@Override
		public void onDismiss(Set<Integer> groups, Set<Integer> teams) {
			mFilteredGroups = groups;
			mFilteredTeams = teams;
			refreshFilter();
		}
	}

	private final class MembersListResultHandler implements RepositoryResultHandler<Member.RepositoryData> {
		@Override
		public void onError(int errorMessage) {
			mMembers = Member.RepositoryData.empty();
			onRepositoryError(errorMessage);
		}

		@Override
		public void onSuccess(Member.RepositoryData result) {
			mMembers = result;
			onRepositorySuccess();
		}
	}

	private final class SearchChangedListener implements TextWatcher {
		@Override
		public void afterTextChanged(Editable s) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			mSearchQuery = s.toString().toLowerCase();
			if (null != mListAdapter) {
				mListAdapter.getFilter().filter(mSearchQuery);
			}
		}
	}
}
