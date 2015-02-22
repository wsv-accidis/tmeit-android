package se.tmeit.app.ui;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import se.tmeit.app.R;

/**
 * Fragment which displays information about the app.
 */
public final class AboutFragment extends MainActivity.MainActivityFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    protected int getTitle() {
        return R.string.about_title;
    }
}
