package com.specknet.pdiotapp.utils;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.specknet.pdiotapp.HistoryDisplayActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker.
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it.
        return new DatePickerDialog(requireContext(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Log the selected date to check if it's being set correctly
        Log.d("DatePickerFragment", "Selected Date: " + year + "-" + (month + 1) + "-" + day);

        // Format the date
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        final String formattedDate = dateFormat.format(cal.getTime());

        // Update the UI on the main thread
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Update UI elements here
                ((HistoryDisplayActivity) getActivity()).processDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            }
        });}
}
