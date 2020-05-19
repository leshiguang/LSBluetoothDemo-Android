/**
 * 
 */
package com.bluetooth.demo.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bluetooth.demo.R;
import com.lifesense.ble.bean.LsDeviceInfo;

/**
 * @author CaiChiXiang
 *
 */
public class ShowTextDialogFragment  extends DialogFragment{

	private LsDeviceInfo lsDeviceInfo;
	private TextView infoTextView;
	private OnDialogClickListener mDialogClickListener;
	
	public ShowTextDialogFragment() {
		
	}
	
	public ShowTextDialogFragment(LsDeviceInfo deviceInfo,OnDialogClickListener onClickListener)
	{
		lsDeviceInfo=deviceInfo;
		mDialogClickListener=onClickListener;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) 
	{
		ContextThemeWrapper ctw = new ContextThemeWrapper(this.getActivity(), android.R.style.Theme_Holo_Light);
		final AlertDialog.Builder builder = new AlertDialog.Builder(ctw);
		final View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_show_info, null);
		infoTextView = (TextView) dialogView.findViewById(R.id.show_info_text_view);
	
		builder.setTitle("Paired Device Info");
		showPairResults(lsDeviceInfo);	
		final AlertDialog dialog = builder.setView(dialogView).create();
		
		Button cancelBtn = (Button) dialogView.findViewById(R.id.action_cancel);
	
		cancelBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) 
			{
				if (v.getId() == R.id.action_cancel) 
				{
					dialog.dismiss();
				}
				if(mDialogClickListener!=null)
				{
					mDialogClickListener.onDialogCancel();
				}
			}
		});
		
		return dialog;
	}
	
	/**
	 * @param lsDeviceInfo2
	 */
	private void showScanResults(LsDeviceInfo device) 
	{

		if(device!=null)
		{       
			
			infoTextView.append("unpaired device information"+"\n");
			infoTextView.append("------------------------------------"+"\n");
			infoTextView.append("deviceName: "+device.getDeviceName()+"\n");
			infoTextView.append("broadcastID: "+device.getBroadcastID()+"\n");
			infoTextView.append("deviceType: "+device.getDeviceType()+"\n"); 
			infoTextView.append("modelNumber: "+device.getModelNumber()+"\n");  	      	  
			infoTextView.append("protocolType: "+device.getProtocolType()+"\n");
		}
		else
		{
			infoTextView.append("Failed to show scan results,is null"+"\n");
		}	 	       


	}

	private void showPairResults(LsDeviceInfo device) 
	{
		if(device!=null)
		{   
			infoTextView.append("paired device information"+"\n");
			infoTextView.append("------------------------------------"+"\n");
			infoTextView.append("deviceName: "+device.getDeviceName()+"\n");			
			infoTextView.append("deviceMac: "+device.getMacAddress()+"\n");
			infoTextView.append("broadcastID: "+device.getBroadcastID()+"\n");
			infoTextView.append("deviceType: "+device.getDeviceType()+"\n"); 
			infoTextView.append("password: "+device.getPassword()+"\n"); 
			String deviceId=getDeviceId(device.getDeviceId(), device.getManufactureData());
			infoTextView.append("deviceID: "+deviceId+"\n");
			infoTextView.append("deviceSN: "+device.getDeviceSn()+"\n");
			infoTextView.append("modelNumber: "+device.getModelNumber()+"\n");  	      	  
			infoTextView.append("firmwareVersion: "+device.getFirmwareVersion()+"\n");
			infoTextView.append("hardwareVersion: "+device.getHardwareVersion()+"\n");   
			infoTextView.append("UserNumber: "+device.getDeviceUserNumber()+"\n");
			infoTextView.append("protocolType: "+device.getProtocolType()+"\n");
			infoTextView.append("manufactureData: "+device.getManufactureData()+"\n");
			if(device.getBattery() > 0){
				infoTextView.append("battery: "+device.getBattery()+" %"+"\n");
			}

		}
		else
		{
			infoTextView.append("Failed paired!Please try again"+"\n");
		}	 	       
	}
	

	/**
	 * 获取设备Id
	 * @param deviceId
	 * @param broadcastData
	 * @return
	 */
	private String getDeviceId(String deviceId,String broadcastData){
		try 
		{
			if(broadcastData==null || broadcastData.length() < 12){
				return deviceId;
			}
			int beginIndex=broadcastData.length()-12;
			return broadcastData.substring(beginIndex, broadcastData.length());
		} catch (Exception e) {
			e.printStackTrace();
			return deviceId;
		}
	}
	
}
