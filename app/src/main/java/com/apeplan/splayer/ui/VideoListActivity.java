package com.apeplan.splayer.ui;

import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Toast;

import com.apeplan.splayer.LocalVideoPlayerActivity;
import com.apeplan.splayer.R;
import com.apeplan.splayer.domain.MediaEntry;
import com.apeplan.splayer.frame.BasicActivity;
import com.apeplan.splayer.ui.adapters.VideoListAdapter;
import com.apeplan.splayer.utils.FormatHelper;
import com.apeplan.splayer.widget.StateView;

import java.util.ArrayList;
import java.util.List;


public class VideoListActivity extends BasicActivity {

    private RecyclerView mRecyclerview;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_video_list;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        mRecyclerview = (RecyclerView) findViewById(R.id.recycle_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerview.setLayoutManager(layoutManager);

    }

    @Override
    protected void initEventAndData() {
        new RequestVideo().execute();
    }

    @Override
    protected StateView getLoadingView() {
        return null;
    }

    private void goPlayer(MediaEntry mediaEntry) {

        //传递列表使用bundle
        Bundle bundle = new Bundle();
        bundle.putSerializable("video", mediaEntry);
       startIntent(LocalVideoPlayerActivity.class,bundle);
    }

    class RequestVideo extends AsyncTask<Void,Integer,List<MediaEntry>>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(VideoListActivity.this, "开始读取视频文件", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected List<MediaEntry> doInBackground(Void... params) {
            return getVideo();
        }

        @Override
        protected void onPostExecute(List<MediaEntry> mediaEntries) {
            super.onPostExecute(mediaEntries);
            Toast.makeText(VideoListActivity.this, "读取结束： " + mediaEntries.size(), Toast.LENGTH_SHORT)
                    .show();
            final VideoListAdapter adapter = new VideoListAdapter(mediaEntries);
            adapter.setItemOnClickListener(new VideoListAdapter.ItemOnClickListener() {
                @Override
                public void onItemClick(View itemView, int position) {
                    MediaEntry item = adapter.getItem(position);
                    goPlayer(item);
                }
            });
            mRecyclerview.setAdapter(adapter);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }
    }

    public List<MediaEntry> getVideo() {
        List<MediaEntry> list = new ArrayList<>();
        MediaEntry videoItem;
        //得到视频的数据
//        Uri uri = MediaStore.getVolumeUri();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Video.Media.TITLE,//标题
                MediaStore.Video.Media.DURATION,//时长
                MediaStore.Video.Media.SIZE,//视频文件大小
                MediaStore.Video.Media.ARTIST,//艺术家
                MediaStore.Video.Media.DATA//视频播放地址
        };
        Cursor cursor = getContentResolver().query(uri, projection , null, null, null);
        while (cursor != null && cursor.moveToNext()) {
            videoItem = new MediaEntry();

            String title = cursor.getString(0);
            videoItem.setTitle(title);//标题
//            holder.mSize.setText(
            String duration = cursor.getString(1);
            String time = FormatHelper.stringForTime(Integer.valueOf(duration));
            videoItem.setDuration(time);//设置时长

            long size = cursor.getLong(2);
            String s = Formatter.formatFileSize(VideoListActivity.this, size);
            videoItem.setSize(s);

            String artist = cursor.getString(3);
            videoItem.setArtist(artist);

            String data = cursor.getString(4);
            videoItem.setData(data);

            list.add(videoItem);
        }

        return list;
    }


}
