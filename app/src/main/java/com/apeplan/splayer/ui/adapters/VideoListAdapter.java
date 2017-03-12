package com.apeplan.splayer.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.apeplan.splayer.R;
import com.apeplan.splayer.domain.MediaEntry;

import java.util.List;

/**
 * describe:
 *
 * @author Apeplan
 * @date 2017/3/12
 * @email hanzx1024@gmail.com
 */

public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.VHolder> {

    private List<MediaEntry> mDatas;
    private ItemOnClickListener mItemOnClickListener;

    public VideoListAdapter(List<MediaEntry> datas) {
        mDatas = datas;
    }

    @Override
    public VHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, null);
        return new VHolder(view);
    }

    @Override
    public void onBindViewHolder(final VHolder holder, final int position) {
        MediaEntry mediaEntry = mDatas.get(position);
        String title = mediaEntry.getTitle();
        holder.mName.setText(title);
        holder.mTime.setText(mediaEntry.getDuration());
        holder.mSize.setText(mediaEntry.getSize());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mItemOnClickListener) {
                    mItemOnClickListener.onItemClick(holder.itemView, position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDatas != null ? mDatas.size() : 0;
//        return 5;
    }

    public MediaEntry getItem(int position) {
        return mDatas.get(position);
    }

    public void setItemOnClickListener(ItemOnClickListener itemOnClickListener) {
        mItemOnClickListener = itemOnClickListener;
    }

    class VHolder extends RecyclerView.ViewHolder {

        private final TextView mName;
        private final TextView mTime;
        private final TextView mSize;

        public VHolder(View itemView) {
            super(itemView);
            mName = (TextView) itemView.findViewById(R.id.tv_video_name);
            mTime = (TextView) itemView.findViewById(R.id.tv_video_time);
            mSize = (TextView) itemView.findViewById(R.id.tv_video_size);
        }
    }

    public interface ItemOnClickListener {
        void onItemClick(View itemView, int position);
    }
}
