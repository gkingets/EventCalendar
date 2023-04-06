//package com.magic.eventcalendar;
//
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageButton;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.cardview.widget.CardView;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.firebase.firestore.FirebaseFirestore;
//
//import java.util.List;
//import java.util.Locale;
//
//public class SearchPopularAdapter extends RecyclerView.Adapter<SearchPopularAdapter.ViewHolder>{
//
//    FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//    private final java.util.List<String> iDocId;
//    private final java.util.List<String> iTitle;
//    private final java.util.List<String> iCategory;
//    private final java.util.List<String> iDateSimple;
//    Context context;
//    String docId;
//    ImageButton deleteBtn;
//
//    public class ViewHolder extends RecyclerView.ViewHolder {
//
//        // each data item is just a string in this case
//        TextView textTitle;
//        ImageView imageView;
//        CardView cardView;
//
//        ViewHolder(View v) {
//            super(v);
//            textTitle = v.findViewById(R.id.search_popular_list_title);
//            imageView = v.findViewById(R.id.search_popular_list_image);
//            context = v.getContext();
//            cardView = v.findViewById(R.id.search_popular_list_image_card);
//            deleteBtn = v.findViewById(R.id.search_popular_list_copy);
//        }
//    }
//
//    SearchPopularAdapter(List<String> itemDocid, List<String> itemTitle, List<String> itemCategory,
//                         List<String> itemDateSimple) {
//        this.iDocId = itemDocid;
//        this.iTitle = itemTitle;
//        this.iCategory = itemCategory;
//        this.iDateSimple = itemDateSimple;
//    }
//
//    @NonNull
//    @Override
//    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        // create a new view
//        View view = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.search_popular_list, parent, false);
//        // set the view's size, margins, paddings and layout parameters
//        return new ViewHolder(view);
//    }
//
//
//    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        holder.textTitle.setText(iTitle.get(position));
//        int res = context.getResources().getIdentifier(iCategory.get(position).toLowerCase(Locale.ROOT), "drawable", context.getPackageName());
//        holder.imageView.setImageResource(res);
//
//
//    }
//
//
//    @Override
//    public int getItemCount() {
////        return iTitle.size();
//        return 3;
//    }
//}
