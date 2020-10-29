/**
 * 
 */
package com.bluetooth.demo;

import android.annotation.TargetApi;
import android.app.Application;
import android.util.Log;

import com.bluetooth.demo.utils.DeviceDataUtils;
import com.bluetooth.demo.utils.FileUtils;
import com.lifesense.ble.LsBleInterface;
import com.lifesense.ble.LsBleManager;
import com.lifesense.ble.bean.DefaultCallConfig;
import com.lifesense.ble.bean.WeightAppendData;
import com.lifesense.ble.bean.constant.SexType;

/**
 * @author sky
 *
 */
public class MyApplication  extends Application {

	@TargetApi(23)
	@Override
	public void onCreate()
	{
		super.onCreate();
		//init LSBluetoothManager
		LsBleManager.getInstance().initialize(getApplicationContext(), "88d01e7cb606c28eb35f9667df309aeb57ccf54b");

		//register bluetooth broadacst receiver
		LsBleManager.getInstance().registerBluetoothBroadcastReceiver(getApplicationContext());

		Log.e("LS-BLE", "LSDevice Bluetooth SDK Version:"+LsBleInterface.BLUETOOTH_SDK_VERSION);
		//for debug mode
		LsBleManager.getInstance().enableWriteDebugMessageToFiles(true, LsBleInterface.PERMISSION_WRITE_LOG_FILE);
		//设置日志路径
		String logPath=FileUtils.createPortraitUrl(getApplicationContext(), "lifesense/log");
		LsBleManager.getInstance().setBlelogFilePath(logPath, "test","1.0");

		//register message service if need
		LsBleManager.getInstance().registerMessageService();
		
		//set defulat call message
		LsBleManager.getInstance().setCustomConfig(new DefaultCallConfig("unknown"));

		//for test
		calculateBodyCompositionData(550);
		
		//时间及时区测试
		long utc=DeviceDataUtils.formatUtcTime(9, 45);
		byte[] timezoneUtc=DeviceDataUtils.formatLocalTime(9, 45);
		Log.e("LS-BLE", "local time >> "+String.format("%08X", utc)+"; withTimeZone >> "+DeviceDataUtils.byte2hex(timezoneUtc));
}



/**
 * 根据电阻值，计算脂肪数据
 * @param resistance
 */
private void calculateBodyCompositionData(double resistance)
{
	//		double resistance=wData.getImpedance();
	double height=1.65;      // unit M
	double weight=50;     //  unit kg
	int age=30;
	SexType gender=SexType.MALE;
	boolean isAthlete=true;

	/**
	 * registance=667
	 * app message >>bodycomposition >> WeightAppendData [basalMetabolism=1643.91,
	 *  bodyFatRatio=10.999307983673468,
	 *  bodyWaterRatio=66.79657985714287, muscleMassRatio=31.106678549142856, 
	 *  boneDensity=3.243327118469387, imp=80.1, bmi=22.13877551020408, 
	 *  proteinContent=17.420443842857132, visceralFat=-21.042570427086673]
	 *  
	 *  ios test
	 *  2018-03-08 17:31:42.279 LSBluetooth-Demo[2630:1753262] body composition data {
			basalMetabolism = "1643.91004196167";  //卡路里
			bmi = "22.13877650669643";			   //无单位
			bodyFatRatio = "10.99930904536467";		//百分比
			bodywaterRatio = "66.79657865652244";   //百分比
			boneDensity = "3.243327204449583";		//百分比
			imp = "80.10000610351562";				//阻抗
			muscleMassRatio = "31.10667937584521";  //百分比
			protein = "17.42044407029045";          //百分比
			userNumber = 0;
			visceralFat = "-21.04256826543626";
			voltageData = 0;
			
				basalMetabolism 基础代谢 单位（卡路里），
				bmi 无单位
			    bodyFatRatio 脂肪率 单位 百分比
			    bodywaterRatio 水分含量 百分比
			    boneDensity 骨质含量 百分比
			    muscleMassRatio 肌肉 百分比
	 */
	WeightAppendData bodyComposition=LsBleManager.getInstance().parseAdiposeData(gender, weight, height, age, resistance, isAthlete);
	Log.e("LS-BLE", "body composition >> "+bodyComposition.toString());
	LsBleManager.getInstance().setLogMessage("bodycomposition >> "+bodyComposition.toString());
}
}
