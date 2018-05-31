package com.example.lenovo.busq;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.services.busline.BusLineItem;
import com.amap.api.services.busline.BusLineResult;
import com.amap.api.services.busline.BusStationItem;
import com.amap.api.services.route.BusPath;
import com.example.lenovo.busq.R;
public class BusLineAdapter extends BaseAdapter{
    private List<BusLineItem> busLineItems;
    private LayoutInflater layoutInflater;

    public BusLineAdapter(Context context, List<BusLineItem> busLineItems) {
        this.busLineItems = busLineItems;
        layoutInflater = LayoutInflater.from(context);

    }

    @Override
    public int getCount() {
        return busLineItems.size();
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
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.busline_item, null);
            holder = new ViewHolder();
            holder.busName = (TextView) convertView.findViewById(R.id.busname);
            holder.busTime = (TextView) convertView.findViewById(R.id.bustime);
            holder.busPrice = (TextView) convertView.findViewById(R.id.busprice);
            holder.busStation = (TextView) convertView.findViewById(R.id.busstation);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.busName.setText(busLineItems.get(position).getBusLineName());
        if(busLineItems.get(position).getFirstBusTime() != null){
            String time = new SimpleDateFormat("HH:mm").format(busLineItems.get(position)
                    .getFirstBusTime()) + " - " + new SimpleDateFormat("HH:mm").format(busLineItems.get(position)
                    .getLastBusTime());
            holder.busTime.setText("发车时间 : " + time);
        }else if(busLineItems.get(position).getFirstBusTime() == null){
            holder.busTime.setVisibility(View.GONE);
        }
        int price = (int) busLineItems.get(position).getBasicPrice();
        holder.busPrice.setText("票价 : " + price + "元");
        holder.strs = new String[] {busLineItems.get(position).getBusStations().toString()};
        for(int i = 0; i<holder.strs.length;i ++){
//            String name = holder.strs[i];
//            String[] names = holder.strs[i].split(",");
//
//                holder.busStation.setText(names.toString());
//                for(int j=0;j<names.length;i++){
//                    Log.e("err","err"+names[j]);
//            }

//            Log.e("err","err" + holder.strs[i].getClass());
            String w = "";
            Pattern p = Pattern.compile("\\s+");
            String name = holder.strs[i];
            String reg = "[^\u4e00-\u9fa5]";
            name = name.replaceAll(reg, " ").trim();
            Matcher m = p.matcher(name);
            w= m.replaceAll("\n");
            holder.busStation.setText(w);
            Log.e("err","err" + name);

        }
        return convertView;
    }

    class ViewHolder {
        public TextView busName;
        public TextView busTime;
        public TextView busPrice;
        public TextView busStation;
        private String[] strs;
    }

}
