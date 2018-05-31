package com.example.lenovo.busq;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.services.busline.BusLineItem;
import com.amap.api.services.busline.BusLineQuery;
import com.amap.api.services.busline.BusLineQuery.SearchType;
import com.amap.api.services.busline.BusLineResult;
import com.amap.api.services.busline.BusLineSearch;
import com.amap.api.services.busline.BusLineSearch.OnBusLineSearchListener;
import com.amap.api.services.busline.BusStationItem;
import com.amap.api.services.busline.BusStationQuery;
import com.amap.api.services.busline.BusStationResult;
import com.amap.api.services.busline.BusStationSearch;
import com.amap.api.services.busline.BusStationSearch.OnBusStationSearchListener;
import com.amap.api.services.core.AMapException;
import com.example.lenovo.busq.R;
import com.example.lenovo.busq.overlay.BusStationOverlay;
import com.example.lenovo.busq.util.ToastUtil;
import com.example.lenovo.busq.overlay.BusLineOverlay;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
public class StationActivity extends AppCompatActivity implements LocationSource, AMapLocationListener,
        OnMarkerClickListener, InfoWindowAdapter, OnItemSelectedListener,
        OnBusStationSearchListener, OnClickListener{
    private AMap aMap;
    private MapView mapView;
    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;
    private LocationSource.OnLocationChangedListener mListener = null;
    private boolean isFirstLoc = true;
    private UiSettings mUiSettings;

    private ProgressDialog progDialog = null;// 进度框
    private EditText searchName;// 输入公交线路名称
    private Spinner selectCity;// 选择城市下拉列表
    private String[] itemCitys = { "哈尔滨" };
    private String cityCode = "";// 城市区号
    private int currentpage = 0;
    private BusStationResult busStationResult;// 公交线路搜索返回的结果
    private BusStationQuery busStationQuery;// 公交线路查询的查询类
    private List<BusStationItem> stationItems = null;// 公交线路搜索返回的busline
    private BusStationSearch busStationSearch;// 公交线路列表查询

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line);
        mapView = (MapView) findViewById(R.id.map);
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
    }
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
            setUpMap();
        }
        Button searchByName = (Button) findViewById(R.id.searchbyname);
        searchByName.setText("查询");
        searchByName.setOnClickListener(this);
        selectCity = (Spinner) findViewById(R.id.cityName);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, itemCitys);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectCity.setAdapter(adapter);
        selectCity.setPrompt("请选择城市：");
        selectCity.setOnItemSelectedListener(this);
        searchName = (EditText) findViewById(R.id.busName);
        searchName.setHint("请输入站点名称");
    }
    /**
     * 设置marker的监听和信息窗口的监听
     */
    private void setUpMap() {
        aMap.setOnMarkerClickListener(this);
        aMap.setInfoWindowAdapter(this);
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

    /**
     * 公交线路搜索
     */
    public void searchLine() {
        showProgressDialog();
        String search = searchName.getText().toString().trim();
        if ("".equals(search)) {
            search = "望京";
            searchName.setText(search);
        }
        busStationQuery = new BusStationQuery(search, cityCode);// 第一个参数表示公交线路名，第二个参数表示公交线路查询，第三个参数表示所在城市名或者城市区号
//		busStationQuery.setPageSize(10);// 设置每页返回多少条数据
        // busStationQuery.setPageNumber(currentpage);// 设置查询第几页，第一页从0开始算起
        busStationSearch = new BusStationSearch(this, busStationQuery);// 设置条件
        busStationSearch.setOnBusStationSearchListener(this);// 设置查询结果的监听
        busStationSearch.setQuery(new BusStationQuery(search,cityCode));
        busStationSearch.searchBusStationAsyn();
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
        progDialog.setMessage("正在搜索:\n");
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
     * 提供一个给默认信息窗口定制内容的方法
     */
    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    /**
     * 提供一个个性化定制信息窗口的方法
     */
    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    /**
     * 点击marker回调函数
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;// 点击marker时把此marker显示在地图中心点
    }

    /**
     * 选择城市
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
                               long id) {
        String cityString = itemCitys[position];
        cityCode = cityString.substring(cityString.indexOf("-") + 1);
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        cityCode = "0451";
    }

    public void onBusStationSearched(BusStationResult result, int rCode) {
        dissmissProgressDialog();
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getQuery() != null
                    && result.getQuery().equals(busStationQuery)) {
                    if (result.getPageCount() > 0
                            && result.getBusStations() != null
                            && result.getBusStations().size() > 0) {
                        busStationResult = result;
                        stationItems = result.getBusStations();
                        if(stationItems != null) {
                            showResultList(stationItems);
                        }
                    }
                /*aMap.clear();// 清理地图上的marker
                busStationResult = result;
                stationItems = busStationResult.getBusStations();
                if(stationItems != null && stationItems.size() > 0) {
                    BusStationOverlay busStationOverlay = new BusStationOverlay(this,
                            aMap, stationItems.get(0));
                    busStationOverlay.removeFromMap();
                    busStationOverlay.addToMap();
                    busStationOverlay.zoomToSpan();
                }*/

            } else {
                ToastUtil.show(StationActivity.this, R.string.no_result);
            }
        } else {
            ToastUtil.showerror(StationActivity.this, rCode);
        }
    }
    interface OnListItemlistener {
        public void onListItemClick(BusStationDialog dialog, BusStationItem item);
    }
    /**
     * 所有公交线路显示页面
     */
    class BusStationDialog extends Dialog implements OnClickListener {

        private List<BusStationItem> busStationItems;
        private BusStationAdapter busStationAdapter;
        private Button preButton, nextButton;
        private ListView listView;
        protected OnListItemlistener onListItemlistener;

        public BusStationDialog(Context context, int theme) {
            super(context, theme);
        }

        public void onListItemClicklistener(
                OnListItemlistener onListItemlistener) {
            this.onListItemlistener = onListItemlistener;

        }

        public BusStationDialog(Context context, List<BusStationItem> busStationItems) {
            this(context, android.R.style.Theme_NoTitleBar);
            this.busStationItems = busStationItems;
            busStationAdapter = new BusStationAdapter(context, busStationItems);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.busstation_dialog);
            preButton = (Button) findViewById(R.id.preButton);
            nextButton = (Button) findViewById(R.id.nextButton);
            listView = (ListView) findViewById(R.id.listview);
            listView.setAdapter(busStationAdapter);
            listView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1,
                                        int arg2, long arg3) {
                    onListItemlistener.onListItemClick(BusStationDialog.this,
                            busStationItems.get(arg2));
                    dismiss();

                }
            });
            preButton.setOnClickListener(this);
            nextButton.setOnClickListener(this);
            if (currentpage <= 0) {
                preButton.setEnabled(false);
            }
            if (currentpage >= busStationResult.getPageCount() - 1) {
                nextButton.setEnabled(false);
            }

        }

        @Override
        public void onClick(View v) {
            this.dismiss();
            if (v.equals(preButton)) {
                currentpage--;
            } else if (v.equals(nextButton)) {
                currentpage++;
            }
            showProgressDialog();
            busStationQuery.setPageNumber(currentpage);// 设置公交查询第几页
            busStationSearch.setOnBusStationSearchListener(StationActivity.this);
            busStationSearch.searchBusStationAsyn();// 异步查询公交线路名称
        }
    }
    public void showResultList(List<BusStationItem> busStationItems) {
        StationActivity.BusStationDialog busStationDialog = new StationActivity.BusStationDialog(this, busStationItems);
        busStationDialog.onListItemClicklistener(new StationActivity.OnListItemlistener() {
            @Override
            public void onListItemClick(StationActivity.BusStationDialog dialog,
                                        final BusStationItem item) {
                showProgressDialog();

                String stationId = item.getBusStationId();// 得到当前点击item公交线路id
                busStationQuery = new BusStationQuery(stationId,cityCode);// 第一个参数表示公交线路id，第二个参数表示公交线路id查询，第三个参数表示所在城市名或者城市区号
                BusStationSearch busStationSearch = new BusStationSearch(
                        StationActivity.this, busStationQuery);
                busStationSearch.setOnBusStationSearchListener(StationActivity.this);
                busStationSearch.searchBusStationAsyn();// 异步查询公交线路id
            }
        });
        busStationDialog.show();

    }
    /**
     * 查询公交线路
     */
    @Override
    public void onClick(View v) {
        searchLine();
    }
}
