package com.nhnent.exam.wikisearch.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nhnent.exam.netlibrary.ImageLoader;
import com.nhnent.exam.wikisearch.R;
import com.nhnent.exam.wikisearch.models.WikiModel;

import java.util.List;

/**
 * Created by gradler on 18/08/2017.
 */

public class WikiAdapter extends RecyclerView.Adapter<WikiAdapter.ViewHolder> {

    private static final int ITEM_TYPE_HEADER = 0;
    private static final int ITEM_TYPE_CONTENT = 1;

    final private Context mContext;
    final private List<WikiModel> mContentList;
    final private ImageLoader mImageLoader;
    private OnItemSelectedListener mOnItemSelectedListener;

    public WikiAdapter(Context context, List<WikiModel> contentList, ImageLoader imageLoader) {
        mContext = context;
        mContentList = contentList;
        mImageLoader = imageLoader;
    }

    @Override
    public int getItemViewType(int position) {
        if (mContentList != null && mContentList.get(position).isHeader()) {
            return ITEM_TYPE_HEADER;
        } else {
            return ITEM_TYPE_CONTENT;
        }
    }

    @Override
    public WikiAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE_HEADER) {
            return new ViewHolder(LayoutInflater.from(mContext)
                    .inflate(R.layout.list_header, parent, false));
        } else {
            return new ViewHolder(LayoutInflater.from(mContext)
                    .inflate(R.layout.list_content, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(final WikiAdapter.ViewHolder holder, int position) {
        final WikiModel item = mContentList.get(position);
        if (!TextUtils.isEmpty(item.getThumbnailUrl())) {
            mImageLoader.loadThumbnail(mContext, item.getThumbnailUrl(), new ImageLoader.OnResultListener() {
                @Override
                public void onResult(final Bitmap bitmap) {
                    if (bitmap != null) {
                        holder.thumbnailView.setVisibility(View.VISIBLE);
                        holder.thumbnailView.setImageBitmap(bitmap);
                    } else {
                        holder.thumbnailView.setVisibility(View.GONE);
                    }
                }
            });
        } else {
            holder.thumbnailView.setVisibility(View.GONE);
        }

        holder.titleView.setText(item.getDisplayTitle());
        holder.extractView.setText(item.getExtractText());
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnItemSelectedListener != null) {
                    mOnItemSelectedListener.onItemSelected(item);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mContentList != null ? mContentList.size() : 0;
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        mOnItemSelectedListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final public RelativeLayout layout;
        final public ImageView thumbnailView;
        final public TextView titleView;
        final public TextView extractView;

        public ViewHolder(View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.list_layout);
            thumbnailView = itemView.findViewById(R.id.img_thumbnail);
            titleView = itemView.findViewById(R.id.txt_title);
            extractView = itemView.findViewById(R.id.txt_extract);
        }
    }

    public interface OnItemSelectedListener {
        void onItemSelected(WikiModel item);
    }
}
