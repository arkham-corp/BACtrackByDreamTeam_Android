package co.jp.dreamteam.bactrackbydreamteam2;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    private final DatePickerDialog.OnDateSetListener listener;

    public DatePickerFragment(DatePickerDialog.OnDateSetListener listener) {
        super();
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        String defaultValue = "";
        if (getArguments() != null) {
            defaultValue = getArguments().getString("default", "");
        }

        if (defaultValue.isEmpty()) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(), this, year, month, day);
        } else {
            int year = Integer.parseInt(defaultValue.substring(0, 4));
            int month = Integer.parseInt(defaultValue.substring(5, 7)) - 1;
            int day = Integer.parseInt(defaultValue.substring(8, 10));

            return new DatePickerDialog(getActivity(), this, year, month, day);
        }
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        listener.onDateSet(view, year, month + 1, day);
    }
}
