package com.example.firebase;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.List;

public class PictureAdapter extends BaseAdapter {
    private Context c;
    private List<String> imgList;

    public PictureAdapter(Context c, List<String> imgList) {
        this.c = c;
        this.imgList = imgList;
    }

    public void updateImages(List<String> lstImg){
        this.imgList = lstImg;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return imgList.size();
    }

    @Override
    public Object getItem(int position) {
        return imgList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView = new ImageView(c);
        Glide.with(c)
                .load(imgList.get(position))
                .into(imageView);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(350, 350));
        return imageView;
    }
}
