package com.mtkj.cnpc.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mtkj.cnpc.R;
import com.mtkj.cnpc.entity.MyVods;

import java.util.List;

public class VideoAdapter extends BaseAdapter {
    List<MyVods> vodsList;
    Context context;
    public VideoAdapter(Context context, List<MyVods> vodsList){
        this.context = context;
        this.vodsList = vodsList;
    }
    @Override
    public int getCount() {
        return vodsList.size();
    }

    @Override
    public Object getItem(int i) {
        return vodsList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        convertView = View.inflate(context, R.layout.item_video_detail,null);
        TextView tv_name = convertView.findViewById(R.id.tv_name);
        TextView tv_time = convertView.findViewById(R.id.tv_time);
        tv_name.setText(vodsList.get(position).getVodId()+"");
        tv_time.setText(vodsList.get(position).getDisplayName());
        return convertView;
    }
}
