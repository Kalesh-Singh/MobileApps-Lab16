package com.techexchange.mobileapps.lab16;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private final LayoutInflater layoutInflater;
    private final List<ImageViewData> imageViewDataList;
    private final Context context;
    private final ThumbnailDownloader<ImageView> thumbnailThread;

    public ImageAdapter(Context context, List<ImageViewData> imageViewDataList, ThumbnailDownloader<ImageView> thumbnailThread) {
        this.imageViewDataList = imageViewDataList;
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.thumbnailThread = thumbnailThread;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        View itemView = layoutInflater.inflate(R.layout.image_item, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ImageViewData imageViewData = imageViewDataList.get(position);
        holder.imageView.setImageBitmap(imageViewData.bitmap);
        holder.imageView.setImageDrawable(context.getDrawable(R.drawable.ic_launcher_foreground));

        // Add the image to the ThumbnailDownloader queue.
        thumbnailThread.queueThumbnail(holder.imageView, imageViewDataList.get(position).url);

    }

    @Override
    public int getItemCount() {
        return imageViewDataList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        final ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_item);
        }
    }
}
