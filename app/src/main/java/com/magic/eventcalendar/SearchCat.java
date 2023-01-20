package com.magic.eventcalendar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class SearchCat extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ImageView imageView, backImage;
    TextView catText;
    String category, uid, currentDate, iUID, iCategory;;
    int iPersonal, iReuse;
    RecyclerView recyclerView;

//    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_cat);

        Intent intent = getIntent();
        int imageId = intent.getIntExtra("IMAGEID", 0);
        category = intent.getStringExtra("CATEGORY");

        imageView = (ImageView) findViewById(R.id.search_cat_image);
        imageView.setImageResource(imageId);

        catText = (TextView) findViewById(R.id.search_cat_text);
        catText.setText(category);

        backImage = (ImageView) findViewById(R.id.search_cat_back);
        backImage.setOnClickListener(v -> {finish();});

        recyclerView = (RecyclerView) findViewById(R.id.search_cat_recyclerview);

        // Get UID
        try {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            uid = user.getUid();
        } catch (Exception e) {
            intent = new Intent(this, MypageLogin.class);
            startActivity(intent);
        }

        listChanges();

    }

    // リストの内容を更新する
    public void listChanges() {

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager rLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(rLayoutManager);

        List<String> itemDocid = new ArrayList<>();
        List<String> itemTitle = new ArrayList<>();
        List<String> itemCategory = new ArrayList<>();
        List<String> itemDate = new ArrayList<>();
        List<String> itemDateSimple = new ArrayList<>();
        List<String> itemPersonal = new ArrayList<>();
        List<String> itemTimestamp = new ArrayList<>();

        String cat = category.substring(0, 1).toUpperCase() + category.substring(1);

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
                                iCategory = document.getData().get("Category").toString();
                                iReuse = Integer.parseInt(document.getData().get("Reuse").toString());
                                // 2つ目の条件をつける
                                if (iUID.equals(uid) == false || iUID.equals("we0lP6JayRbbGKF83gdF4fy2nmz2") == false ) {
                                    if (iPersonal != 1) {
                                        if (iCategory.equals(cat)) {
                                            if (iReuse != 1) {
                                                itemDocid.add(document.getId());
                                                itemTitle.add(document.getData().get("Title").toString());
                                                itemCategory.add(iCategory);
                                                itemDate.add(document.getData().get("Date").toString());
                                                itemDateSimple.add(document.getData().get("DateSimple").toString());
                                                itemPersonal.add(document.getData().get("Personal").toString());
                                                itemTimestamp.add(document.getData().get("Timestamp").toString());
                                            }
                                        }
                                    }
                                }
                            }
                            SearchCatAdopter adapter = new SearchCatAdopter(itemDocid, itemTitle, itemDate, itemDateSimple);
                            recyclerView.setAdapter(adapter);
                        } else {
                            Log.d("genki", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}
