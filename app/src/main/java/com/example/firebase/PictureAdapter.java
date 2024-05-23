package com.example.firebase;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;
import android.app.Activity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.annotations.Nullable;

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
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        if (position == getCount() - 1) {
                            long endTime = System.currentTimeMillis();
                            long duration = endTime - MainActivity.startTime;
                            Toast.makeText(c, "Time taken to load and display all images: " + duration /  1000 + " seconds", Toast.LENGTH_SHORT).show();
                        }
                        return false;
                    }
                })
                .into(imageView);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        DisplayMetrics displayMetrics = c.getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        imageView.setLayoutParams(new ViewGroup.LayoutParams(screenWidth / 4, screenWidth / 4));
        return imageView;
    }
}
