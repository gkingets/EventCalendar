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
import android.widget.GridView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class SearchFragment extends Fragment implements AdapterView.OnItemClickListener {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    View view;
    String[] category_list;
    ArrayList<String> categoryEN;
    ArrayList<String> categoryJP;
    List<Integer> imgList;
    String uid, currentDate, iUID, iCategory;
    int iPersonal, iReuse;


    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.search, container, false);

        // String arrayからリストを取得する
        Resources res = getResources();
//        category_list = res.getStringArray(R.array.categoryJP);

        // Resource IDを格納するarray
        imgList = new ArrayList<>();
        categoryEN = new ArrayList<>(Arrays.asList(res.getStringArray(R.array.categoryEN)));
        categoryJP = new ArrayList<>(Arrays.asList(res.getStringArray(R.array.categoryJP)));

        // for-each member名をR.drawable.名前としてintに変換してarrayに登録
        for (String member: categoryJP){
            int catIndex = categoryJP.indexOf(member);
            int imageId = getResources().getIdentifier(
                    categoryEN.get(catIndex).toLowerCase(Locale.ROOT),"drawable", getContext().getPackageName());
            imgList.add(imageId);
        }

        // GridViewのインスタンスを生成
        GridView gridview = view.findViewById(R.id.search_grid_view);
        // BaseAdapter を継承したGridAdapterのインスタンスを生成
        // 子要素のレイアウトファイル grid_items.xml を
        // activity_main.xml に inflate するためにGridAdapterに引数として渡す
        SearchGridAdapter adapter = new SearchGridAdapter(getActivity().getApplicationContext(),
                R.layout.search_grid,
                imgList,
                categoryJP
        );

        // gridViewにadapterをセット
        gridview.setAdapter(adapter);

        // item clickのListnerをセット
        gridview.setOnItemClickListener(this);

        popularListChanges();

        return view;

    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity().getApplication(), SearchCat.class);
        intent.putExtra("IMAGEID", imgList.get(position));
        intent.putExtra("CATEGORY", categoryJP.get(position));
        startActivity( intent );
    }

    // リストの内容を更新する
    public void popularListChanges() {
        RecyclerView recyclerView = view.findViewById(R.id.search_recyclerview);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager rLayoutManager = new LinearLayoutManager(getActivity());

        recyclerView.setLayoutManager(rLayoutManager);

        List<String> itemDocid = new ArrayList<>();
        List<String> itemTitle = new ArrayList<>();
        List<String> itemCategory = new ArrayList<>();
        List<String> itemDateSimple = new ArrayList<>();

        // current date
        TimeZone tz = TimeZone.getTimeZone("Asia/Tokyo");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        sdf.setTimeZone(tz);
        currentDate = sdf.format(new Date());

        // ***ここにCOUNTを入れる
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
                                iCategory = document.getData().get("Category").toString();
                                iReuse = Integer.parseInt(document.getData().get("Reuse").toString());
                                // 2つ目の条件をつける
                                if (iUID.equals(uid) == false || iUID.equals("we0lP6JayRbbGKF83gdF4fy2nmz2") == true ) {
                                    if (iPersonal != 1) {
                                            if (iReuse != 1) {
                                                itemDocid.add(document.getId());
                                                itemTitle.add(document.getData().get("Title").toString());
                                                itemCategory.add(iCategory);
                                                itemDateSimple.add(document.getData().get("DateSimple").toString());
                                            }
                                    }
                                }
                            }
                            SearchPopularAdapter adapter = new SearchPopularAdapter(itemDocid, itemTitle, itemCategory, itemDateSimple);
                            recyclerView.setAdapter(adapter);
                        } else {
                            Log.d("genki", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}