package com.victoria.foodconnect.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.utils.MyLinkedMap;


import java.util.Optional;

public class StatsPageGrid extends BaseAdapter {

    //grid of admin menu

    public StatsPageGrid() {

    }

    @Override
    public int getCount() {
        return getServiceProviderDrawables().size();
    }

    @Override
    public Object getItem(int position) {
        return Optional.of(getServiceProviderDrawables().getEntry(position));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_row, null);
        }


        String name = getServiceProviderDrawables().getKey(position);

        TextView title = convertView.findViewById(R.id.title_row);
        title.setText(name);
        ImageView icon = convertView.findViewById(R.id.icon_row);
        Glide.with(parent.getContext()).load(getServiceProviderDrawables().getValue(position)).into(icon);


        return convertView;
    }

    private MyLinkedMap<String, Integer> getServiceProviderDrawables() {

        MyLinkedMap<String, Integer> map = new MyLinkedMap<>();

        map.put("Seller Stats", R.drawable.ic_person_black);
        map.put("Donor Stats", R.drawable.ic_give_food);

        return map;
    }

}
