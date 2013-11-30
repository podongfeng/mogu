package com.mogu.wifilocation;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mogu.wifilocation.db.DbHandler;
import com.mogu.wifilocation.util.Const;
import com.mogu.wifilocation.util.WifiUtil;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnClickListener,
		OnTouchListener {
	
	private DbHandler dbHandler;

	private ImageView iv_main_map;
	private TextView tv_main_current_location_x, tv_main_current_location_y;
	private Button btn_main_record, btn_main_cal;
	double x = 0, y = 0, resultX, resultY;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		init();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void init() {
		initDb();
		init_tv();
		init_iv();
		init_btn();
	}

	// 初始化数据库
	private void initDb() {
		try {
			String packageName = this.getPackageName();
			InputStream inputStream = getAssets().open(Const.DB_NAME);
			dbHandler = new DbHandler(packageName, inputStream);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void init_iv() {
		iv_main_map = (ImageView) findViewById(R.id.iv_main_map);
		iv_main_map.setOnTouchListener(this);
	}

	private void init_tv() {
		tv_main_current_location_x = (TextView) findViewById(R.id.tv_main_current_location_x);
		tv_main_current_location_y = (TextView) findViewById(R.id.tv_main_current_location_y);
		tv_main_current_location_x.setText("x=" + x);
		tv_main_current_location_y.setText("y=" + y);
	}

	private void init_btn() {
		btn_main_record = (Button) findViewById(R.id.btn_main_record);
		btn_main_cal = (Button) findViewById(R.id.btn_main_cal);
		btn_main_record.setOnClickListener(this);
		btn_main_cal.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		int viewId = v.getId();
		switch (viewId) {
		case R.id.btn_main_record:
			record_wifi();
			break;
		case R.id.btn_main_cal:
			cal_location();
			break;

		default:
			break;
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (v.getId() == R.id.iv_main_map) {
			x = event.getX();
			y = event.getY();
			tv_main_current_location_x.setText("x=" + x);
			tv_main_current_location_y.setText("y=" + y);
			return true;
		}
		return false;
	}

	/**
	 * 记录当前位置的wifi记录
	 */
	private void record_wifi() {
		Integer locationId = null;
		List<Map<String, Object>> locationList = dbHandler.queryLocation(x, y);
		if(locationList==null||locationList.size()==0){
			dbHandler.insertLoc(x, y);
			locationList = dbHandler.queryLocation(x, y);
		}
		locationId = Integer.parseInt(String.valueOf(locationList.get(0).get("location_id")));
		WifiManager wifimanager = (WifiManager)getSystemService(WIFI_SERVICE);
		wifimanager.startScan();
		List<ScanResult> scanResults = wifimanager.getScanResults();
		if (scanResults!=null&&scanResults.size()>0) {
			ScanResult maxScanResult = scanResults.get(0);
			for (ScanResult item : scanResults) {
				if(item.level>maxScanResult.level){
					maxScanResult = item;
				}
			}
			dbHandler.insertWifi(maxScanResult.SSID, maxScanResult.level, locationId);
			Toast.makeText(MainActivity.this, "记录成功", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(MainActivity.this, "当前没有检测到wifi", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 根据当前检测到的wifi强度计算当前位置
	 */
	private void cal_location() {
		WifiManager wifimanager = (WifiManager)getSystemService(WIFI_SERVICE);
		wifimanager.startScan();
		List<ScanResult> scanResults = wifimanager.getScanResults();
		if (scanResults!=null&&scanResults.size()>0) {
			ScanResult maxScanResult = scanResults.get(0);
			for (ScanResult item : scanResults) {
				if(item.level>maxScanResult.level){
					maxScanResult = item;
				}
			}
			int wifiLevel = maxScanResult.level;
			List<Map<String, Object>> wifiList = dbHandler.queryWifiBySsid(maxScanResult.SSID);
			if(wifiList==null||wifiList.size()==0){
				resultX = 0;
				resultY = 0;
				Toast.makeText(MainActivity.this, "结果：x=" + resultX +", y=" + resultY, Toast.LENGTH_SHORT).show();
				return;
			} else {
				Integer resultLocationId = null; 
				int min = 10000;
				for(Map<String, Object> item:wifiList){
					if(resultLocationId==null){
						resultLocationId = Integer.parseInt(String.valueOf(wifiList.get(0).get("location_id")));
						int level = Integer.parseInt(String.valueOf(wifiList.get(0).get("wifi_level")));
						min = Math.abs(wifiLevel-level);
					} else {
						int level = Integer.parseInt(String.valueOf(item.get("wifi_level")));
						if(Math.abs(wifiLevel-level)<min){
							min = Math.abs(wifiLevel-level);
							resultLocationId = Integer.parseInt(String.valueOf(item.get("location_id")));
						}
					}
				}
				Map<String, Object> locMap = dbHandler.queryLocationById(resultLocationId);
				resultX = Double.parseDouble(String.valueOf(locMap.get("x")));
				resultY = Double.parseDouble(String.valueOf(locMap.get("y")));;
				Toast.makeText(MainActivity.this, "结果：x=" + resultX +", y=" + resultY, Toast.LENGTH_SHORT).show();
				return;
			}
		} else {
			Toast.makeText(MainActivity.this, "当前没有检测到wifi", Toast.LENGTH_SHORT).show();
		}
		Toast.makeText(MainActivity.this, "计算成功", Toast.LENGTH_SHORT).show();
	}
	
}
