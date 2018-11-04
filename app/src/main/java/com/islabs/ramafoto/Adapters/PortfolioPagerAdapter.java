package com.islabs.ramafoto.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.islabs.ramafoto.Activity.PhotoView;
import com.islabs.ramafoto.R;
import com.islabs.ramafoto.Utils.StaticData;

import java.util.List;

public class PortfolioPagerAdapter extends PagerAdapter {

    private Context context;
    private List<String> imageUrls;
    private String youtubeLink;

    public PortfolioPagerAdapter(Context context, List<String> imageUrls, String youtubeLink) {
        this.context = context;
        this.imageUrls = imageUrls;
        this.youtubeLink = youtubeLink;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.portfolio_pager_layout, container, false);
        ImageView portfolioImage = view.findViewById(R.id.portfolio_image);
        ImageView backImage = view.findViewById(R.id.back_image);
        ImageView playVideo = view.findViewById(R.id.play_youtube);

        if (position < imageUrls.size()) {
            Glide.with(context).load(imageUrls.get(position)).into(portfolioImage);
            Glide.with(context).load(imageUrls.get(position)).into(backImage);
            playVideo.setVisibility(View.GONE);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, PhotoView.class);
                    intent.putExtra("photo_url", imageUrls.get(position));
                    context.startActivity(intent);
                }
            });
        } else {
            playVideo.setVisibility(View.VISIBLE);
            playVideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(youtubeLink));
                    context.startActivity(intent);
                }
            });
            Uri uri = Uri.parse(youtubeLink);
            try {
                String videoId = uri.getQueryParameter("v");
                String thumbnail = "https://img.youtube.com/vi/" + videoId + "/0.jpg";
                Glide.with(context).load(thumbnail).into(portfolioImage);
                Glide.with(context).load(thumbnail).into(backImage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

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
        return StaticData.checkString(youtubeLink) ? imageUrls.size() + 1 : imageUrls.size();
    }
}
