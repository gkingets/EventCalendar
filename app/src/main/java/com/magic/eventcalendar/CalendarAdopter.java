package com.magic.eventcalendar;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class CalendarAdopter extends RecyclerView.Adapter<CalendarAdopter.ViewHolder> {

    private final java.util.List<String> iDocId;
    private final java.util.List<String> iTitle;
    private final java.util.List<String> iCategory;
    private final java.util.List<String> iDate;
    private final java.util.List<String> iDateSimple;
    private final java.util.List<String> iPersonal;
    private final java.util.List<String> iTimestamp;
    private final String iUid;
    Context context;
    String dif;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder {

        // each data item is just a string in this case
        TextView textTitle, textCategory, textDate, textDateLeft;
        ImageView imageView, personal;
        CardView cardView;

        ViewHolder(View v) {
            super(v);
            textTitle = v.findViewById(R.id.calendar_title);
//            textCategory = v.findViewById(R.id.calendar_category);
            textDate = v.findViewById(R.id.calendar_date);
            textDateLeft = v.findViewById(R.id.calendar_left);
            imageView = v.findViewById(R.id.calendar_image);
            context = v.getContext();
            personal = v.findViewById(R.id.calendar_personal_icon);
            cardView = v.findViewById(R.id.calendar_image_card);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    CalendarAdopter(List<String> itemDocid, List<String> itemTitle, List<String> itemCategory, List<String> itemDate,
                    List<String> itemDateSimple, List<String> itemPersonal, List<String> itemTimestamp,
                    String uid) {
        this.iDocId = itemDocid;
        this.iTitle = itemTitle;
        this.iCategory = itemCategory;
        this.iDate = itemDate;
        this.iDateSimple = itemDateSimple;
        this.iPersonal = itemPersonal;
        this.iTimestamp = itemTimestamp;
        this.iUid = uid;

    }



    // Create new views (invoked by the layout manager)
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.calendar_list, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textTitle.setText(iTitle.get(position));
//        holder.textCategory.setText(iCategory.get(position));
        holder.textDate.setText(iDateSimple.get(position));
        try {
            calDate(String.valueOf(iDate.get(position)), iDateSimple.get(position));
        } catch (ParseException e) {
            e.printStackTrace();
            Log.d("genki", "error");
        }
        holder.textDateLeft.setText(dif);

        int res = context.getResources().getIdentifier(iCategory.get(position).toLowerCase(Locale.ROOT), "drawable", context.getPackageName());
        holder.imageView.setImageResource(res);

        // Personalフラグが立っていれば、Imageを出す
        if (iPersonal.get(position).equals("1")) {
            holder.personal.setVisibility(View.VISIBLE);
        } else {
            holder.personal.setVisibility(View.GONE);
        }

        holder.cardView.setOnClickListener(v -> {
            dialog(iDocId.get(position));
        });

    }

    @Override
    public int getItemCount() {
        return iTitle.size();
    }

    public void calDate(String date, String dateSimple) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        Date calendarDate = sdf.parse(date);
        Date currentDate = new Date();
        if (dateSimple.equals("-/-")) {
            dif = "-";
        } else {
            long tempTimestamp = calendarDate.getTime() - currentDate.getTime();

            dif = String.valueOf((int) TimeUnit.DAYS.convert(calendarDate.getTime() - currentDate.getTime(), TimeUnit.MILLISECONDS)+1 );
        }
    }

    public void dialog(String docId) {
        CalendarEdit dialogRight = new CalendarEdit();
        // 渡す値をセット
        Bundle args = new Bundle();

        args.putString("docId", docId);

        dialogRight.setArguments(args);

        // AdopterにDialogを使うために、((AppCompatActivity) context)を追加
        dialogRight.show(((AppCompatActivity) context).getSupportFragmentManager(), "my_dialog");
    }

}

