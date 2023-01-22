package com.magic.eventcalendar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.AggregateQuery;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class MypageFragment extends Fragment {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextView textWelcome, textCount;
    Button btnLogin, btnLogout;
    ImageView imageInfo;
    String uid, currentDate, iUID, iCategory, iMonth;
    String[] category_list;
    ArrayList<String> categoryEN;
    ArrayList<String> categoryJP;
    Integer bar1 = 0, bar2 = 0, bar3 = 0, bar4 = 0, bar5 = 0, bar6 = 0;
    PieChart pieChart;
    BarChart barChart;
    BarData barData;
    BarDataSet barDataSet;
    ArrayList barEntriesArrayList;
    Map<String, Integer> categoryMap = new HashMap<>();
    ArrayList<Integer> colors;
    int num = 0;
    View view;
    List<String> monthList, monthLabel;

    public MypageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        getUid();
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.mypage, container, false);

        findView();


        // Get uid
        try {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            uid = user.getUid();
        } catch (Exception e) {
            Intent intent = new Intent(getActivity(), MypageLogin.class);
            startActivity(intent);
        }

        // Click Login button
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MypageLogin.class);
            startActivity(intent);
        });

        // Click Logout button
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), MypageLogin.class);
            startActivity(intent);
        });

        imageInfo.setOnClickListener(v -> {
            dialogInfo("test");
        });



        Button btnTest = (Button) view.findViewById(R.id.mypage_test);
        btnTest.setOnClickListener(v -> {
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction =
                    fragmentManager.beginTransaction();
            // BackStackを設定
            fragmentTransaction.addToBackStack(null);

            fragmentTransaction.replace(R.id.container, new CalendarFragment());
            fragmentTransaction.commit();

            BottomNavigationView navigationView = getActivity().findViewById(R.id.bottom_navigation);
            navigationView.getMenu().getItem(0).setChecked(true);
        });




        showPieChart();
        getBarEntries();
        listChanges();

        return view;
    }


    private void findView() {
        pieChart = view.findViewById(R.id.idPieChart);
        barChart = view.findViewById(R.id.idBarChart);
        btnLogin = (Button) view.findViewById(R.id.mypage_login);
        btnLogout = (Button) view.findViewById(R.id.mypage_logout);
        textCount = (TextView) view.findViewById(R.id.mypage_count);
        imageInfo = (ImageView) view.findViewById(R.id.mypage_info);

        Resources res = getResources();
        categoryEN = new ArrayList<>(Arrays.asList(res.getStringArray(R.array.categoryEN)));
        categoryJP = new ArrayList<>(Arrays.asList(res.getStringArray(R.array.categoryJP)));

    }


    private void showPieChart() {
        // Firestore からデータ取得
        CollectionReference collection = db.collection("event");
        for (int i = 0; i < categoryEN.size(); i++) {
            int x = i;
            Query query = collection.whereEqualTo("Category", categoryEN.get(i)).whereEqualTo("uid", uid);
            AggregateQuery countQuery = query.count();
            countQuery.get(AggregateSource.SERVER).addOnCompleteListener(new OnCompleteListener<AggregateQuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<AggregateQuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        AggregateQuerySnapshot snapshot = task.getResult();
                        if ((int) snapshot.getCount() == 0) {
                            return;
                        }
                        num += snapshot.getCount();
                        String catJP = categoryJP.get(categoryEN.indexOf(categoryEN.get(x)));
                        categoryMap.put(catJP, (int) snapshot.getCount());

                    } else {
                        Log.d("genki", "Count failed: ");// task.getException()
                    }
                    setPieChart(colors);
                }
            });

        }

        //initializing colors for the entries
        colors = new ArrayList<>();
        colors.add(Color.parseColor("#03045e"));
        colors.add(Color.parseColor("#023e8a"));
        colors.add(Color.parseColor("#0077b6"));
        colors.add(Color.parseColor("#0096c7"));
        colors.add(Color.parseColor("#00b4d8"));
        colors.add(Color.parseColor("#48cae4"));
        colors.add(Color.parseColor("#90e0ef"));
        colors.add(Color.parseColor("#ade8f4"));
        colors.add(Color.parseColor("#caf0f8"));

    }

    private void setPieChart(ArrayList<Integer> colors) {
        ArrayList<PieEntry> pieEntries = new ArrayList<>();

        //input data and fit data into pie chart entry
        for (String type : categoryMap.keySet()) {
            pieEntries.add(new PieEntry(categoryMap.get(type).floatValue(), type));
        }

        //collecting the entries with label name
        PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
        //setting text size of the value
        pieDataSet.setValueTextSize(0f);
        //providing color list for coloring different entries
        pieDataSet.setColors(colors);
        //grouping the data set from entry to chart
        PieData pieData = new PieData(pieDataSet);
        //showing the value of the entries, default true if not set
        pieData.setDrawValues(true);

        Legend legend = pieChart.getLegend();
        legend.setEnabled(false);

        pieChart.getDescription().setEnabled(false);
        pieChart.setData(pieData);
        pieChart.invalidate();

        textCount.setText(""+num);
    }

    private void getBarEntries() {

        // current date
        TimeZone tz = TimeZone.getTimeZone("Asia/Tokyo");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        sdf.setTimeZone(tz);
        currentDate = sdf.format(new Date());

        monthList = new ArrayList<>();
        monthLabel = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(new Date().getTime());
            cal.add(Calendar.MONTH, i-1);
            monthList.add(sdf.format(cal.getTime()).substring(0,6));
            monthLabel.add(sdf.format(cal.getTime()).substring(5,6) + "月");
        }

        db.collection("event")
                .whereGreaterThanOrEqualTo("DateInt", Integer.parseInt(currentDate))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                iUID = document.getData().get("uid").toString();
                                iMonth = document.getData().get("DateInt").toString().substring(0,6);
                                if (iUID.equals(uid) == true) {
                                    if (iMonth.equals(monthList.get(1))) {
                                        bar1 += 1;
                                    } else if (iMonth.equals(monthList.get(2))) {
                                        bar2 += 1;
                                    } else if (iMonth.equals(monthList.get(3))) {
                                        bar3 += 1;
                                    } else if (iMonth.equals(monthList.get(4))) {
                                        bar4 += 1;
                                    } else if (iMonth.equals(monthList.get(5))) {
                                        bar5 += 1;
                                    } else if (iMonth.equals(monthList.get(6))) {
                                        bar6 += 1;
                                    } else {
                                    }
                                }
                            }
                        } else {
                            Log.d("genki", "Error getting documents: ", task.getException());
                        }
                        setBarChart();
                    }
                });
    }

    public void setBarChart() {
        // X軸
        XAxis xAxis = barChart.getXAxis();
        //X軸に表示するLabelのリスト(最初の""は原点の位置)
        xAxis.setValueFormatter(new IndexAxisValueFormatter(monthLabel));
        XAxis bottomAxis = barChart.getXAxis();
        bottomAxis.setTextSize(12);
        bottomAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        bottomAxis.setDrawLabels(true);
        bottomAxis.setDrawGridLines(false);
        bottomAxis.setDrawAxisLine(true);

        //Y軸(左)
        YAxis left = barChart.getAxisLeft();
        left.setAxisMinimum(0);
        left.setEnabled(false);

        //Y軸(右)
        YAxis right = barChart.getAxisRight();
        right.setEnabled(false);

        // creating a new array list
        barEntriesArrayList = new ArrayList<>();
        barEntriesArrayList.add(new BarEntry(1f, bar1));
        barEntriesArrayList.add(new BarEntry(2f, bar2));
        barEntriesArrayList.add(new BarEntry(3f, bar3));
        barEntriesArrayList.add(new BarEntry(4f, bar4));
        barEntriesArrayList.add(new BarEntry(5f, bar5));
        barEntriesArrayList.add(new BarEntry(6f, bar6));

        // creating a new bar data set.
        barDataSet = new BarDataSet(barEntriesArrayList, "");
        // adding color to our bar data set.
        barDataSet.setColors(colors);

        // setting text color.
        barDataSet.setValueTextColor(Color.GRAY);
        // setting text size
        barDataSet.setValueTextSize(10);
        // passing our bar data set.
        barData = new BarData(barDataSet);
        // below line is to set data
        barChart.getDescription().setEnabled(false);

        //グラフ上の表示
        barChart.setDrawValueAboveBar(true);
        barChart.getDescription().setEnabled(false);
        barChart.setClickable(false);

        //グラフを少し上にあげる
        barChart.setExtraBottomOffset(10);

        //凡例
        barChart.getLegend().setEnabled(false);
        barChart.setScaleEnabled(false);
        // to our bar chart.
        barChart.setData(barData);
        barChart.invalidate(); // これで更新できたぁ!

    }


    // dialogインスタンスを生成して、docIdを渡す
    public void dialogInfo(String docId) {
        MypageInfoDialog dialogRight = new MypageInfoDialog();
        // 渡す値をセット
        Bundle args = new Bundle();

        args.putString("docId", docId);

        dialogRight.setArguments(args);
        dialogRight.show(getActivity().getSupportFragmentManager(), "my_dialog");

    }

    // Change welcome text
//    public void changeWelcome() {
//        try {
//            getUid();
//            if (uid != null) {
//                textWelcome.setText("Welcome back");
//            }
//        } catch (Exception e) {
//            textWelcome.setText("Welcome GUEST");
//        }
//    }


    // Get uid
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

    // リストの内容を更新する
    public void listChanges() {
        RecyclerView recyclerView = view.findViewById(R.id.mypage_recyclerview);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager rLayoutManager = new LinearLayoutManager(getActivity());

        recyclerView.setLayoutManager(rLayoutManager);

        List<String> itemDocid = new ArrayList<>();
        List<String> itemTitle = new ArrayList<>();
        List<String> itemCategory = new ArrayList<>();

        // current date
        TimeZone tz = TimeZone.getTimeZone("Asia/Tokyo");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        sdf.setTimeZone(tz);
        currentDate = sdf.format(new Date());

        db.collection("event")
                .whereLessThanOrEqualTo("DateInt", Integer.parseInt(currentDate))
                .orderBy("DateInt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                iUID = document.getData().get("uid").toString();
                                iCategory = document.getData().get("Category").toString();
                                // 2つ目の条件をつける
                                if (iUID.equals(uid) == true) {
                                            itemDocid.add(document.getId());
                                            itemTitle.add(document.getData().get("Title").toString());
                                            itemCategory.add(iCategory);
                                }
                            }
                            MypageAdopter adapter = new MypageAdopter(itemDocid, itemTitle, itemCategory);
                            recyclerView.setAdapter(adapter);
                        } else {
                            Log.d("genki", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    public static MypageFragment newInstance() {
        MypageFragment fragment = new MypageFragment();

        return fragment;
    }
}