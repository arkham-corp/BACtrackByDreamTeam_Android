package co.jp.dreamteam.bactrackbydreamteam2;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    private final TimePickerDialog.OnTimeSetListener listener;

    public TimePickerFragment(TimePickerDialog.OnTimeSetListener listener) {
        super();
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        String defaultValue = "";
        if (getArguments() != null) {
            defaultValue = getArguments().getString("default", "");
        }

        if (defaultValue.isEmpty()) {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
        }
        else
        {
            int hourOfDay = Integer.parseInt(defaultValue.substring(0, 2));
            int minute = Integer.parseInt(defaultValue.substring(3, 5));

            return new TimePickerDialog(getActivity(), this, hourOfDay, minute, DateFormat.is24HourFormat(getActivity()));
        }
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        listener.onTimeSet(view, hourOfDay, minute);
    }
}

