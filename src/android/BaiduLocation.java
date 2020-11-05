package com.qdc.plugins.baidu;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
//import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;

/**
 * 百度云推送插件
 *
 * @author mrwutong
 * 2016-06-07 peng 修改，支持 Android定位SDKv6.2.3
 */
public class BaiduLocation extends CordovaPlugin {

    /** LOG TAG */
    private static final String LOG_TAG = "BaiduLocation";//BaiduLocation.class.getSimpleName();

    /** JS回调接口对象 */
    public static CallbackContext cbCtx = null;

    /** 百度定位客户端 */
    public LocationClient mLocationClient = null;
	//private Object  objLock = new Object();

    /** 百度定位监听 */
    public BDLocationListener myListener = new BDLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation location) {
            try {
                /*
				 * 时间也可以使用systemClock.elapsedRealtime()方法 获取的是自从开机以来，每次回调的时间；
				 * location.getTime() 是指服务端出本次结果的时间，如果位置不发生变化，则时间不变
				*/
                StringBuilder sb = new StringBuilder(256);
                sb.append("time : ");
                sb.append(location.getTime());
                sb.append("\nerror code : ");
                sb.append(location.getLocType());
                sb.append("\nlatitude : ");
                sb.append(location.getLatitude());
                sb.append("\nlontitude : ");
                sb.append(location.getLongitude());
                sb.append("\nradius : ");
                sb.append(location.getRadius());
               	sb.append("\nCountryCode : ");
				sb.append(location.getCountryCode());
				sb.append("\nCountry : ");
				sb.append(location.getCountry());
				sb.append("\ncitycode : ");
				sb.append(location.getCityCode());
				sb.append("\ncity : ");
				sb.append(location.getCity());
				sb.append("\nDistrict : ");
				sb.append(location.getDistrict());
				sb.append("\nStreet : ");
				sb.append(location.getStreet());
                /*
				sb.append("\naddr : ");
				sb.append(location.getAddrStr());
				sb.append("\nDescribe: ");
				sb.append(location.getLocationDescribe());
				sb.append("\nDirection(not all devices have value): ");
				sb.append(location.getDirection());
				sb.append("\nPoi: ");
				if (location.getPoiList() != null && !location.getPoiList().isEmpty()) {
					for (int i = 0; i < location.getPoiList().size(); i++) {
						Poi poi = (Poi) location.getPoiList().get(i);
						sb.append(poi.getName() + ";");
					}
				}
                */
                JSONObject json = new JSONObject();
                json.put("time", location.getTime());
                json.put("locType", location.getLocType());
                json.put("latitude", location.getLatitude());
                json.put("lontitude", location.getLongitude());
                json.put("radius", location.getRadius());

                if (location.getLocType() == BDLocation.TypeGpsLocation){// GPS定位结果
                    sb.append("\nspeed : ");
                    sb.append(location.getSpeed());// 单位：公里每小时
                    sb.append("\nsatellite : ");
                    sb.append(location.getSatelliteNumber());
                    sb.append("\nheight : ");
                    sb.append(location.getAltitude());// 单位：米
                    sb.append("\ndirection : ");
                    sb.append(location.getDirection());// 单位度
                    sb.append("\naddr : ");
                    sb.append(location.getAddrStr());
                    sb.append("\ndescribe : ");
                    sb.append("gps定位成功");

                    json.put("speed", location.getSpeed());
                    json.put("satellite", location.getSatelliteNumber());
                    json.put("height", location.getAltitude());
                    json.put("direction", location.getDirection());
                    json.put("addr", location.getAddrStr());
                    json.put("describe", "gps定位成功");
                } else if (location.getLocType() == BDLocation.TypeNetWorkLocation){// 网络定位结果
                    sb.append("\naddr : ");
                    sb.append(location.getAddrStr());
                    //运营商信息
                    sb.append("\noperationers : ");
                    sb.append(location.getOperators());
                    sb.append("\ndescribe : ");
                    sb.append("网络定位成功");

                    json.put("addr", location.getAddrStr());
                    json.put("operationers", location.getOperators());
                    json.put("describe", "网络定位成功");
                } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                    sb.append("\ndescribe : ");
                    sb.append("离线定位成功，离线定位结果也是有效的");

                    json.put("describe", "离线定位成功，离线定位结果也是有效的");
                } else if (location.getLocType() == BDLocation.TypeServerError) {
                    sb.append("\ndescribe : ");
                    sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");

                    json.put("describe", "服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
                } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                    sb.append("\ndescribe : ");
                    sb.append("网络不同导致定位失败，请检查网络是否通畅");

                    json.put("describe", "网络不同导致定位失败，请检查网络是否通畅");
                } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                    sb.append("\ndescribe : ");

                    sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
                    json.put("describe", "无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
                }
    //            sb.append("\nlocationdescribe : ");
    //            sb.append(location.getLocationDescribe());// 位置语义化信息
    //            List<Poi> list = location.getPoiList();// POI数据
    //            if (list != null) {
    //                sb.append("\npoilist size = : ");
    //                sb.append(list.size());
    //                for (Poi p : list) {
    //                    sb.append("\npoi= : ");
    //                    sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
    //                }
    //            }
                Log.i(LOG_TAG, sb.toString());

                //PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, sb.toString());
                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, json);
                pluginResult.setKeepCallback(true);
                cbCtx.sendPluginResult(pluginResult);
            } catch (JSONException e) {
                String errMsg = e.getMessage();
                Log.e(LOG_TAG, errMsg, e);

                PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, errMsg);
                pluginResult.setKeepCallback(true);
                cbCtx.sendPluginResult(pluginResult);
            } finally {
                mLocationClient.stop();
            }
        }
    };

    /**
     * 插件主入口
     */
     
    @Override
    public boolean execute(String action, final JSONArray args, CallbackContext callbackContext) throws JSONException {
        //Log.i(LOG_TAG, "BaiduPush#execute");

        boolean ret = false;

        //if ("getCurrentPosition".equalsIgnoreCase(action)) {
         if (action.equalsIgnoreCase("getCurrentPosition")) {
     
            cbCtx = callbackContext;

            //PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
            //pluginResult.setKeepCallback(true);
            //cbCtx.sendPluginResult(pluginResult);

            if (mLocationClient == null) {
                mLocationClient = new LocationClient(this.webView.getContext());
                mLocationClient.registerLocationListener(myListener);

            	//if(mLocationClient.isStarted()) mLocationClient.stop();
		
                // 配置定位SDK参数
                initLocation();
            }

           //mLocationClient.start();
       	   //mLocationClient.requestLocation();
           
           //实际上在start之后会默认发起一次定位请求，开发者无须检查这个函数，只要等着定位回调就可以了
           if (mLocationClient.isStarted() == false) {
                mLocationClient.start();
                Log.i(LOG_TAG, "LocationClient start done");
           }
           /*
           if (mLocationClient != null && mLocationClient.isStarted()) {
               mLocationClient.requestLocation();
           } else {
              String errMsg = "";
              if (mLocationClient == null)
                   errMsg = "LocationClient is null,";
              if (mLocationClient.isStarted() == false)
                   errMsg = "LocationClient is not started";
              Log.e(LOG_TAG, errMsg);
              //Thread.sleep(100);
		      //mLocationClient.requestLocation();
              Timer timer = new Timer();// 实例化Timer类
              timer.schedule(new TimerTask() {
                 public void run() {
         		    //mLocationClient.requestLocation();
                     if (mLocationClient != null && mLocationClient.isStarted()) {
                        mLocationClient.requestLocation();
                     } else {
                       Log.e(LOG_TAG, "LocationClient is not started after delay");
                     }
                    this.cancel();
                  }
              }, 500);// 这里百毫秒
           }
           */
           ret = true;
        }

        return ret;
    }
    
    /**
     * 配置定位SDK参数
     */
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        
        option.setOpenGps(true);
        option.setLocationMode(LocationMode.Hight_Accuracy);
    	//option.setLocationMode(LocationMode.Battery_Saving);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
		option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系，如果配合百度地图使用，建议设置为bd09ll;
        //option.setScanSpan(3000);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
	    option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
	    option.setIsNeedLocationDescribe(true);//可选，设置是否需要地址描述
	    option.setNeedDeviceDirect(false);//可选，设置是否需要设备方向结果
	    option.setLocationNotify(false);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
	    option.setIgnoreKillProcess(true);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死   
	    option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
	    option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
	    option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
		option.setProdName("UltraCRM-BaiduLocation");
		option.disableCache(true);// 禁止启用缓存定位

        mLocationClient.setLocOption(option);
    }
}
