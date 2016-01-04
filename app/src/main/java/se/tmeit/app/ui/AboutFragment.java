package se.tmeit.app.ui;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import se.tmeit.app.R;
import se.tmeit.app.utils.AndroidUtils;

/**
 * Fragment which displays information about the app.
 */
public final class AboutFragment extends Fragment implements MainActivity.HasTitle, MainActivity.HasNavigationItem {
	@Override
	public int getItemId() {
		return R.id.nav_about;
	}

	@Override
	public int getTitle() {
		return R.string.about_nav_title;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_about, container, false);

		TextView aboutText = (TextView) view.findViewById(R.id.about_text);
		aboutText.setMovementMethod(LinkMovementMethod.getInstance());

		// TODO Use format string
		TextView versionText = (TextView) view.findViewById(R.id.version_text);
		versionText.setText("v" + AndroidUtils.getAppVersionName(getActivity()));

		return view;
	}
}
