package com.magic.eventcalendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SearchGridAdapter extends BaseAdapter {

    static class ViewHolder {
        ImageView imageView;
        TextView textView;
    }

    private final List<Integer> imageList;
    private final List<String> category;
    private final LayoutInflater inflater;
    private final int layoutId;

    // 引数がMainActivityからの設定と合わせる
    SearchGridAdapter(Context context,
                      int layoutId,
                      List<Integer> iList,
                      ArrayList<String> iCategory) {

        super();
        this.inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.layoutId = layoutId;
        imageList = iList;
        category = iCategory;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            // main.xml の <GridView .../> に grid_items.xml を inflate して convertView とする
            convertView = inflater.inflate(layoutId, parent, false);
            // ViewHolder を生成
            holder = new ViewHolder();

            holder.imageView = convertView.findViewById(R.id.search_image_view);
            holder.textView = convertView.findViewById(R.id.search_text_view);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.imageView.setImageResource(imageList.get(position));
        holder.textView.setText(category.get(position));

        return convertView;
    }

    @Override
    public int getCount() {
        // List<String> imgList の全要素数を返す
        return imageList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
}
