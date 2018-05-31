package com.example.lenovo.busq;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.amap.api.services.busline.BusLineItem;
import com.amap.api.services.busline.BusStationItem;
import com.example.lenovo.busq.R;

public class BusStationAdapter extends BaseAdapter{
    private List<BusStationItem> busStationItems;
    private LayoutInflater layoutInflater;

    public BusStationAdapter(Context context, List<BusStationItem> busStationItems) {
        this.busStationItems = busStationItems;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return busStationItems.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BusStationAdapter.ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.busstation_item, null);
            holder = new BusStationAdapter.ViewHolder();
            holder.busName = (TextView) convertView.findViewById(R.id.busname);
            holder.busLine = (TextView) convertView.findViewById(R.id.busline);

            convertView.setTag(holder);
        } else {
            holder = (BusStationAdapter.ViewHolder) convertView.getTag();
        }
        holder.busName.setText("站点名称:"
                + busStationItems.get(position).getBusStationName());
//        holder.busLine.setText(busStationItems.get(position).getBusStationId());

        holder.strs = new String[] {busStationItems.get(position).getBusLineItems().toString()};

            for(int i = 0; i<holder.strs.length;i ++){
                String name = holder.strs[i];
                Log.e("err","err"+name);
                if(name == "[]"){
                    holder.busLine.setVisibility(View.GONE);
                }else {
                    name = name.replaceAll(",", "\n");
                    name = name.replace("[", "");
                    name = name.replace("]", "");
                    holder.busLine.setText("经过该站点的公交线路\n" + name);
                }
            }



        return convertView;
    }

    class ViewHolder {
        public TextView busName;
        public TextView busLine;
        public TextView title;
        private String[] strs;
    }
}
