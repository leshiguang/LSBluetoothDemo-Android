/**
 * 
 */
package com.bluetooth.demo.ui;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.PreferenceFragment;

import com.bluetooth.demo.R;
import com.lifesense.ble.bean.constant.BroadcastType;
import com.lifesense.ble.bean.constant.DeviceType;

/**
 * @author CaiChiXiang
 *
 */
@SuppressLint("DefaultLocale")
public class ScanFilterFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener{


	private ListPreference broadcastTypePreference;
	private MultiSelectListPreference deviceTypePreference;
	private EditTextPreference productBarcodePreference;
	
	private  BroadcastType mBroadcastType=BroadcastType.ALL;
	private  List<DeviceType> mScanDeviceType;

	private static final String SPF_BROADCAST_TYPE_KEY="broadcast_type";
	private static final String SPF_DEVICE_TYPE_KEY="device_type";
	private static final String SPF_PRODUCT_BARCODE_KEY="add_barcode_preference";
	
	private static final String[] broadcastTypeValues=new String[]{
		"All Broadcast",
		"Pairing Broadcast",
		"Normal Broadcast"
	};
	
	private static final String[] broadcastTypeKeys=new String[]{
		"0",
		"1",
		"2"
		
	};
	
	
	private static final String[] deviceTypeValues=new String[]{
		"Fat Scale",
		"Height Meter",
		"Kitchen Scale",
		"Pedometer",
		"Blood Pressure Monitors",
		"Weight Scale"
	};
	
	private static final String[] deviceTypeKeys=new String[]{
		DeviceType.FAT_SCALE.toString(),
		DeviceType.HEIGHT_RULER.toString(),
		DeviceType.KITCHEN_SCALE.toString(),
		DeviceType.PEDOMETER.toString(),
		DeviceType.SPHYGMOMANOMETER.toString(),
		DeviceType.WEIGHT_SCALE.toString()
	};
	
	public ScanFilterFragment()
	{
		
	}
	
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.setting);

		 broadcastTypePreference = (ListPreference)findPreference(SPF_BROADCAST_TYPE_KEY);
		 broadcastTypePreference.setEntryValues(broadcastTypeKeys);
		 broadcastTypePreference.setEntries(broadcastTypeValues);

		 deviceTypePreference=(MultiSelectListPreference) findPreference(SPF_DEVICE_TYPE_KEY);
		 deviceTypePreference.setEntries(deviceTypeValues);
		 deviceTypePreference.setEntryValues(deviceTypeKeys); 
		
		 productBarcodePreference=(EditTextPreference) findPreference(SPF_PRODUCT_BARCODE_KEY);
		 
		 setBroadcastTypeSummary();
		 setDeviceTypeSummary();
		 setProductBarcodeSummary();
	}
	
	
	@Override
	public void onStart() {
		super.onStart();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	
	}


	@Override
	public void onStop() {
		super.onStop();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		
	}


	/* (non-Javadoc)
	 * @see android.content.SharedPreferences.OnSharedPreferenceChangeListener#onSharedPreferenceChanged(android.content.SharedPreferences, java.lang.String)
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,String key) 
	{
		String str=null;
		if(sharedPreferences!=null)
		{
			if(key.equals(SPF_BROADCAST_TYPE_KEY))
			{
				str=sharedPreferences.getString(SPF_BROADCAST_TYPE_KEY, null);
				setBroadcastTypeSummary();
			}
			else if(key.equals(SPF_DEVICE_TYPE_KEY))
			{
				setDeviceTypeSummary();
			
			}
			else if(key.equals(SPF_PRODUCT_BARCODE_KEY))
			{
				setProductBarcodeSummary();
			}
			
		}
		
		System.err.println("onSharedPreferenceChanged,key="+key+";value ="+str);
	}

	
	/**
	 * 
	 */
	private void setProductBarcodeSummary() 
	{
		if(productBarcodePreference!=null )
		{
			String barcode=productBarcodePreference.getText();
			productBarcodePreference.setSummary(barcode);
		}
		
	}

	/**
	 * 
	 */
	private void setDeviceTypeSummary() {
		mScanDeviceType=new ArrayList<DeviceType>();
		Set<String> strSet=getPreferenceScreen().getSharedPreferences().getStringSet(SPF_DEVICE_TYPE_KEY, null);
		if(strSet!=null)
		{
			for(String value:strSet)
			{
				if(value.equals(DeviceType.FAT_SCALE.toString()))
				{
					 mScanDeviceType.add(DeviceType.FAT_SCALE);
				}
				else if(value.equals(DeviceType.HEIGHT_RULER.toString()))
				{
					mScanDeviceType.add(DeviceType.HEIGHT_RULER);
				}
				else if(value.equals(DeviceType.KITCHEN_SCALE.toString()))
				{
					 mScanDeviceType.add(DeviceType.KITCHEN_SCALE);
				}
				else if(value.equals(DeviceType.PEDOMETER.toString()))
				{
					mScanDeviceType.add(DeviceType.PEDOMETER);
				}
				else if(value.equals(DeviceType.SPHYGMOMANOMETER.toString()))
				{
					mScanDeviceType.add(DeviceType.SPHYGMOMANOMETER);
				}
				else if(value.equals(DeviceType.WEIGHT_SCALE.toString()))
				{
					 mScanDeviceType.add(DeviceType.WEIGHT_SCALE);
				}
				
				System.err.println("my device type multi choose:"+value);
			}
			
		}
		
	}


	public  BroadcastType getBroadcastType() {
		return mBroadcastType;
	}


	public  List<DeviceType> getDeviceTypes() {
		return mScanDeviceType;
	}

	
	
	private void setBroadcastTypeSummary() 
	{
		
		if(broadcastTypePreference!=null && broadcastTypePreference.getValue()!=null)
		{
			int index=Integer.valueOf(broadcastTypePreference.getValue());
			System.out.println("my broadcast type choose:"+index);
			broadcastTypePreference.setSummary(broadcastTypeValues[index]);	
			mBroadcastType=BroadcastType.ALL;
			
			if(index==1)
			{
				mBroadcastType=BroadcastType.PAIR;
			}
			if(index==2)
			{
				mBroadcastType=BroadcastType.NORMAL;
			}
			
			
		}

	}
	
}
