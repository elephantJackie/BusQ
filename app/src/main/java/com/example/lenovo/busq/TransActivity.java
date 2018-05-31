package com.example.lenovo.busq;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;
import com.amap.api.services.routepoisearch.RoutePOISearchQuery;
import com.example.lenovo.busq.util.AMapUtil;
import com.example.lenovo.busq.util.ToastUtil;

import java.util.ArrayList;

public class TransActivity extends AppCompatActivity implements LocationSource, AMapLocationListener
        ,AMap.OnMapClickListener, AMap.OnMarkerClickListener, AMap.OnInfoWindowClickListener
        , AMap.InfoWindowAdapter, RouteSearch.OnRouteSearchListener ,View.OnClickListener,GeocodeSearch.OnGeocodeSearchListener {
    private AMap aMap;
    private MapView mapView;
    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;
    private LocationSource.OnLocationChangedListener mListener = null;
    private boolean isFirstLoc = true;
    private UiSettings mUiSettings;
    private Context mContext;
    private RouteSearch mRouteSearch;
    private BusRouteResult mBusRouteResult;
    private LatLonPoint mStartPoint = new LatLonPoint(45.708441, 126.622339);;//起点，116.335891,39.942295
    private LatLonPoint mEndPoint = new LatLonPoint(45.773941, 126.618958);//终点，116.481288,39.995576
    private String mCurrentCityName = "哈尔滨";
    private final int ROUTE_TYPE_BUS = 1;
    private LinearLayout mBusResultLayout;
    private LinearLayout header;
    private ListView mBusResultList;
    private ImageView mBus;
    private ProgressDialog progDialog = null;// 搜索时进度条
    private Button btn;
    private GeocodeSearch geocoderSearch;
    private Marker geoMarker, regeoMarker;
    private LatLonPoint addressName;
    private EditText startPoint;
    private EditText endPoint;
    private PoiSearch.Query startSearchQuery;
    private PoiSearch.Query endSearchQuery;
    LatLonPoint arrayList[] = new LatLonPoint[2];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trans);
        mContext = this.getApplicationContext();
        mapView = (MapView) findViewById(R.id.route_map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        init();
        mUiSettings = aMap.getUiSettings();
        aMap.setLocationSource(this);
        // 是否显示定位按钮
        mUiSettings.setMyLocationButtonEnabled(false);
        // 是否可触发定位并显示定位层
        aMap.setMyLocationEnabled(true);
        mUiSettings.setZoomControlsEnabled(false);
        mUiSettings.setScrollGesturesEnabled(true);
        mUiSettings.setZoomGesturesEnabled(true);
        mUiSettings.setTiltGesturesEnabled(true);
        mUiSettings.setRotateGesturesEnabled(true);
        initLoc();
        setfromandtoMarker();
        header = (LinearLayout) findViewById(R.id.header);
        mBusResultLayout = (LinearLayout) findViewById(R.id.bus_result);
        mapView.setVisibility(View.VISIBLE);
        header.setVisibility(View.VISIBLE);
        mBusResultLayout.setVisibility(View.GONE);
        //searchRouteResult(ROUTE_TYPE_BUS, RouteSearch.BUS_DEFAULT);
        btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(this);



    }

    public void onClick(View v) {

        mapView.setVisibility(View.GONE);
        header.setVisibility(View.GONE);
        mBusResultLayout.setVisibility(View.VISIBLE);
        startpoint();

    }
    public void startpoint(){
        startPoint = (EditText) findViewById(R.id.start);
        String start = startPoint.getText().toString().trim();
        geocoderSearch = new GeocodeSearch(this);
        geocoderSearch.setOnGeocodeSearchListener(this);
        GeocodeQuery query = new GeocodeQuery(start, "0451");// 第一个参数表示地址，第二个参数表示查询城市，中文或者中文全拼，或citycode、adcode，null为全国
        geocoderSearch.getFromLocationNameAsyn(query);// 设置同步地理编码请求
    }
    public void endpoint (){
        endPoint = (EditText) findViewById(R.id.end);
        String end = endPoint.getText().toString().trim();
        geocoderSearch = new GeocodeSearch(this);
        geocoderSearch.setOnGeocodeSearchListener(this);
        GeocodeQuery query = new GeocodeQuery(end, "0451");// 第一个参数表示地址，第二个参数表示查询城市，中文或者中文全拼，或citycode、adcode，null为全国
        geocoderSearch.getFromLocationNameAsyn(query);// 设置同步地理编码请求
    }
    private void initLoc() {
        //初始化定位
        locationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        locationClient.setLocationListener(this);
        //初始化定位参数
        locationOption = new AMapLocationClientOption();
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置是否返回地址信息（默认返回地址信息）
        locationOption.setNeedAddress(true);
        //设置是否只定位一次,默认为false
        locationOption.setOnceLocation(false);
        //设置是否强制刷新WIFI，默认为强制刷新
        locationOption.setWifiActiveScan(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        locationOption.setMockEnable(false);
        //设置定位间隔,单位毫秒,默认为2000ms
        locationOption.setInterval(2000);
        //给定位客户端对象设置定位参数
        locationClient.setLocationOption(locationOption);
        //启动定位
        locationClient.startLocation();
    }
    public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                // 如果不设置标志位，此时再拖动地图时，它会不断将地图移动到当前的位置
                if (isFirstLoc) {
                    //设置缩放级别
                    aMap.moveCamera(CameraUpdateFactory.zoomTo(17));
                    //将地图移动到定位点
                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude())));
                    //点击定位按钮 能够将地图的中心移动到定位点
                    mListener.onLocationChanged(amapLocation);
                    //获取定位信息
                    isFirstLoc = false;
                }
            } else {
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Log.e("AmapError", "location Error, ErrCode:"
                        + amapLocation.getErrorCode() + ", errInfo:"
                        + amapLocation.getErrorInfo());

                Toast.makeText(getApplicationContext(), "定位失败", Toast.LENGTH_LONG).show();
            }
        }
    }
    public void activate(LocationSource.OnLocationChangedListener listener) {
        mListener = listener;
    }
    public void deactivate() {
        mListener = null;
    }
    private void setfromandtoMarker() {
        aMap.addMarker(new MarkerOptions()
                .position(AMapUtil.convertToLatLng(mStartPoint))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.start)));
        aMap.addMarker(new MarkerOptions()
                .position(AMapUtil.convertToLatLng(mEndPoint))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.end)));
    }
    /**
     * 初始化AMap对象
     */
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        registerListener();
        mRouteSearch = new RouteSearch(this);
        mRouteSearch.setRouteSearchListener(this);
        mBusResultLayout = (LinearLayout) findViewById(R.id.bus_result);
        mBusResultList = (ListView) findViewById(R.id.bus_result_list);

    }
    /**
     * 注册监听
     */
    private void registerListener() {
        aMap.setOnMapClickListener(TransActivity.this);
        aMap.setOnMarkerClickListener(TransActivity.this);
        aMap.setOnInfoWindowClickListener(TransActivity.this);
        aMap.setInfoWindowAdapter(TransActivity.this);
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

    @Override
    public void onMapClick(LatLng arg0) {
        // TODO Auto-generated method stub

    }
    /**
     * 公交路线搜索
     */
    public void onBusClick(View view) {

        mBus.setImageResource(R.drawable.route_bus_select);
        mapView.setVisibility(View.GONE);
        mBusResultLayout.setVisibility(View.VISIBLE);
    }


    /**
     * 开始搜索路径规划方案
     */
    public void searchRouteResult(int routeType, int mode , LatLonPoint start,LatLonPoint end) {
        if (start == null) {
            ToastUtil.show(mContext, "定位中，稍后再试...");
            return;
        }
        if (end == null) {
            ToastUtil.show(mContext, "终点未设置");
        }
        showProgressDialog();
        final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
                start, end);
        if (routeType == ROUTE_TYPE_BUS) {// 公交路径规划
            RouteSearch.BusRouteQuery query = new RouteSearch.BusRouteQuery(fromAndTo, mode,
                    mCurrentCityName, 0);// 第一个参数表示路径规划的起点和终点，第二个参数表示公交查询模式，第三个参数表示公交查询城市区号，第四个参数表示是否计算夜班车，0表示不计算
            mRouteSearch.calculateBusRouteAsyn(query);// 异步路径规划公交模式查询
        }
    }

    /**
     * 规划路线结果回调方法
     */
    @Override
    public void onBusRouteSearched(BusRouteResult result, int errorCode) {
        dissmissProgressDialog();
        aMap.clear();// 清理地图上的所有覆盖物
        if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getPaths() != null) {
                if (result.getPaths().size() > 0) {
                    mBusRouteResult = result;
                    BusResultListAdapter mBusResultListAdapter = new BusResultListAdapter(mContext, mBusRouteResult);
                    mBusResultList.setAdapter(mBusResultListAdapter);
                } else{
                    ToastUtil.show(mContext, "对不起");
                }
            } else {
                ToastUtil.show(mContext, R.string.no_result);
            }
        } else {
            ToastUtil.showerror(this.getApplicationContext(), errorCode);
        }
    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult result, int errorCode) {

    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult result, int errorCode) {

    }


    /**
     * 显示进度框
     */
    private void showProgressDialog() {
        if (progDialog == null)
            progDialog = new ProgressDialog(this);
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(true);
        progDialog.setMessage("正在搜索");
        progDialog.show();
    }

    /**
     * 隐藏进度框
     */
    private void dissmissProgressDialog() {
        if (progDialog != null) {
            progDialog.dismiss();
        }
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onRideRouteSearched(RideRouteResult arg0, int arg1) {
        // TODO Auto-generated method stub

    }

//    @Override
//    public void onPoiSearched(PoiResult poiResult, int i) {
//        if (i == 1000) {// 返回成功
//            if (poiResult != null && poiResult.getQuery() != null
//                    && poiResult.getPois() != null && poiResult.getPois().size() > 0) {// 搜索poi的结果
//                {
//                    if (poiResult.getQuery().equals(startSearchQuery)) {
//                        mStartPoint = poiResult.getPois().get(0).getLatLonPoint();
//                        endpoint();// 开始搜终点
//                    } else if (poiResult.getQuery().equals(endSearchQuery)) {
//                        mEndPoint = poiResult.getPois().get(0).getLatLonPoint();
//                        searchRouteResult(ROUTE_TYPE_BUS, RouteSearch.BUS_DEFAULT,mStartPoint, mEndPoint);// 进行路径规划搜索
//                    } else {
//                        ToastUtil.show(mContext, 4);
//                    }
//                }
//            }else{
//                ToastUtil.show(mContext, 5);
//            }
//        } else if (i == 27) {
//            ToastUtil.show(mContext, R.string.no_result);
//        } else if (i == 32) {
//            ToastUtil.show(mContext, R.string.no_result);
//        } else {
//            ToastUtil.show(mContext, R.string.no_result);
//        }
//    }
    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {

    }

    public void onGeocodeSearched(GeocodeResult result, int rCode) {
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getGeocodeAddressList() != null
                    && result.getGeocodeAddressList().size() > 0) {
                GeocodeAddress address = result.getGeocodeAddressList().get(0);
                aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        AMapUtil.convertToLatLng(address.getLatLonPoint()), 15));
                addressName = address.getLatLonPoint();

                if(arrayList[0] != null){
                    arrayList[1] = addressName;
                    Log.e("err","start : "+arrayList[0]+",end : "+arrayList[1]);
                    searchRouteResult(ROUTE_TYPE_BUS, RouteSearch.BUS_DEFAULT,arrayList[0], arrayList[1]);
                }else if(arrayList[0] == null){
                    arrayList[0] = addressName;
                    endpoint();
                }

            } else {

            }
        } else {

        }
    }


//    @Override
//    public void onPoiItemSearched(PoiItem poiItem, int i) {
//
//    }
}
