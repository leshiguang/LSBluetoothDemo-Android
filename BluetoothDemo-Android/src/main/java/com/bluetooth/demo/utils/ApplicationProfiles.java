/**
 * 
 */
package com.bluetooth.demo.utils;

import net.vidageek.mirror.dsl.Mirror;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.lifesense.ble.bean.constant.MessageType;

/**
 * @author sky
 *
 */
public class ApplicationProfiles {

	public static final String PACKAGE_NAME_WEIXIN = "com.tencent.mm";
	public static final String PACKAGE_NAME_QQ="com.tencent.mobileqq";
	public static final String PACKAGE_NAME_FACEBOOK="com.facebook.katana";
	public static final String PACKAGE_NAME_TWITTER="com.twitter.android";
	public static final String PACKAGE_NAME_LINE="jp.naver.line.android";
	public static final String PACKAGE_NAME_GMAIL="com.google.android.gm";
	public static final String PACKAGE_NAME_KAKAO="com.gun0912.kakao";
	public static final String PACKAGE_NAME_SeWellness="com.sewellness.android";
	public static final String PACKAGE_NAME_WHATSAPP="com.whatsapp";
	public static final String PACKAGE_NAME_INSTAGRAM="com.burbn.instagram";

	//new change for 1.4.0 2018-05-15
	public static final String PACKAGE_NAME_TianruiHealth="cn.com.tianruihealth";
	public static final String PACKAGE_NAME_Wgh3hSee="tw.com.wgh3h_SEE";
	public static final String PACKAGE_NAME_Wgh3h="tw.com.wgh3h";


	
	public static MessageType getAppMessageType(int index,CharSequence[] menulist)
	{
		if(index > menulist.length)
		{
			return MessageType.UNKNOWN;
		}
		switch (index)
		{
		case 0:
		{
			return MessageType.SMS;
		}
		case 1:
		{
			return MessageType.WECHAT;
		}
		case 2:
		{
			return MessageType.QQ;
		}
		case 3:
		{
			return MessageType.FACEBOOK;
		}
		case 4:
		{
			return MessageType.TWITTER;
		}
		case 5:
		{
			return MessageType.LINE;
		}
		case 6:
		{
			return MessageType.GMAIL;
		}
		case 7:
		{
			return MessageType.SE_WELLNESS;
		}
		case 8:
		{
			return MessageType.KAKAO;
		}
		case 9:
		{
			return MessageType.ALL;
		}
		default:
		{
			return MessageType.OTHER;
		}
		}
	}

	/**
	 * @param msgType
	 * @return
	 */
	public static String getMessagePackageName(MessageType msgType) 
	{
		if(MessageType.WECHAT==msgType)
		{
			return PACKAGE_NAME_WEIXIN;
		}
		if(MessageType.QQ==msgType)
		{
			return PACKAGE_NAME_QQ;
		}
		if(MessageType.FACEBOOK==msgType)
		{
			return PACKAGE_NAME_FACEBOOK;
		}
		if(MessageType.LINE==msgType)
		{
			return PACKAGE_NAME_LINE;
		}
		if(MessageType.GMAIL==msgType)
		{
			return PACKAGE_NAME_GMAIL;
		}
		if(MessageType.SE_WELLNESS==msgType)
		{
			return PACKAGE_NAME_SeWellness;
		}if(MessageType.KAKAO==msgType)
		{
			return PACKAGE_NAME_KAKAO;
		}
		if(MessageType.TWITTER==msgType)
		{
			return PACKAGE_NAME_TWITTER;
		}
		if(MessageType.OTHER == msgType){
			return "com.bluetooth.demo";
		}
		if(MessageType.INSTAGRAM == msgType){
			return PACKAGE_NAME_INSTAGRAM;
		}
		else return "";
	}


	public static CharSequence[] getAppMessageMenu()
	{
		return new String[]
				{
				"SMS Message",
				"Wechat Message",
				"QQ Message",
				"Facebook Message",
				"Twitter Message",
				"Line Message",
				"Gmail Message",
				"Se Wellness Message",
				"Kakao Message",
				};
	}
	
	/**
	 * 获取App当前的版本信息
	 * @param appContext
	 * @return
	 */
	public static  String getAppVersion(Context appContext) 
	{
		if(appContext==null){
			return "App Version unknown";
		}
		String version = "";
		try 
		{
			PackageManager manager = appContext.getPackageManager();
			if(manager!=null)
			{
				PackageInfo info = manager.getPackageInfo(appContext.getPackageName(), 0);
				if(info!=null)
				{
					version = info.versionName;
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return version;
	}
	
	public static String getAppVersionCode(Context context){
		if(context==null){
			return "0.0.0";
		}
		try 
		{
			if(context.getPackageManager()!=null){
				PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
				return info.versionCode+"";
			}
			return "0.0.0";
		} catch (Exception e) {
			e.printStackTrace();
			return "0.0.0";
		}
	}
	
	/**
	 * 获取手机真实的蓝牙MAC地址
	 * @return
	 */
	public static String getBtAddressViaReflection() {
	    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	    Object bluetoothManagerService = new Mirror().on(bluetoothAdapter).get().field("mService");
	    if (bluetoothManagerService == null) {
	        Log.e("LS-BLE", "couldn't find bluetoothManagerService");
	        return null;
	    }
	    Object address = new Mirror().on(bluetoothManagerService).invoke().method("getAddress").withoutArgs();
	    if (address != null && address instanceof String) {
	        Log.e("LS-BLE", "using reflection to get the BT MAC address: " + address);
	        return (String) address;
	    } else {
	        return null;
	    }
	}
}
