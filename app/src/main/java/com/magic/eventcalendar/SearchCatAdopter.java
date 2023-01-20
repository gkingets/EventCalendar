package com.magic.eventcalendar;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SearchCatAdopter extends RecyclerView.Adapter<SearchCatAdopter.ViewHolder>{

    private final java.util.List<String> iDocId;
    private final java.util.List<String> iTitle;
    private final java.util.List<String> iDate;
    private final java.util.List<String> iDateSimple;
    Context context;
    String dif;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder {

        // each data item is just a string in this case
        TextView textTitle, textCategory, textDate, textDateLeft;
        ImageButton btnCopy;
        TextView colorTextView;

        ViewHolder(View v) {
            super(v);
            colorTextView = v.findViewById(R.id.search_cat_list_color);
            textTitle = v.findViewById(R.id.search_cat_list_title);
            textDate = v.findViewById(R.id.search_cat_list_date);
            textDateLeft = v.findViewById(R.id.search_cat_list_left);
            btnCopy = v.findViewById(R.id.search_cat_list_copy);
            context = v.getContext();

        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    SearchCatAdopter(List<String> itemDocId, List<String> itemTitle, List<String> itemDate,
                     List<String> itemDateSimple) {
        this.iDocId = itemDocId;
        this.iTitle = itemTitle;
        this.iDate = itemDate;
        this.iDateSimple = itemDateSimple;
    }

    // Create new views (invoked by the layout manager)
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_cat_list, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.colorTextView.setBackgroundColor(Color.rgb(randomInt(),randomInt(),randomInt()));
        holder.textTitle.setText(iTitle.get(position));
        holder.textDate.setText(iDateSimple.get(position));
        try {
            calDate(String.valueOf(iDate.get(position)));
        } catch (ParseException e) {
            e.printStackTrace();
            Log.d("genki", "error");
        }
        holder.textDateLeft.setText(dif);

        holder.btnCopy.setOnClickListener(v -> {
            dialog(iDocId.get(position));
        });
        
    }

    private int randomInt() {
        int min = 120; // Minimum value of range
        int max = 240; // Maximum value of range
        int random_int = (int)Math.floor(Math.random() * (max - min + 1) + min);
        return random_int;
    }

    @Override
    public int getItemCount() {
        return iTitle.size();
    }

    public void calDate(String date) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        Date calendarDate = sdf.parse(date);
        Date currentDate = new Date();
        dif = String.valueOf((int) TimeUnit.DAYS.convert(calendarDate.getTime() - currentDate.getTime(), TimeUnit.MILLISECONDS)+1 );
    }

    public void dialog(String docId) {
        SearchCatAdopterCopyDialog dialogRight = new SearchCatAdopterCopyDialog();
        // 渡す値をセット
        Bundle args = new Bundle();
        args.putString("docId", docId);
        dialogRight.setArguments(args);

        // AdopterにDialogを使うために、((AppCompatActivity) context)を追加
        dialogRight.show(((AppCompatActivity) context).getSupportFragmentManager(), "my_dialog");

    }


}
