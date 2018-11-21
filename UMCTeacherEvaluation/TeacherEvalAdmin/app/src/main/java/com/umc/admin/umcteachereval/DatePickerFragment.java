package com.umc.admin.umcteachereval;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment implements OnDateSetListener {

    DatePickerListener datePickerListener;
    int resId;

    public static DialogFragment newInstance(DatePickerListener listener, int resId) {
        DatePickerFragment fragment = new DatePickerFragment();
        fragment.setDatePickerListener(listener, resId);
        return fragment;
    }


    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth);

        long timeInMillis = calendar.getTimeInMillis();
        notifyDatePickerListener(timeInMillis, resId);
    }

    private void notifyDatePickerListener(long timeInMillis, int resId) {
        if (this.datePickerListener != null) {
            this.datePickerListener.onDateSet(timeInMillis, resId);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    private void setDatePickerListener(DatePickerListener listener, int resId) {
        this.datePickerListener = listener;
        this.resId = resId;
    }


    public interface DatePickerListener {
        void onDateSet(long date, int resId);
    }
}
