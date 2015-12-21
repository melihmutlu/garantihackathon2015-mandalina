package com.example.melih.mandalina;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Created by erkam on 26.2.2015.
 */
public class GridAdapter extends BaseAdapter {

    private List<Product> mProductList = new ArrayList();
    private LayoutInflater mInflater;
    private Context context;

    public GridAdapter(List<Product> mProductList, Context context) {
        this.mProductList = mProductList;
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
    }

    @Override
    public int getCount() {
        return mProductList.size();
    }

    @Override
    public Object getItem(int position) {
        return mProductList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }



    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = mInflater.inflate(R.layout.grid_cell, null);
        TextView nameTextView = (TextView) v.findViewById(R.id.nameTextView);
        TextView priceTextView = (TextView) v.findViewById(R.id.priceTextView);
        ImageView imageView = (ImageView) v.findViewById(R.id.imageView);
        Picasso.with(context).load(mProductList.get(position).getImgUrl()).into(imageView);
        nameTextView.setText(mProductList.get(position).getName());
        priceTextView.setText(mProductList.get(position).getPrice());
        return v;
    }
}
