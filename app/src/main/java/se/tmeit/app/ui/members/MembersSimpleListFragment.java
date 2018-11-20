package se.tmeit.app.ui.members;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import se.tmeit.app.R;
import se.tmeit.app.model.Member;
import se.tmeit.app.services.Repository;
import se.tmeit.app.services.RepositoryResultHandler;
import se.tmeit.app.ui.ListFragmentBase;

/**
 * Simple list fragment for cases where we just need a selectable list of members.
 */
public final class MembersSimpleListFragment extends ListFragmentBase {
	private static final String STATE_LIST_VIEW = "membersSimpleListState";
	private static final String TAG = MembersSimpleListFragment.class.getSimpleName();
	private final MembersListResultHandler mRepositoryResultHandler = new MembersListResultHandler();
	private OnMemberSelectedListener mListener;
	private Member.RepositoryData mMembers;

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if (v.getTag() instanceof Member) {
			getListView().setItemChecked(position, true);
			Member member = (Member) v.getTag();
			if (null != mListener) {
				mListener.onMemberSelected(member);
			}
		} else {
			Log.w(TAG, "Got a click on a list item without an associated member.");
		}
	}

	public void setOnMemberSelectedListener(OnMemberSelectedListener listener) {
		mListener = listener;
	}

	@Override
	protected void getDataFromRepository(Repository repository) {
		repository.getMembers(mRepositoryResultHandler, false);
	}

	@Override
	protected String getStateKey() {
		return STATE_LIST_VIEW;
	}

	@Override
	protected void initializeList() {
		finishInitializeList(new MembersSimpleListAdapter(getActivity(), mMembers));
		getListView().setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
	}

	public interface OnMemberSelectedListener {
		void onMemberSelected(Member member);
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

	private final class MembersSimpleListAdapter extends BaseAdapter {
		private final LayoutInflater mInflater;
		private final List<Member> mMembers;

		private MembersSimpleListAdapter(Context context, Member.RepositoryData members) {
			mMembers = members.getMembers();
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return mMembers.size();
		}

		@Override
		public Object getItem(int position) {
			return mMembers.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (null == view) {
				view = mInflater.inflate(R.layout.list_item_simple_member, parent, false);
			}

			Member item = mMembers.get(position);
			TextView text1 = view.findViewById(android.R.id.text1);
			text1.setText(item.realName());

			view.setTag(item);
			return view;
		}
	}
}
