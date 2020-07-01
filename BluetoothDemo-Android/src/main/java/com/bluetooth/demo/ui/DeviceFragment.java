package com.bluetooth.demo.ui;

import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bluetooth.demo.R;
import com.bluetooth.demo.database.AsyncTaskRunner;
import com.bluetooth.demo.device.DeviceSettiingProfiles;
import com.bluetooth.demo.device.kchiing.BPMSettingProfiles;
import com.bluetooth.demo.device.kchiing.KchiingSettingProfiles;
import com.bluetooth.demo.ui.dialog.DialogUtils;
import com.bluetooth.demo.ui.dialog.IDialogActionListener;
import com.bluetooth.demo.ui.dialog.SelectFileDialog;
import com.bluetooth.demo.ui.dialog.SettingDialogFragment;
import com.bluetooth.demo.utils.DeviceDataUtils;
import com.bluetooth.demo.utils.FileUtils;
import com.lifesense.android.api.callback.AuthorizationCallback;
import com.lifesense.android.api.enums.AuthorizationResult;
import com.lifesense.ble.LsBleManager;
import com.lifesense.ble.OnDeviceReadListener;
import com.lifesense.ble.OnDeviceUpgradeListener;
import com.lifesense.ble.OnSettingListener;
import com.lifesense.ble.ReceiveDataCallback;
import com.lifesense.ble.bean.BloodGlucoseData;
import com.lifesense.ble.bean.BloodPressureData;
import com.lifesense.ble.bean.KitchenScaleData;
import com.lifesense.ble.bean.LsDeviceInfo;
import com.lifesense.ble.bean.PedometerData;
import com.lifesense.ble.bean.SportNotify;
import com.lifesense.ble.bean.WeightData_A2;
import com.lifesense.ble.bean.WeightData_A3;
import com.lifesense.ble.bean.WeightUserInfo;
import com.lifesense.ble.bean.bmp.BMPCommand;
import com.lifesense.ble.bean.bmp.BMPCommandPacket;
import com.lifesense.ble.bean.constant.DeviceConnectState;
import com.lifesense.ble.bean.constant.DeviceTypeConstants;
import com.lifesense.ble.bean.constant.DeviceUpgradeStatus;
import com.lifesense.ble.bean.constant.ManagerStatus;
import com.lifesense.ble.bean.constant.PacketProfile;
import com.lifesense.ble.bean.constant.ProtocolType;
import com.lifesense.ble.bean.kchiing.KAppointmentReminder;
import com.lifesense.ble.bean.kchiing.KSimpleReminder;

import java.io.File;
import java.util.List;

public class DeviceFragment extends Fragment{

	private static final String TAG="LS-BLE";
	
	private View rootView;
	private TextView deviceNameView,stateTextView,logTextView,batteryTextView,newDataTextView;
	private LsDeviceInfo currentDevice;
	private Dialog mDialog;
	private File mImageFile;
	private long startTime;
	private ProgressBar connectingProgressBar;
	private ScrollView mScrollView;
	private ProgressDialog upgradingDialog;
	private Handler mainHandler;
	private int mDataIndex;
	private DeviceSettingReceiver mSettingReceiver;
	private boolean hasSportNotify;
	private SportNotify mSportsNotify;
	private boolean isRealtimeDataShowing;
	
	/**
	 * 自定义广播接收者
	 * @author sky
	 *
	 */
	private class DeviceSettingReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			logMessage("broadcast receiver >>"+intent);
			if(intent==null){
				return ;
			}
			if(!DeviceSettiingProfiles.SETTING_PROFIES_ACTION.equalsIgnoreCase(intent.getAction().toString())){
				return ;
			}
			String msg=intent.getStringExtra("errorMsg");
			logMessage("broadcast resutls >>"+msg);
			showDeviceMeasuringData(msg);			
		}
		
	}
	


	/**
	 * Device Setting Listener
	 */
	private  OnSettingListener mSettingListener=new OnSettingListener() 
	{
		@Override
		public void onFailure(int errorCode) 
		{
			String msg=getResources().getString(R.string.setting_fail);
			DialogUtils.showToastMessage(getActivity(),msg+",errorCode="+errorCode);
		}

		@Override
		public void onSuccess(String mac) {
			String msg=getResources().getString(R.string.setting_ok);
			DialogUtils.showToastMessage(getActivity(),msg);
		}
	};
	
	/**
	 * Sport Notify Confirm Listener
	 */
	private OnSettingListener mSportNotifyConfirmListener=new OnSettingListener() 
	{
		@Override
		public void onSuccess(String macAddress) 
		{
			mainHandler.post(new Runnable() {
				@Override
				public void run() {
					mSportsNotify=null;
					String msg=getResources().getString(R.string.setting_ok);
					DialogUtils.showToastMessage(getActivity(),msg);					
				}
			});

		}

		@Override
		public void onFailure(final int errorCode) {
			mainHandler.post(new Runnable() {
				@Override
				public void run() {
					mSportsNotify=null;
					String msg=getResources().getString(R.string.setting_fail);
					DialogUtils.showToastMessage(getActivity(),msg+",errorCode="+errorCode);					
				}
			});
		
		}
	};
	
	/**
	 * 设备固件升级进度的回调对象
	 */
	private OnDeviceUpgradeListener deviceUpgradeListener=new OnDeviceUpgradeListener() 
	{

		@Override
		public void onDeviceUpdradeStateChange(String address,final DeviceUpgradeStatus upgradeStatus,final int errorCode) 
		{
			mainHandler.post(new Runnable() {
				@Override
				public void run() {
					if (upgradeStatus==DeviceUpgradeStatus.UPGRADE_SUCCESS) 
					{
						long time = (System.currentTimeMillis() - startTime) / 1000;
						int minute = (int) (time / 60);
						int second = (int) (time % 60);
						String content = getResources().getString(R.string.update_success);
						content = content.replace("%m", minute + "");
						content = content.replace("%s", second + "");
						//cancel upgrading dialog
						cancelUpgradingDialog();
						DialogUtils.showPromptDialog(getActivity(), null, content);
					}
					else if(upgradeStatus==DeviceUpgradeStatus.UPGRADE_FAILURE)
					{
						String content = getResources().getString(R.string.update_fail);
						content = content.replace("%@", "Code:"+errorCode);
						//cancel upgrading dialog
						cancelUpgradingDialog();
						DialogUtils.showPromptDialog(getActivity(), null, content);
					}
					else if(upgradeStatus==DeviceUpgradeStatus.UPGRADING
							||upgradeStatus==DeviceUpgradeStatus.ENTER_UPGRADE_MODE)
					{
						String msg="upgrading.......";
						if(DeviceUpgradeStatus.ENTER_UPGRADE_MODE==upgradeStatus)
						{
							msg="enter upgrade mode...";
						}
						showUpgradeMessage(msg);
					}
				}
			});
		}

		@Override
		public void onDeviceUpgradeProcess(final int value)
		{
			String msg="upgrading...."+value+"%";
			showUpgradeMessage(msg);

		}
	};	
	
	/**
	 * Device measurement data synchronization callback object
	 */
	private ReceiveDataCallback mDataCallback=new ReceiveDataCallback()
	{
		@Override
		public void onDeviceConnectStateChange(DeviceConnectState connectState,
				String broadcastId) 
		{
			//Device Connection Status
			updateDeviceConnectState(connectState);
		}

		@Override
		public void onReceiveWeightData_A3(WeightData_A3 wData) 
		{
			LsBleManager.getInstance().setLogMessage("object data >> "+wData.toString());
			updateNewDatMessage();
			//for test
			if(wData.getImpedance() >0){
				DeviceSettiingProfiles.calculateBodyCompositionData(wData.getImpedance());
			}
			if(currentDevice.getProtocolType().equalsIgnoreCase(ProtocolType.A6.toString())){
				//show realtime data if support
				shwoRealtimeData(wData);
			}
			else{
				/**
				 * Weight Scale Measurement Data
				 * A3 product
				 */
				showDeviceMeasuringData(wData);
			}
		}

		@Override
		public void onReceiveUserInfo(WeightUserInfo proUserInfo) 
		{
			/**
			 * Weight Scale Product User Info
			 * A3 product
			 */
			showDeviceMeasuringData(proUserInfo);
		}

		@Override
		public void onReceivePedometerMeasureData(Object dataObject,
				PacketProfile packetType, String sourceData) 
		{
			int devicePower=DeviceDataUtils.getDevicePowerPercent(dataObject, packetType);
			updateDevicePower(devicePower);
			//update new data message
			updateNewDatMessage();
			/**
			 * Pedoemter Measurement Data
			 * Product：BonbonC、Mambo、MamboCall、MamboHR、Mambo Watch、MT/Gold、ZIVA
			 */
			showDeviceMeasuringData(dataObject);
			LsBleManager.getInstance().setLogMessage("object data >> "+dataObject.toString());
		}

		@Override
		public void onReceiveWeightDta_A2(WeightData_A2 wData) 
		{
			updateNewDatMessage();
			/**
			 * Weight Scale Measurement Data
			 * A2 product
			 */
			showDeviceMeasuringData(wData);
		}

		@Override
		public void onReceiveBloodPressureData(BloodPressureData bpData) 
		{
			updateNewDatMessage();
			/**
			 * Blood Pressure Measurement Data
			 * A2/A3 product
			 */
			showDeviceMeasuringData(bpData);
		}

		@Override
		public void onReceivePedometerData(PedometerData pData) 
		{
			if(pData.getBatteryPercent() >0){
				updateDevicePower(pData.getBatteryPercent());
			}
			updateNewDatMessage();
			/**
			 * Pedometer Measurement Data
			 * A2 product
			 */
			showDeviceMeasuringData(pData);
		}

		@Override
		public void onReceiveKitchenScaleData(KitchenScaleData kiScaleData) 
		{
			updateNewDatMessage();
			/**
			 * Kitchen Scale Measurement Data
			 */
			showDeviceMeasuringData(kiScaleData);
		}

		@Override
		public void onReceiveDeviceInfo(LsDeviceInfo lsDevice) 
		{
			if(lsDevice==null || currentDevice==null)
			{
				return ;
			}
			Log.e("LS-BLE", "Demo-Update Device Info:"+lsDevice.toString());
			//update and reset device's firmware version
			currentDevice.setFirmwareVersion(lsDevice.getFirmwareVersion());
			currentDevice.setHardwareVersion(lsDevice.getHardwareVersion());
			currentDevice.setModelNumber(lsDevice.getModelNumber());
			if(getActivity()!=null)
			{
				//update and save device information
				AsyncTaskRunner runner = new AsyncTaskRunner(getActivity(),currentDevice);
				runner.execute();
				//show device information
				StringBuffer strBuffer=new StringBuffer();
				strBuffer.append("Device Version Information....,");
				strBuffer.append("ModelNumber:"+currentDevice.getModelNumber()+",");
				strBuffer.append("firmwareVersion:"+currentDevice.getFirmwareVersion()+",");
				strBuffer.append("hardwareVersion:"+currentDevice.getHardwareVersion()+",");

				showDeviceMeasuringData(strBuffer);
			}
		}

		@Override
		public void onPedometerSportsModeNotify(final String macAddress,final SportNotify sportNotify) 
		{
			showDeviceMeasuringData(sportNotify);
			if(mSportsNotify!=null &&
					mSportsNotify.getRequestType() == sportNotify.getRequestType() 
					&& mSportsNotify.getSportsType() == sportNotify.getSportsType()){
				Log.e("LS-BLE", "the same sport notify:"+sportNotify.toString()+"; local >> "+mSportsNotify.toString());
				return ;
			}
			mSportsNotify = sportNotify;
			mainHandler.post(new Runnable() {
				@Override
				public void run() {
					//回复GPS的定位状态
					DeviceSettiingProfiles.updatePhoneGpsStatus(macAddress, sportNotify, mSportNotifyConfirmListener);
				}
			});
		}

		@Override
		public void onReceiveBloodGlucoseData(BloodGlucoseData bgData) 
		{
			updateNewDatMessage();
			showDeviceMeasuringData(bgData);
		}
		
		
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) 
	{
		rootView=inflater.inflate(R.layout.activity_device, container, false);
		deviceNameView=(TextView)rootView.findViewById(R.id.device_name_tv);
		stateTextView=(TextView)rootView.findViewById(R.id.device_connect_state_tv);
		newDataTextView=(TextView)rootView.findViewById(R.id.new_data_text_view);
		connectingProgressBar=(ProgressBar)rootView.findViewById(R.id.syncing_progress_bar);
		mScrollView=(ScrollView)rootView.findViewById(R.id.device_scrollView);
		logTextView=(TextView)rootView.findViewById(R.id.device_log_text_view);
		batteryTextView=(TextView)rootView.findViewById(R.id.device_battery_tv);
		//init view
		mScrollView.setBackgroundColor(Color.WHITE);
		logTextView.setVisibility(View.VISIBLE);
		logTextView.setText("");
		batteryTextView.setText("");
		connectingProgressBar.setVisibility(View.GONE);
		setHasOptionsMenu(true);
		
		//注册设置回调广播
		mSettingReceiver=new DeviceSettingReceiver();
		IntentFilter filter=new IntentFilter();
		filter.addAction(DeviceSettiingProfiles.SETTING_PROFIES_ACTION);
		//注册广播
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mSettingReceiver,  filter);
		return rootView;
	}


	protected void shwoRealtimeData(final WeightData_A3 wData) 
	{
		mainHandler.post(new Runnable() {
			@Override
			public void run() {
				if(wData.isRealtimeData()){
					if(isRealtimeDataShowing){
						//update real time data
						String msg="Scale Realtime Data:"+wData.getWeight();
						showUpgradeMessage(msg);
						return ;
					}
					else{
						isRealtimeDataShowing=true;
						showUpgradingDialog(null, "Scale Realtime Data:"+wData.getWeight());
					}
				}
				else{
					cancelUpgradingDialog();
					isRealtimeDataShowing=false;
					//print data
					showDeviceMeasuringData(wData);
				}
			}
		});
	}


	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		logMessage("onCreate");
		mDataIndex=0;
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onStart() 
	{
		logMessage("onStart");
		super.onStart();
		Bundle args = getArguments();
		if (args!=null && args.containsKey("DeviceInfo")) 
		{
			currentDevice = args.getParcelable("DeviceInfo");
			//update test device info
			deviceNameView.setText(currentDevice.getDeviceName()+" ["+currentDevice.getMacAddress()+"]");
		}
		if(currentDevice!=null && !TextUtils.isEmpty(currentDevice.getBroadcastID()))
		{
			//判断设备是否有缓存的测量数据
			List<Object> cacheDatas=DeviceDataUtils.getDeviceMeasurementData(currentDevice.getBroadcastID());
			if(cacheDatas==null || cacheDatas.size()==0)
			{
				return ;
			}
			for(Object obj:cacheDatas)
			{
				showDeviceMeasuringData(obj);
			}
		}
		//清空缓存数据
		DeviceDataUtils.clearCacheData();

	}


	@Override
	public void onStop() 
	{
		logMessage("onStop");
		super.onStop();
	}

	@Override
	public void onResume() {
		logMessage("onResume....");
		super.onResume();
		DeviceSettiingProfiles.initActivityContext(getActivity());
		if(mainHandler==null){
			mainHandler=new Handler(getActivity().getMainLooper());
			DialogUtils.initDialogHandler(mainHandler);
		}
		//try to connected device
		connectDevice();
	}



	@Override
	public void onDestroy() 
	{
		logMessage("onDestroy");
		super.onDestroy();
		LsBleManager.getInstance().stopDataReceiveService();
		if(getActivity()!=null){
			LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mSettingReceiver);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) 
	{
		if(DeviceSettiingProfiles.isKchiingDevice(currentDevice)){
			inflater.inflate(R.menu.kchiing_functions, menu);
		}
		else if(DeviceSettiingProfiles.isNewProtocolDevice(currentDevice)){
			inflater.inflate(R.menu.pedometer_functions, menu);
		}
		else if(currentDevice!=null && 
				ProtocolType.A6.toString().equalsIgnoreCase(currentDevice.getProtocolType()))
		{
			inflater.inflate(R.menu.scale_functions, menu);
		}
		else if(currentDevice!=null &&
				ProtocolType.A2.toString().equalsIgnoreCase(currentDevice.getProtocolType()) 
				&& DeviceTypeConstants.PEDOMETER.equalsIgnoreCase(currentDevice.getDeviceType())){
			//A2 手环
			inflater.inflate(R.menu.a2_pedometer, menu);
		}
		else{
			inflater.inflate(R.menu.common_functions, menu);
		}
	}
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		handleDeviceFunction(item.getItemId());
		return super.onOptionsItemSelected(item);
	}

	/**
	 * ************************************************
	 */
	
	/**
	 * 连接设备
	 */
	private void connectDevice() {
		if(!LsBleManager.getInstance().isSupportLowEnergy())
		{
			DialogUtils.showPromptDialog(getActivity(),"Prompt", "Unsupport Bluetooth Low Energy");
			return ;
		}
		if(!LsBleManager.getInstance().isOpenBluetooth())
		{
			DialogUtils.showPromptDialog(getActivity(),"Prompt", "Please turn on Bluetooth");
			return ;
		}
		if(currentDevice==null){
			DialogUtils.showPromptDialog(getActivity(),"Prompt", "No Devices!");
			return ;
		}
		if(LsBleManager.getInstance().checkDeviceConnectState(currentDevice.getMacAddress())==DeviceConnectState.CONNECTED_SUCCESS){
			LsBleManager.getInstance().registerDataSyncCallback(mDataCallback);
			connectingProgressBar.setVisibility(View.GONE);
			stateTextView.setTextColor(Color.BLUE);
			stateTextView.setText(getResources().getString(R.string.state_connected));
			return ;
		}
		if(LsBleManager.getInstance().getLsBleManagerStatus() == ManagerStatus.DATA_RECEIVE){
			return ;
		}
		LsBleManager.getInstance().stopDataReceiveService();
		//clear measure device list
		LsBleManager.getInstance().setMeasureDevice(null);
		//set delay disconnect
		currentDevice.setDelayDisconnect(true);
		//add target measurement device
		LsBleManager.getInstance().addMeasureDevice(currentDevice, new AuthorizationCallback() {
			@Override
			public void callback(AuthorizationResult authorizationResult) {
				if(authorizationResult!=null && authorizationResult.getCode() == AuthorizationResult.SUCCESS.getCode()){
					Log.e("LS-BLE","resp of auth >>"+authorizationResult.toString());
					//set product user info on data syncing mode
					DeviceSettiingProfiles.setProductUserInfoOnSyncingMode(currentDevice);
					//start data syncing service
					LsBleManager.getInstance().startDataReceiveService(mDataCallback);
				}
				else{
					Log.e("LS-BLE","resp of auth >>"+authorizationResult.toString());
					//认证失败
					DialogUtils.showPromptDialog(getActivity(),"Prompt", "Device Authentication Failure!"+currentDevice.getDeviceName()+"["+currentDevice.getMacAddress()+"]");
					updateDeviceConnectState(DeviceConnectState.UNKNOWN);
				}
			}
		});
		//update connect state
		updateDeviceConnectState(DeviceConnectState.CONNECTING);
	}
	
	

	/**
	 * 展示设备详细信息
	 * @param device
	 */
	private void showDeviceInfo(final LsDeviceInfo device) 
	{
		if(getActivity()==null){
			return ;
		}
		mainHandler.post(new Runnable() {
			@Override
			public void run() {
				logTextView.append("------------------------------------"+"\r\n");
				logTextView.append("deviceName: "+device.getDeviceName()+"\n");
				logTextView.append("broadcastID: "+device.getBroadcastID()+"\n");
				logTextView.append("deviceType: "+device.getDeviceType()+"\n"); 
				logTextView.append("password: "+device.getPassword()+"\n"); 
				logTextView.append("deviceID: "+device.getDeviceId()+"\n");
				logTextView.append("deviceSN: "+device.getDeviceSn()+"\n");
				logTextView.append("modelNumber: "+device.getModelNumber()+"\n");  	      	  
				logTextView.append("firmwareVersion: "+device.getFirmwareVersion()+"\n");
				logTextView.append("hardwareVersion: "+device.getHardwareVersion()+"\n");   
				logTextView.append("softwareVersion: "+device.getSoftwareVersion()+"\n");
				logTextView.append("UserNumber: "+device.getDeviceUserNumber()+"\n");
				logTextView.append("protocolType: "+device.getProtocolType()+"\n");				
			}
		});
		
	}

	/**
	 * 展示设备的测量数据
	 * @param obj
	 */
	private void showDeviceMeasuringData(final Object obj)
	{
		if(obj==null || getActivity()==null){
			return ;
		}
		mainHandler.post(new Runnable() {
			
			@Override
			public void run() {
				logTextView.append("\r\n");
				logTextView.append(Html.fromHtml("<font color='red'>"+"-----------------------"+"</font>"));
				logTextView.append("\r\n");
				logTextView.append(DeviceDataUtils.formatStringValue(obj.toString()));	
				logTextView.append("\r\n");
			}
		});
	}

	/**
	 * 更新数据提示信息
	 */
	private void updateNewDatMessage(){
		if(getActivity()==null){
			return ;
		}
		mainHandler.post(new Runnable() {
			@Override
			public void run() {
				newDataTextView.clearAnimation();
				mDataIndex++;
				String newsStr=getResources().getString(R.string.str_new_data);
				newsStr=newsStr.replace("%@", mDataIndex+"");
				newDataTextView.setText(newsStr);
				newDataTextView.setTextColor(Color.RED);	
				newDataTextView.setAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in));
			}
		});
	}
	
	/**
	 * 更新设备电量显示
	 * @param percentage
	 */
	private void updateDevicePower(final int percentage) 
	{
		if(getActivity() ==null){
			return ;
		}
		if(percentage <=0){
			return ;
		}
		mainHandler.post(new Runnable() {
			@Override
			public void run() {
				batteryTextView.clearAnimation();
				String power=getResources().getString(R.string.str_battery_percentage);
				power=power.replace("%@", percentage+"%");
				batteryTextView.setText(power);
				batteryTextView.setVisibility(View.VISIBLE);
				batteryTextView.setAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in));
			}
		});
	}
	
	/**
	 * 更新设备的连接状态信息
	 * @param connectState
	 */
	private void updateDeviceConnectState(final DeviceConnectState connectState) 
	{
		if(getActivity()==null){
			return ;
		}
		mainHandler.post(new Runnable() {
			@Override
			public void run() {
				if(DeviceConnectState.CONNECTED_SUCCESS==connectState){
					connectingProgressBar.setVisibility(View.GONE);
					stateTextView.setTextColor(Color.BLUE);
					newDataTextView.setVisibility(View.VISIBLE);
					newDataTextView.setText(getResources().getString(R.string.str_no_data));
					newDataTextView.setTextColor(Color.GRAY);
					logTextView.setText("");
					stateTextView.setText(getResources().getString(R.string.state_connected));
					DialogUtils.showToastMessage(getActivity(), "Successful connection.");
				}
				else{
					mDataIndex=0;
					if(DeviceConnectState.DISCONNECTED == connectState){
						stateTextView.setTextColor(Color.RED);
						stateTextView.setText(getResources().getString(R.string.state_disconnect));
						DialogUtils.showToastMessage(getActivity(), stateTextView.getText().toString());
					}
					else{
						stateTextView.setTextColor(Color.GRAY);
						stateTextView.setText(getResources().getString(R.string.state_connecting));
					}
					//
					if(LsBleManager.getInstance().getLsBleManagerStatus()==ManagerStatus.DATA_RECEIVE){
						connectingProgressBar.setVisibility(View.VISIBLE);
						newDataTextView.setVisibility(View.GONE);
					}
					else{
						newDataTextView.setVisibility(View.GONE);
						connectingProgressBar.setVisibility(View.GONE);
						stateTextView.setTextColor(Color.BLACK);
						stateTextView.setText(getResources().getString(R.string.state_unknown));
					}
				}
			}
		});
	}


	/**
	 * log debug message
	 * @param msg
	 */
	private void logMessage(String msg){
		msg=DeviceFragment.class.getSimpleName()+" >> "+msg;
		Log.e(TAG, msg);
	}
	
	/**
	 * 显示升级提示对话框
	 * @param title
	 * @param message
	 */
	private void showUpgradingDialog(String title,final String message)
	{
		ContextThemeWrapper ctw = new ContextThemeWrapper(getActivity(), android.R.style.Theme_Holo_Light);
		upgradingDialog = ProgressDialog.show(ctw, null, message, false);
		upgradingDialog.setCancelable(false);
		upgradingDialog.setOnDismissListener(new OnDismissListener() 
		{
			@Override
			public void onDismiss(DialogInterface dialog) 
			{
				
			}
		});
	}
	
	/**
	 * 显示升级提示信息
	 * @param msg
	 */
	private void showUpgradeMessage(final String msg){
		mainHandler.post(new Runnable() {
			
			@Override
			public void run() {
				if(upgradingDialog!=null && upgradingDialog.isShowing()){
					upgradingDialog.setMessage(msg);
				}
			}
		});
	}
	
	/**
	 * 取消升级提示Dialog
	 */
	private void cancelUpgradingDialog(){
		if(upgradingDialog!=null && upgradingDialog.isShowing()){
			upgradingDialog.dismiss();
		}
	}
	
	/**
	 * upgrade device firmware
	 */
	private void upgradeDevice() 
	{
		/**
		 * stop all data syncing or previous upgrading,make sure the status of sdk is idle
		 */
		LsBleManager.getInstance().stopDataReceiveService();
		LsBleManager.getInstance().interruptUpgradeProcess(currentDevice.getMacAddress());
		//get upgrade file from assets folder
		final List<File> fileList = FileUtils.getUpgradeFileFromSDcard();//FileTools.getUpgradeFileFromAssets(getActivity(),currentDevice);
		if(fileList==null || fileList.size() ==0)
		{
			DialogUtils.showPromptDialog(getActivity(),"Upgrade Failure", "File Not Found!");
			return ;
		}
		//show upgrading dialog
		mDialog = new SelectFileDialog.Builder(getActivity())
		.setFileList(fileList)
		.setOnItemClickListener(new AdapterView.OnItemClickListener() 
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
			{
				//save upgrade file in SD card if need 
				mImageFile=fileList.get(position);//FileTools.saveUpgradeFile(getActivity(),fileList.get(position).getName());
				stateTextView.setText(mImageFile.getName());
				mDialog.dismiss();
				//calling sdk interface
				LsBleManager.getInstance().upgradeDeviceFirmware(currentDevice.getMacAddress(), mImageFile, deviceUpgradeListener);
				startTime = System.currentTimeMillis();
				cancelUpgradingDialog();
				showUpgradingDialog(null, "upgrade firmware...");
				connectingProgressBar.setVisibility(View.GONE);
			}
		}) .create();
		mDialog.show();
	}
	
	/**
	 * 处理设备功能
	 * @param index
	 */
	private void handleDeviceFunction(int index) 
	{
		String deviceMac=currentDevice.getMacAddress();
		if(TextUtils.isEmpty(deviceMac)){
			Toast.makeText(getActivity(), "undefined.....", Toast.LENGTH_LONG).show();
			return ;
		}
		switch (index) 
		{
			case R.id.fun_device_info:{
				//设备信息
				showDeviceInfo(currentDevice);
			}break;
			case R.id.fun_clear:{
				//清空文本信息
				logTextView.setText("");
			}break;
			case R.id.fun_upgrade:{
				//固件升级
				upgradeDevice();
			}break;
			case R.id.fun_alarm_clock:{
				//闹钟提醒
				DeviceSettiingProfiles.updateAlarmClockWithoutTitle(deviceMac, mSettingListener);
			}break;
			case R.id.fun_sedentry:{
				//久坐提醒
				DeviceSettiingProfiles.updateSedentaryInfo(deviceMac, mSettingListener);
			}break;
			case R.id.fun_event_reminder:{
				//事件提醒
				DeviceSettiingProfiles.updateEventRemind(deviceMac, mSettingListener);
			}break;
			case R.id.fun_night_mode:{
				//夜间模式
				DeviceSettiingProfiles.updateNightDisplayMode(deviceMac, mSettingListener);
			}break;
			case R.id.fun_screen_mode:{
				//屏幕显示
				DeviceSettiingProfiles.updateScreenDisplayMode(deviceMac, mSettingListener);
			}break;
			case R.id.fun_wear_mode:{
				//佩戴方式
				DeviceSettiingProfiles.updateWearingStyles(deviceMac, mSettingListener);
			}break;
			case R.id.fun_hr_detect_mode:{
				//心率检测
				DeviceSettiingProfiles.updateHeartDetectionMode(deviceMac, mSettingListener);
			}break;
			case R.id.fun_weather:{
				//天气提醒
				DeviceSettiingProfiles.updateWeater(deviceMac, mSettingListener);
			}break;
			case R.id.fun_dictance_unit:{
				//距离显示
				DeviceSettiingProfiles.updateDistanceUnit(deviceMac, mSettingListener);
			}break;
			case R.id.fun_time_unit:{
				//时间显示
				DeviceSettiingProfiles.updateTimeFormat(deviceMac, mSettingListener);
			}break;
			case R.id.fun_dialpace_mode:{
				//表盘样式
				DeviceSettiingProfiles.updateDialpace(deviceMac, mSettingListener);
			}break;
			case R.id.fun_auto_discern:{
				//自动识别
				DeviceSettiingProfiles.updateAutoRecognition(deviceMac, mSettingListener);
			}break;
			case R.id.fun_hr_warning:{
				//心率预警
				DeviceSettiingProfiles.updateHeartRateWarning(deviceMac, mSettingListener);
			}break;
			case R.id.fun_behavior_reminder:{
				//行为提醒
//				DeviceSettiingProfiles.updateBehaviorRemind(deviceMac, mSettingListener);
			}break;
			case R.id.fun_target:{
				//目标设置
				DeviceSettiingProfiles.updateEncourage(deviceMac, mSettingListener);
			}break;
			case R.id.fun_read_battery:{
				//读取电量
				boolean isSuccess=DeviceSettiingProfiles.readDeviceBattery(deviceMac, new OnDeviceReadListener() 
				{
					@Override
					public void onDeviceVoltageValue(byte[] srcData, int flag, float vlotage, int percentage) {
						//update device voltage
						updateDevicePower(percentage);
						DialogUtils.showToastMessage(getActivity(),"vlotage="+vlotage+"; percentage"+percentage);
					}
				});
				if(!isSuccess){
					DialogUtils.showToastMessage(getActivity(),getResources().getString(R.string.str_device_not_connected));
				}
			}break;
			case R.id.fun_custom_pages:{
				//自定义页面
				DeviceSettiingProfiles.updateCustomPage(deviceMac, mSettingListener);
			}break;
			case R.id.fun_app_permission:{
				//应用权限设置
				DeviceSettiingProfiles.showAppPermissionSetting(new IDialogActionListener() {
					@Override
					public void onIntentResults(Intent intent) 
					{
						if(intent!=null){
							startActivity(intent);
						}
					}
				});
			}break;
			case R.id.fun_connect:{
				//连接，同步数据
				connectDevice();
			}break;
			case R.id.fun_cancel_connect:{
				//断开连接
				LsBleManager.getInstance().stopDataReceiveService();
				updateDeviceConnectState(DeviceConnectState.DISCONNECTED);
			}break;
			case R.id.fun_message_remind:{
				//更新消息提醒设置
				DeviceSettiingProfiles.updateMessageRemind(deviceMac, mSettingListener);
			}break;
			case R.id.fun_test_message_remind:{
				//模拟应用消息通知，测试消息提醒
				DeviceSettiingProfiles.testMessageRemind();
			}break;
			case R.id.fun_test_incoming_call:{
				//模拟手机来电信息，测试来电功能
				DeviceSettiingProfiles.testIncomingCall(deviceMac);
			}break;
			case R.id.fun_user_info:{
				//更新秤的用户信息
				DeviceSettiingProfiles.updateScaleUserInfo(deviceMac, mSettingListener);
			}break;
			case R.id.fun_bind_device:{
				//测试绑定功能
				cancelUpgradingDialog();
				showUpgradingDialog(null, "binding device...");
				DeviceSettiingProfiles.bindingDevice(currentDevice,new IDialogActionListener()
				{
					@Override
					public void onBindingResults() {
						cancelUpgradingDialog();
					}
					
				});
			}break;
			case R.id.fun_health_score:{
				//更新健康信息
				DeviceSettiingProfiles.updateHealthScoreInfo(deviceMac, mSettingListener);
			}break;
			case R.id.fun_device_positioning:{
				//寻找手环功能，设备定位
				DeviceSettiingProfiles.testDevicePositioning(deviceMac, mSettingListener);
			}break;
			case R.id.fun_photographing:{
				//远程拍摄
				DeviceSettiingProfiles.testRemotePhotographing(deviceMac, mSettingListener);
			}break;
			case R.id.fun_device_restart:{
				//重启设备
				DeviceSettiingProfiles.restartDevice(deviceMac, mSettingListener);
			}break;
			case R.id.fun_device_switch:{
				//更新设备开关状态信息
				DeviceSettiingProfiles.updateDeviceFunctionStatus(deviceMac, mSettingListener);
			}break;
			case R.id.fun_sport_request:{
				//推送运动请求信息
				DeviceSettiingProfiles.sendSportRequest(deviceMac, mSettingListener);
			}break;
			case R.id.fun_mood_reminder:{
				//设置心情记录提示
				DeviceSettiingProfiles.updateMoodbeamReminder(deviceMac, mSettingListener);
			}break;
			case R.id.kchiing_fun_appointment_reminder:{
				//Kchiing 会议提醒或约定事件提醒
				SettingDialogFragment appointmentReminer=new SettingDialogFragment(KAppointmentReminder.class, deviceMac,mSettingListener);
				appointmentReminer.show(getFragmentManager(), "");
			}break;
			case R.id.kchiing_fun_message_reminder:{
				//Kchiing 消息提醒
				KchiingSettingProfiles.addMessageReminder(deviceMac, mSettingListener);
			}break;
			case R.id.kchiing_fun_simple_reminder:{
				//Kchiing 简单提醒 
				SettingDialogFragment simpleReminer=new SettingDialogFragment(KSimpleReminder.class,deviceMac,mSettingListener);
				simpleReminer.show(getFragmentManager(), "");
			}break;
			case R.id.kchiing_fun_wakeup_reminder:{
				//Kchiing 睡眠叫醒
				KchiingSettingProfiles.addWakeupReminder(deviceMac, mSettingListener);
			}break;
			case R.id.fun_quiet_mode:{
				//勿扰模式设置
				DeviceSettiingProfiles.updateQuietModeSetting(deviceMac, mSettingListener);
			}break;
			case R.id.fun_bpm_get_measurement_data:{
				BPMSettingProfiles.getBpmMeasurementData(deviceMac, mSettingListener);
			}break;
			case R.id.fun_bpm_delete_measurement_data:{
				BPMSettingProfiles.deleteBpmMeasurementData(deviceMac, mSettingListener);
			}break;
			case R.id.fun_bpm_set_measurement_time_interval:{
				BPMSettingProfiles.setMeasurementTimeInterval(deviceMac,mSettingListener);
			}break;
			case R.id.fun_bpm_read_measurement_time_interval:{
				BMPCommandPacket readCmd=new BMPCommandPacket(BMPCommand.ReadMeasurementTimeInterval);
				LsBleManager.getInstance().pushDeviceMessage(deviceMac, readCmd, mSettingListener);
			}break;
			case R.id.fun_a2_alarm_clock:{
				String deviceId=null;
				if(currentDevice!=null){
					deviceId=currentDevice.getDeviceId();
					DeviceSettiingProfiles.updateAlarmClock(deviceId,mSettingListener);
				}
				else{
					mSettingListener.onFailure(-1);
				}
				
			}break;

		}
	}
}
