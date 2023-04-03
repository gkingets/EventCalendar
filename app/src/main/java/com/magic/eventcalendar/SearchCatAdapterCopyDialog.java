package com.magic.eventcalendar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SearchCatAdapterCopyDialog extends DialogFragment {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    View dialogLayout;
    String uid, docId, title, category, dateSimple, date, place, description, iDocId;
    Integer dateInt, time;
    BottomNavigationView navigationView;

    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        LayoutInflater inflater = requireActivity().getLayoutInflater();
//        dialogLayout = inflater.inflate(R.layout., null);

        builder.setTitle("イベントをコピーしますか？")
                .setPositiveButton("はい", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        getDataFromFireStore();
                    }
                })
                .setNeutralButton("キャンセル", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        navigationView = (BottomNavigationView) dialogLayout.findViewById(R.id.bottom_navigation);
//        Log.d("genki","navigationView|"+navigationView);

        docId = getArguments().getString("docId", "");
        getUid();

        return builder.create(); // returnを後で入れる
    }

    public void getDataFromFireStore() {

        db.collection("event")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // 2つ目の条件をつける
                                if (docId.equals(document.getId())) {
                                    title = document.getData().get("Title").toString();
                                    category = document.getData().get("Category").toString();
                                    date = document.getData().get("Date").toString();
                                    dateInt = Integer.parseInt(document.getData().get("DateInt").toString());
                                    dateSimple = document.getData().get("DateSimple").toString();
                                    time = Integer.parseInt(document.getData().get("Time").toString());
                                    place = document.getData().get("Place").toString();
                                    description = document.getData().get("Description").toString();
                                }
                            }
                        } else {
                            Log.d("genki", "Error getting documents: ", task.getException());
                        }
                        MenuItem item = navigationView.getMenu().getItem(0);
                        Log.d("genki", "item"+item);
                        createEvent(item);
                        Log.d("genki", "success");
                    }
                });
    }

    public void createEvent(MenuItem item) {
        // Timestamp
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

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
        event.put("Personal", 0);
        event.put("Reuse", 1);
        event.put("Rating", 0);

        // Add a new document with a generated ID
        db.collection("event")
                .add(event)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("genki", "DocumentSnapshot added with ID: " + documentReference.getId());

                        item.setChecked(true);
//                        navigationView.getMenu().getItem(0).setChecked(true);
                        // 多分これ正解ではなくて、Activity取得してるからCalendarに移動しているのだと思う
//                        Intent intent = new Intent(getActivity(), MainActivity.class);
//                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("genki", "Error adding document", e);
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

}
