/**
 * 
 */
package com.bluetooth.demo.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import com.lifesense.ble.bean.PedometerData;
import com.lifesense.ble.bean.constant.PacketProfile;

import android.text.TextUtils;
import android.util.Log;

/**
 * @author sky
 *
 */
public class DeviceDataUtils {

	private static Map<String, List<Object>>dataMap=new HashMap<String, List<Object>>();

	
	public static void clearCacheData()
	{
		dataMap=new HashMap<String, List<Object>>();
	}
	
	public static List<Object> getDeviceMeasurementData(String broadcastID)
	{
		if(dataMap.size()==0)
		{
			return null;
		}
		return dataMap.get(getObjectKey(broadcastID));
	}
	
	
	public static void addDeviceMeasurementData(String broadcastId,Object obj)
	{
		List<Object> dataObjs=getDeviceMeasurementData(broadcastId);
		if(dataObjs==null)
		{
			dataObjs=new ArrayList<Object>();
		}
		String key=getObjectKey(broadcastId);
		dataObjs.add(obj);
		//remove old obj
		dataMap.remove(key);
		//caching new data obj
		dataMap.put(key, dataObjs);
	}
	

	private static String getObjectKey(String broadcastID)
	{
		if(TextUtils.isEmpty(broadcastID))
		{
			return broadcastID;
		}
		
		if(dataMap==null || dataMap.size()==0)
		{
			return broadcastID;
		}
		String targetKey=broadcastID;
		Set<String> keys=dataMap.keySet();
		for(String key:keys)
		{
			if(key!=null && key.equalsIgnoreCase(broadcastID))
			{
				targetKey=key;
			}
		}
		return targetKey;
	}
	
	/**
	 * 格式化字符串信息
	 * @param message
	 * @return
	 */
	public static  String formatStringValue(String message)
	{
		String tempMessage=message.replace("[", "\r\n");
		tempMessage=tempMessage.replace(",", ",\r\n");
		tempMessage=tempMessage.replace("]", "\r\n");
		return tempMessage;
	}
	
	/**
	 * 从设备测量数据中，获取电量信息
	 * @param obj
	 * @param packet
	 * @return
	 */
	public static int getDevicePowerPercent(Object obj,PacketProfile packet){
		if(obj==null){
			return 0;
		}
		if(packet==PacketProfile.UNKNOWN){
			return 0;
		}
		if(obj instanceof PedometerData){
			PedometerData data=(PedometerData)obj;
			return data.getBatteryPercent();
		}
		try
		{
			if(PacketProfile.DAILY_MEASUREMENT_DATA == packet 
					|| PacketProfile.PER_HOUR_MEASUREMENT_DATA == packet 
					|| PacketProfile.PEDOMETER_DATA_CA == packet 
					|| PacketProfile.PEDOMETER_DATA_C9 == packet 
					|| PacketProfile.PEDOMETER_DATA_82 == packet 
					|| PacketProfile.PEDOMETER_DATA_8B == packet){
				if(obj instanceof List)
				{
					@SuppressWarnings("unchecked")
					List<PedometerData> datas=(List<PedometerData>)obj;
					return datas.get(datas.size()-1).getBatteryPercent();
				}
				return 0;
			}
			else {
				return 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	
	/**
	 * 11:09
	 * @param time
	 * @return
	 */
	public static int getHourFromTime(String time){
		if(TextUtils.isEmpty(time)){
			return 0;
		}
		return Integer.parseInt(time.substring(0, time.lastIndexOf(":")));
	}
	
	public static int getMinuteFromTime(String time){
		if(TextUtils.isEmpty(time)){
			return 0;
		}
		return Integer.parseInt(time.substring(time.lastIndexOf(":")+1));
	}
	
	/**
	 * 将非小数点的double value转成有效的double value
	 * 保留1位小数
	 * @param value
	 * @return
	 */
	public static double toDouble(String value,int digitNumber)
	{
		if(value==null){
			return 0;
		}
		try 
		{
			BigDecimal decimal = new BigDecimal(value);
			decimal=decimal.setScale(digitNumber,BigDecimal.ROUND_HALF_UP);
	        return decimal.doubleValue();
		} catch (Exception e) 
		{
			Log.e("LS-BLE", "failed to format double value..."+value);
			e.printStackTrace();
			return 0;
		}
	}
	
	/**
	 * 将字节数组转换成16进制数据,带格式转换 e.g 00-AA-BB
	 * @param data
	 * @return
	 */
	public static String byte2hex(byte[] data) 
	{
		if(data==null)
		{
			return "";
		}

		String HS = "";
		String STMP = "";
		for (int n=0;n<data.length;n++) 
		{
			STMP=(Integer.toHexString(data[n]&0XFF));
			if(STMP.length()==1) 
			{
				HS=HS+"0"+STMP;
			}
			else  
			{
				HS=HS+STMP;
			}
			if(n<data.length-1)  
			{
				HS=HS+"-";
			}		 
		}
		return   HS.toUpperCase();	 
	}
	
	/**
	 * format Local time,带时区的转换
	 * @param hourOfDay
	 * @param minute
	 * @return
	 */
	public static byte[] formatLocalTime(int hourOfDay,int minute)
	{
//		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		String utcStr = "";
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		int year=cal.get(Calendar.YEAR);
		int month=cal.get(Calendar.MONTH);
		int day=cal.get(Calendar.DAY_OF_MONTH);
		cal.set(year, month, day, hourOfDay, minute);
		//0时区utc
		long utc=cal.getTimeInMillis();
		//获取当前时区的偏移量
		utc=utc+getCurrentTimezoneOffsetInMillis();
		Log.e("LS-BLE", "set custom local time utc >> "+(utc/1000)+"; year="+year+"; month="+month+"; day="+day);
		utcStr =String.format("%08X", (utc/1000));
		return hexStringToBytes(utcStr);
	}
	
	/**
	 * 根据小时与分钟，转成UTC,不带时区
	 * @param hourOfDay
	 * @param minute
	 * @return
	 */
	public static long formatUtcTime(int hourOfDay,int minute)
	{
		Calendar cal = Calendar.getInstance();
		int year=cal.get(Calendar.YEAR);
		int month=cal.get(Calendar.MONTH);
		int day=cal.get(Calendar.DAY_OF_MONTH);
		cal.set(year, month, day, hourOfDay, minute);
		//0时区utc
		return cal.getTimeInMillis()/1000;
	}
	
	/**
	 * 根据小时与分钟，转成UTC,不带时区
	 * @param hourOfDay
	 * @param minute
	 * @return
	 */
	public static long formatUtcTime(int year,int month,int day,int hourOfDay,int minute)
	{
		Calendar cal = Calendar.getInstance();
//		int year=cal.get(Calendar.YEAR);
//		int month=cal.get(Calendar.MONTH);
//		int day=cal.get(Calendar.DAY_OF_MONTH);
		cal.set(year, month, day, hourOfDay, minute);
		//0时区utc
		return cal.getTimeInMillis()/1000;
	}
	
	/**
	 * 16进制数据转换成字节数组，无格式转换 e.g 00AABB
	 * @param hexString
	 * @return
	 */
	public static byte[] hexStringToBytes(String hexString) 
	{
		if (hexString == null || hexString.equals("")) 
		{   return null;  
		}  
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;  
		char[] hexChars = hexString.toCharArray();  
		byte[] data = new byte[length];  
		for (int i = 0; i < length; i++) 
		{  
			int pos = i * 2;  
			data[i] = (byte) (charToByte(hexChars[pos]) << 4 
					| charToByte(hexChars[pos + 1]));
		}  
		return data;  
	} 
	
	/**
	 * 根据字符返回一个十六进制的字符
	 * @param c
	 * @return
	 */
	public static byte charToByte(char c)
	{ 
		return (byte) "0123456789ABCDEF".indexOf(c);  	
	}
	
	/**
	 * 获取时区的时间偏移毫秒数
	 * @return
	 */
	public static int getCurrentTimezoneOffsetInMillis()
	{
		TimeZone tz = TimeZone.getDefault();  
		Calendar cal = GregorianCalendar.getInstance(tz);
		int offsetInMillis = tz.getOffset(cal.getTimeInMillis());
		return offsetInMillis;
	}


}
