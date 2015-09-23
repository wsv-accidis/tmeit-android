package se.tmeit.app.ui.internalEvents;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Spinner;

import org.florescu.android.rangeseekbar.RangeSeekBar;

import se.tmeit.app.R;
import se.tmeit.app.model.InternalEventWorker;

/**
 * Dialog for working an internal event.
 */
public final class InternalEventWorkDialogFragment extends DialogFragment {
    private int mRangeEnd;
    private int mRangeStart;
    private RadioButton mWorkBetweenRadio;
    private String mWorkBetweenString;
    private RadioButton mWorkDontKnowRadio;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mWorkBetweenString = getString(R.string.event_work_between);

        final Bundle args = getArguments();
        boolean isSaved = args.getBoolean(InternalEventWorker.Keys.IS_SAVED);
        boolean hasRange = args.containsKey(InternalEventWorker.Keys.RANGE_START) && args.containsKey(InternalEventWorker.Keys.RANGE_END);
        mRangeStart = hasRange ? args.getInt(InternalEventWorker.Keys.RANGE_START) : (InternalEventWorker.RANGE_MIN_HOUR + 2);
        mRangeEnd = hasRange ? args.getInt(InternalEventWorker.Keys.RANGE_END) : (InternalEventWorker.RANGE_MAX_HOUR - 2);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_internal_event_work, null);

        mWorkDontKnowRadio = (RadioButton) view.findViewById(R.id.event_work_dont_know);
        mWorkBetweenRadio = (RadioButton) view.findViewById(R.id.event_work_between);
        refreshWorkBetweenText();

        if(hasRange || !isSaved) {
            mWorkBetweenRadio.setChecked(true);
        } else {
            mWorkDontKnowRadio.setChecked(true);
        }

        @SuppressWarnings("unchecked")
        RangeSeekBar<Integer> rangeBar = (RangeSeekBar<Integer>) view.findViewById(R.id.event_work_range);
        rangeBar.setSelectedMinValue(mRangeStart);
        rangeBar.setSelectedMaxValue(mRangeEnd);
        rangeBar.setNotifyWhileDragging(true);
        rangeBar.setOnRangeSeekBarChangeListener(new OnRangeChangeListener());

        Spinner optionsSpinner = (Spinner) view.findViewById(R.id.event_work_options);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.event_work_options, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        optionsSpinner.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setPositiveButton(R.string.event_save, new SaveButtonClickedListener())
                .setNegativeButton(android.R.string.cancel, null);

        return builder.create();
    }

    private void refreshWorkBetweenText() {
        if (null != mWorkBetweenRadio) {
            mWorkBetweenRadio.setText(String.format(mWorkBetweenString, (mRangeStart % 24), (mRangeEnd % 24)));
        }
    }

    private final class OnRangeChangeListener implements RangeSeekBar.OnRangeSeekBarChangeListener<Integer> {
        @Override
        public void onRangeSeekBarValuesChanged(RangeSeekBar<?> rangeSeekBar, Integer one, Integer two) {
            mRangeStart = one;
            mRangeEnd = two;
            refreshWorkBetweenText();
        }
    }

    private final class SaveButtonClickedListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            /*
            if (null == mListener) {
                return;
            }

            ExternalEventAttendee attendee = new ExternalEventAttendee();
            attendee.setDateOfBirth(mDobText.getText().toString());
            attendee.setDrinkPreferences(mDrinkPrefsText.getText().toString());
            attendee.setFoodPreferences(mFoodPrefsText.getText().toString());
            attendee.setNotes(mNotesText.getText().toString());
            mListener.saveClicked(attendee);
            */
        }
    }
}
