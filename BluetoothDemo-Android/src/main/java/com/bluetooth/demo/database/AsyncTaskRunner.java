/**
 * 
 */
package com.bluetooth.demo.database;

import android.content.Context;
import android.os.AsyncTask;

import com.lifesense.ble.bean.LsDeviceInfo;

/**
 * @author CaiChiXiang
 *
 */
public class AsyncTaskRunner extends AsyncTask<String, String, String>
{
	private LsDeviceInfo saveDeviceInfo;
	private Context mAppContext;
	
	public AsyncTaskRunner(Context context,LsDeviceInfo lsDeviceInfo) 
	{
		mAppContext=context;
		saveDeviceInfo=lsDeviceInfo;
	}
	
	@Override
	protected String doInBackground(String... params) 
	{
		System.err.println("异步执行保存设备信息=================");
		savePairedDeviceInfo();
		return null;
	}
	
	
	
	@Override
	protected void onPreExecute() 
	{
		super.onPreExecute();
		System.err.println("异步onPreExecute()=================");
	}

	@Override
	protected void onPostExecute(String result) 
	{
		super.onPostExecute(result);
		System.err.println("异步onPostExecute(String result)=================");
	}

	
	private void savePairedDeviceInfo()
	{
		if(saveDeviceInfo!=null)
		{
			//save paired device info to file
			String key=PairedDeviceInfo.class.getName();
			SettingInfoManager.savePairedDeviceInfoToFile(mAppContext, key,saveDeviceInfo);
		}
		
	}

}
