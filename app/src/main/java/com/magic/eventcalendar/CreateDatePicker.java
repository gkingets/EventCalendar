package com.magic.eventcalendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class CreateDatePicker extends DialogFragment {


    int dateYear;
    int dateMonth;
    int dateDate;

    // 引数で年月日を入れて、DatePickerがCurrent Dateにならないように設定
    public CreateDatePicker(int year, int month, int date) {
        dateYear = year;
        dateMonth = month;
        dateDate = date;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstantState){

        //デフォルトのタイムゾーンおよびロケールを使用してカレンダを取得
        final Calendar c = Calendar.getInstance();
//        今の日付を使うver
//        int year = c.get(Calendar.YEAR);
//        int month = c.get(Calendar.MONTH);
//        int day = c.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(), (DatePickerDialog.OnDateSetListener) getTargetFragment(),  dateYear, dateMonth, dateDate);

//        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), (DatePickerDialog.OnDateSetListener)getActivity(), year, month, day); //this はonDateSetListener
//        return datePickerDialog;
    }
}
