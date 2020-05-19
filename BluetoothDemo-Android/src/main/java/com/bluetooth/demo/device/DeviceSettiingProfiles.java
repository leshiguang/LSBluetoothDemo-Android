package com.bluetooth.demo.device;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bluetooth.demo.R;
import com.bluetooth.demo.ui.SearchFragment;
import com.bluetooth.demo.ui.adapter.SettingAdapter;
import com.bluetooth.demo.ui.dialog.DialogUtils;
import com.bluetooth.demo.ui.dialog.IDialogActionListener;
import com.bluetooth.demo.ui.dialog.OnEditTextViewListener;
import com.bluetooth.demo.utils.ApplicationProfiles;
import com.bluetooth.demo.utils.DeviceDataUtils;
import com.lifesense.ble.LsBleManager;
import com.lifesense.ble.OnDeviceReadListener;
import com.lifesense.ble.OnSettingListener;
import com.lifesense.ble.PairCallback;
import com.lifesense.ble.bean.AppMessage;
import com.lifesense.ble.bean.BehaviorRemindInfo;
import com.lifesense.ble.bean.DeviceFunctionInfo;
import com.lifesense.ble.bean.DeviceRestartInfo;
import com.lifesense.ble.bean.HealthScoreInfo;
import com.lifesense.ble.bean.LsDeviceInfo;
import com.lifesense.ble.bean.MoodRecordReminder;
import com.lifesense.ble.bean.PairedConfirmInfo;
import com.lifesense.ble.bean.PedometerAlarmClock;
import com.lifesense.ble.bean.PedometerAutoRecognition;
import com.lifesense.ble.bean.PedometerDialPeaceInfo;
import com.lifesense.ble.bean.PedometerEncourage;
import com.lifesense.ble.bean.PedometerEventReminder;
import com.lifesense.ble.bean.PedometerHeartRateAlert;
import com.lifesense.ble.bean.PedometerNightMode;
import com.lifesense.ble.bean.PedometerQuietMode;
import com.lifesense.ble.bean.PedometerSedentaryInfo;
import com.lifesense.ble.bean.PedometerWeather;
import com.lifesense.ble.bean.PedometerWeatherFuture;
import com.lifesense.ble.bean.PhoneStateMessage;
import com.lifesense.ble.bean.PhotographingInfo;
import com.lifesense.ble.bean.PositioningInfo;
import com.lifesense.ble.bean.SportNotify;
import com.lifesense.ble.bean.SportNotifyConfirm;
import com.lifesense.ble.bean.SportRequestInfo;
import com.lifesense.ble.bean.WeightAppendData;
import com.lifesense.ble.bean.WeightUserInfo;
import com.lifesense.ble.bean.constant.AutoRecognitionType;
import com.lifesense.ble.bean.constant.DeviceConfigInfoType;
import com.lifesense.ble.bean.constant.DeviceFunctionType;
import com.lifesense.ble.bean.constant.DeviceTypeConstants;
import com.lifesense.ble.bean.constant.DialPeaceStyle;
import com.lifesense.ble.bean.constant.ErrorCode;
import com.lifesense.ble.bean.constant.HeartRateDetectionMode;
import com.lifesense.ble.bean.constant.HourSystem;
import com.lifesense.ble.bean.constant.LengthUnit;
import com.lifesense.ble.bean.constant.ManagerStatus;
import com.lifesense.ble.bean.constant.MessageType;
import com.lifesense.ble.bean.constant.OperationCommand;
import com.lifesense.ble.bean.constant.PairedConfirmState;
import com.lifesense.ble.bean.constant.PairedResultsCode;
import com.lifesense.ble.bean.constant.PairingInputRandomStatus;
import com.lifesense.ble.bean.constant.PedometerEncourageType;
import com.lifesense.ble.bean.constant.PedometerPage;
import com.lifesense.ble.bean.constant.PedometerScreenMode;
import com.lifesense.ble.bean.constant.PedometerSportsType;
import com.lifesense.ble.bean.constant.PedometerWearingStyles;
import com.lifesense.ble.bean.constant.PedometerWeatherState;
import com.lifesense.ble.bean.constant.PhoneGpsStatus;
import com.lifesense.ble.bean.constant.PhoneState;
import com.lifesense.ble.bean.constant.ProtocolType;
import com.lifesense.ble.bean.constant.SexType;
import com.lifesense.ble.bean.constant.UnitType;
import com.lifesense.ble.bean.constant.VibrationMode;
import com.lifesense.ble.bean.constant.WeekDay;
import com.lifesense.ble.message.NotificationAccessService;

@SuppressLint({ "DefaultLocale", "NewApi" }) 
public class DeviceSettiingProfiles  {

	private static final String TAG="LS-BLE";
	private static final String FORMAT_STRING_SIMPLE_DATE = "yyyy-MM-dd HH:mm:ss";
	public static final  String SETTING_PROFIES_ACTION="com.bluetooth.demo.setting.profiles.ACTION";

	private static AlertDialog dialog;
	private static Context baseContext;
	private static int lastGroupIndex=0;
	private static EditText currentEditView;


	private static final String[] APP_PERMISSIONS=new String[]{
		"GPS",
		"Notification",
		"Accessibility"
	};

	protected static final String[] SWITCH_STATUS=new String[]{
		"Enable",
		"Disable"
	};


	private static final String[] USER_GENDER=new String[]{
		"Female",
		"Male"
	};

	private static final String[] PHOTOGRAPHING_STATE=new String[]{
		"Exits Photographing",
		"Enter Photographing"
	};
	
	private static final String[] NAME_SCAN_FILTER=new String[]{
		"All",
		"My Mambo",
		"Mambo",
		"Mambo 2",
		"Mambo 3",
		"ziva",
		"ziva plus",
		"Mambo HR",
		"Mambo Watch",
		"LS_noi",
		"LS band",
		"LSBand",
	};
	
	private static final String[] RSSI_SCAN_FILTER=new String[]{
		"-20",
		"-30",
		"-40",
		"-50",
		"-60",
		"-70",
		"-80",
		"-90",
		"-100",
	};
	
	private static final String[] DEVICE_FUNS=new String[]{
		"Disable Screen Lights Up",
		"Enable Screen Lights Up"

	};
	
	private static IDialogActionListener mDialogActionListener;

	/**
	 * pairing callback
	 */
	private static PairCallback mPairCallback=new PairCallback() 
	{
		@Override
		public void onDeviceOperationCommandUpdate(final String macAddress,OperationCommand command, Object obj) 
		{
			if(OperationCommand.CMD_RANDOM_NUMBER == command){

				showRandomNumberInputView(macAddress);
			}
			else if(OperationCommand.CMD_PAIRED_CONFIRM == command)
			{
				PairedConfirmInfo pairedConfirmInfo = new PairedConfirmInfo(PairedConfirmState.PAIRING_SUCCESS );
				pairedConfirmInfo.setUserNumber(0);
				LsBleManager.getInstance().inputOperationCommand(macAddress, OperationCommand.CMD_PAIRED_CONFIRM, pairedConfirmInfo);	
			}
		}

		@Override
		public void onPairResults(LsDeviceInfo lsDevice, int status) 
		{
			if(mDialogActionListener!=null){
				mDialogActionListener.onBindingResults();
				mDialogActionListener=null;
			}
			//绑定成功
			DialogUtils.showToastMessage(baseContext, getBindResults(status));
		}

	};


	public static void initActivityContext(Activity activity){
		baseContext=activity;
	}

	/**
	 * 获取字符串资源内容
	 * @param id
	 * @return
	 */
	public static String getResourceString(int id){
		if(baseContext==null){
			return "null";
		}
		return baseContext.getResources().getString(id);
	}

	/**
	 * set product user info on data syncing mode
	 * @param device
	 */
	public static void setProductUserInfoOnSyncingMode(LsDeviceInfo lsDevice) 
	{
		if(lsDevice==null)
		{
			return ;
		}
		/**
		 * in some old products, such as A2, A3, 
		 * you can change the device's settings info before syncing  
		 */
		if(DeviceTypeConstants.FAT_SCALE.equalsIgnoreCase(lsDevice.getDeviceType())
				||DeviceTypeConstants.WEIGHT_SCALE.equalsIgnoreCase(lsDevice.getDeviceType()))
		{
			/**
			 * optional step,set product user info in syncing mode
			 */
			WeightUserInfo weightUserInfo=new WeightUserInfo();
			weightUserInfo.setAge(30);					//user age
			weightUserInfo.setHeight((float) 1.88); 	//unit of measurement is m
			weightUserInfo.setGoalWeight(78);			//unit of measurement is kg
			weightUserInfo.setUnit(UnitType.UNIT_KG);	//measurement unit
			weightUserInfo.setSex(SexType.FEMALE);		//user gender
			weightUserInfo.setAthlete(true);			//it is an athlete 		   
			weightUserInfo.setAthleteActivityLevel(3);	//athlete level
			weightUserInfo.setProductUserNumber(lsDevice.getDeviceUserNumber());
			weightUserInfo.setMacAddress(lsDevice.getMacAddress());
			weightUserInfo.setDeviceId(lsDevice.getDeviceId()); //set target device's id
			//calling interface
			LsBleManager.getInstance().setProductUserInfo(weightUserInfo);
		}
	}


	/**
	 * 根据电阻值，计算脂肪数据
	 * @param resistance
	 */
	public static void calculateBodyCompositionData(double resistance)
	{
		double height=1.75;      // unit M
		double weight=67.8;     //  unit kg
		int age=34;
		SexType gender=SexType.MALE;
		boolean isAthlete=true;

		WeightAppendData bodyComposition=LsBleManager.getInstance().parseAdiposeData(gender, weight, height, age, resistance, isAthlete);
		Log.e("LS-BLE", "body composition >> "+bodyComposition.toString());
		LsBleManager.getInstance().setLogMessage("bodycomposition >> "+bodyComposition.toString());
	}

	/**
	 * 判断该设备是否为Kchiing 定制设备
	 * @param lsDevice
	 * @return
	 */
	public static boolean isKchiingDevice(LsDeviceInfo lsDevice){
		if(lsDevice==null || TextUtils.isEmpty(lsDevice.getModelNumber())){
			Log.e("LS-BLE", "is kchiing device false>>"+lsDevice.toString());
			return false;
		}
		if("422CG".equalsIgnoreCase(lsDevice.getModelNumber())){
			Log.e("LS-BLE", "is kchiing device true>>"+lsDevice.toString());

			return true;
		}
		else{
			Log.e("LS-BLE", "is kchiing device false 1>>"+lsDevice.toString());

			return false;
		}
	}
	
	
	/**
	 * 判断设备是否使用了新协议
	 * @return
	 */
	public static boolean isNewProtocolDevice(LsDeviceInfo lsDevice){
		if(lsDevice==null){
			return false;
		}
		String devicePro=lsDevice.getProtocolType();
		if(TextUtils.isEmpty(devicePro)){
			return false;
		}
		if(devicePro.equalsIgnoreCase(ProtocolType.A2.toString())){
			return false;
		}
		if(devicePro.equalsIgnoreCase(ProtocolType.A3.toString())){
			return false;
		}
		if(devicePro.equalsIgnoreCase(ProtocolType.A3_1.toString())){
			return false;
		}
		if(devicePro.equalsIgnoreCase(ProtocolType.A3_3.toString())){
			return false;
		}
		String deviceType=lsDevice.getDeviceType();
		if(deviceType==null ||!deviceType.equalsIgnoreCase(DeviceTypeConstants.PEDOMETER)){
			return false;
		}
		else{
			return true;
		}
	}

	public static List<WeekDay> getWeekDays()
	{
		List<WeekDay> weekDays=new ArrayList<WeekDay>();//重复周期
		weekDays.add(WeekDay.FRIDAY);			//
		weekDays.add(WeekDay.MONDAY);			//
		weekDays.add(WeekDay.SATURDAY);			//
		weekDays.add(WeekDay.SUNDAY);			//
		weekDays.add(WeekDay.THURSDAY);			//
		weekDays.add(WeekDay.TUESDAY);			//
		weekDays.add(WeekDay.WEDNESDAY);		//
		return weekDays;
	}

	/**
	 * 展示扫描过滤条件设置
	 */
	public static void showScanFilterSetting()
	{

		String title=baseContext.getResources().getString(R.string.title_scan_filter);
		final String labelName=getResourceString(R.string.label_filter_device_name);
		final String labelRssi=getResourceString(R.string.label_filter_device_rssi);
		
		//init setting items
		List<SettingItem> menuItems=new ArrayList<SettingItem>();
		SettingItem nameItem=new SettingItem();
		nameItem.setOptions(SettingOptions.SingleChoice);
		nameItem.setTitle(labelName);
		nameItem.setChoiceItems(Arrays.asList(NAME_SCAN_FILTER));
		//rssi filter
		SettingItem rssiItem=new SettingItem();
		rssiItem.setOptions(SettingOptions.SingleChoice);
		rssiItem.setTitle(labelRssi);
		rssiItem.setChoiceItems(Arrays.asList(RSSI_SCAN_FILTER));

		//add item to list
		menuItems.add(nameItem);
		menuItems.add(rssiItem);
		
		//show setting dialog
		showSettingDialog(title, menuItems, new IDialogActionListener() {
			@Override
			public void onSettingItems(List<SettingItem> items) 
			{
				for(SettingItem item:items){
					if(item.getTitle().equalsIgnoreCase(labelName) && !TextUtils.isEmpty(item.getTextViewValue())){
						SearchFragment.setScanFilter(item.getTextViewValue());
					}
					if(item.getTitle().equalsIgnoreCase(labelRssi) && !TextUtils.isEmpty(item.getTextViewValue())){
						
						SearchFragment.setScanFilter(item.getTextViewValue());
					}
				}				
			}
		});
	}

	/**
	 * 读取设备的电量电压或电量百分比
	 * @param deviceMac
	 * @param listener
	 * @return
	 */
	public static boolean readDeviceBattery(String deviceMac,OnDeviceReadListener listener){
		if(!isConnected(deviceMac)){
			return false;
		}
		LsBleManager.getInstance().readDeviceVoltage(deviceMac, listener);
		return true;
	}

	/**
	 * 
	 */
	public static void showAppPermissionSetting(final IDialogActionListener listener)
	{
		String title=baseContext.getResources().getString(R.string.title_app_permission);
		showSingleChoiceDialog(title, APP_PERMISSIONS, new IDialogActionListener() 
		{
			@Override
			public void onSingleChoiceItemValue(int index) 
			{
				if(index==0){
					Intent intent=new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
					listener.onIntentResults(intent);
				}
				else if(index==1){
					Intent intent=new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
					listener.onIntentResults(intent);
				}
				else if(index==2){
					Intent intent=new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
					listener.onIntentResults(intent);
				}
				else{
					listener.onIntentResults(null);
				}
			}
		});

	}

	public static void bindingDevice(final LsDeviceInfo lsDevice, IDialogActionListener listener) 
	{
		mDialogActionListener=listener;
		ManagerStatus status=LsBleManager.getInstance().getLsBleManagerStatus() ;
		if(status == ManagerStatus.DATA_RECEIVE)
		{
			LsBleManager.getInstance().stopDataReceiveService();
		}
		else if(status == ManagerStatus.DEVICE_SEARCH){
			LsBleManager.getInstance().stopSearch();
		}
		else if(status == ManagerStatus.DEVICE_PAIR){
			LsBleManager.getInstance().cancelDevicePairing(lsDevice);
		}
		else if(status== ManagerStatus.UPGRADE_FIRMWARE_VERSION){
			LsBleManager.getInstance().cancelAllUpgradeProcess();
		}
		//delay 5 seconds to binding if need or test
		Handler handler =new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {		
				LsBleManager.getInstance().pairingWithDevice(lsDevice, mPairCallback);
			}
		}, 3*1000L);

	}

	/**
	 * 更新A2手环的闹钟设置
	 * @param deviceMac
	 * @param listener
	 * @return
	 */
	public static boolean updateAlarmClock(final String deviceId,final OnSettingListener listener){
		String title=baseContext.getResources().getString(R.string.title_alarm_clock);
		showTimeChoiceDialog(title, new IDialogActionListener() 
		{
			@Override
			public void onTimeChoiceValue(int hour,int minute) 
			{
				String time=hour+":"+minute;
				PedometerAlarmClock alarmClock=new PedometerAlarmClock();
				alarmClock.setDay2(PedometerAlarmClock.WORKDAY);
				alarmClock.setHour2(hour);
				alarmClock.setMinute2(minute);
				alarmClock.setSwitch2(2);
				alarmClock.setDeviceId(deviceId);
				LsBleManager.getInstance().setPedometerAlarmClock(alarmClock);
				listener.onSuccess(deviceId);
			}
		});
		return true;
	}
	
	
	/**
	 * 更新手环的闹钟提醒，旧固件手环的闹钟提醒
	 * @param deviceMac
	 * @param listener
	 */
	public static boolean updateAlarmClockWithoutTitle(final String deviceMac,final OnSettingListener listener){
		if(!isConnected(deviceMac)){
			return false;
		}
		String title=baseContext.getResources().getString(R.string.title_alarm_clock);
		showTimeChoiceDialog(title, new IDialogActionListener() 
		{
			@Override
			public void onTimeChoiceValue(int hour,int minute) 
			{
				String time=hour+":"+minute;

				/**
				 * 闹钟1 21:30
				 */
				PedometerAlarmClock alarmClock1=new PedometerAlarmClock();
				alarmClock1.setTime(time);				//闹钟时间21:30
				alarmClock1.setEnableSwitch(true);		//闹钟开关
				alarmClock1.setRepeatDay(getWeekDays());	//闹钟重复星期
				alarmClock1.setVibrationDuration(3);	    //振动时间，单位秒
				alarmClock1.setVibrationIntensity1(3);	//振动等级1
				alarmClock1.setVibrationIntensity2(4);	//振动等级2
				alarmClock1.setVibrationMode(VibrationMode.CONTINUOUS_VIBRATION); //振动方式


				/**
				 * 闹钟2 for test 21:45
				 */
				PedometerAlarmClock alarmClock2=new PedometerAlarmClock();
				String time2=hour+":"+minute+2;
				alarmClock2.setTime(time2);			//闹钟时间21:45
				alarmClock2.setEnableSwitch(true);		//闹钟开关
				alarmClock2.setRepeatDay(getWeekDays());	//闹钟重复星期
				alarmClock2.setVibrationDuration(10);	    //振动时间，单位秒
				alarmClock2.setVibrationIntensity1(5);	//振动等级1
				alarmClock2.setVibrationIntensity2(6);	//振动等级2
				alarmClock2.setVibrationMode(VibrationMode.CONTINUOUS_VIBRATION); //振动方式

				//add list
				List<PedometerAlarmClock> alarmClocks=new ArrayList<PedometerAlarmClock>();
				alarmClocks.add(alarmClock1);
				alarmClocks.add(alarmClock2);

				//calling interface
				LsBleManager.getInstance().updatePedometerAlarmClock(deviceMac, true, alarmClocks, listener);
			}
		});
		return true;
	}



	/**
	 * 更新手环的闹钟提醒，带标签内容的闹钟提醒
	 */
	public static boolean updateEventRemind(final String deviceMac,final OnSettingListener listener)
	{
		if(!isConnected(deviceMac)){
			return false;
		}
		String title=getResourceString(R.string.title_event_clock);
		final String labelTime=getResourceString(R.string.label_clock_time);
		final String labelIndex=getResourceString(R.string.label_clock_index);

		//init setting items
		List<SettingItem> items=new ArrayList<SettingItem>();
		SettingItem startTimeItem=new SettingItem();
		startTimeItem.setOptions(SettingOptions.TimePicker);
		startTimeItem.setTitle(labelTime);

		SettingItem indexItem=new SettingItem();
		indexItem.setOptions(SettingOptions.NumberPicker);
		indexItem.setTitle(labelIndex);
		indexItem.setMinValue(1);
		indexItem.setMaxValue(5);

		SettingItem titleItem=new SettingItem(SettingOptions.Text);
		titleItem.setTitle(getResourceString(R.string.label_clock));
		titleItem.setInputType(InputType.TYPE_CLASS_TEXT);

		//add item to list
		items.add(indexItem);
		items.add(startTimeItem);
		items.add(titleItem);

		//show setting dialog
		showSettingDialog(title, items, new IDialogActionListener() {
			@Override
			public void onSettingItems(List<SettingItem> items) 
			{
				if(!checkSettingItems(items, listener)){
					return ;
				}
				int index=1;
				int clockHour=0;
				int clockMinute=0;
				String clockTitle="test";
				for(SettingItem item:items){
					logMessage("my setting item >"+item.toString());
					if(item.getTitle().equalsIgnoreCase(labelTime)){
						clockHour=DeviceDataUtils.getHourFromTime(item.getTime());
						clockMinute=DeviceDataUtils.getMinuteFromTime(item.getTime());
					}
					if(item.getTitle().equalsIgnoreCase(labelIndex)){
						index=item.getIndex();
					}
					if(item.getTitle().equalsIgnoreCase(getResourceString(R.string.label_clock))){
						clockTitle=item.getTextViewValue();
					}
				}
				//call interface
				logMessage("event clock  >> "+index+"; time="+clockHour+":"+clockMinute+"; title="+clockTitle);

				PedometerEventReminder eventRemind=new PedometerEventReminder();
				eventRemind.setIndex(index); 					//闹钟索引1~5，允许设置5个闹钟
				eventRemind.setEnable(true); 					//闹钟开关
				eventRemind.setReminderContent(clockTitle); 	//闹钟内容
				eventRemind.setTime(clockHour, clockMinute);	//闹钟时间20:15

				//default value
				eventRemind.setRepeatDay(getWeekDays());		//获取重复星期
				eventRemind.setVibrationMode(VibrationMode.CONTINUOUS_VIBRATION);//振动方式
				eventRemind.setVibrationDuration(10);   //震动时间
				eventRemind.setVibrationIntensity1(8);	//振动等级1
				eventRemind.setVibrationIntensity2(6);  //振动等级2
				//调用设置接口
				LsBleManager.getInstance().updatePedometerEventReminder(deviceMac, eventRemind, listener);
			}
		});
		return true;
	}


	/**
	 * 更新手环的久坐提醒设置信息
	 * @param deviceMac
	 * @param listener
	 */
	public static boolean updateSedentaryInfo(final String deviceMac,final OnSettingListener listener)
	{
		if(!isConnected(deviceMac)){
			return false;
		}
		String title=getResourceString(R.string.title_sedentary);		
		final String labelStart=getResourceString(R.string.label_start_time);
		final String labelEnd=getResourceString(R.string.label_end_time);
		final String labelSedentaryTime=getResourceString(R.string.label_sedentary_time);

		//init setting items
		List<SettingItem> items=new ArrayList<SettingItem>();
		SettingItem startTimeItem=new SettingItem();
		startTimeItem.setOptions(SettingOptions.TimePicker);
		startTimeItem.setTitle(labelStart);

		SettingItem endTimeItem=new SettingItem();
		endTimeItem.setOptions(SettingOptions.TimePicker);
		endTimeItem.setTitle(labelEnd);

		SettingItem sedentaryTimeItem=new SettingItem();
		sedentaryTimeItem.setOptions(SettingOptions.NumberPicker);
		sedentaryTimeItem.setTitle(labelSedentaryTime);
		sedentaryTimeItem.setMinValue(1);
		sedentaryTimeItem.setMaxValue(60);


		//add item to list
		items.add(startTimeItem);
		items.add(endTimeItem);
		items.add(sedentaryTimeItem);

		//show setting dialog
		showSettingDialog(title, items, new IDialogActionListener() {
			@Override
			public void onSettingItems(List<SettingItem> items) 
			{
				if(!checkSettingItems(items, listener)){
					return ;
				}
				String startTime=null;
				String endTime=null;
				int sedentaryTime=0;
				for(SettingItem item:items){
					if(item.getTitle().equalsIgnoreCase(labelStart)){
						startTime=item.getTime();
					}
					if(item.getTitle().equalsIgnoreCase(labelEnd)){
						endTime=item.getTime();
					}
					if(item.getTitle().equalsIgnoreCase(labelSedentaryTime)){
						sedentaryTime=item.getIndex();
					}
				}
				//call interface
				logMessage("sedentary info >> "+sedentaryTime+"; startTime="+startTime+"; endTime="+endTime);
				/**
				 * 设置18:20~20:50的久坐提醒
				 */
				PedometerSedentaryInfo sedentary=new PedometerSedentaryInfo();
				sedentary.setEnableSedentaryReminder(true);		//开关
				sedentary.setEnableSedentaryTime(sedentaryTime);//设置久坐时间，即在2分钟内，手环步数据无更新或处于静止状态时
				sedentary.setReminderStartTime(startTime);	//开始时间
				sedentary.setReminderEndTime(endTime);  	//结束时间
				sedentary.setRepeatDay(getWeekDays());	 	//重复星期

				sedentary.setVibrationMode(VibrationMode.CONTINUOUS_VIBRATION);//振动方式
				sedentary.setVibrationIntensity1(7);    //振动等级1
				sedentary.setVibrationIntensity2(5);	//振动等级2
				sedentary.setVibrationDuration(10);		//振动时间

				//add list
				List<PedometerSedentaryInfo> sedentarys=new ArrayList<PedometerSedentaryInfo>();
				sedentarys.add(sedentary);

				//calling interface
				LsBleManager.getInstance().updatePedometerSedentary(deviceMac, true, sedentarys, listener);
			}
		});
		return true;
	}




	/**
	 * 设置手环的夜间显示模式
	 * @param deviceMac
	 * @param listener
	 */
	public static void updateNightDisplayMode(final String deviceMac,final OnSettingListener listener)
	{
		final String errorMsg=getResourceString(R.string.title_error_parameter);
		final String title=baseContext.getResources().getString(R.string.title_night_mode);
		final String labelStart=baseContext.getResources().getString(R.string.label_start_time);
		final String labelEnd=baseContext.getResources().getString(R.string.label_end_time);
		//init setting items
		List<SettingItem> items=new ArrayList<SettingItem>();
		SettingItem startTimeItem=new SettingItem();
		startTimeItem.setOptions(SettingOptions.TimePicker);
		startTimeItem.setTitle(labelStart);
		SettingItem endTimeItem=new SettingItem();
		endTimeItem.setOptions(SettingOptions.TimePicker);
		endTimeItem.setTitle(labelEnd);

		SettingItem enableItem=new SettingItem(SettingOptions.SingleChoice);
		enableItem.setTitle(getResourceString(R.string.label_switch));
		enableItem.setChoiceItems(Arrays.asList(SWITCH_STATUS));

		//add item to list
		items.add(enableItem);
		items.add(startTimeItem);
		items.add(endTimeItem);
		//show setting dialog
		showSettingDialog(title, items, new IDialogActionListener() 
		{
			@Override
			public void onSettingItems(List<SettingItem> items) 
			{
				if(!checkSettingItems(items, listener)){
					return ;
				}
				String startTime=null;
				String endTime=null;
				boolean isEnable=false;
				for(SettingItem item:items){
					if(item.getTitle().equalsIgnoreCase(labelStart)){
						startTime=item.getTime();
					}
					if(item.getTitle().equalsIgnoreCase(labelEnd)){
						endTime=item.getTime();
					}
					if(item.getTitle().equalsIgnoreCase(getResourceString(R.string.label_switch))){
						if(item.getIndex() == 0){
							isEnable=true;
						}
					}
				}
				logMessage("night display mode,startTime="+startTime+"; endTime="+endTime+"; isEnable="+isEnable);
				/**
				 * 设置21:00 ~~ 23:00 为夜间显示模式
				 */
				PedometerNightMode nightMode=new PedometerNightMode(startTime, endTime);
				//calling interface
				LsBleManager.getInstance().updatePedometerNightMode(deviceMac, isEnable, nightMode, listener);
			}
		});
	}

	/**
	 * 设置手环的心率检测方式
	 * @param deviceMac
	 * @param listener
	 */
	public static void updateHeartDetectionMode(final String deviceMac,final OnSettingListener listener)
	{
		final String[] items=new String[HeartRateDetectionMode.values().length];
		int index=0;
		for(HeartRateDetectionMode mode:HeartRateDetectionMode.values()){
			items[index]=mode.toString().toLowerCase();
			index++;
		}
		String title=baseContext.getResources().getString(R.string.title_hr_detect_mode);
		showSingleChoiceDialog(title, items, new IDialogActionListener() 
		{

			@Override
			public void onSingleChoiceItemValue(int index) 
			{
				//to mode
				HeartRateDetectionMode selectMode=HeartRateDetectionMode.values()[index];
				//calling interface
				LsBleManager.getInstance().updatePedometerHeartDetectionMode(deviceMac, selectMode, listener);
			}
		});
	}


	/**
	 * 设置手环的屏幕显示方式
	 * @param deviceMac
	 * @param listener
	 */
	public static void updateScreenDisplayMode(final String deviceMac,final OnSettingListener listener)
	{
		final String[] items=new String[PedometerScreenMode.values().length];
		int index=0;
		for(PedometerScreenMode mode:PedometerScreenMode.values()){
			items[index]=mode.toString().toLowerCase();
			index++;
		}
		String title=baseContext.getResources().getString(R.string.title_screen_mode);
		showSingleChoiceDialog(title, items, new IDialogActionListener() 
		{
			@Override
			public void onSingleChoiceItemValue(int index) 
			{
				//to mode
				PedometerScreenMode selectMode=PedometerScreenMode.values()[index];
				//calling interface
				LsBleManager.getInstance().updatePedometerScreenMode(deviceMac, selectMode, listener);
			}
		});
	}


	/**
	 * 设置手环的佩戴显示方式
	 * @param deviceMac
	 * @param listener
	 */
	public static void updateWearingStyles(final String deviceMac,final OnSettingListener listener)
	{
		final String[] items=new String[PedometerWearingStyles.values().length];
		int index=0;
		for(PedometerWearingStyles mode:PedometerWearingStyles.values()){
			items[index]=mode.toString().toLowerCase();
			index++;
		}
		String title=baseContext.getResources().getString(R.string.title_wearing_mode);
		showSingleChoiceDialog(title, items, new IDialogActionListener() 
		{
			@Override
			public void onSingleChoiceItemValue(int index) 
			{
				//to mode
				PedometerWearingStyles selectMode=PedometerWearingStyles.values()[index];
				//calling interface
				LsBleManager.getInstance().updatePedometerWearingStyles(deviceMac, selectMode, listener);
			}
		});
	}

	/**
	 * 设置手环的距离显示方式
	 * @param deviceMac
	 * @param listener
	 */
	public static void updateDistanceUnit(final String deviceMac,final OnSettingListener listener)
	{
		final String[] items=new String[LengthUnit.values().length];
		int index=0;
		for(LengthUnit mode:LengthUnit.values()){
			items[index]=mode.toString().toLowerCase();
			index++;
		}
		String title=baseContext.getResources().getString(R.string.title_distance_unit);
		showSingleChoiceDialog(title, items, new IDialogActionListener() 
		{
			@Override
			public void onSingleChoiceItemValue(int index) 
			{
				//to mode
				LengthUnit selectMode=LengthUnit.values()[index];
				//calling interface
				LsBleManager.getInstance().updateDeviceDistanceUnit(deviceMac, selectMode, listener);
			}
		});
	}

	/**
	 * 设置手环的时间显示方式
	 * @param deviceMac
	 * @param listener
	 */
	public static void updateTimeFormat(final String deviceMac,final OnSettingListener listener)
	{
		final String[] items=new String[HourSystem.values().length];
		int index=0;
		for(HourSystem mode:HourSystem.values()){
			items[index]=mode.toString().toLowerCase();
			index++;
		}
		String title=baseContext.getResources().getString(R.string.title_time_format);
		showSingleChoiceDialog(title, items, new IDialogActionListener() 
		{
			@Override
			public void onSingleChoiceItemValue(int index) 
			{
				//to mode
				HourSystem selectMode=HourSystem.values()[index];
				//calling interface
				LsBleManager.getInstance().updateDeviceTimeFormat(deviceMac, selectMode, listener);
			}
		});
	}

	/**
	 * 设置手环的表盘样式
	 * @param deviceMac
	 * @param listener
	 */
	public static void updateDialpace(final String deviceMac,final OnSettingListener listener)
	{
		final String[] items=new String[DialPeaceStyle.values().length];
		int index=0;
		for(DialPeaceStyle mode:DialPeaceStyle.values()){
			items[index]=mode.toString().toLowerCase();
			index++;
		}
		String title=baseContext.getResources().getString(R.string.title_dialpeace);
		showSingleChoiceDialog(title, items, new IDialogActionListener() 
		{
			@Override
			public void onSingleChoiceItemValue(int index) 
			{
				//to mode
				DialPeaceStyle selectMode=DialPeaceStyle.values()[index];
				PedometerDialPeaceInfo dialPeaceInfo=new PedometerDialPeaceInfo(selectMode);
				//calling interface
				LsBleManager.getInstance().updatePedometerDialPeace(deviceMac, dialPeaceInfo, listener);
			}
		});
	}

	/**
	 * 设置手环运动模式的自动识别功能
	 * @param deviceMac
	 * @param listener
	 */
	public static void updateAutoRecognition(final String deviceMac,final OnSettingListener listener)
	{
		final String[] items=new String[AutoRecognitionType.values().length];
		int index=0;
		for(AutoRecognitionType mode:AutoRecognitionType.values()){
			items[index]=mode.toString().toLowerCase();
			index++;
		}
		String title=baseContext.getResources().getString(R.string.title_auto_discern);
		showSingleChoiceDialog(title, items, new IDialogActionListener() 
		{
			@Override
			public void onSingleChoiceItemValue(int index) 
			{
				//to mode
				AutoRecognitionType selectMode=AutoRecognitionType.values()[index];
				PedometerAutoRecognition autoRecognition=new PedometerAutoRecognition(true, selectMode);
				List<PedometerAutoRecognition> autoRecognitions=new ArrayList<PedometerAutoRecognition>();
				autoRecognitions.add(autoRecognition);
				//calling interface
				LsBleManager.getInstance().updatePedometerAutoRecognition(deviceMac, autoRecognitions, listener);
			}
		});
	}

	/**
	 * 设置手环目标
	 * @param deviceMac
	 * @param listener
	 */
	public static void updateEncourage(final String deviceMac,final OnSettingListener listener)
	{
		final String[] items=new String[PedometerEncourageType.values().length];
		int index=0;
		for(PedometerEncourageType mode:PedometerEncourageType.values()){
			items[index]=mode.toString().toLowerCase();
			index++;
		}
		String title=baseContext.getResources().getString(R.string.title_target);

		//init setting items
		List<SettingItem> settingItems=new ArrayList<SettingItem>();
		SettingItem typeItem=new SettingItem(SettingOptions.SingleChoice);
		typeItem.setTitle(getResourceString(R.string.label_target_type));
		typeItem.setChoiceItems(Arrays.asList(items));

		SettingItem targetItem=new SettingItem(SettingOptions.Text);
		targetItem.setTitle(getResourceString(R.string.label_target_value));
		targetItem.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);

		settingItems.add(typeItem);
		settingItems.add(targetItem);

		showSettingDialog(title, settingItems, new IDialogActionListener() {
			@Override
			public void onSettingItems(List<SettingItem> items) 
			{
				if(!checkSettingItems(items, listener)){
					return ;
				}
				int index=0;
				float value=0;
				//
				for (SettingItem item : items) {
					if(item.getTitle().equalsIgnoreCase(getResourceString(R.string.label_target_type))){
						index=item.getIndex();
					}
					else if(item.getTitle().equalsIgnoreCase(getResourceString(R.string.label_target_value))){
						value=(float)DeviceDataUtils.toDouble(item.getTextViewValue(),1);
					}
				}
				logMessage("encourage info >> "+index+"; value="+value);
				//to mode
				PedometerEncourageType selectMode=PedometerEncourageType.values()[index];
				PedometerEncourage encourage=new PedometerEncourage(selectMode, true, value);
				//calling interface
				LsBleManager.getInstance().updatePedometerEncourage(deviceMac, encourage, listener);
			}
		});
	}

	/**
	 * 设置手环自定义页面
	 * @param deviceMac
	 * @param listener
	 */
	public static void updateCustomPage(final String deviceMac,final OnSettingListener listener)
	{
		final String[] items=new String[PedometerPage.values().length];
		int index=0;
		for(PedometerPage mode:PedometerPage.values()){
			items[index]=mode.toString().toLowerCase();
			index++;
		}
		String title=baseContext.getResources().getString(R.string.title_custom_pages);
		showMultiChoiceDialog(title, items, new IDialogActionListener(){
			@Override
			public void onMultiChoiceItemValue(List<Integer> items) 
			{
				if(items==null){
					listener.onFailure(0);
					return ;
				}
				List<PedometerPage> pages=new ArrayList<PedometerPage>();
				for(Integer index:items){
					pages.add(PedometerPage.values()[index]);
					Log.e(TAG, "my page >> "+PedometerPage.values()[index]);
				}
				LsBleManager.getInstance().updatePedometerPageSequence(deviceMac, pages, listener);
			}
		});
	}
//
//	/**
//	 * 设置行为提醒
//	 * @param deviceMac
//	 * @param listener
//	 */
//	public static void updateBehaviorRemind(final String deviceMac,final OnSettingListener listener)
//	{
//		final String[] items=new String[BehaviorRemindType.values().length];
//		int index=0;
//		for(BehaviorRemindType mode:BehaviorRemindType.values()){
//			items[index]=mode.toString().toLowerCase();
//			index++;
//		}
//		String title=baseContext.getResources().getString(R.string.title_behavior_remind);
//		final String labelType=getResourceString(R.string.label_reminder_type);
//		final String labelStatus=getResourceString(R.string.label_switch);
//		final String labelStart=getResourceString(R.string.label_start_time);
//		final String labelEnd=getResourceString(R.string.label_end_time);
//		final String labelInterval=getResourceString(R.string.label_reminder_interval);
//
//		//init setting items
//		List<SettingItem> menuItems=new ArrayList<SettingItem>();
//		SettingItem typeItem=new SettingItem();
//		typeItem.setOptions(SettingOptions.SingleChoice);
//		typeItem.setTitle(labelType);
//		typeItem.setChoiceItems(Arrays.asList(items));
//
//		SettingItem statusItem=new SettingItem();
//		statusItem.setOptions(SettingOptions.SingleChoice);
//		statusItem.setTitle(labelStatus);
//		statusItem.setChoiceItems(Arrays.asList(SWITCH_STATUS));
//
//
//		SettingItem startTimeItem=new SettingItem();
//		startTimeItem.setOptions(SettingOptions.TimePicker);
//		startTimeItem.setTitle(labelStart);
//
//		SettingItem endTimeItem=new SettingItem();
//		endTimeItem.setOptions(SettingOptions.TimePicker);
//		endTimeItem.setTitle(labelEnd);
//
//		SettingItem sedentaryTimeItem=new SettingItem();
//		sedentaryTimeItem.setOptions(SettingOptions.NumberPicker);
//		sedentaryTimeItem.setTitle(labelInterval);
//		sedentaryTimeItem.setMinValue(1);
//		sedentaryTimeItem.setMaxValue(60);
//
//
//		//add item to list
//		menuItems.add(statusItem);
//		menuItems.add(startTimeItem);
//		menuItems.add(endTimeItem);
//		menuItems.add(sedentaryTimeItem);
//
//		//show setting dialog
//		showSettingDialog(title, menuItems, new IDialogActionListener() {
//			@Override
//			public void onSettingItems(List<SettingItem> items)
//			{
//				if(!checkSettingItems(items, listener)){
//					return ;
//				}
//				String startTime=null;
//				String endTime=null;
//				int intervalTime=0;
//				boolean status=false;
//				int type=0;
//				BehaviorRemindType selectMode=BehaviorRemindType.DRINKING_WATER;
//				for(SettingItem item:items){
//					if(item.getTitle().equalsIgnoreCase(labelStart)){
//						startTime=item.getTime();
//					}
//					if(item.getTitle().equalsIgnoreCase(labelEnd)){
//						endTime=item.getTime();
//					}
//					if(item.getTitle().equalsIgnoreCase(labelInterval)){
//						intervalTime=item.getIndex();
//					}
//					if(item.getTitle().equalsIgnoreCase(labelStatus)){
//						if(SWITCH_STATUS[0].equalsIgnoreCase(item.getTextViewValue())){
//							status=true;
//						}
//					}
//				}
//
//				//to mode
//				BehaviorRemindInfo remindInfo=new BehaviorRemindInfo(selectMode);
//				remindInfo.setEnable(status);
//				remindInfo.setStartTime(startTime);
//				remindInfo.setEndTime(endTime);
//				remindInfo.setIntervalTime(intervalTime);
//
//				logMessage("behavior remind info >> "+remindInfo.toString());
//
//				//calling interface
//				LsBleManager.getInstance().updateDeviceBehaviorReminder(deviceMac, remindInfo, listener);
//			}
//		});
//	}


	/**
	 * 设置天气提醒
	 * @param deviceMac
	 * @param listener
	 */
	public static void updateWeater(final String deviceMac,final OnSettingListener listener)
	{
		final String[] items=new String[PedometerWeatherState.values().length];
		int index=0;
		for(PedometerWeatherState mode:PedometerWeatherState.values()){
			items[index]=mode.toString().toLowerCase();
			index++;
		}
		String title=baseContext.getResources().getString(R.string.title_weather);
		showSingleChoiceDialog(title, items, new IDialogActionListener() 
		{
			@Override
			public void onSingleChoiceItemValue(int index) 
			{
				//to mode
				PedometerWeatherState selectMode=PedometerWeatherState.values()[index];
				int currentTime=(int)(System.currentTimeMillis()/1000);
				List<PedometerWeatherFuture> weathers=new ArrayList<PedometerWeatherFuture>();
				//for test
				PedometerWeatherFuture weather=new PedometerWeatherFuture();
				weather.setAqi(10);   //for test
				weather.setTemperature1(37);
				weather.setTemperature2(-10);
				weather.setWeatherState(selectMode);
				//add list
				weathers.add(weather);
				PedometerWeather weatherInfo=new PedometerWeather(currentTime, weathers);
				//calling interface
				LsBleManager.getInstance().updatePedometerWeather(deviceMac, weatherInfo, listener);
			}
		});
	}


	/**
	 * 设置运动心率预警
	 */
	public static void updateHeartRateWarning(final String deviceMac,final OnSettingListener listener)
	{
		String title=getResourceString(R.string.title_hr_warning);		
		final String labelMin=getResourceString(R.string.label_min_heart_rate);
		final String labelMax=getResourceString(R.string.label_max_heart_rate);
		final String labelSwitch=getResourceString(R.string.label_switch);

		//init setting items
		List<SettingItem> items=new ArrayList<SettingItem>();
		SettingItem minItem=new SettingItem();
		minItem.setOptions(SettingOptions.Text);
		minItem.setTitle(labelMin);
		minItem.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);

		SettingItem maxItem=new SettingItem();
		maxItem.setOptions(SettingOptions.Text);
		maxItem.setTitle(labelMax);
		maxItem.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);

		SettingItem switchItem=new SettingItem();
		switchItem.setOptions(SettingOptions.SingleChoice);
		switchItem.setTitle(labelSwitch);
		switchItem.setChoiceItems(Arrays.asList(SWITCH_STATUS));

		//add item to list
		items.add(switchItem);
		items.add(minItem);
		items.add(maxItem);

		//show dialog
		showSettingDialog(title, items, new IDialogActionListener() 
		{
			@Override
			public void onSettingItems(List<SettingItem> items) 
			{
				if(!checkSettingItems(items, listener)){
					return ;
				}
				boolean isEnable=false;
				int minHeartRate=0;
				int maxHeartRate=0;
				for(SettingItem item:items){
					if(labelSwitch.equalsIgnoreCase(item.getTitle())){
						if(SWITCH_STATUS[0].equalsIgnoreCase(item.getTextViewValue())){
							isEnable=true;
						}
					}
					if(labelMin.equalsIgnoreCase(item.getTitle())){
						minHeartRate=(int) DeviceDataUtils.toDouble(item.getTextViewValue(),1);
					}
					if(labelMax.equalsIgnoreCase(item.getTitle())){
						maxHeartRate=(int) DeviceDataUtils.toDouble(item.getTextViewValue(),1);
					}
				}
				logMessage("Heart Rate Warning >> "+isEnable+"; minValue="+minHeartRate+"; maxValue="+maxHeartRate);
				PedometerHeartRateAlert hrWarning=new PedometerHeartRateAlert();
				hrWarning.setEnable(isEnable);
				hrWarning.setMinHeartRate(minHeartRate);
				hrWarning.setMaxHeartRate(maxHeartRate);
				LsBleManager.getInstance().updatePedometerHeartRateAlert(deviceMac, hrWarning, listener);
			}
		});
	}

	/**
	 * 更新消息提醒
	 * @param deviceMac
	 * @param listener
	 * @return
	 */
	public static boolean updateMessageRemind(final String deviceMac, final OnSettingListener listener)
	{
		final String[] types=new String[MessageType.values().length-1];
		int index=0;
		for(MessageType mode:MessageType.values()){
			if(mode!=MessageType.UNKNOWN){
				types[index]=mode.toString().toLowerCase();
				index++;
			}
		}
		String title=baseContext.getResources().getString(R.string.title_message_remind);
		final String labelType=getResourceString(R.string.label_message_type);
		final String labelSwitch=getResourceString(R.string.label_switch);

		//init setting items
		List<SettingItem> items=new ArrayList<SettingItem>();

		SettingItem typeItem=new SettingItem();
		typeItem.setOptions(SettingOptions.SingleChoice);
		typeItem.setTitle(labelType);
		typeItem.setChoiceItems(Arrays.asList(types));

		SettingItem switchItem=new SettingItem();
		switchItem.setOptions(SettingOptions.SingleChoice);
		switchItem.setTitle(labelSwitch);
		switchItem.setChoiceItems(Arrays.asList(SWITCH_STATUS));

		//add item to list
		items.add(switchItem);
		items.add(typeItem);

		//show dialog
		showSettingDialog(title, items, new IDialogActionListener() 
		{
			@Override
			public void onSettingItems(List<SettingItem> items) 
			{
				if(!checkSettingItems(items, listener)){
					return ;
				}
				boolean isEnable=false;
				MessageType messageType=MessageType.UNKNOWN;
				for(SettingItem item:items){
					if(item.getTextViewValue()==null || item.getTextViewValue().length()==0){
						continue;
					}
					if(labelSwitch.equalsIgnoreCase(item.getTitle())){
						if(SWITCH_STATUS[0].equalsIgnoreCase(item.getTextViewValue())){
							isEnable=true;
						}
					}
					if(labelType.equalsIgnoreCase(item.getTitle())){
						messageType=MessageType.valueOf(item.getTextViewValue().toUpperCase());
					}
				}
				logMessage("Message Remind >> "+isEnable+"; msgType="+messageType);
				LsBleManager.getInstance().updateMessageRemind(deviceMac, isEnable, messageType, listener);	
			}
		});
		return true;
	}


	/**
	 * 更新秤的用户信息
	 * @param deviceMac
	 * @param listener
	 * @return
	 */
	public static boolean updateScaleUserInfo(final String deviceMac, final OnSettingListener listener)
	{
		String title=baseContext.getResources().getString(R.string.title_set_user_info);
		final String labelNumber=getResourceString(R.string.label_user_number);
		final String labelGender=getResourceString(R.string.label_user_gender);
		final String labelHeight=getResourceString(R.string.label_user_height);
		final String labelWeight=getResourceString(R.string.label_user_weight);
		final String labelAge=getResourceString(R.string.label_user_age);
		final String labelAthleteLevel=getResourceString(R.string.label_athlete_level);

		//init setting items
		List<SettingItem> items=new ArrayList<SettingItem>();

		SettingItem numberItem=new SettingItem();
		numberItem.setOptions(SettingOptions.NumberPicker);
		numberItem.setTitle(labelNumber);
		numberItem.setMinValue(1);
		numberItem.setMaxValue(255);
		numberItem.setUnit("");

		SettingItem ageItem=new SettingItem();
		ageItem.setOptions(SettingOptions.NumberPicker);
		ageItem.setTitle(labelAge);
		ageItem.setMinValue(18);
		ageItem.setMaxValue(99);
		ageItem.setUnit("");

		SettingItem heightItem=new SettingItem();
		heightItem.setOptions(SettingOptions.Text);
		heightItem.setTitle(labelHeight);

		SettingItem weightItem=new SettingItem();
		weightItem.setOptions(SettingOptions.Text);
		weightItem.setTitle(labelWeight);

		SettingItem gender=new SettingItem();
		gender.setOptions(SettingOptions.SingleChoice);
		gender.setTitle(labelGender);
		gender.setChoiceItems(Arrays.asList(USER_GENDER));

		SettingItem levelItem=new SettingItem();
		levelItem.setOptions(SettingOptions.NumberPicker);
		levelItem.setTitle(labelAthleteLevel);
		levelItem.setMinValue(0);
		levelItem.setMaxValue(5);
		levelItem.setUnit("");


		//add item to list
		items.add(numberItem);
		items.add(gender);
		items.add(ageItem);
		items.add(heightItem);
		items.add(weightItem);
		items.add(levelItem);

		//show dialog
		showSettingDialog(title, items, new IDialogActionListener() 
		{
			@Override
			public void onSettingItems(List<SettingItem> items) 
			{
				if(!checkSettingItems(items, listener)){
					return ;
				}
				SexType gender=SexType.MALE;
				boolean isAthlete=false;
				int level=0;
				float userHeight=1.65f;
				float userWeight=0f;
				int userNumber=0;
				int userAge=20;
				for(SettingItem item:items){
					if(labelGender.equalsIgnoreCase(item.getTitle())){
						if(item.getTextViewValue()!=null 
								&& USER_GENDER[0].equalsIgnoreCase(item.getTextViewValue())){
							gender=SexType.FEMALE;
						}
					}
					if(labelAthleteLevel.equalsIgnoreCase(item.getTitle())){
						logMessage("athlete level >> "+item.getIndex());
						level=item.getIndex();
						if(level >0){
							isAthlete=true;
						}
					}
					if(labelNumber.equalsIgnoreCase(item.getTitle())){
						userNumber=item.getIndex();
					}
					if(labelHeight.equalsIgnoreCase(item.getTitle())){
						userHeight=(float) DeviceDataUtils.toDouble(item.getTextViewValue(),2);
					}
					if(labelWeight.equalsIgnoreCase(item.getTitle())){
						userWeight=(float)DeviceDataUtils.toDouble(item.getTextViewValue(),2);
					}
					if(labelAge.equalsIgnoreCase(item.getTitle())){
						userAge=item.getIndex();
					}
				}
				WeightUserInfo weightUserInfo = new WeightUserInfo();
				weightUserInfo.setProductUserNumber(userNumber);
				weightUserInfo.setSex(gender);
				weightUserInfo.setAge(userAge);
				weightUserInfo.setHeight(userHeight);
				weightUserInfo.setAthlete(isAthlete);
				weightUserInfo.setAthleteActivityLevel(level);
				weightUserInfo.setWeight(userWeight);
				//用户信息
				logMessage("User Info  >> "+weightUserInfo);
				LsBleManager.getInstance().setLogMessage("update user info >> "+weightUserInfo.toString());
				LsBleManager.getInstance().updateWeightScaleSetting(deviceMac, DeviceConfigInfoType.A6_WEIGHT_SCALE_USER_INFO, weightUserInfo, listener);
			}
		});
		return true;
	}


	/**
	 * 模拟手机来电提醒，测试固件来电提醒功能是否正常
	 * @return
	 */
	public static boolean testIncomingCall(final String deviceMac)
	{
		String title=baseContext.getResources().getString(R.string.title_test_incoming_call_remind);
		final String labelTitle=getResourceString(R.string.label_incoming_call);

		//init setting items
		List<SettingItem> items=new ArrayList<SettingItem>();
		SettingItem titleItem=new SettingItem();
		titleItem.setOptions(SettingOptions.Text);
		titleItem.setTitle(labelTitle);
		titleItem.setInputType(InputType.TYPE_CLASS_TEXT);
		//add item to list
		items.add(titleItem);

		//show dialog
		showSettingDialog(title, items, new IDialogActionListener() 
		{
			@Override
			public void onSettingItems(List<SettingItem> items) 
			{
				String msgTitle="title";
				for(SettingItem item:items){

					if(labelTitle.equalsIgnoreCase(item.getTitle())){
						msgTitle=item.getTextViewValue();
					}
				}
				PhoneStateMessage stateMessage=new PhoneStateMessage();
				stateMessage.setPhoneState(PhoneState.RINGING);
				stateMessage.setCallNumber(msgTitle);
				stateMessage.setContactsName(msgTitle);

				logMessage("Send Test Incoming Call Message >> "+stateMessage.toString());
				//call test methods
				LsBleManager.getInstance().testPhoneCallMessage(deviceMac, stateMessage);
			}
		});
		return true;
	}	

	/**
	 * 模拟手机应用消息提醒，测试固件的消息提醒设置是否有效
	 * @return
	 */
	public static boolean testMessageRemind()
	{
		final String[] types=new String[MessageType.values().length-1];
		int index=0;
		for(MessageType mode:MessageType.values()){
			if(mode!=MessageType.UNKNOWN){
				types[index]=mode.toString().toLowerCase();
				index++;
			}
		}
		String title=baseContext.getResources().getString(R.string.title_test_message_remind);
		final String labelType=getResourceString(R.string.label_message_type);
		final String labelTitle=getResourceString(R.string.label_message_title);
		final String labelContent=getResourceString(R.string.label_message_content);

		//init setting items
		List<SettingItem> items=new ArrayList<SettingItem>();

		SettingItem typeItem=new SettingItem();
		typeItem.setOptions(SettingOptions.SingleChoice);
		typeItem.setTitle(labelType);
		typeItem.setChoiceItems(Arrays.asList(types));

		SettingItem titleItem=new SettingItem();
		titleItem.setOptions(SettingOptions.Text);
		titleItem.setTitle(labelTitle);
		titleItem.setInputType(InputType.TYPE_CLASS_TEXT);

		SettingItem contentItem=new SettingItem();
		contentItem.setOptions(SettingOptions.Text);
		contentItem.setTitle(labelContent);
		contentItem.setInputType(InputType.TYPE_CLASS_TEXT);

		//add item to list
		items.add(typeItem);
		items.add(titleItem);
		items.add(contentItem);


		//show dialog
		showSettingDialog(title, items, new IDialogActionListener() 
		{
			@Override
			public void onSettingItems(List<SettingItem> items) 
			{
				String msgTitle="title";
				String msgContent="for test";
				String packageName=null;
				MessageType messageType=MessageType.UNKNOWN;
				for(SettingItem item:items){
					if(labelType.equalsIgnoreCase(item.getTitle())){
						messageType=MessageType.valueOf(item.getTextViewValue().toUpperCase());
						packageName=ApplicationProfiles.getMessagePackageName(messageType);
					}
					if(labelTitle.equalsIgnoreCase(item.getTitle())){
						msgTitle=item.getTextViewValue();
					}
					if(labelContent.equalsIgnoreCase(item.getTitle())){
						msgContent=item.getTextViewValue();
					}
				}
				AppMessage appMsg=new AppMessage(messageType);
				appMsg.setPackageName(packageName);
				appMsg.setTitle(msgTitle);
				appMsg.setContent(msgContent);
				appMsg.setEnable(true);
				logMessage("Send Test App Message >> "+appMsg.toString());
				//call test methods
				NotificationAccessService.sendTestNotifcationMessage(appMsg);
			}
		});
		return true;
	}

	/**
	 * 更新手环的健康分数信息
	 * @param deviceMac
	 * @param listener
	 * @return
	 */
	public static boolean updateHealthScoreInfo(final String deviceMac, final OnSettingListener listener){

		String title=baseContext.getResources().getString(R.string.title_set_health_score);
		final String labelType=getResourceString(R.string.label_health_score);

		//init setting items
		List<SettingItem> items=new ArrayList<SettingItem>();

		SettingItem typeItem=new SettingItem();
		typeItem.setOptions(SettingOptions.Text);
		typeItem.setTitle(labelType);

		//add item to list
		items.add(typeItem);

		//show dialog
		showSettingDialog(title, items, new IDialogActionListener() 
		{
			@Override
			public void onSettingItems(List<SettingItem> items) 
			{
				if(!checkSettingItems(items, listener)){
					return ;
				}
				int score=0;
				for(SettingItem item:items){
					if(labelType.equalsIgnoreCase(item.getTitle())){
						score=(int)DeviceDataUtils.toDouble(item.getTextViewValue(), 1);
					}
				}
				HealthScoreInfo healthInfo=new HealthScoreInfo(score);
				logMessage("HealthScoreInfo >> "+healthInfo.toString()+";byte >>"+DeviceDataUtils.byte2hex(healthInfo.getData()));
				LsBleManager.getInstance().pushDeviceMessage(deviceMac, healthInfo, listener);
			}
		});
		return true;
	}


	/**
	 * 测试远程拍摄指令功能
	 * @param deviceMac
	 * @param listener
	 * @return
	 */
	public static boolean testRemotePhotographing(final String deviceMac, final OnSettingListener listener){
		String title=baseContext.getResources().getString(R.string.title_test_photographing);
		final String labelType=getResourceString(R.string.label_photographing_state);

		//init setting items
		List<SettingItem> items=new ArrayList<SettingItem>();

		SettingItem typeItem=new SettingItem();
		typeItem.setOptions(SettingOptions.SingleChoice);
		typeItem.setTitle(labelType);
		typeItem.setChoiceItems(Arrays.asList(PHOTOGRAPHING_STATE));

		//add item to list
		items.add(typeItem);

		//show dialog
		showSettingDialog(title, items, new IDialogActionListener() 
		{
			@Override
			public void onSettingItems(List<SettingItem> items) 
			{
				if(!checkSettingItems(items, listener)){
					return ;
				}
				int state=0;
				for(SettingItem item:items){
					state=item.getIndex();
				}
				PhotographingInfo stateInfo=new PhotographingInfo(state);
				logMessage("PhotographingInfo >> "+stateInfo.toString()+";byte >>"+DeviceDataUtils.byte2hex(stateInfo.getData()));
				LsBleManager.getInstance().pushDeviceMessage(deviceMac, stateInfo, listener);
			}
		});
		return true;
	}

	/**
	 * 测试寻找手环指令功能
	 * @param deviceMac
	 * @param listener
	 * @return
	 */
	public static boolean testDevicePositioning(final String deviceMac, final OnSettingListener listener){
		PositioningInfo positioning=new PositioningInfo();
		logMessage("PositioningInfo >> "+positioning.toString()+";byte >>"+DeviceDataUtils.byte2hex(positioning.getData()));
		LsBleManager.getInstance().pushDeviceMessage(deviceMac, positioning, listener);
		return true;
	}

	/**
	 * 重启设备
	 * @param deviceMac
	 * @param listener
	 * @return
	 */
	public static boolean restartDevice(final String deviceMac, final OnSettingListener listener)
	{
		DeviceRestartInfo restart=new DeviceRestartInfo();
		logMessage("DeviceRestartInfo >> "+restart.toString()+";byte >>"+DeviceDataUtils.byte2hex(restart.getData()));
		LsBleManager.getInstance().pushDeviceMessage(deviceMac, restart, listener);
		return true;
	}

	public static boolean updateDeviceFunctionStatus(final String deviceMac, final OnSettingListener listener)
	{
		final String[] types=new String[DeviceFunctionType.values().length-1];
		int index=0;
		for(DeviceFunctionType mode:DeviceFunctionType.values()){
			if(DeviceFunctionType.UNKNOWN!=mode){
				types[index]=mode.toString().toLowerCase().replace("_", " ");
				index++;
			}
		}
		String title=baseContext.getResources().getString(R.string.title_device_function);
		final String labelStatus=getResourceString(R.string.label_switch);
		final String labelType=getResourceString(R.string.label_function_type);

		//init setting items
		List<SettingItem> items=new ArrayList<SettingItem>();

		SettingItem typeItem=new SettingItem();
		typeItem.setOptions(SettingOptions.SingleChoice);
		typeItem.setTitle(labelType);
		typeItem.setChoiceItems(Arrays.asList(types));

		SettingItem statusItem=new SettingItem();
		statusItem.setOptions(SettingOptions.SingleChoice);
		statusItem.setTitle(labelStatus);
		statusItem.setChoiceItems(Arrays.asList(SWITCH_STATUS));


		//add item to list
		items.add(statusItem);
		items.add(typeItem);

		//show dialog
		showSettingDialog(title, items, new IDialogActionListener() 
		{
			@Override
			public void onSettingItems(List<SettingItem> items) 
			{
				boolean isEnable=false;
				DeviceFunctionType funType=DeviceFunctionType.UNKNOWN;
				for(SettingItem item:items){
					if(labelType.equalsIgnoreCase(item.getTitle())){
						funType=DeviceFunctionType.valueOf(item.getTextViewValue().toUpperCase().replace(" ", "_"));
					}
					if(labelStatus.equalsIgnoreCase(item.getTitle())){
						if(SWITCH_STATUS[0].equalsIgnoreCase(item.getTextViewValue())){
							isEnable=true;
						}					
					}
				}
				DeviceFunctionInfo functionInfo=new DeviceFunctionInfo();
				functionInfo.setEnable(isEnable);
				functionInfo.setType(funType);
				//
				logMessage("DeviceFunctionInfo >> "+functionInfo.toString());
				LsBleManager.getInstance().updateDeviceFunctionInfo(deviceMac, functionInfo, listener);
			}
		});
		return true;
	}

	/**
	 * 更新手机的GPS状态
	 * @param deviceMac
	 * @param sportNotify
	 * @param listener
	 */
	public static void updatePhoneGpsStatus(final String deviceMac,final SportNotify sportNotify,final OnSettingListener listener)
	{
		if(sportNotify==null || sportNotify.getRequestType()!=0x01){
			Log.e("LS-BLE", "no permission to show view...."+sportNotify.toString());
			return ;
		}

		final String[] types=new String[PhoneGpsStatus.values().length];
		int index=0;
		for(PhoneGpsStatus mode:PhoneGpsStatus.values()){
			types[index]=mode.toString();
			index++;
		}
		String title=baseContext.getResources().getString(R.string.title_gps_status);
		final String labelStatus=getResourceString(R.string.label_gps_status);

		//init setting items
		List<SettingItem> items=new ArrayList<SettingItem>();

		SettingItem statusItem=new SettingItem();
		statusItem.setOptions(SettingOptions.SingleChoice);
		statusItem.setTitle(labelStatus);
		statusItem.setChoiceItems(Arrays.asList(types));

		//add item to list
		items.add(statusItem);

		//show dialog
		showSettingDialog(title, items, new IDialogActionListener() 
		{
			@Override
			public void onSettingItems(List<SettingItem> items) 
			{
				PhoneGpsStatus gpsStatus=PhoneGpsStatus.Unavailable;
				for(SettingItem item:items){
					if(labelStatus.equalsIgnoreCase(item.getTitle()) 
							&& !TextUtils.isEmpty(item.getTextViewValue())){
						gpsStatus=PhoneGpsStatus.valueOf(item.getTextViewValue());				
					}
				}
				SportNotifyConfirm notifyConfirm=new SportNotifyConfirm(sportNotify, gpsStatus);

				//
				logMessage("SportNotifyConfirm >> "+notifyConfirm.toString());
				LsBleManager.getInstance().pushDeviceMessage(deviceMac, notifyConfirm, listener);
			}
		});
	}


	/**
	 * 向设备发起运动请求，如开始或结束运动
	 * @param deviceMac
	 * @param listener
	 */
	public static void sendSportRequest(final String deviceMac,final OnSettingListener listener)
	{
		SportRequestInfo requestInfo=new SportRequestInfo(PedometerSportsType.AEROBIC_SPORT_12);
		//stop
		requestInfo.setState(SportRequestInfo.STOP);
		LsBleManager.getInstance().pushDeviceMessage(deviceMac, requestInfo, listener);
	}
	
	/**
	 * 更新手环的心情提醒设置
	 * @param deviceMac
	 * @param listener
	 */
	public static void updateMoodbeamReminder(final String deviceMac,final OnSettingListener listener)
	{
		String title=baseContext.getResources().getString(R.string.title_mood_reminder);
		final String labelStatus=getResourceString(R.string.label_switch);
		final String labelStart=getResourceString(R.string.label_start_time);
		final String labelEnd=getResourceString(R.string.label_end_time);
		final String labelInterval=getResourceString(R.string.label_vibration_interval);
		final String labelTime=getResourceString(R.string.label_vibration_time);

		//init setting items
		List<SettingItem> menuItems=new ArrayList<SettingItem>();
		SettingItem statusItem=new SettingItem();
		statusItem.setOptions(SettingOptions.SingleChoice);
		statusItem.setTitle(labelStatus);
		statusItem.setChoiceItems(Arrays.asList(SWITCH_STATUS));


		SettingItem startTimeItem=new SettingItem();
		startTimeItem.setOptions(SettingOptions.TimePicker);
		startTimeItem.setTitle(labelStart);

		SettingItem endTimeItem=new SettingItem();
		endTimeItem.setOptions(SettingOptions.TimePicker);
		endTimeItem.setTitle(labelEnd);

		SettingItem intervalItem=new SettingItem();
		intervalItem.setOptions(SettingOptions.Text);
		intervalItem.setTitle(labelInterval);
//		intervalItem.setMinValue(1);
//		intervalItem.setMaxValue(655);
		
		SettingItem timeItem=new SettingItem();
		timeItem.setOptions(SettingOptions.NumberPicker);
		timeItem.setTitle(labelTime);
		timeItem.setMinValue(1);
		timeItem.setMaxValue(30);
		timeItem.setUnit(" s");


		//add item to list
		menuItems.add(statusItem);
		menuItems.add(startTimeItem);
		menuItems.add(endTimeItem);
		menuItems.add(intervalItem);
		menuItems.add(timeItem);


		//show setting dialog
		showSettingDialog(title, menuItems, new IDialogActionListener() {
			@Override
			public void onSettingItems(List<SettingItem> items) 
			{
				if(!checkSettingItems(items, listener)){
					return ;
				}
				String startTime=null;
				String endTime=null;
				int interval=0;
				int time=0;
				boolean status=false;
				int type=0;
//				BehaviorRemindType selectMode=BehaviorRemindType.DRINKING_WATER;
				for(SettingItem item:items){
					if(item.getTitle().equalsIgnoreCase(labelStart)){
						startTime=item.getTime();
					}
					if(item.getTitle().equalsIgnoreCase(labelEnd)){
						endTime=item.getTime();
					}
					if(item.getTitle().equalsIgnoreCase(labelInterval)){
						interval=(int)DeviceDataUtils.toDouble(item.getTextViewValue(), 1);
//						interval=;//item.getIndex();
					}
					if(item.getTitle().equalsIgnoreCase(labelTime)){
						time=item.getIndex();
					}
					if(item.getTitle().equalsIgnoreCase(labelStatus)){
						if(SWITCH_STATUS[0].equalsIgnoreCase(item.getTextViewValue())){
							status=true;
						}
					}
				}

//				//to mode
				MoodRecordReminder remindInfo=new MoodRecordReminder();
				remindInfo.setEnable(status);
				remindInfo.setStartTime(startTime);
				remindInfo.setEndTime(endTime);
				remindInfo.setVibrationInterval(interval);
				remindInfo.setVibrationTime(time);
				logMessage("moodbeam reminder info >> "+remindInfo.toString());
				//calling interface
				LsBleManager.getInstance().pushDeviceMessage(deviceMac, remindInfo, listener);
			}
		});
	}

	/**
	 * 更新手环的勿扰模式设置
	 * @param deviceMac
	 * @param listener
	 */
	public static void updateQuietModeSetting(final String deviceMac,final OnSettingListener listener){
		String title=baseContext.getResources().getString(R.string.title_fun_quiet_mode);
		final String labelStatus=getResourceString(R.string.label_switch);
		final String labelStart=getResourceString(R.string.label_start_time);
		final String labelEnd=getResourceString(R.string.label_end_time);
		final String labelFuns=getResourceString(R.string.label_fun_list);

		//init setting items
		List<SettingItem> menuItems=new ArrayList<SettingItem>();
		SettingItem statusItem=new SettingItem();
		statusItem.setOptions(SettingOptions.SingleChoice);
		statusItem.setTitle(labelStatus);
		statusItem.setChoiceItems(Arrays.asList(SWITCH_STATUS));
		//start time
		SettingItem startTimeItem=new SettingItem();
		startTimeItem.setOptions(SettingOptions.TimePicker);
		startTimeItem.setTitle(labelStart);
		//ends time
		SettingItem endTimeItem=new SettingItem();
		endTimeItem.setOptions(SettingOptions.TimePicker);
		endTimeItem.setTitle(labelEnd);
		//fun list item
		SettingItem funsItems=new SettingItem();
		funsItems.setOptions(SettingOptions.SingleChoice);
		funsItems.setTitle(labelFuns);
		funsItems.setChoiceItems(Arrays.asList(DEVICE_FUNS));

		//add item to list
		menuItems.add(statusItem);
		menuItems.add(startTimeItem);
		menuItems.add(endTimeItem);
		menuItems.add(funsItems);

		//show setting dialog
		showSettingDialog(title, menuItems, new IDialogActionListener() {
			@Override
			public void onSettingItems(List<SettingItem> items) 
			{
				if(!checkSettingItems(items, listener)){
					return ;
				}
				String startTime=null;
				String endTime=null;
				boolean status=false;
				List<DeviceFunctionInfo> functionInfos=new ArrayList<DeviceFunctionInfo>();
				for(SettingItem item:items){
					if(item.getTitle().equalsIgnoreCase(labelStart)){
						startTime=item.getTime();
					}
					if(item.getTitle().equalsIgnoreCase(labelEnd)){
						endTime=item.getTime();
					}
					if(item.getTitle().equalsIgnoreCase(labelStatus)
							&& !TextUtils.isEmpty(item.getTextViewValue()) ){
						if(SWITCH_STATUS[0].equalsIgnoreCase(item.getTextViewValue())){
							status=true;
						}
					}
					if(item.getTitle().equalsIgnoreCase(labelFuns) 
							&& !TextUtils.isEmpty(item.getTextViewValue()) ){
						DeviceFunctionInfo fun=new DeviceFunctionInfo();
						fun.setType(DeviceFunctionType.LIFT_WRIST_POWER_SWITCH);
						fun.setEnable(false);
						if(DEVICE_FUNS[1].equalsIgnoreCase(item.getTextViewValue())){
							fun.setEnable(false);
						}
						functionInfos.add(fun);
					}
				}
				//to mode
				PedometerQuietMode quietMode=new PedometerQuietMode();
				quietMode.setStartTime(startTime);
				quietMode.setEndsTime(endTime);
				quietMode.setStatus(status);
				quietMode.setFunctions(functionInfos);
				logMessage("quiet mode setting >> "+quietMode.toString());
				//calling interface
				LsBleManager.getInstance().pushDeviceMessage(deviceMac, quietMode, listener);
			}
		});
	}
	
	
	
	
	/**
	 * 判断设备是否已连接
	 * @param deviceMac
	 * @return
	 */
	private static boolean isConnected(String deviceMac){
		return true;
		//		if(TextUtils.isEmpty(deviceMac)){
		//			return false;
		//		}
		//		DeviceConnectState state=LsBleManager.getInstance().checkDeviceConnectState(deviceMac);
		//		if(state==DeviceConnectState.CONNECTED_SUCCESS){
		//			return true;
		//		}
		//		else{
		//			return false;
		//		}
	}


	/**
	 * Single Choice Dialog View
	 */
	private static void showSingleChoiceDialog(String title,CharSequence[] items,final IDialogActionListener listener) 
	{
		if(items==null || baseContext==null){
			return ;
		}
		ContextThemeWrapper ctw = new ContextThemeWrapper(baseContext, android.R.style.Theme_Holo_Light);
		// Strings to Show In Dialog with Radio Buttons
		// Creating and Building the Dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(ctw);
		builder.setTitle(title);
		builder.setSingleChoiceItems(items, -1,new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog, int which) 
			{
				listener.onSingleChoiceItemValue(which);
				dialog.dismiss();
			}
		});
		dialog = builder.create();
		dialog.show();
	}

	/**
	 * Single Choice Dialog View
	 */
	private static void showMultiChoiceDialog(String title,CharSequence[] items,final IDialogActionListener listener) 
	{
		if(items==null || baseContext==null){
			return ;
		}
		final List<Integer> pages=new ArrayList<Integer>();
		ContextThemeWrapper ctw = new ContextThemeWrapper(baseContext, android.R.style.Theme_Holo_Light);
		// Strings to Show In Dialog with Radio Buttons
		// Creating and Building the Dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(ctw);
		builder.setTitle(title);
		builder.setMultiChoiceItems(items, null, new AlertDialog.OnMultiChoiceClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				if(isChecked){
					pages.add(which);
				}
				else{
					pages.remove(which);
				}
				Log.e(TAG, "my multi choice value >> "+which+"; isChecked ?"+isChecked);
			}
		})
		.setPositiveButton("OK", new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				listener.onMultiChoiceItemValue(pages);
				dialog.dismiss();
			}
		});
		dialog = builder.create();
		dialog.show();
	}

	private static void showTitleAndTimeChoiceDialog(String title,final IDialogActionListener listener){
		ContextThemeWrapper ctw = new ContextThemeWrapper(baseContext, android.R.style.Theme_Holo_Light);
		// Strings to Show In Dialog with Radio Buttons
		// Creating and Building the Dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(ctw);
		builder.setTitle(title);
		//add timepicker
		final View dialogView = LayoutInflater.from(baseContext).inflate(R.layout.event_reminder, null);
		final TimePicker timePicker=(TimePicker) dialogView.findViewById(R.id.timePicker);
		final EditText  titleView=(EditText) dialogView.findViewById(R.id.event_title_textView);
		titleView.setInputType(InputType.TYPE_CLASS_TEXT);
		timePicker.setIs24HourView(true);
		builder.setPositiveButton("OK", new AlertDialog.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String time=timePicker.getHour()+":"+timePicker.getMinute();
				String titleStr=titleView.getText().toString();
				Log.e(TAG, "my time choice value >> "+time+"; title >> "+titleStr);
				listener.onTitleAndTimeChoiceValue(titleStr,timePicker.getHour(),timePicker.getMinute());
				dialog.dismiss();		
			}
		});
		dialog = builder.setView(dialogView).create();
		dialog.show();
	}


	/**
	 * 时间选择Dialog
	 * @param title
	 * @param listener
	 */
	@SuppressLint("NewApi") 
	private static void showTimeChoiceDialog(String title,final IDialogActionListener listener){
		ContextThemeWrapper ctw = new ContextThemeWrapper(baseContext, android.R.style.Theme_Holo_Light);
		// Strings to Show In Dialog with Radio Buttons
		// Creating and Building the Dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(ctw);
		builder.setTitle(title);
		//add timepicker
		final View dialogView = LayoutInflater.from(baseContext).inflate(R.layout.time_select, null);
		final TimePicker timePicker=(TimePicker) dialogView.findViewById(R.id.timePicker);
		timePicker.setIs24HourView(true);
		builder.setPositiveButton("OK", new AlertDialog.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				String time=timePicker.getHour()+":"+timePicker.getMinute();
				Log.e(TAG, "my time choice value >> "+time);
				listener.onTimeChoiceValue(timePicker.getHour(),timePicker.getMinute());
				dialog.dismiss();				
			}

		});
		dialog = builder.setView(dialogView).create();
		dialog.show();
	}


	/**
	 * 获取手机当前时间
	 * @return
	 */
	private static final String getCurrentTime(){
		Calendar now = Calendar.getInstance();  
		return now.get(Calendar.HOUR_OF_DAY)+":"+now.get(Calendar.MINUTE);
	}


	/**
	 * 判断是否存在设置项
	 * @param items
	 * @return
	 */
	private static List<SettingItem> getEditSettingItem(List<SettingItem> items){
		if(items==null || items.size() ==0){
			return null;
		}
		List<SettingItem> editItems=new ArrayList<SettingItem>();
		for(SettingItem item:items){
			if(item.getOptions() == SettingOptions.Text){
				editItems.add(item);
			}
		}
		return editItems;
	}


	/**
	 * 根据设置项，初始化EditText View
	 * @param items
	 * @param listView 
	 */
	private static void initEditTextView(LinearLayout layout,List<SettingItem> items,final ExpandableListView listView, final OnEditTextViewListener listener){
		List<SettingItem> editItems=getEditSettingItem(items);
		if(editItems==null || editItems.size()==0){
			return ;
		}
		for(final SettingItem item:editItems){
			//add edit text cell
			LayoutInflater layoutInflater = (LayoutInflater) baseContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View editCellView= layoutInflater.inflate(R.layout.setting_item, null);
			TextView titleView=(TextView) editCellView.findViewById(R.id.setting_title_text_view);
			titleView.setText(item.getTitle());

			final EditText editView=(EditText) editCellView.findViewById(R.id.edit_text_view);
			editView.setInputType(item.getInputType());//InputType.TYPE_CLASS_TEXT
			final TextView valueTextView=(TextView) editCellView.findViewById(R.id.setting_value_text_view);
			editView.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}

				@Override
				public void afterTextChanged(Editable s) {
					if(s != null && s.length() > 0)
					{
						String editValue=editView.getText().toString().trim();
						valueTextView.setText(editValue);
						//更新内容
						item.setEditValue(editValue);
						item.setTextViewValue(editValue);
						//回调文本内容
						listener.onEditTextResults(editView,item);		            
					}
				}
			});
			//			editView.setOnFocusChangeListener(focusChangeListener);
			editCellView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(lastGroupIndex!=-1){
						listView.collapseGroup(lastGroupIndex);
						lastGroupIndex=-1;
					}
					logMessage("on  setOnClickListener >> "+v);
					listener.onEditTextResults(editView,item);
					valueTextView.setVisibility(View.GONE);
					editView.setVisibility(View.VISIBLE);
					editView.setEnabled(true);
					editView.setHint(getResourceString(R.string.label_please_enter));
					editView.requestFocus();
					//弹出系统输入键盘
					InputMethodManager imm = (InputMethodManager) baseContext.getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.showSoftInput(editView, InputMethodManager.SHOW_IMPLICIT);
				}
			});
			layout.addView(editCellView);
		}
	}

	/**
	 * 展示设备功能设置项
	 * @param title
	 * @param items
	 * @param listener
	 */
	protected static void showSettingDialog(String title,List<SettingItem> items,final IDialogActionListener listener)
	{
		currentEditView=null;
		final List<SettingItem> settings=new ArrayList<SettingItem>();
		for(SettingItem item:items){
			if(item.getOptions()!=SettingOptions.Text){
				settings.add(item);
			}
		}
		final List<SettingItem> editSettings=new ArrayList<SettingItem>();
		ContextThemeWrapper ctw = new ContextThemeWrapper(baseContext, android.R.style.Theme_Holo_Light);
		// Strings to Show In Dialog with Radio Buttons
		// Creating and Building the Dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(ctw);
		builder.setTitle(title);
		//add timepicker
		final View dialogView = LayoutInflater.from(baseContext).inflate(R.layout.function_list, null);
		LinearLayout layout=(LinearLayout) dialogView.findViewById(R.id.functions_list_layout);
		//初始化function list
		final ExpandableListView expandableListView=(ExpandableListView) dialogView.findViewById(R.id.device_functions_list);
		//初始化EditTextView
		initEditTextView(layout, items,expandableListView,new OnEditTextViewListener() {
			@Override
			public void onEditTextResults(EditText editView,SettingItem item) {
				currentEditView=editView;
				if(!editSettings.contains(item)){
					editSettings.add(item);
				}
			}
		});
		//init list adapter
		final SettingAdapter expandableListAdapter = new SettingAdapter(baseContext, settings);
		expandableListView.setAdapter(expandableListAdapter);
		expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
			@Override
			public void onGroupExpand(int groupPosition) {
				//collapse the old expanded group, if not the same
				//as new group to expand
				if(groupPosition != lastGroupIndex && lastGroupIndex!=-1){
					//update item value
					expandableListAdapter.notifyDataSetChanged();
					expandableListView.collapseGroup(lastGroupIndex);
				}
				lastGroupIndex = groupPosition;
				//隐藏输入键盘及去除焦点
				if(currentEditView!=null){
					//获取当前文本的输入内容
					logMessage("current edit input text >> "+currentEditView.getText().toString().trim());
					InputMethodManager imm = (InputMethodManager) baseContext.getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(currentEditView.getWindowToken(), 0);
					currentEditView=null;
				}
			}
		});
		//重点击事件
		expandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
			@Override
			public void onGroupCollapse(int groupPosition) {
				//update item value
				expandableListAdapter.notifyDataSetChanged();
			}
		});

		//set click listener
		builder.setPositiveButton("OK", new AlertDialog.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(lastGroupIndex!=-1){
					//update item value
					expandableListAdapter.notifyDataSetChanged();
					expandableListView.collapseGroup(lastGroupIndex);
					lastGroupIndex=-1;
				}
				//回调设置信息
				List<SettingItem> results=new ArrayList<SettingItem>();
				results.addAll(settings);
				results.addAll(editSettings);
				listener.onSettingItems(results);
				dialog.dismiss();				
			}
		});
		dialog = builder.setView(dialogView).create();
		dialog.show();
	}


	/**
	 * 发送设置失败的广播
	 * @param msg
	 */
	private static void sendSettingFailureBroadcast(String msg)
	{
		//
		logMessage("send broadcast >>"+msg);
		Intent errorIntent=new Intent();
		errorIntent.setAction(SETTING_PROFIES_ACTION);
		errorIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);

		errorIntent.putExtra("errorMsg", getResourceString(R.string.title_error_parameter)+" \r\n "+msg);
		//以广播的形式，回调设置失败的原因
		//		baseContext.sendBroadcast(errorIntent);
		LocalBroadcastManager.getInstance(baseContext).sendBroadcast(errorIntent);
	}


	/**
	 * 检查设置项
	 * @param items
	 * @param listener
	 */
	public static boolean checkSettingItems(List<SettingItem> items,OnSettingListener listener){
		if(items==null || items.size()==0){
			listener.onFailure(ErrorCode.PARAMETER_ERROR_CODE);
			sendSettingFailureBroadcast("no setting items");
			logMessage("on setting items  is failure ....");
			return false;
		}
		boolean isSuccess=true;
		for(SettingItem item:items){
			if(item.getOptions() == SettingOptions.TimePicker){
				if(item.getTime()==null || item.getTime().length()==0){
					sendSettingFailureBroadcast(item.getTitle()+"="+item.getTime());
					logMessage("on setting items  time is failure ....");
					isSuccess=false;
					break ;
				}
				if(item.getOptions() == SettingOptions.Text){
					if(TextUtils.isEmpty(item.getTextViewValue())){
						logMessage("on setting items  text is failure ....");
						sendSettingFailureBroadcast(item.getTitle()+"="+item.getTextViewValue());
						isSuccess=false;
						break;
					}
				}
			}
		}
		if(!isSuccess){
			logMessage("on setting items  all is failure ....");
			listener.onFailure(ErrorCode.PARAMETER_ERROR_CODE);
			return false;
		}
		return true;
	}


	/**
	 * 输入随机码View
	 * @param macAddress 
	 */
	public static void showRandomNumberInputView(final String macAddress) 
	{
		String title=baseContext.getResources().getString(R.string.title_input_random_number);
		final String labelRandomNumber=getResourceString(R.string.label_random_number);

		//init setting items
		List<SettingItem> items=new ArrayList<SettingItem>();

		SettingItem numberItem=new SettingItem();
		numberItem.setOptions(SettingOptions.Text);
		numberItem.setTitle(labelRandomNumber);
		items.add(numberItem);

		//show dialog
		showSettingDialog(title, items, new IDialogActionListener() 
		{
			@Override
			public void onSettingItems(List<SettingItem> items) 
			{
				String randomNumber="null";
				for(SettingItem item:items){
					if(item.getTextViewValue()==null || item.getTextViewValue().length()==0){
						continue;
					}
					if(labelRandomNumber.equalsIgnoreCase(item.getTitle())){
						randomNumber=item.getTextViewValue();
					}
				}
				//用户信息
				logMessage("input random number >> "+randomNumber);
				int statue = LsBleManager.getInstance().inputOperationCommand(macAddress, OperationCommand.CMD_RANDOM_NUMBER, randomNumber);
				if(statue == PairingInputRandomStatus.SUCCESS){
					return ;
				}
				final String msg=getCommandStateMessage(statue);
				if(statue == PairingInputRandomStatus.FAIL_CHECK_RANDOM_CODE_ERR){
					DialogUtils.showToastMessage(baseContext, msg);
					//提示重新输入
					showRandomNumberInputView(macAddress);
				}
				else{
					DialogUtils.showToastMessage(baseContext, msg);
				}
			}
		});
	}

	/**
	 * for test
	 */
	protected static void showSettingView(){
		ContextThemeWrapper ctw = new ContextThemeWrapper(baseContext, android.R.style.Theme_Holo_Light);
		// Strings to Show In Dialog with Radio Buttons
		// Creating and Building the Dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(ctw);
		builder.setTitle("for test");
		//add timepicker
		final View dialogView = LayoutInflater.from(baseContext).inflate(R.layout.function_list, null);
		LinearLayout layout=(LinearLayout) dialogView.findViewById(R.id.functions_list_layout);

		//add edit text cell
		LayoutInflater layoutInflater = (LayoutInflater) baseContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View editCellView= layoutInflater.inflate(R.layout.setting_item, null);
		TextView titleView=(TextView) editCellView.findViewById(R.id.setting_title_text_view);
		titleView.setText("闹钟标题");
		final EditText editView=(EditText) editCellView.findViewById(R.id.edit_text_view);
		editView.setInputType(InputType.TYPE_CLASS_TEXT);
		final TextView valueTextView=(TextView) editCellView.findViewById(R.id.setting_value_text_view);

		editView.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(!hasFocus){
					String editValue=editView.getText().toString().trim();
					valueTextView.setVisibility(View.VISIBLE);
					editView.setVisibility(View.GONE);
					valueTextView.setText(editValue);
				}
			}
		});

		editCellView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				valueTextView.setVisibility(View.GONE);
				editView.setVisibility(View.VISIBLE);
				editView.setEnabled(true);
				editView.setFocusable(true);
				editView.setHint("请输入内容");
				editView.requestFocus();
				InputMethodManager imm = (InputMethodManager) baseContext.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(editView, InputMethodManager.SHOW_IMPLICIT);
			}
		});

		layout.addView(editCellView);

		final ExpandableListView expandableListView=(ExpandableListView) dialogView.findViewById(R.id.device_functions_list);
		final List<SettingItem> settings=new ArrayList<SettingItem>();
		SettingItem switchItem=new SettingItem();
		switchItem.setOptions(SettingOptions.SingleChoice);
		switchItem.setTitle("开关");

		SettingItem startTimeItem=new SettingItem();
		startTimeItem.setOptions(SettingOptions.TimePicker);
		startTimeItem.setTitle("开始时间");

		SettingItem endTimeItem=new SettingItem();
		endTimeItem.setOptions(SettingOptions.TimePicker);
		endTimeItem.setTitle("结束时间");

		SettingItem editItem=new SettingItem();
		editItem.setOptions(SettingOptions.Text);
		editItem.setTitle("标题");

		//add item to list
		settings.add(switchItem);
		settings.add(startTimeItem);
		settings.add(endTimeItem);

		final SettingAdapter expandableListAdapter = new SettingAdapter(baseContext, settings);
		expandableListView.setAdapter(expandableListAdapter);
		expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

			@Override
			public void onGroupExpand(int groupPosition) {
				Toast.makeText(baseContext,  settings.get(groupPosition) + " List Expanded.",
						Toast.LENGTH_SHORT).show();
				SettingItem item=settings.get(groupPosition);
				if(item.getOptions() == SettingOptions.Text){
					item.setEdit(true);
					//show edit text view
					expandableListAdapter.notifyDataSetChanged();
				}
				else{
					InputMethodManager imm = (InputMethodManager) baseContext.getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(editView.getWindowToken(), 0);				}
			}
		});

		expandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
			@Override
			public void onGroupCollapse(int groupPosition) {
				SettingItem item=settings.get(groupPosition);
				Toast.makeText(baseContext," List Collapsed >> "+item.toString(), Toast.LENGTH_SHORT).show();
				if(item.getOptions() == SettingOptions.Text){
					item.setEdit(false);
				}
				//update item value
				expandableListAdapter.notifyDataSetChanged();

			}
		});

		expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) 
			{
				return false;
			}
		});
		builder.setPositiveButton("OK", new AlertDialog.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {

				dialog.dismiss();				
			}

		});
		dialog = builder.setView(dialogView).create();
		dialog.show();
	}


	/**
	 * 获取绑定结果
	 * @param status
	 * @return
	 */
	private static String getBindResults(int status) 
	{
		String resutlsStr = "";
		switch (status) {
		case PairedResultsCode.PAIR_SUCCESSFULLY:
			resutlsStr = getResourceString(R.string.str_pair_status_ok);
			break;
		case PairedResultsCode.PAIR_FAILED_RANDOM_CHECK:
			resutlsStr = getResourceString(R.string.str_verification_failed);
			break;
		case PairedResultsCode.PAIR_FAILED_BLUETOOTH_CLOSE:
			resutlsStr = getResourceString(R.string.str_bluetooth_unavailiable);
			break;
		case PairedResultsCode.PAIR_FAILED_TIMEOUT:
			resutlsStr = getResourceString(R.string.str_pair_status_timeout);
			break;
		default:
			resutlsStr=getResourceString(R.string.str_pair_status_failed)+"; code="+status;
			break;
		}
		return resutlsStr;
	}

	/**
	 * 获取状态信息
	 * @param cmdState
	 * @return
	 */
	private static String getCommandStateMessage(int cmdState)
	{
		String msg="unsupported!";
		switch (cmdState) 
		{
		case PairingInputRandomStatus.SUCCESS:
			msg = getResourceString(R.string.str_random_number_ok)+"\n";
			break;
		case PairingInputRandomStatus.FAIL_CHECK_RANDOM_CODE_ERR:
			msg = getResourceString(R.string.str_random_number_error)+"\n";
			break;
		case PairingInputRandomStatus.FAIL_DEVICE_NOT_RANDOM_PAIRING:
			msg = msg+"\t >> "+cmdState;
			break;
		case PairingInputRandomStatus.FAIL_REPEAT_INPUT_RANDOM_CODE:
			msg = msg+"\t >> "+cmdState;
			break;
		case PairingInputRandomStatus.FAIL_DEVICE_UNRQUEST_RENDOM_CODE:
			msg = msg+"\t >> "+cmdState;
			break;
		case PairingInputRandomStatus.FAIL_DEVICE_DISCONNECT:
			msg = getResourceString(R.string.str_device_not_connected)+"\n";
			break;
		default:
			break;
		}
		return msg;
	}

	/**
	 * 判断是否是有氧运动
	 * @param sportsType
	 * @return
	 */
	private static boolean isAerobicExercise(PedometerSportsType sportsType)
	{
		boolean isAerobicExercise=false;
		if(sportsType == PedometerSportsType.AEROBIC_SPORT 
				|| sportsType == PedometerSportsType.AEROBIC_SPORT_12 
				|| sportsType == PedometerSportsType.AEROBIC_SPORT_6){
			isAerobicExercise=true;
		}
		return isAerobicExercise;
	}

	/**
	 * log message
	 * @param msg
	 */
	public static void logMessage(String msg){
		Log.e(TAG, msg);
	}
}
