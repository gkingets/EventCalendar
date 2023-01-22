package com.magic.eventcalendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

public class CalendarEdit extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    View dialogLayout;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ArrayAdapter<CharSequence> adapterCategory;
    String iDocId, docId, title, category, date, dateSimple,
            place, description, uid, flagDate;
    ArrayList<String> categoryEN;
    ArrayList<String> categoryJP;
    TextInputEditText textTitle, textPlace, textDescription;
    TextView textDate;
    ImageView categoryImage, deleteImage, copyImage, calendarImage, backImage;
    Spinner spinnerCategory;
    Integer spinnerPosition, personalFlag, dateInt, time;
    Switch switchPersonal;
    Button editBtn;
    Boolean allDay = true;
    Integer flagUndecided = 0;
    LinearLayout timePickerLayout, dateLayout;
    MaterialButtonToggleGroup materialButtonToggleGroup;
    TimePicker timePickerFrom, timePickerTo, textTimeFrom, textTimeTo;

    @SuppressLint("MissingInflatedId")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        dialogLayout = inflater.inflate(R.layout.calendar_edit, null);

        findView(dialogLayout);

        getDataFromFireStore();
        getUid();
        getDocId();

        setCategorySpinner();
        setCalendar();
        setToggleButton();
        setPersonalSwitch();
//        setHideSoftKeyboard(dialogLayout);


        // Editクリック
        editBtn.setOnClickListener(v -> {
            clickEdit();
        });

        // DeleteImageクリック
        deleteImage.setOnClickListener(v -> {
            clickDelete(docId);
        });

        // CalendarImageクリック
        calendarImage.setOnClickListener(v -> {
            calendarIntent();
        });

        // BackImageクリック
        backImage.setOnClickListener(v -> {
            dismiss();
        });


        builder.setView(dialogLayout)
                .setPositiveButton("", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        return builder.create(); // returnを後で入れる
    }

    private void setPersonalSwitch() {
        // スイッチの操作
        switchPersonal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    personalFlag = 1;
                } else {
                    personalFlag = 0;
                }
            }
        });
    }

    private void setToggleButton() {
        // Toggle Button のクリック処理
        materialButtonToggleGroup.setSelectionRequired(true);
        materialButtonToggleGroup.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (isChecked) {
                    if (checkedId == R.id.edit_toggle_all_day) {
                        dateLayout.setVisibility(View.VISIBLE);
                        timePickerLayout.setVisibility(View.GONE);
                        flagDate = "allDay";
                    } else if (checkedId == R.id.edit_toggle_set_time) {
                        dateLayout.setVisibility(View.VISIBLE);
                        timePickerLayout.setVisibility(View.VISIBLE);
                        flagDate = "time";
                    } else if (checkedId == R.id.edit_toggle_undecided) {
                        dateLayout.setVisibility(View.GONE);
                        timePickerLayout.setVisibility(View.GONE);
                        flagDate = "undecided";
                    }
                }
            }
        });
    }

    private void getDataFromFireStore() {
        // DBからuidとtimestampをもとにデータを取得
        db.collection("event")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                iDocId = document.getId();
                                // 2つ目の条件をつける
                                if (docId.equals(iDocId)) {
                                    title = document.getData().get("Title").toString();
                                    category = document.getData().get("Category").toString();
                                    date = document.getData().get("Date").toString();
                                    dateInt = Integer.parseInt(document.getData().get("DateInt").toString());
                                    dateSimple = document.getData().get("DateSimple").toString();
                                    time = Integer.parseInt(document.getData().get("Time").toString());
                                    place = document.getData().get("Place").toString();
                                    description = document.getData().get("Description").toString();
                                    personalFlag = Integer.parseInt(document.getData().get("Personal").toString());
                                }
                            }
                        } else {
                            Log.d("genki", "Error getting documents: ", task.getException());
                        }
                        if (personalFlag == 1) {
                            switchPersonal.setChecked(true);
                        }
                        if (time != 0) {
                            allDay = false;
                        }
                        setText(dialogLayout);
                    }
                });
    }

    private void setCategorySpinner() {
        // Category Spinner
        adapterCategory = ArrayAdapter.createFromResource(getActivity().getBaseContext(),
                R.array.categoryJP, android.R.layout.simple_spinner_item);
        adapterCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapterCategory);
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int catIndex = categoryJP.indexOf(spinnerCategory.getSelectedItem().toString());
                int res = getResources().getIdentifier(categoryEN.get(catIndex).toLowerCase(Locale.ROOT), "drawable", getActivity().getPackageName());
                categoryImage.setImageResource(res);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    //DatePickerFragment で日付がセットされたときにtextViewに取得した日付を代入する
    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
        textDate.setText(String.format("%d/%d/%d", year, month + 1, dayOfMonth));
        dateInt = Integer.parseInt(String.format("%02d%02d%02d", year, month + 1, dayOfMonth));
        dateSimple = String.format("%2d/%2d", month + 1, dayOfMonth);
    }

    private void findView(View dialogLayout) {
        textTitle = (TextInputEditText) dialogLayout.findViewById(R.id.edit_title);
        spinnerCategory = (Spinner) dialogLayout.findViewById(R.id.edit_category);
        textDate = (TextView) dialogLayout.findViewById(R.id.edit_date);
        textPlace = (TextInputEditText) dialogLayout.findViewById(R.id.edit_place);
        textDescription = (TextInputEditText) dialogLayout.findViewById(R.id.edit_description);
        switchPersonal = (Switch) dialogLayout.findViewById(R.id.edit_personal);
        categoryImage = (ImageView) dialogLayout.findViewById(R.id.edit_image);
        editBtn = (Button) dialogLayout.findViewById(R.id.edit_save);
        deleteImage = (ImageView) dialogLayout.findViewById(R.id.edit_delete);
        copyImage = (ImageView) dialogLayout.findViewById(R.id.edit_copy);
        calendarImage = (ImageView) dialogLayout.findViewById(R.id.edit_calendar);
//        textDate = dialogLayout.findViewById(R.id.edit_date);

        timePickerFrom = (TimePicker) dialogLayout.findViewById(R.id.edit_timepicker_from); // initiate a time picker
        timePickerTo = (TimePicker) dialogLayout.findViewById(R.id.edit_timepicker_to); // initiate a time picker
        timePickerLayout = (LinearLayout) dialogLayout.findViewById(R.id.edit_timepicker_layout);
        dateLayout = (LinearLayout) dialogLayout.findViewById(R.id.edit_date_layout);
        materialButtonToggleGroup = dialogLayout.findViewById(R.id.edit_toggle_group);
        backImage = (ImageView) dialogLayout.findViewById(R.id.edit_back);

        Resources res = getResources();
        categoryEN = new ArrayList<>(Arrays.asList(res.getStringArray(R.array.categoryEN)));
        categoryJP = new ArrayList<>(Arrays.asList(res.getStringArray(R.array.categoryJP)));
    }

    private void setText(View dialogLayout) {
        // Set Text
        textTitle.setText(title);
        textDate.setText(date);
        textPlace.setText(place);
        textDescription.setText(description);

        // FireStoreから取得したTimeをセット
        int timeFromHour = getTimeFromHour();
        int timeFromMinute = getTimeFromMinute(timeFromHour);
        int timeToHour = getTimeToHour(timeFromHour, timeFromMinute);
        int timeToMinute = getTimeToMinute(timeFromHour, timeFromMinute, timeToHour);

        timePickerFrom.setHour(timeFromHour); // .setText(String.format("%hh:%mm", 1,1));
        timePickerFrom.setMinute(timeFromMinute);
        timePickerTo.setHour(timeToHour);
        timePickerTo.setMinute(timeToMinute);

        timePickerFrom.setIs24HourView(true); // set 24 hours mode for the time picker
        timePickerTo.setIs24HourView(true); // set 24 hours mode for the time picker

        // 初期設定のダイアログをそれぞれ設定にする
        if (time > 0) {
            materialButtonToggleGroup.check(R.id.edit_toggle_set_time);
        } else if (dateInt > 30000000) {
            materialButtonToggleGroup.check(R.id.edit_toggle_undecided);
        } else {
            materialButtonToggleGroup.check(R.id.edit_toggle_all_day);
        }


        // Set Spinner
        spinnerPosition = adapterCategory.getPosition(categoryJP.get(categoryEN.indexOf(category))); // Get position number from Firestore Category
        spinnerCategory.setSelection(spinnerPosition);

        int catIndex = categoryJP.indexOf(spinnerCategory.getSelectedItem().toString());
        int res = getResources().getIdentifier(categoryEN.get(catIndex).toLowerCase(Locale.ROOT), "drawable", getActivity().getPackageName());
        categoryImage.setImageResource(res);
    }

    public void getUid() {
        // Get UID
        try {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            uid = user.getUid();
        } catch (Exception e) {
            Intent intent = new Intent(getActivity(), MypageLogin.class);
            startActivity(intent);
        }
    }

    public void getDocId() {
        docId = getArguments().getString("docId","");
    }

    public void setCalendar() {

        // Set Calendar
        textDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int year = (int) Math.floor(dateInt/10000);
                int month = (int) Math.floor(dateInt/100) - year*100;
                int date = dateInt - (year*10000 + month*100);

                CalendarEditDatePicker datePicker = new CalendarEditDatePicker(year,month-1,date);
                datePicker.setTargetFragment(CalendarEdit.this, 0);
                datePicker.show(getParentFragmentManager(), "date picker");
            }
        });
    }

    public void clickEdit() {
        findView(dialogLayout);

        title = textTitle.getText().toString();
        category = categoryEN.get(categoryJP.indexOf(spinnerCategory.getSelectedItem()));
        date = textDate.getText().toString();
        place = textPlace.getText().toString();
        description = textDescription.getText().toString();
        // Time
        if (flagDate.equals("time")) {
            textTimeFrom = (TimePicker) dialogLayout.findViewById(R.id.edit_timepicker_from);
            textTimeTo = (TimePicker) dialogLayout.findViewById(R.id.edit_timepicker_to);
            Integer timeFromHour = textTimeFrom.getHour();
            Integer timeFromMinute = textTimeFrom.getMinute();
            Integer timeToHour = textTimeTo.getHour();
            Integer timeToMinute = textTimeTo.getMinute();
            time = timeFromHour * 1000000 +
                    timeFromMinute * 10000 +
                    timeToHour * 100 +
                    timeToMinute;

            // Empty
            if (timeFromHour*100+timeFromMinute > timeToHour*100+timeToMinute) {
                Toast.makeText(getActivity(), "Set the time correctly", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            time = 0;
        }

        // Empty
        if (TextUtils.isEmpty(title)) {
            textTitle.setError("Title cannot be empty");
            textTitle.requestFocus();
            return;
        }

        DocumentReference Ref = db.collection("event").document(docId);
        Ref.update(
                "Title", title,
                "Category", category,
                "DateInt", dateInt,
                "DateSimple", dateSimple,
                "Date", date,
                "Time", time,
                "Place", place,
                "Description", description,
                "Personal", personalFlag)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("genki", "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("genki", "Error updating document", e);
                    }
                });


        getActivity().recreate();
        dismiss();
    }

    public void clickDelete(String docId) {
        new AlertDialog.Builder(getContext())
                .setTitle("イベントを削除しますか？")
                .setPositiveButton( "はい", new  DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Func del = new Func();
                        del.deleteData(docId);
                        getActivity().recreate();
                        dismiss();
                    }
                })
                .setNeutralButton( "キャンセル", new  DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    public void calendarIntent() {
        Calendar beginTime = Calendar.getInstance();
        Calendar endTime = Calendar.getInstance();

        int year = (int) Math.floor(dateInt/10000);
        int month = (int) Math.floor(dateInt/100) - year*100;
        int date = dateInt - (year*10000 + month*100);

        int timeFromHour = getTimeFromHour();
        int timeFromMinute = getTimeFromMinute(timeFromHour);
        int timeToHour = getTimeToHour(timeFromHour, timeFromMinute);
        int timeToMinute = getTimeToMinute(timeFromHour, timeFromMinute, timeToHour);

        if (flagDate == "allDay") {
            allDay = false;
        }

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

    private int getTimeFromHour() {
        int timeFromHour = (int) Math.floor(time/1000000);
        return timeFromHour;
    }

    private int getTimeFromMinute(int timeFromHour) {
        int timeFromMinute = (int) Math.floor(time/10000) - timeFromHour *100;
        return timeFromMinute;
    }

    private int getTimeToHour(int timeFromHour, int timeFromMinute) {
        int timeToHour = (int) Math.floor(time/100) - (timeFromHour *10000 + timeFromMinute *100);
        return timeToHour;
    }

    private int getTimeToMinute(int timeFromHour, int timeFromMinute, int timeToHour) {
        int timeToMinute = time - (timeFromHour *1000000 + timeFromMinute *10000 + timeToHour *100);
        return timeToMinute;
    }

    // Keyboardを下げるFunc
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        if(inputMethodManager.isAcceptingText()){
            inputMethodManager.hideSoftInputFromWindow(
                    activity.getCurrentFocus().getWindowToken(),
                    0
            );
        }
    }

    // EditText以外をタッチしたらKeyboardを下げるFuncを起動する
    public void setHideSoftKeyboard(View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(getActivity());
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setHideSoftKeyboard(innerView);
            }
        }
    }


}
