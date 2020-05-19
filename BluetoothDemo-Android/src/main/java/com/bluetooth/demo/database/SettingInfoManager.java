/**
 * 
 */
package com.bluetooth.demo.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.lifesense.ble.bean.LsDeviceInfo;

/**
 * @author CaiChiXiang
 *
 */
public class SettingInfoManager {

	public static final String KEY_PAIRED_DEVICE_LIST=PairedDeviceInfo.class.getName();
	
	private static final String TAG=SettingInfoManager.class.getSimpleName();
	

	public static  List<LsDeviceInfo> getPairedDeviceInfo(Context appContext)
	{
		List<LsDeviceInfo> deviceList=null;
		String key=PairedDeviceInfo.class.getName();
		PairedDeviceInfo mPairedDeviceInfo=readPairedDeviceInfoFromFile(appContext, key);
		if(mPairedDeviceInfo!=null && mPairedDeviceInfo.getPairedDeviceMap()!=null)
		{
			deviceList=new ArrayList<LsDeviceInfo>();
			Map<String,LsDeviceInfo> deviceMap=mPairedDeviceInfo.getPairedDeviceMap();
			Iterator<Entry<String, LsDeviceInfo>> it = deviceMap.entrySet().iterator();
			while (it.hasNext()) 
			{
				Entry<String, LsDeviceInfo> entry = it.next();
				LsDeviceInfo lsDevice=entry.getValue();
				if(lsDevice!=null)
				{
					deviceList.add(lsDevice);
				}
			}
		}
		return deviceList;
	}
	
	/**
	 * 判断该设备是否已配对
	 * @param appContext
	 * @param broadcastID
	 * @return
	 */
	public static boolean isDevicePaired(Context appContext,String broadcastID)
	{
		if(appContext==null || broadcastID==null || broadcastID.length()==0)
		{
			return false;
		}
		String key=PairedDeviceInfo.class.getName();
		PairedDeviceInfo pairedDeviceInfo=readPairedDeviceInfoFromFile(appContext, key);
		if(pairedDeviceInfo!=null)
		{
			if(pairedDeviceInfo.getPairedDeviceMap()!=null 
					&& !pairedDeviceInfo.getPairedDeviceMap().isEmpty())
			{
				return pairedDeviceInfo.getPairedDeviceMap().containsKey(broadcastID);
			}
			else return false;
		}
		else return false;
	}
	
	/**
	 *  根据广播ID获取已配对的设备信息
	 * @param broadcastID
	 * @return
	 */
	public static LsDeviceInfo getPairedDeviceInfoByBroadcastID(Context appContext,String broadcastID)
	{
		if(appContext==null || broadcastID==null || broadcastID.length()==0)
		{
			return null;
		}
		String key=PairedDeviceInfo.class.getName();
		PairedDeviceInfo pairedDeviceInfo=readPairedDeviceInfoFromFile(appContext, key);
		if(pairedDeviceInfo!=null)
		{
			if(pairedDeviceInfo.getPairedDeviceMap()!=null 
					&& !pairedDeviceInfo.getPairedDeviceMap().isEmpty())
			{
				return pairedDeviceInfo.getPairedDeviceMap().get(broadcastID);
			}
			else return null;
		}
		else return null;
		
	}

	/**
	 * 从本地SharedPreferences文件中读取已配对的设备对象信息
	 * @return
	 */
	public static PairedDeviceInfo readPairedDeviceInfoFromFile(Context appContext,String key)
	{
		
		PairedDeviceInfo pairedDeice=null;
		//从文件中读取已保存的设备对象信息
		SharedPreferences readPrefs=appContext.getSharedPreferences(
				appContext.getApplicationInfo().name, Context.MODE_PRIVATE);
		if(readPrefs!=null)
		{
			//key=PairedDeviceInfo.class.getName()
			String jsonString=readPrefs.getString(key, null);
			Gson gson = new Gson(); 
			Log.e("read info=", jsonString+"");
			pairedDeice=gson.fromJson(jsonString, PairedDeviceInfo.class);
			return pairedDeice;
		}
		else return null;
		
	}
	
	public static boolean deletePairedDeviceInfo(Context appContext,String broadcastId)
	{
		boolean delete=false;
		if(broadcastId!=null && broadcastId.length()>0)
		{
			PairedDeviceInfo savePairedDeviceInfo=PairedDeviceInfo.readPairedDeviceInfoFromFile(appContext);
			if(savePairedDeviceInfo!=null)
			{
				HashMap<String,LsDeviceInfo> pairedDeviceHashMap=savePairedDeviceInfo.getPairedDeviceMap();
				
				if(pairedDeviceHashMap!=null && pairedDeviceHashMap.size()>0)
				{
					//本地存在已配对过的设备对象信息
					if(pairedDeviceHashMap.containsKey(broadcastId))
					{
						delete=true;
						//该设备已保存在本地文件，则先删除旧对象信息，后添加新的对象信息 
						pairedDeviceHashMap.remove(broadcastId);
						//放入已配对的设备信息对象属性
						savePairedDeviceInfo.setPairedDeviceMap(pairedDeviceHashMap);
						
						Gson gson = new Gson(); 
						String deviceInfo=gson.toJson(savePairedDeviceInfo);
						Log.e("更新设备信息",deviceInfo);
						String key=PairedDeviceInfo.class.getName();
						saveToSharedPreferences(appContext, key, deviceInfo);
					}
				}
			}
			
		}
		return delete;
	}
	
	/**
	 * 保存已成功配对的设备对象信息
	 * @param pairedDevice
	 */
	@SuppressLint("CommitPrefEdits")
	public static void savePairedDeviceInfoToFile(Context appContext,String key,LsDeviceInfo pairedDevice)
	{
		if(pairedDevice==null)
		{
			return ;
		}
		String deviceKey=pairedDevice.getDeviceId();
		if(!TextUtils.isEmpty(pairedDevice.getBroadcastID()))
		{
			deviceKey=pairedDevice.getBroadcastID();
		}
		HashMap<String,LsDeviceInfo> pairedDeviceHashMap=null;
		PairedDeviceInfo savePairedDeviceInfo=PairedDeviceInfo.readPairedDeviceInfoFromFile(appContext);
		if(savePairedDeviceInfo!=null)
		{
			pairedDeviceHashMap=savePairedDeviceInfo.getPairedDeviceMap();
			if(pairedDeviceHashMap!=null && pairedDeviceHashMap.size()>0)
			{
				String tempBroadcastID=null;
				tempBroadcastID=isPairedDeviceExist(pairedDeviceHashMap, deviceKey);
		
				//本地存在已配对过的设备对象信息
				if(tempBroadcastID!=null)
				{
					//该设备已保存在本地文件，则先删除旧对象信息，后添加新的对象信息 
					pairedDeviceHashMap.remove(tempBroadcastID);
					pairedDeviceHashMap.put(pairedDevice.getBroadcastID(), pairedDevice);
				}
				else
				{
					//该设备未保存在本地文件
					pairedDeviceHashMap.put(pairedDevice.getBroadcastID(), pairedDevice);
				}
				//放入已配对的设备信息对象属性
				savePairedDeviceInfo.setPairedDeviceMap(pairedDeviceHashMap);
			}
			else
			{
				//初次保存已配对的设备对象信息
				//生成Broadcast ID与设备信息的Map对照表
				HashMap<String,com.lifesense.ble.bean.LsDeviceInfo> deviceInfoMap=new HashMap<String, com.lifesense.ble.bean.LsDeviceInfo>();
				
				//以Broadcast ID作为Key,已配对的设备信息对象作为Value存入Map对照表
				deviceInfoMap.put(pairedDevice.getBroadcastID(), pairedDevice);

				//放入已配对的设备信息对象属性
				savePairedDeviceInfo.setPairedDeviceMap(deviceInfoMap);
			}
		}
		else
		{
			savePairedDeviceInfo=new PairedDeviceInfo();
			
			//初次保存已配对的设备对象信息
			//生成Broadcast ID与设备信息的Map对照表
			HashMap<String,com.lifesense.ble.bean.LsDeviceInfo> deviceInfoMap=new HashMap<String, com.lifesense.ble.bean.LsDeviceInfo>();
			
			//以Broadcast ID作为Key,已配对的设备信息对象作为Value存入Map对照表
			deviceInfoMap.put(pairedDevice.getBroadcastID(), pairedDevice);

			//放入已配对的设备信息对象属性
			savePairedDeviceInfo.setPairedDeviceMap(deviceInfoMap);
		}
	
		Gson gson = new Gson(); 
		String deviceInfo=gson.toJson(savePairedDeviceInfo);
		Log.e("将内容写入文件",deviceInfo);
		//key=PairedDeviceInfo.class.getName();
		saveToSharedPreferences(appContext, key, deviceInfo);

	
	}
	
	
	/**
	 * 根据key从SharedPreferences中读取相应的信息
	 * @param appContext
	 * @param key
	 * @return
	 */
	private static String readFromSharedPreferences(Context appContext,String key)
	{
		if(appContext!=null && key!=null && key.length()>0)
		{
			SharedPreferences readPrefs=appContext.getSharedPreferences(
					appContext.getApplicationInfo().name, Context.MODE_PRIVATE);
			if(readPrefs!=null)
			{
				//key=PairedDeviceInfo.class.getName()
				String jsonString=readPrefs.getString(key, null);
				Log.d(TAG, "read value from share preferences,key ="+key+";value="+jsonString);
				return jsonString;
			}
			else return null;
		}
		else
		{
			Log.e(TAG, "Failed to read value from share preferences,is null....");
			return null;
		}
		
	}
	
	
	/**
	 * 将内容保存至SharedPreferences
	 * @param appContext
	 * @param key
	 * @param value
	 */
	private static void saveToSharedPreferences(Context appContext,String key,String value)
	{
		if(appContext!=null && key!=null && key.length()>0 && value!=null)
		{
			SharedPreferences savePrefs=appContext.getSharedPreferences(
					appContext.getApplicationInfo().name, Context.MODE_PRIVATE);
			SharedPreferences.Editor ed=savePrefs.edit();
			ed.putString(key, value);
			ed.commit();
		}
		else
		{
			Log.e(TAG, "Failed to save content to share preferences,is null....");
		}
	}
	
	/**
	 * 判断该设备是否重复配对过
	 * @param hashMap
	 * @return
	 */
	public static String isPairedDeviceExist(Map<String,LsDeviceInfo> hashMap,String deviceId)
	{
		if(deviceId!=null && hashMap!=null && !hashMap.isEmpty())
		{
			Iterator<Entry<String, LsDeviceInfo>> it = hashMap.entrySet().iterator();
			while (it.hasNext()) 
			{
				Entry<String, LsDeviceInfo> entry = it.next();
				LsDeviceInfo lsDevice=entry.getValue();
				if(lsDevice!=null && deviceId.equals(lsDevice.getDeviceId()))
				{
					return lsDevice.getBroadcastID();
				}
			}
			return null;
		}
		else return null;

	}
	
}
