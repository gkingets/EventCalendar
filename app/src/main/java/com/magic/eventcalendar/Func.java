package com.magic.eventcalendar;

import android.app.Activity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Func {
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void deleteData(String docId) {
        db.collection("event").document(docId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("genki", "DocumentSnapshot successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("genki", "Error deleting document", e);
                    }
                });

    }

    public void createUser(String uid) {

        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        // Create a new user with a first and last name
        Map<String, Object> user = new HashMap<>();
        user.put("uid", uid);
        user.put("registerDate", timeStamp);
        user.put("loginDate", "");
        user.put("termsOfUseSelected", 1);
        user.put("privacyPolicySelected", 1);

        db.collection("user").document(uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // 既存のユーザーの場合、loginDateを更新する
                            DocumentReference Ref = db.collection("user").document(uid);
                            Ref.update("loginDate", timeStamp)
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
                        } else {
                            // 新規ユーザーの場合、ポイントを付与して追加
                            db.collection("user").document(uid)
                                    .set(user)
                                    .addOnSuccessListener(aVoid -> {
                                        // 成功時の処理
                                        Log.d("genki","OK");
                                    })
                                    .addOnFailureListener(e -> {
                                        // 失敗時の処理
                                        Log.d("genki","NG");
                                    });
                        }
                    } else {
                        // 失敗時の処理
                    }
                });

    }






}
