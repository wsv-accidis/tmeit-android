package se.tmeit.app.ui;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import se.tmeit.app.R;

/**
 * Fragment which helps determine the minimum legal age to be served alcohol.
 */
public final class AgeCheckFragment extends Fragment implements MainActivity.HasTitle {
    private TextView mBirthDateText;
    private final Handler mHandler = new Handler();
    private final Runnable mUpdateRunner = new UpdateRunner();
    private static final int UPDATE_DELAY = 30000;
    private static final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    public int getTitle() {
        return R.string.age_check_nav_title;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_age_check, container, false);
        mBirthDateText = (TextView) view.findViewById(R.id.age_check_birthdate);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateBirthDate();
        mHandler.postDelayed(mUpdateRunner, UPDATE_DELAY);
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mUpdateRunner);
    }

    private void updateBirthDate() {
        if (isVisible() && null != mBirthDateText) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.YEAR, -18);
            mBirthDateText.setText(mDateFormat.format(calendar.getTime()));
        }
    }

    private final class UpdateRunner implements Runnable {
        @Override
        public void run() {
            updateBirthDate();
            mHandler.postDelayed(mUpdateRunner, UPDATE_DELAY);
        }
    }
}
