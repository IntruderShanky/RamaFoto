package com.islabs.ramafoto.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.islabs.ramafoto.R;

import java.util.List;

public class PortfolioPagerAdapter extends PagerAdapter {

    private Context context;
    private List<String> imageUrls;

    public PortfolioPagerAdapter(Context context, List<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull final ViewGroup container, int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.portfolio_pager_layout, container, false);
        ImageView portfolioImage = view.findViewById(R.id.portfolio_image);
        ImageView backImage = view.findViewById(R.id.back_image);

        Glide.with(context).load(imageUrls.get(position)).into(portfolioImage);
        Glide.with(context).load(imageUrls.get(position)).into(backImage);
        container.addView(view);
        return view;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((FrameLayout) object);
    }

    @Override
    public int getCount() {
        return imageUrls.size();
    }
}
