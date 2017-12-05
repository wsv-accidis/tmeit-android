package se.tmeit.app.ui.internalEvents;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import org.florescu.android.rangeseekbar.RangeSeekBar;

import se.tmeit.app.R;
import se.tmeit.app.model.InternalEventWorker;

/**
 * Dialog for working an internal event.
 */
public final class InternalEventWorkDialogFragment extends DialogFragment {
	private static final int WORK_OPTION_MAYBE = 1;
	private static final int WORK_OPTION_NO = 2;
	private static final int WORK_OPTION_YES = 0;
	private static final int MAX_COMMENT_LENGTH = 200;
	private final RangeChangedListener mRangeChangedListener = new RangeChangedListener();
	private final SaveButtonClickedListener mSaveClickedListener = new SaveButtonClickedListener();
	private final OptionsSpinnerChangedListener mSpinnerChangedListener = new OptionsSpinnerChangedListener();
	private EditText mComment;
	private InternalEventWorkDialogListener mListener;
	private int mRangeEnd;
	private int mRangeStart;
	private RadioButton mWorkBetweenRadio;
	private String mWorkBetweenString;
	private View mWorkLayout;
	private int mWorking;

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		mWorkBetweenString = getString(R.string.event_work_between);

		final Bundle args = getArguments();
		boolean isSaved = args.getBoolean(InternalEventWorker.Keys.IS_SAVED, false);
		boolean hasRange = args.containsKey(InternalEventWorker.Keys.RANGE_START) && args.containsKey(InternalEventWorker.Keys.RANGE_END);
		mRangeStart = hasRange ? args.getInt(InternalEventWorker.Keys.RANGE_START) : (InternalEventWorker.RANGE_MIN_HOUR + 2);
		mRangeEnd = hasRange ? args.getInt(InternalEventWorker.Keys.RANGE_END) : (InternalEventWorker.RANGE_MAX_HOUR - 2);
		int working = args.containsKey(InternalEventWorker.Keys.WORKING) ? args.getInt(InternalEventWorker.Keys.WORKING) : WORK_OPTION_YES;
		String comment = args.getString(InternalEventWorker.Keys.COMMENT);

		LayoutInflater inflater = getActivity().getLayoutInflater();
		@SuppressLint("InflateParams")
		View view = inflater.inflate(R.layout.dialog_internal_event_work, null);

		mComment = (EditText) view.findViewById(R.id.event_work_comment);
		mComment.setText(comment.substring(0, MAX_COMMENT_LENGTH));

		mWorkLayout = view.findViewById(R.id.event_work_layout);
		RadioButton workDontKnowRadio = (RadioButton) view.findViewById(R.id.event_work_dont_know);
		mWorkBetweenRadio = (RadioButton) view.findViewById(R.id.event_work_between);
		refreshWorkBetweenText();

		if (hasRange || !isSaved) {
			mWorkBetweenRadio.setChecked(true);
		} else {
			workDontKnowRadio.setChecked(true);
		}

		@SuppressWarnings("unchecked")
		RangeSeekBar<Integer> rangeBar = (RangeSeekBar<Integer>) view.findViewById(R.id.event_work_range);
		rangeBar.setSelectedMinValue(mRangeStart);
		rangeBar.setSelectedMaxValue(mRangeEnd);
		rangeBar.setNotifyWhileDragging(true);
		rangeBar.setOnRangeSeekBarChangeListener(mRangeChangedListener);

		Spinner optionsSpinner = (Spinner) view.findViewById(R.id.event_work_options);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.event_work_options, R.layout.spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		optionsSpinner.setAdapter(adapter);
		optionsSpinner.setOnItemSelectedListener(mSpinnerChangedListener);
		optionsSpinner.setSelection(getOptionsIndexFromWorking(working));

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(view)
			.setPositiveButton(R.string.event_save, mSaveClickedListener)
			.setNegativeButton(android.R.string.cancel, null);

		return builder.create();
	}

	public void setListener(InternalEventWorkDialogListener listener) {
		mListener = listener;
	}

	private static int getOptionsIndexFromWorking(int working) {
		switch (InternalEventWorker.Working.fromInt(working)) {
			case YES:
				return WORK_OPTION_YES;
			case MAYBE:
				return WORK_OPTION_MAYBE;
			default:
				return WORK_OPTION_NO;
		}
	}

	private static InternalEventWorker.Working getWorkingFromOptionsIndex(int index) {
		switch (index) {
			case WORK_OPTION_YES:
				return InternalEventWorker.Working.YES;
			case WORK_OPTION_MAYBE:
				return InternalEventWorker.Working.MAYBE;
			default:
				return InternalEventWorker.Working.NO;
		}
	}

	private void refreshWorkBetweenText() {
		if (null != mWorkBetweenRadio) {
			mWorkBetweenRadio.setText(String.format(mWorkBetweenString, (mRangeStart % 24), (mRangeEnd % 24)));
		}
	}

	public interface InternalEventWorkDialogListener {
		void saveClicked(InternalEventWorker worker);
	}


	private final class OptionsSpinnerChangedListener implements AdapterView.OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			mWorking = position;
			boolean showRange = (position != WORK_OPTION_NO);
			mWorkLayout.setVisibility(showRange ? View.VISIBLE : View.GONE);
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
		}
	}

	private final class RangeChangedListener implements RangeSeekBar.OnRangeSeekBarChangeListener<Integer> {
		@Override
		public void onRangeSeekBarValuesChanged(RangeSeekBar<?> rangeSeekBar, Integer one, Integer two) {
			mRangeStart = one;
			mRangeEnd = two;
			refreshWorkBetweenText();
			mWorkBetweenRadio.setChecked(true);
		}
	}

	private final class SaveButtonClickedListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			if (mRangeStart >= mRangeEnd) {
				Toast toast = Toast.makeText(getContext(), getString(R.string.event_work_zero_hours), Toast.LENGTH_LONG);
				toast.show();
				return;
			}

			if (null == mListener) {
				return;
			}

			InternalEventWorker.Builder builder = InternalEventWorker.builder()
				.setComment(mComment.getText().toString())
				.setWorking(getWorkingFromOptionsIndex(mWorking));

			if (mWorkBetweenRadio.isChecked()) {
				builder.setRangeStart(mRangeStart);
				builder.setRangeEnd(mRangeEnd);
			}

			mListener.saveClicked(builder.build());
		}
	}
}
