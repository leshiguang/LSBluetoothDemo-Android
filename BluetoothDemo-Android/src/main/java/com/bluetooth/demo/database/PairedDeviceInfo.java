package com.bluetooth.demo.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.lifesense.ble.bean.LsDeviceInfo;

public class PairedDeviceInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3066238256371591835L;
	private HashMap<String,com.lifesense.ble.bean.LsDeviceInfo>pairedDeviceMap;

	public HashMap<String,com.lifesense.ble.bean.LsDeviceInfo> getPairedDeviceMap() {
		return pairedDeviceMap;
	}

	public void setPairedDeviceMap(HashMap<String,com.lifesense.ble.bean.LsDeviceInfo> pairedDeviceMap) {
		this.pairedDeviceMap = pairedDeviceMap;
	}

	// Constant with a file name
	public static String fileName = "LSPairedDeviceInfo.ser";

	
	// Serializes an object and saves it to a file
	public void saveToFile(Context context) {
	    try {
	    	File saveingFile=new File(Environment.getExternalStorageDirectory(), "/LSPairedDeviceInfo.ser");
	        FileOutputStream fileOutputStream = context.openFileOutput(saveingFile.getName(), Context.MODE_PRIVATE);
	        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
	        objectOutputStream.writeObject(this);
	        objectOutputStream.close();
	        fileOutputStream.close();
	        
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	/**
	 * 从本地SharedPreferences文件中读取已配对的设备对象信息
	 * @return
	 */
	public static PairedDeviceInfo readPairedDeviceInfoFromFile(Context appContext)
	{
		
		PairedDeviceInfo pairedDeice=null;
		//从文件中读取已保存的设备对象信息
		SharedPreferences readPrefs=appContext.getSharedPreferences(
				appContext.getApplicationInfo().name, Context.MODE_PRIVATE);
		if(readPrefs!=null)
		{
			String jsonString=readPrefs.getString(PairedDeviceInfo.class.getName(), null);
			Gson gson = new Gson(); 
			Log.e("read info=", jsonString+"");

			pairedDeice=gson.fromJson(jsonString, PairedDeviceInfo.class);
			return pairedDeice;
		}
		else return null;
		
	}

	public static  LsDeviceInfo getDeviceByBroadcastId(String key,Context appContext)
	{
		if(key!=null && key.length()>0)
		{
			PairedDeviceInfo pairDeviceInfo=readPairedDeviceInfoFromFile(appContext);
		    if(pairDeviceInfo!=null && pairDeviceInfo.getPairedDeviceMap()!=null 
		    		&& !pairDeviceInfo.getPairedDeviceMap().isEmpty())
		    {
		    	return pairDeviceInfo.getPairedDeviceMap().get(key);
		    }
		    else return null;
		}
		else return null;
	}
	// Creates an object by reading it from a file
	public static PairedDeviceInfo readFromFile(Context context)
	{
		
		PairedDeviceInfo createResumeForm = null;
	    try {
	    	
	        FileInputStream fileInputStream = context.openFileInput(fileName);
	        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
	        createResumeForm = (PairedDeviceInfo) objectInputStream.readObject();
	        objectInputStream.close();
	        fileInputStream.close();
	    } catch (IOException e) 
	    {
	    	e.printStackTrace();
	    	return null;
	        
	    }
	    catch (ClassNotFoundException e) 
	    {
	        e.printStackTrace();
	        return null;
	    }
	    return createResumeForm;
	}
}
