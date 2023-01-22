package com.magic.eventcalendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CreateFragment extends Fragment implements DatePickerDialog.OnDateSetListener{

    TextView textTitle, textDate, textPlace, textDescription;
    Integer flagPersonal = 0;
    Integer flagAllDay = 1;
    Integer flagUndecided = 0;
    Button btnSave;
    String title, category, date, place, description, uid, dateSimple, docId;
    ArrayList<String> categoryEN;
    ArrayList<String> categoryJP;
    Integer dateInt, time;
    TimePicker textTimeFrom, textTimeTo, timePickerFrom, timePickerTo;
    Spinner spinnerCategory;
    View view;
    ImageView image;
    Switch switchPersonal;
    LinearLayout timePickerLayout, dateLayout;
    MaterialButtonToggleGroup materialButtonToggleGroup;


    public CreateFragment() {
        // Required empty public constructor
    }

   @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.create, container, false);

        findView();
        getUid();
        setCategorySpinner();
        setRepeatSpinner();
        setCalendar();
        setTimePicker();
        setRepeatSwitch();
        setToggleButton();
        setPersonalSwitch();

        setHideSoftKeyboard(view);

        // Click Save
        btnSave.setOnClickListener(v -> {
            clickSave();
        });

        return view;
    }

    private void setPersonalSwitch() {
        // Personal Flag
        switchPersonal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    flagPersonal = 1;
                } else {
                    flagPersonal = 0;
                }
            }
        });
    }

    public void setToggleButton() {
        // Toggle Button のクリック処理
        materialButtonToggleGroup.setSelectionRequired(true);
        // 初期設定を終日にする
        materialButtonToggleGroup.check(R.id.create_toggle_all_day);

        materialButtonToggleGroup.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (isChecked) {
                    if (checkedId == R.id.create_toggle_all_day) {
                        dateLayout.setVisibility(View.VISIBLE);
                        timePickerLayout.setVisibility(View.GONE);
                        flagAllDay = 1;
                        flagUndecided = 0;
                        setCalendar();
                    } else if (checkedId == R.id.create_toggle_set_time) {
                        dateLayout.setVisibility(View.VISIBLE);
                        timePickerLayout.setVisibility(View.VISIBLE);
                        flagAllDay = 0;
                        flagUndecided = 0;
                        setCalendar();
                    } else if (checkedId == R.id.create_toggle_undecided) {
                        dateLayout.setVisibility(View.GONE);
                        timePickerLayout.setVisibility(View.GONE);
                        flagAllDay = 1;
                        flagUndecided = 1;
                        setCalendar();
                    }
                }
            }
        });
    }

    public void setRepeatSwitch() {
        // Repeat Switch
        LinearLayout repeatDate = (LinearLayout) view.findViewById(R.id.create_repeat_layout);
        Switch switchRepeat = (Switch) view.findViewById(R.id.create_repeat);
        switchRepeat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    repeatDate.setVisibility(View.VISIBLE);
                } else {
                    repeatDate.setVisibility(View.GONE);
                }
            }
        });
    }

    public void setTimePicker() {
        // Time Picker
        timePickerFrom.setIs24HourView(true); // set 24 hours mode for the time picker
        timePickerFrom.setMinute(0);
        timePickerTo.setIs24HourView(true); // set 24 hours mode for the time picker
        timePickerTo.setMinute(0);
    }

    public void setRepeatSpinner() {
        // Repeat Spinner
        Spinner spinnerRepeat = (Spinner) view.findViewById(R.id.create_repeat_option);
        ArrayAdapter<CharSequence> adapterRepeat = ArrayAdapter.createFromResource(getActivity().getBaseContext(),
                R.array.repeat_list, android.R.layout.simple_spinner_item);
        adapterRepeat.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRepeat.setAdapter(adapterRepeat);
    }

    public void setCategorySpinner() {

        // Category Spinner
        ArrayAdapter<CharSequence> adapterCategory = ArrayAdapter.createFromResource(getActivity().getBaseContext(),
                R.array.categoryJP, android.R.layout.simple_spinner_item);
        adapterCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapterCategory);
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int catIndex = categoryJP.indexOf(spinnerCategory.getSelectedItem().toString());
                int res = getResources().getIdentifier(categoryEN.get(catIndex).toLowerCase(Locale.ROOT), "drawable", getActivity().getPackageName());
                image.setImageResource(res);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
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

    public void findView() {
        image = (ImageView) view.findViewById(R.id.create_image);
        spinnerCategory = (Spinner) view.findViewById(R.id.create_category);
        switchPersonal = (Switch) view.findViewById(R.id.create_personal);
        timePickerFrom = (TimePicker) view.findViewById(R.id.create_timepicker_from); // initiate a time picker
        timePickerTo = (TimePicker) view.findViewById(R.id.create_timepicker_to); // initiate a time picker
        timePickerLayout = (LinearLayout) view.findViewById(R.id.create_timepicker_layout);
        dateLayout = (LinearLayout) view.findViewById(R.id.create_date_layout);
        materialButtonToggleGroup = view.findViewById(R.id.create_toggle_group);
        btnSave = (Button) view.findViewById(R.id.create_save);
        textDate = view.findViewById(R.id.create_date);

        Resources res = getResources();
        categoryEN = new ArrayList<>(Arrays.asList(res.getStringArray(R.array.categoryEN)));
        categoryJP = new ArrayList<>(Arrays.asList(res.getStringArray(R.array.categoryJP)));

    }


    public void setCalendar() {
        // Set Calendar
        textDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int year = (int) Math.floor(dateInt/10000);
                int month = (int) Math.floor(dateInt/100) - year*100;
                int date = dateInt - (year*10000 + month*100);
                CreateDatePicker datePicker = new CreateDatePicker(year,month-1,date);
                datePicker.setTargetFragment(CreateFragment.this, 0);
                datePicker.show(getParentFragmentManager(), "date picker");
            }
        });
        Calendar c = Calendar.getInstance();
        if (flagUndecided == 0) {
            // 最初の日付を変数に設定する
            date = String.format("%d/%d/%d", c.get(Calendar.YEAR), c.get(Calendar.MONTH)+1, c.get(Calendar.DAY_OF_MONTH));
            dateInt = Integer.parseInt(String.format("%02d%02d%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH)+1, c.get(Calendar.DAY_OF_MONTH)));
            dateSimple = String.format("%2d/%2d", c.get(Calendar.MONTH)+1, c.get(Calendar.DAY_OF_MONTH));
            textDate.setText(date);
        } else {
            date = String.format("%d/%d/%d", c.get(Calendar.YEAR)+100, c.get(Calendar.MONTH)+1, c.get(Calendar.DAY_OF_MONTH));
            dateInt = Integer.parseInt(String.format("%02d%02d%02d", c.get(Calendar.YEAR)+1000, c.get(Calendar.MONTH)+1, c.get(Calendar.DAY_OF_MONTH)));
            dateSimple = "-/-"; // String.format("%02d/%02d", 0, 0);
        }

    }

    //DatePickerFragment で日付がセットされたときにtextViewに取得した日付を代入する
    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
        textDate.setText(String.format("%d/%d/%d", year, month + 1, dayOfMonth));
        dateInt = Integer.parseInt(String.format("%02d%02d%02d", year, month + 1, dayOfMonth));
        dateSimple = String.format("%2d/%2d", month + 1, dayOfMonth);
    }

    public void clickSave(){

        // Timestamp
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
//        Integer timeStampInt = Integer.parseInt(timeStamp);

        // Title
        textTitle = (TextView) view.findViewById(R.id.create_title);
        title = textTitle.getText().toString();

        // Category
        category = categoryEN.get(categoryJP.indexOf(spinnerCategory.getSelectedItem()));

                // Date
        textDate = (TextView) view.findViewById(R.id.create_date);
        date = textDate.getText().toString();

        // Time
        if (flagAllDay == 0) {
            textTimeFrom = (TimePicker) view.findViewById(R.id.create_timepicker_from);
            textTimeTo = (TimePicker) view.findViewById(R.id.create_timepicker_to);
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

        // Place
        textPlace = (TextView) view.findViewById(R.id.create_place);
        place = textPlace.getText().toString();

        // Description
        textDescription = (TextView) view.findViewById(R.id.create_description);
        description = textDescription.getText().toString();

        // Empty
        if (TextUtils.isEmpty(title)) {
            textTitle.setError("Title cannot be empty");
            textTitle.requestFocus();
            return;
        }


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> event = new HashMap<>();
        event.put("uid", uid);
        event.put("Timestamp", timeStamp);
        event.put("Title", title);
        event.put("Category", category);
        event.put("DateInt", dateInt);
        event.put("DateSimple", dateSimple);
        event.put("Date", date);
        event.put("Time", time);
        event.put("Place", place);
        event.put("Description", description);
        event.put("Personal", flagPersonal);
        event.put("Reuse", 0);
        event.put("Rating", 0);

        // Add a new document with a generated ID
        db.collection("event")
                .add(event)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("genki", "DocumentSnapshot added with ID: " + documentReference.getId());
                        // dialogを生成
                        dialog(documentReference.getId());
                        deleteText();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("genki", "Error adding document", e);
                    }
                });

        // キーボードを非表示にする
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

    }

    // dialogインスタンスを生成して、docIdを渡す
    public void dialog(String docId) {
        CreateDialog dialogRight = new CreateDialog();
        // 渡す値をセット
        Bundle args = new Bundle();

        args.putString("docId", docId);

        dialogRight.setArguments(args);
        dialogRight.show(getActivity().getSupportFragmentManager(), "my_dialog");

    }

    public void deleteText() {
        textTitle.setText("");
        textPlace.setText("");
        textDescription.setText("");

        title = "";
        place = "";
        description = "";

        textTitle.requestFocus();
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