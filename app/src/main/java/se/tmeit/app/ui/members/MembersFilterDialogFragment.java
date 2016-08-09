package se.tmeit.app.ui.members;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import java.util.Map;

import se.tmeit.app.R;

/**
 * Fragment allowing the user to modify the filters applied to the members list.
 */
public final class MembersFilterDialogFragment extends DialogFragment {
	public static final String ARG_TEAMS = "teams";
	public static final String ARG_GROUPS = "groups";

	@NonNull
	@Override
	@SuppressWarnings("unchecked")
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = getActivity().getLayoutInflater();

		@SuppressLint("InflateParams")
		View view = inflater.inflate(R.layout.dialog_members_filter, null);
		LinearLayout listLayout = (LinearLayout) view.findViewById(R.id.members_filter_list);

		Bundle args = getArguments();
		Map<Integer, String> teamsMap = (Map<Integer, String>) args.getSerializable(ARG_TEAMS);
		Map<Integer, String> groupsMap = (Map<Integer, String>) args.getSerializable(ARG_GROUPS);
		if (null == teamsMap || null == groupsMap) {
			throw new IllegalArgumentException("Dialog is missing required arguments.");
		}

		// Java generics are a horrible kludge, there is no nice way to avoid round-tripping via Object[] here
		Object[] teams = teamsMap.entrySet().toArray();
		Object[] groups = groupsMap.entrySet().toArray();
		int length = Math.max(teams.length, groups.length);
		for (int i = 0; i < length; i++) {
			Map.Entry<Integer, String> group = (i < groups.length ? (Map.Entry<Integer, String>) groups[i] : null);
			Map.Entry<Integer, String> team = (i < teams.length ? (Map.Entry<Integer, String>) teams[i] : null);

			ViewGroup row = (ViewGroup) inflater.inflate(R.layout.list_item_members_filter, listLayout, false);
			CheckBox checkBox1 = (CheckBox) row.findViewById(R.id.members_filter_check1);
			if (null != group) {
				checkBox1.setText(group.getValue());
			} else {
				checkBox1.setVisibility(View.INVISIBLE);
			}

			CheckBox checkBox2 = (CheckBox) row.findViewById(R.id.members_filter_check2);
			if (null != team) {
				checkBox2.setText(team.getValue());
			} else {
				checkBox2.setVisibility(View.INVISIBLE);
			}

			listLayout.addView(row);
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		AlertDialog dialog = builder.setView(view)
			.setPositiveButton(android.R.string.ok, null) // TODO Set listener which returns checked items
			.setNegativeButton(R.string.members_show_all, null)
			.create();

		Button showAllButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
		showAllButton.setOnClickListener(null); // TODO Set custom listener which does not dismiss

		return dialog;
	}
}
