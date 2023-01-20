package com.magic.eventcalendar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;

public class CreateDialog extends DialogFragment {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    View dialogLayout;
    String docId, title, place, description;
    Button btnBack, btnGoogleCal, btnCalendar;
    Integer dateInt, time;
    Boolean allDay = true;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        dialogLayout = inflater.inflate(R.layout.create_dialog, null);

        builder.setView(dialogLayout)
                .setPositiveButton("", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

        // 値を受け取る
        docId = getArguments().getString("docId", "");

        db.collection("event")
                .whereEqualTo(FieldPath.documentId(),docId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                title = document.getData().get("Title").toString();
                                dateInt = Integer.parseInt(document.getData().get("DateInt").toString());
                                time = Integer.parseInt(document.getData().get("Time").toString());
                                place = document.getData().get("Place").toString();
                                description = document.getData().get("Description").toString();
                            }
                        } else {
                            Log.d("genki", "Error getting documents: ", task.getException());
                        }

                        if (time != 0) {
                            allDay = false;
                        }

                    }
                });

        btnBack = (Button) dialogLayout.findViewById(R.id.create_dialog_btn_back);
        btnGoogleCal = (Button) dialogLayout.findViewById(R.id.create_dialog_btn_google_calendar);
        btnCalendar = (Button) dialogLayout.findViewById(R.id.create_dialog_btn_calendar);

        btnBack.setOnClickListener(v -> {
            dismiss();
        });

        btnGoogleCal.setOnClickListener(v -> {
            calendarAppIntent();
            dismiss();
        });

        btnCalendar.setOnClickListener(v -> {
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.addToBackStack(null);

            fragmentTransaction.replace(R.id.container, new CalendarFragment());
            fragmentTransaction.commit();

            BottomNavigationView navigationView = getActivity().findViewById(R.id.bottom_navigation);
            navigationView.getMenu().getItem(0).setChecked(true);

            dismiss();
        });



        return builder.create(); // returnを後で入れる

    }


    public void calendarAppIntent() {
        Calendar beginTime = Calendar.getInstance();
        Calendar endTime = Calendar.getInstance();

        int year = (int) Math.floor(dateInt/10000);
        int month = (int) Math.floor(dateInt/100) - year*100;
        int date = dateInt - (year*10000 + month*100);

        int timeFromHour = (int) Math.floor(time/1000000);
        int timeFromMinute = (int) Math.floor(time/10000) - timeFromHour*100;
        int timeToHour = (int) Math.floor(time/100) - (timeFromHour*10000 + timeFromMinute*100);
        int timeToMinute = time - (timeFromHour*1000000 + timeFromMinute*10000 + timeToHour*100);

        beginTime.set(year, month-1, date, timeFromHour, timeFromMinute);
        endTime.set(year, month-1, date, timeToHour, timeToMinute);
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, allDay)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                .putExtra(CalendarContract.Events.TITLE, title)
                .putExtra(CalendarContract.Events.DESCRIPTION, description)
                .putExtra(CalendarContract.Events.EVENT_LOCATION, place)
                .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);
//                .putExtra(Intent.EXTRA_EMAIL, "kingetsugenki@gmail.com");
        startActivity(intent);
    }
}
