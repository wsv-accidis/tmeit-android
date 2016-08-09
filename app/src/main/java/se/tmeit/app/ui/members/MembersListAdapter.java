package se.tmeit.app.ui.members;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import se.tmeit.app.R;
import se.tmeit.app.model.Member;

/**
 * List adapter for the members list. Implements filtering.
 */
public final class MembersListAdapter extends BaseAdapter implements Filterable {
	private final Context mContext;
	private final MemberFaceHelper mFaceHelper;
	private final Filter mFilter = new MembersListFilter();
	private final Set<Integer> mFilteredGroups;
	private final Set<Integer> mFilteredTeams;
	private final LayoutInflater mInflater;
	private List<Member> mFilteredList;
	private Member.RepositoryData mMembers;

	public MembersListAdapter(Context context, Member.RepositoryData members, Set<Integer> filteredGroups, Set<Integer> filteredTeams) {
		mContext = context;
		mMembers = members;
		mFilteredGroups = filteredGroups;
		mFilteredList = members.getMembers();
		mFilteredTeams = filteredTeams;

		mFaceHelper = MemberFaceHelper.getInstance(context);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return mFilteredList.size();
	}

	@Override
	public Filter getFilter() {
		return mFilter;
	}

	@Override
	public Object getItem(int position) {
		return mFilteredList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		if (convertView == null) {
			view = mInflater.inflate(R.layout.list_item_member, parent, false);
		} else {
			view = convertView;
		}

		Member member = mFilteredList.get(position);

		ImageView imageView = (ImageView) view.findViewById(R.id.member_face);
		List<String> faces = member.getFaces();
		if (!faces.isEmpty()) {
			mFaceHelper.picasso(faces)
				.resizeDimen(R.dimen.tmeit_members_list_face_size, R.dimen.tmeit_members_list_face_size)
				.centerInside()
				.placeholder(R.drawable.member_placeholder)
				.into(imageView);
		} else {
			mFaceHelper.placeholder().into(imageView);
		}

		TextView nameView = (TextView) view.findViewById(R.id.member_real_name);
		nameView.setText(member.getRealName());

		TextView titleTextView = (TextView) view.findViewById(R.id.member_title);
		titleTextView.setText(member.getTitleText(mContext, mMembers));

		TextView teamTextView = (TextView) view.findViewById(R.id.member_team);
		teamTextView.setText(member.getTeamText(mContext, mMembers));

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

	public void setContent(Member.RepositoryData members) {
		mMembers = members;
		notifyDataSetChanged();
	}

	private final class MembersListFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();
			List<Member> filteredList;

			boolean noFilters = mFilteredGroups.isEmpty() && mFilteredTeams.isEmpty();

			if (noFilters && TextUtils.isEmpty(constraint)) {
				filteredList = mMembers.getMembers();
			} else if (noFilters) {
				filteredList = new ArrayList<>();
				for (Member member : mMembers.getMembers()) {
					if (member.matches(constraint)) {
						filteredList.add(member);
					}
				}
			} else {
				filteredList = new ArrayList<>();
				for (Member member : mMembers.getMembers()) {
					if (member.matches(constraint) && isIncludedGroup(member.getGroupId()) && isIncludedTeam(member.getTeamId())) {
						filteredList.add(member);
					}
				}
			}

			results.values = filteredList;
			results.count = filteredList.size();
			return results;
		}

		@Override
		@SuppressWarnings("unchecked")
		protected void publishResults(CharSequence constraint, FilterResults results) {
			mFilteredList = (List<Member>) results.values;
			if (results.count > 0) {
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
		}

		private boolean isIncludedGroup(int groupId) {
			return (0 == groupId && mFilteredGroups.isEmpty()) || (0 != groupId && !mFilteredGroups.contains(groupId));
		}

		private boolean isIncludedTeam(int teamId) {
			return (0 == teamId && mFilteredTeams.isEmpty()) || (0 != teamId && !mFilteredTeams.contains(teamId));
		}
	}
}
