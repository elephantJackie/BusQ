package com.example.lenovo.busq;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.amap.api.services.busline.BusLineResult;
import com.example.lenovo.busq.overlay.BusLineOverlay;
import com.example.lenovo.busq.overlay.BusRouteOverlay;
import com.example.lenovo.busq.overlay.RouteOverlay;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnInfoWindowClickListener;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMapLoadedListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;
import com.example.lenovo.busq.R;
import com.example.lenovo.busq.util.AMapUtil;

public class BusLineDetailActivity extends AppCompatActivity implements OnMapLoadedListener,
        OnMapClickListener, InfoWindowAdapter, OnInfoWindowClickListener, OnMarkerClickListener{
    private AMap aMap;
    private MapView mapView;
    private BusPath mBuspath;
    private BusLineResult mBusLineResult;
    private TextView mTitle, mTitleBusRoute, mDesBusRoute;
    private ListView mBusSegmentList;
    private BusSegmentListAdapter mBusSegmentListAdapter;
    private LinearLayout mBusMap, mBuspathview;
    private BusLineOverlay mBuslineOverlay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_detail);
        mapView = (MapView) findViewById(R.id.route_map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        getIntentData();
        init();
    }
    private void getIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            mBuspath = intent.getParcelableExtra("bus_path");
            mBusLineResult = intent.getParcelableExtra("bus_result");
        }
    }
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        registerListener();

        mTitle = (TextView) findViewById(R.id.title_center);
        mTitle.setText("公交路线详情");
        mTitleBusRoute = (TextView) findViewById(R.id.firstline);
        String dur = AMapUtil.getFriendlyTime((int) mBuspath.getDuration());
        String dis = AMapUtil.getFriendlyLength((int) mBuspath.getDistance());
        mTitleBusRoute.setText(dur + "(" + dis + ")");
        mBusMap = (LinearLayout)findViewById(R.id.title_map);
        mBusMap.setVisibility(View.VISIBLE);
        mBuspathview = (LinearLayout)findViewById(R.id.bus_path);
        configureListView();
    }
    private void registerListener() {
        aMap.setOnMapLoadedListener(this);
        aMap.setOnMapClickListener(this);
        aMap.setOnMarkerClickListener(this);
        aMap.setOnInfoWindowClickListener(this);
        aMap.setInfoWindowAdapter(this);
    }

    private void configureListView() {
        mBusSegmentList = (ListView) findViewById(R.id.bus_segment_list);
        mBusSegmentListAdapter = new BusSegmentListAdapter(
                this.getApplicationContext(), mBuspath.getSteps());
        mBusSegmentList.setAdapter(mBusSegmentListAdapter);

    }

    public void onBackClick(View view) {
        this.finish();
    }

    public void onMapClick(View view) {
        mBuspathview.setVisibility(View.GONE);
        mBusMap.setVisibility(View.GONE);
        mapView.setVisibility(View.VISIBLE);
        aMap.clear();// 清理地图上的所有覆盖物
        mBuslineOverlay = new BusLineOverlay(this, aMap, mBusLineResult.getBusLines().get(0));
        mBuslineOverlay.removeFromMap();
        mBuslineOverlay.addToMap();
        mBuslineOverlay.zoomToSpan();

    }

    @Override
    public void onMapLoaded() {
        if (mBuslineOverlay != null) {
            mBuslineOverlay.addToMap();
            mBuslineOverlay.zoomToSpan();
        }
    }

    @Override
    public void onMapClick(LatLng arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public View getInfoContents(Marker arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public View getInfoWindow(Marker arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onInfoWindowClick(Marker arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onMarkerClick(Marker arg0) {
        // TODO Auto-generated method stub
        return false;
    }
}
