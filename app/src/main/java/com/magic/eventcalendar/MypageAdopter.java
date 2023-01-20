package com.magic.eventcalendar;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.Locale;

public class MypageAdopter extends RecyclerView.Adapter<MypageAdopter.ViewHolder> {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final java.util.List<String> iDocId;
    private final java.util.List<String> iTitle;
    private final java.util.List<String> iCategory;
    Context context;

    int ratingInt;
    String docId;
    ImageButton deleteBtn;

    public class ViewHolder extends RecyclerView.ViewHolder {

        // each data item is just a string in this case
        TextView textTitle;
        ImageView imageView;
        CardView cardView;
        RatingBar ratingBar;

        ViewHolder(View v) {
            super(v);
            textTitle = v.findViewById(R.id.mypage_list_title);
            imageView = v.findViewById(R.id.mypage_list_image);
            context = v.getContext();
            cardView = v.findViewById(R.id.mypaage_list_image_card);
            ratingBar = v.findViewById(R.id.mypage_list_rating_bar);
            deleteBtn = v.findViewById(R.id.mypage_list_delete);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    MypageAdopter(List<String> itemDocid, List<String> itemTitle, List<String> itemCategory) {
        this.iDocId = itemDocid;
        this.iTitle = itemTitle;
        this.iCategory = itemCategory;
    }


    @NonNull
    @Override
    public MypageAdopter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.mypage_list, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new MypageAdopter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MypageAdopter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.textTitle.setText(iTitle.get(position));
        int res = context.getResources().getIdentifier(iCategory.get(position).toLowerCase(Locale.ROOT), "drawable", context.getPackageName());
        holder.imageView.setImageResource(res);

        DocumentReference Ref = db.collection("event").document(iDocId.get(position));
        Ref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                ratingInt = Integer.parseInt(documentSnapshot.getData().get("Rating").toString());
                Log.d("genki", "rating|"+ratingInt+"  text"+iTitle.get(position));
                holder.ratingBar.setRating(ratingInt);
            }
        });

        // Delete ボタンのクリック
        deleteBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("イベントを削除しますか？")
                    .setPositiveButton( "はい", new  DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Func del = new Func();
                            del.deleteData(iDocId.get(position));

                            // 更新ができないから、Fragmentを自分にして移動してます・・・
                            FragmentManager fragmentManager = ((AppCompatActivity)context).getSupportFragmentManager();
                            FragmentTransaction fragmentTransaction =
                                    fragmentManager.beginTransaction();
                            fragmentTransaction.addToBackStack(null);
                            fragmentTransaction.replace(R.id.container, new MypageFragment());
                            fragmentTransaction.commit();


                        }
                    })
                    .setNeutralButton( "キャンセル", new  DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();

        });


        // Rating bar を操作する
        holder.ratingBar.setOnRatingBarChangeListener((ratingBar1, rating, fromUser) -> {
            ratingInt = Math.round(rating);
            Ref.update("Rating", ratingInt)
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
            // 評価の変更は一度だけにする場合は↓をコメントイン
            //ratingBar.setIsIndicator(true);

            // 3秒後に処理を実行する
//            new Handler(Looper.getMainLooper()).postDelayed(new Runnable(){
//                @Override public void run() {
//                }}, 3000);
        });

    }

    @Override
    public int getItemCount() {
        return iTitle.size();
    }
}
