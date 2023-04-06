package com.magic.eventcalendar;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class CalendarFragment extends Fragment {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    View view;
    String uid, currentDate, iUID, iCategory;
    String categoryFilter = "イベント";
    ArrayList<String> categoryEN;
    ArrayList<String> categoryJP;
    int iPersonal;
    int personalFlag = 1;
    Spinner spinnerCategory;
    Switch switchPersonal;
    ImageButton btnClear;

    public CalendarFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        getUid();
        super.onResume();
        BottomNavigationView navigationView = getActivity().findViewById(R.id.bottom_navigation);
        navigationView.getMenu().getItem(0).setChecked(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.calendar, container, false);

        findView();
        getUid();
        setCategorySpinner();
        listChanges();
        switchPersonal();

        BottomNavigationView navigationView = getActivity().findViewById(R.id.bottom_navigation);
        navigationView.getMenu().getItem(0).setChecked(true);

        btnClear.setOnClickListener(v -> {
            setClear();
        });

        return view;
    }

    public void findView() {
        spinnerCategory = (Spinner) view.findViewById(R.id.calendar_category);
        switchPersonal = (Switch) view.findViewById(R.id.calendar_personal);
        btnClear = (ImageButton) view.findViewById(R.id.calendar_clear);

        Resources res = getResources();
        categoryEN = new ArrayList<>(Arrays.asList(res.getStringArray(R.array.categoryEN)));
        categoryJP = new ArrayList<>(Arrays.asList(res.getStringArray(R.array.categoryJP)));

    }


    public void setClear() {
        // 強制的にスイッチとスピナーを操作する
        switchPersonal.setChecked(false);
        spinnerCategory.setSelection(0);
    }


    private void switchPersonal() {
        // Personalフィルター
        switchPersonal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    personalFlag = 0;
                } else {
                    personalFlag = 1;
                }
                listChanges();
            }
        });
    }

    public void getUid() {
        // Get UID
        try {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            uid = user.getUid();
        } catch (Exception e) {
            Log.d("genki", "firstOpen|getUid");
            uid = "GUEST";
//            Intent intent = new Intent(getActivity(), MypageLogin.class);
//            startActivity(intent);
        }
    }

    // リストの内容を更新する
    public void listChanges() {
        RecyclerView recyclerView = view.findViewById(R.id.calendar_recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager rLayoutManager = new LinearLayoutManager(getActivity());

        recyclerView.setLayoutManager(rLayoutManager);

        List<String> itemDocid = new ArrayList<>();
        List<String> itemTitle = new ArrayList<>();
        List<String> itemCategory = new ArrayList<>();
        List<String> itemDate = new ArrayList<>();
        List<String> itemDateSimple = new ArrayList<>();
        List<String> itemPersonal = new ArrayList<>();
        List<String> itemTimestamp = new ArrayList<>();

        // current date
        TimeZone tz = TimeZone.getTimeZone("Asia/Tokyo");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        sdf.setTimeZone(tz);
        currentDate = sdf.format(new Date());

        db.collection("event")
                .whereGreaterThanOrEqualTo("DateInt", Integer.parseInt(currentDate))
                .orderBy("DateInt")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    iUID = document.getData().get("uid").toString();
                                    iPersonal = Integer.parseInt(document.getData().get("Personal").toString());
                                    iCategory = categoryJP.get(categoryEN.indexOf(document.getData().get("Category").toString()));
                                    // 2つ目の条件をつける
                                    if (iUID.equals(uid) == true) {
                                        if (iPersonal <= personalFlag) {
                                            if (categoryFilter.equals(iCategory) || categoryFilter.equals(getString(R.string.cat_event))) {
                                                itemDocid.add(document.getId());
                                                itemTitle.add(document.getData().get("Title").toString());
                                                itemCategory.add(document.getData().get("Category").toString());
                                                itemDate.add(document.getData().get("Date").toString());
                                                itemDateSimple.add(document.getData().get("DateSimple").toString());
                                                itemPersonal.add(document.getData().get("Personal").toString());
                                                itemTimestamp.add(document.getData().get("Timestamp").toString());
                                            }
                                        }
                                    }
                                }
                                CalendarAdapter adapter = new CalendarAdapter(itemDocid, itemTitle, itemCategory, itemDate, itemDateSimple,
                                        itemPersonal, itemTimestamp, uid);
                                recyclerView.setAdapter(adapter);
                            } else {
                                Log.d("genki", "Error getting documents: ", task.getException());
                            }
                        }
                });
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
                categoryFilter = (String) spinnerCategory.getSelectedItem();
                listChanges();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                categoryFilter = "Event";
            }
        });

    }


}