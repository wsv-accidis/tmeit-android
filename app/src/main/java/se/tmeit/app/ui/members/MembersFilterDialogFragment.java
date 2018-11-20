package se.tmeit.app.ui.members;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import se.tmeit.app.R;

/**
 * Fragment allowing the user to modify the filters applied to the members list.
 */
public final class MembersFilterDialogFragment extends DialogFragment {
	public static final String ARG_GROUPS = "groups";
	public static final String ARG_GROUPS_FILTERED = "groups_filtered";
	public static final String ARG_TEAMS = "teams";
	public static final String ARG_TEAMS_FILTERED = "teams_filtered";
	private final List<CheckBox> mGroupBoxes = new ArrayList<>();
	private final List<CheckBox> mTeamBoxes = new ArrayList<>();
	private MembersFilterDialogListener mListener;

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setRetainInstance(true);
		Bundle args = getArguments();

		Set<Integer> selectedGroups = getSelectedKeys(ARG_GROUPS_FILTERED, savedInstanceState, args);
		setSelected(mGroupBoxes, selectedGroups);

		Set<Integer> selectedTeams = getSelectedKeys(ARG_TEAMS_FILTERED, savedInstanceState, args);
		setSelected(mTeamBoxes, selectedTeams);
	}

	@NonNull
	@Override
	@SuppressWarnings("unchecked")
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = getActivity().getLayoutInflater();

		@SuppressLint("InflateParams")
		View view = inflater.inflate(R.layout.dialog_members_filter, null);
		LinearLayout listLayout = view.findViewById(R.id.members_filter_list);

		Bundle args = getArguments();
		Map<Integer, String> groupsMap = (Map<Integer, String>) args.getSerializable(ARG_GROUPS);
		Map<Integer, String> teamsMap = (Map<Integer, String>) args.getSerializable(ARG_TEAMS);
		if (null == groupsMap || null == teamsMap) {
			throw new IllegalArgumentException("Dialog is missing required arguments.");
		}

		// Java generics are a horrible kludge, there is no nice way to avoid round-tripping via Object[] here
		Object[] teams = teamsMap.entrySet().toArray();
		Object[] groups = groupsMap.entrySet().toArray();
		return createDialog(view, listLayout, groups, teams);
	}

	@Override
	public void onDestroyView() {
		// Workaround for Android issue 17423: https://code.google.com/p/android/issues/detail?id=17423
		if (getDialog() != null && getRetainInstance()) {
			getDialog().setDismissMessage(null);
		}
		super.onDestroyView();
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(ARG_GROUPS_FILTERED, getSelected(mGroupBoxes));
		outState.putSerializable(ARG_TEAMS_FILTERED, getSelected(mTeamBoxes));
	}

	public void setDialogListener(MembersFilterDialogListener listener) {
		mListener = listener;
	}

	@NonNull
	@SuppressWarnings("unchecked")
	private AlertDialog createDialog(View view, LinearLayout listLayout, Object[] groups, Object[] teams) {
		mGroupBoxes.clear();
		mTeamBoxes.clear();

		LayoutInflater inflater = getActivity().getLayoutInflater();
		int length = Math.max(teams.length, groups.length);
		for (int i = 0; i < length; i++) {
			Map.Entry<Integer, String> group = (i < groups.length ? (Map.Entry<Integer, String>) groups[i] : null);
			Map.Entry<Integer, String> team = (i < teams.length ? (Map.Entry<Integer, String>) teams[i] : null);

			ViewGroup row = (ViewGroup) inflater.inflate(R.layout.list_item_members_filter, listLayout, false);
			CheckBox checkBox1 = row.findViewById(R.id.members_filter_check1);
			if (null != group) {
				checkBox1.setTag(group.getKey());
				checkBox1.setText(group.getValue());
				mGroupBoxes.add(checkBox1);
			} else {
				checkBox1.setVisibility(View.INVISIBLE);
			}

			CheckBox checkBox2 = row.findViewById(R.id.members_filter_check2);
			if (null != team) {
				checkBox2.setTag(team.getKey());
				checkBox2.setText(team.getValue());
				mTeamBoxes.add(checkBox2);
			} else {
				checkBox2.setVisibility(View.INVISIBLE);
			}

			listLayout.addView(row);
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		final AlertDialog dialog = builder.setView(view)
			.setPositiveButton(android.R.string.ok, new OkClickedListener())
			.setNegativeButton(R.string.members_show_all, null)
			.create();

		// Buttons are not instantiated until after show, so must do it this way
		// Using setOnClickListener instead of assigning in setNegativeButton keeps the dialog dismissing
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialogIf) {
				Button showAllButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
				showAllButton.setOnClickListener(new ShowAllClickedListener());
			}
		});

		return dialog;
	}

	private HashSet<Integer> getSelected(List<CheckBox> checkBoxes) {
		HashSet<Integer> tags = new HashSet<>();
		for (CheckBox c : checkBoxes) {
			if (!c.isChecked()) {
				tags.add((Integer) c.getTag());
			}
		}
		return tags;
	}

	@SuppressWarnings("unchecked")
	private static Set<Integer> getSelectedKeys(String arg, Bundle optBundle, Bundle reqBundle) {
		if (null != optBundle && optBundle.containsKey(arg)) {
			return (Set<Integer>) optBundle.getSerializable(arg);
		} else {
			return (Set<Integer>) reqBundle.getSerializable(arg);
		}
	}

	private void setSelected(List<CheckBox> checkBoxes, Set<Integer> values) {
		for (CheckBox c : checkBoxes) {
			Integer key = (Integer) c.getTag();
			c.setChecked(!values.contains(key));
		}
	}

	public interface MembersFilterDialogListener {
		void onDismiss(Set<Integer> groups, Set<Integer> teams);
	}

	private final class OkClickedListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialogInterface, int i) {
			if (null != mListener) {
				Set<Integer> groups = getSelected(mGroupBoxes);
				Set<Integer> teams = getSelected(mTeamBoxes);
				mListener.onDismiss(groups, teams);
			}
		}
	}

	private final class ShowAllClickedListener implements View.OnClickListener {
		@Override
		public void onClick(View view) {
			for (CheckBox c : mGroupBoxes) {
				c.setChecked(true);
			}
			for (CheckBox c : mTeamBoxes) {
				c.setChecked(true);
			}
		}
	}
}
