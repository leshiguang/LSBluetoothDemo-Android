/**
 * 
 */
package com.bluetooth.demo.ui;



import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.MenuItemCompat;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bluetooth.demo.R;
import com.bluetooth.demo.database.AsyncTaskRunner;
import com.bluetooth.demo.device.DeviceSettiingProfiles;
import com.bluetooth.demo.ui.adapter.ScanResultsAdapter;
import com.bluetooth.demo.ui.dialog.OnDialogClickListener;
import com.bluetooth.demo.ui.dialog.ShowTextDialogFragment;
import com.lifesense.ble.LsBleManager;
import com.lifesense.ble.PairCallback;
import com.lifesense.ble.SearchCallback;
import com.lifesense.ble.bean.DeviceUserInfo;
import com.lifesense.ble.bean.LsDeviceInfo;
import com.lifesense.ble.bean.PairedConfirmInfo;
import com.lifesense.ble.bean.WeightUserInfo;
import com.lifesense.ble.bean.constant.BroadcastType;
import com.lifesense.ble.bean.constant.DeviceRegisterState;
import com.lifesense.ble.bean.constant.DeviceType;
import com.lifesense.ble.bean.constant.DeviceTypeConstants;
import com.lifesense.ble.bean.constant.OperationCommand;
import com.lifesense.ble.bean.constant.PairedConfirmState;
import com.lifesense.ble.bean.constant.ProtocolType;
import com.lifesense.ble.bean.constant.SexType;
import com.lifesense.ble.bean.constant.UnitType;


/**
 * @author CaiChiXiang
 *
 */
@SuppressLint("NewApi")
public class SearchFragment extends Fragment{


	private static final String WIFI_SSID="Lifesense_Work";//"Lifesense_Work5G";
	private static final String WIFI_PASSWORD="86358868";//"86358868";
	
	/**
	 * The container view which has layout change animations turned on. In this sample, this view
	 * is a {@link android.widget.LinearLayout}.
	 */
	private View rootView;
	private View loadingView;
	private View scanResultsLayout;
	private TextView progressBarTextView,showScanFilterTextView;
	private  ListView scanResultListView;
	private MenuItem searchMenuItem;
	private boolean isScanning;
	private boolean hasScanResults;
	private List<DeviceType> mScanDeviceType;
	private BroadcastType mBroadcastType;
	private ScanResultsAdapter mListAdapter;
	private List<LsDeviceInfo> mDeviceList;
	private Handler updateListViewHanlder;
	private ArrayList<LsDeviceInfo> tempList;
	private LsDeviceInfo currentPairingDevice;
	private boolean isPairingProcess;
	private  AlertDialog userListDialog;
	private List<DeviceUserInfo> mDeviceUserList;
	private static String scanFilterCondition=null;


	private SearchCallback mSearchCallback=new SearchCallback()
	{

		@Override
		public void onSearchResults(LsDeviceInfo lsDevice) 
		{
			updateScanResults(lsDevice);
		}
		
	};
	
	private PairCallback mPairCallback=new PairCallback() 
	{
		@Override
		public void onPairResults(final LsDeviceInfo lsDevice,final int status)
		{
			if(getActivity()==null)
			{
				return ;
			}
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() 
				{
					if(lsDevice!=null && status==0)
					{
						scanResultsLayout.setVisibility(View.VISIBLE);
						loadingView.setVisibility(View.GONE);
						saveDeviceInfo(lsDevice);
					}
					else
					{
						scanResultsLayout.setVisibility(View.VISIBLE);
						loadingView.setVisibility(View.GONE);
						showPromptDialog("Prompt", "Pairing failed, please try again");
					}
				}
			});
		}

		@Override
		public void onWifiPasswordConfigResults(LsDeviceInfo lsDevice,
				final boolean isSuccess, final int errorCode) 
		{
			if(getActivity()!=null)
			{
				getActivity().runOnUiThread(new Runnable() 
				{
					@Override
					public void run() {
						mListAdapter.clear();
						tempList.clear();
						scanResultsLayout.setVisibility(View.VISIBLE);
						loadingView.setVisibility(View.GONE);
						String msg="success to set device's wifi password ! ";
						if(!isSuccess)
						{
							msg="failed to set device's wifi password : "+errorCode;
						}
						showPromptDialog("Prompt", msg);						
					}
				});

			}
		}

		@Override
		public void onDeviceOperationCommandUpdate(String macAddress,
				OperationCommand cmd, Object obj) {
			Log.e("LS-BLE", "operation command update >> "+cmd+"; from device="+macAddress);
			if(OperationCommand.CMD_DEVICE_ID == cmd)
			{
				//input device id
				String deviceId=macAddress.replace(":", "");//"ff028a0003e1";
				//register device's id for device
				LsBleManager.getInstance().registeringDeviceID(macAddress, deviceId, DeviceRegisterState.NORMAL_UNREGISTER);
			}
			else if(OperationCommand.CMD_PAIRED_CONFIRM == cmd){
				PairedConfirmInfo confirmInfo=new PairedConfirmInfo(PairedConfirmState.PAIRING_SUCCESS);
				confirmInfo.setUserNumber(1);
				LsBleManager.getInstance().inputOperationCommand(macAddress, cmd, confirmInfo);
			}
			
		}

		@Override
		public void onDiscoverUserInfo(String macAddress,final List userList) 
		{
			if(getActivity()==null)
			{
				return ;
			}
			getActivity().runOnUiThread(new Runnable() {
				public void run() {
					if(userList==null || userList.size()==0)
					{
						Toast.makeText(getActivity(), "failed to pairing devie,user list is null...", Toast.LENGTH_LONG).show();
						return ;
					}
					mDeviceUserList=userList;
					showDeviceUserInfo(userList);
				}
			});
		}
		
		
		
	};
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) 
	{
		rootView=inflater.inflate(R.layout.activity_add_device, container, false);
		scanResultsLayout=rootView.findViewById(R.id.scan_results_list_view);
		scanResultListView=(ListView) scanResultsLayout.findViewById(android.R.id.list);
		loadingView=rootView.findViewById(R.id.loadingPanel);
		progressBarTextView=(TextView)rootView.findViewById(R.id.progress_bar_text_view);
		showScanFilterTextView=(TextView)rootView.findViewById(android.R.id.empty);
		setHasOptionsMenu(true);
		return rootView;
	}


	public SearchFragment(){}


	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		updateListViewHanlder = new Handler();
		mDeviceList=new ArrayList<LsDeviceInfo>();
		mListAdapter = new ScanResultsAdapter(getActivity(), (ArrayList<LsDeviceInfo>) mDeviceList);
		tempList=new ArrayList<LsDeviceInfo>();
	}



	@Override
	public void onStart() 
	{
		super.onStart();
		showScanFilterTextView.setText("");
		showScanFilterTextView.append(Html.fromHtml("<font color='red'>"+"Scan Filter"+"</font>")+"\r\n");
		showScanFilterTextView.append(Html.fromHtml("<font color='red'>"+"------------------------------"+"</font>")+"\r\n");
		showScanFilterTextView.append(getScanFilterMsg());
	}
	
	

	@Override
	public void onResume() {
		super.onResume();
		DeviceSettiingProfiles.initActivityContext(getActivity());
	}


	@Override
	public void onStop() 
	{
		super.onStop();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) 
	{
		inflater.inflate(R.menu.add_device, menu);
		searchMenuItem=menu.findItem(R.id.action_search_item);
		if(isScanning)
		{
			LsBleManager.getInstance().stopSearch();
			loadingView.setVisibility(View.GONE);
		}
		super.onCreateOptionsMenu(menu, inflater);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		int id = item.getItemId();
		if(id == R.id.fun_scan_filter){
			DeviceSettiingProfiles.showScanFilterSetting();
		}
		else if (id == R.id.action_search_item) 
		{
			showScanFilterTextView.setVisibility(View.GONE);
			if(!LsBleManager.getInstance().isSupportLowEnergy())
			{
				showPromptDialog("Prompt", "Not support Bluetooth Low Energy");
			}
			if(!LsBleManager.getInstance().isOpenBluetooth())
			{
				showPromptDialog("Prompt", "Please turn on Bluetooth");
			}
			else if(!checkLocationPermission()){
				String msg="Android 6.0 + requries access to device's location in order to scanning";
				showDialog("Permission required",msg,new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
				        ActivityCompat.requestPermissions(getActivity(),
				                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);					
					}
				});
			}
			else
			{
				hasScanResults=false;
				final View refreshView;
				//
				LayoutInflater inflater = (LayoutInflater)getActivity().getActionBar().getThemedContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				refreshView = inflater.inflate(R.layout.actionbar_search_progress, null);
				refreshView.setOnClickListener(new OnClickListener() 
				{
					@Override
					public void onClick(View v)
					{
						MenuItemCompat.setActionView(searchMenuItem, null);
						loadingView.setVisibility(View.GONE);

						LsBleManager.getInstance().stopSearch();
						isScanning=false;
						if(!hasScanResults)
						{
							showScanFilterTextView.setVisibility(View.VISIBLE);
						}
					}
				});

				MenuItemCompat.setActionView(searchMenuItem, refreshView);

				if(scanResultsLayout.getVisibility()==View.VISIBLE)
				{
					scanResultsLayout.setVisibility(View.GONE);
				}
				loadingView.setVisibility(View.VISIBLE);
				mListAdapter.clear();
				tempList.clear();
				isScanning=LsBleManager.getInstance().searchLsDevice(mSearchCallback, getDeviceTypes(), BroadcastType.ALL);
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * 检测系统的定位权限是否已打开或已授权
	 * @return
	 */
	private boolean checkLocationPermission() 
	{
		try{
			if(Build.VERSION.SDK_INT < 23){
				return true;
			}
			if (!(getActivity().getApplicationContext().checkSelfPermission
					(android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED))
			{
				return false;
			}
			else{
				return true;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return true;
		}
				
	}


	public static void setScanFilter(String filter)
	{
		Log.e("LS-BLE", "set scan filter >>>"+filter);
		scanFilterCondition=filter;
		if(!TextUtils.isEmpty(filter) && filter.equalsIgnoreCase("all")){
			scanFilterCondition=null;
		}
	}
	
	/**
	 * 初始化ScanResults layout and list view
	 *
	 */
	@SuppressWarnings("unchecked")
	private void initScanResultsListView()
	{
		scanResultsLayout.setVisibility(View.VISIBLE);
		scanResultListView.setVisibility(View.VISIBLE);
		scanResultListView.setAdapter(mListAdapter);
		scanResultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() 
		{
			@Override
			public void onItemClick(AdapterView<?> parent, final View view,
					int position, long id) 
			{
				MenuItemCompat.setActionView(searchMenuItem, null);
				LsBleManager.getInstance().stopSearch();
				isScanning=false;

				LsDeviceInfo device = (LsDeviceInfo) parent.getAdapter().getItem(position);
				if(device.getPairStatus()==1 
						|| (ProtocolType.A6.toString().equalsIgnoreCase(device.getProtocolType())
							&&  device.getRegisterStatus() ==0 ))
				{
					searchMenuItem.setEnabled(false);
					scanResultsLayout.setVisibility(View.GONE);
					progressBarTextView.setText("");
					progressBarTextView.setMovementMethod(new ScrollingMovementMethod());
					showBleConnectMsg("pairing device,please wait...");
					loadingView.setVisibility(View.VISIBLE);
				
					//set custom broacast Id to device,if need
					//setCustomBroadcastId();
					isPairingProcess=true;			
					currentPairingDevice=device;
					//set product user info on pairing mode
					setProductUserInfoOnPairingMode(device);
					
					//S9 互联秤，在绑定过程中添加用户信息
					WeightUserInfo userInfo=new WeightUserInfo();
					userInfo.setProductUserNumber(2);
					userInfo.setAge(30);
					userInfo.setHeight((float) 1.70);
					userInfo.setWeight(40);
					userInfo.setSex(SexType.MALE);
					userInfo.setAthlete(true);
					userInfo.setAthleteActivityLevel(3);
					//currentPairingDevice.setUserInfo(userInfo);
					//直接配对设备
					LsBleManager.getInstance().pairingWithDevice(currentPairingDevice, mPairCallback);
				}
				else
				{
					searchMenuItem.setEnabled(true);
					LsBleManager.getInstance().stopSearch();
					//for test to save 
					device.setDeviceId(device.getMacAddress().replace(":", ""));
					saveDeviceInfo(device);
				}
			}
		});
	}

	/**
	 * @param msg
	 */
	protected void showBleConnectMsg(final String msg) 
	{
		if(progressBarTextView!=null)
		{
			
			progressBarTextView.append("msg >> "+msg+"\n");
//			progressBarTextView.setTextSize(10);
		}
	}

	/**
	 * @param lsDevice
	 */
	protected void updateScanResults(final LsDeviceInfo lsDevice) 
	{
		if(lsDevice==null || getActivity()==null)
		{
			return ;
		}
		getActivity().runOnUiThread(new Runnable() {
			
			@SuppressWarnings("unchecked")
			@Override
			public void run() 
			{
				hasScanResults=true;
				if(loadingView.getVisibility()==View.VISIBLE)
				{
					System.err.println("is here=========");
					loadingView.setVisibility(View.GONE);
					initScanResultsListView();
				}
				if(!isDeviceExists(lsDevice.getDeviceName(),lsDevice.getMacAddress()))
				{
					Log.e("LS-BLE","scan results "+lsDevice.getDeviceName()+"["+lsDevice.getMacAddress()+"]");
					tempList.add(lsDevice);
					if(TextUtils.isEmpty(scanFilterCondition)){
						//no scan filter
						mListAdapter.add(lsDevice);
						mListAdapter.notifyDataSetChanged();
					}
					else{
						//use scan filter
						mListAdapter.addScanResults(lsDevice);
						mListAdapter.getScanFilter().filter(scanFilterCondition);
					}
				}
				else
				{
					updateListViewBackground(lsDevice);
				}
			}
		});
	}

	
	/**
	 * 显示提示对话框
	 * @param context
	 * @param title
	 * @param message
	 */
	private void showPromptDialog(String title, String message) 
	{
		ContextThemeWrapper ctw = new ContextThemeWrapper(getActivity(), android.R.style.Theme_Holo_Light);
		AlertDialog.Builder promptDialog= new AlertDialog.Builder(ctw)
		.setTitle(title)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				MenuItemCompat.setActionView(searchMenuItem, null);
				loadingView.setVisibility(View.GONE);
				searchMenuItem.setEnabled(true);
				showScanFilterTextView.setVisibility(View.VISIBLE);
			
			}

		})
		.setMessage(message);
		promptDialog.create().show();
	}
	
	private void showDialog(String title, String message,DialogInterface.OnClickListener listener)
	{
		ContextThemeWrapper ctw = new ContextThemeWrapper(getActivity(), android.R.style.Theme_Holo_Light);
		AlertDialog.Builder promptDialog= new AlertDialog.Builder(ctw)
		.setTitle(title)
		.setPositiveButton("OK", listener)
		.setMessage(message);
		promptDialog.create().show();
	}

	private boolean isDeviceExists(String name,String address) 
	{
		if(name==null || name.length()==0)
		{
			return false;
		}
		if(tempList!=null && tempList.size()>0)
		{
			for (int i = 0; i < tempList.size(); i++)
			{
				LsDeviceInfo tempDeInfo=tempList.get(i);
				if (tempDeInfo!=null && tempDeInfo.getDeviceName()!=null 
						&& tempDeInfo.getDeviceName().equals(name)) 
				{
					return true;
				}
			}
			return false;
		}
		else return false;
	}


	private void setListViewBackgroundColor(View view ,final int rssiValue) 
	{
		final TextView rssiView=(TextView)view;
		rssiView.setTextColor(Color.RED);
		rssiView.setText(rssiValue+"");
		updateListViewHanlder.postDelayed(new Runnable() {
			@Override
			public void run() {
				rssiView.setTextColor(Color.GRAY);
				rssiView.setAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_in_left));
			}
		}, 800);
	}

	@SuppressWarnings("unchecked")
	private void updateListViewBackground(LsDeviceInfo lsDevice) 
	{
		if (tempList == null || tempList.isEmpty()) 
		{
			return ;
		}
		for (LsDeviceInfo dev : tempList) 
		{
			String deviceName=lsDevice.getDeviceName();
			if (dev != null && dev.getDeviceName()!=null && dev.getDeviceName().equals(deviceName)) 
			{
				final View view = scanResultListView.getChildAt(mListAdapter.getPosition(dev));				
				if (view != null) 
				{
					TextView rssiView= (TextView) view.findViewById(R.id.info_image_view);					
					rssiView.clearAnimation();
					setListViewBackgroundColor(rssiView,lsDevice.getRssi());
				}
			}
		}
	}

	
	
	
	
	private void saveDeviceInfo(final LsDeviceInfo lsDevice)
	{
		AsyncTaskRunner runner = new AsyncTaskRunner(getActivity(),lsDevice);
		runner.execute();	
		
		ShowTextDialogFragment showInfoDialog=new ShowTextDialogFragment(lsDevice,new OnDialogClickListener()
		{
			@Override
			public void onDialogCancel() 
			{
				Bundle args = new Bundle();
				args.putParcelable("DeviceInfo", lsDevice);
				
				DeviceFragment showDeviceFragment=new DeviceFragment();
				showDeviceFragment.setArguments(args);
				FragmentManager fragmentManager = getFragmentManager();
				FragmentTransaction ft=fragmentManager.beginTransaction();
				ft.setCustomAnimations(R.anim.slide_in_left,R.anim.slide_out_right,0,0);
				ft.replace(R.id.frame_container, showDeviceFragment).commit();
				getActivity().getActionBar().setTitle("Show Device");
			}
		});
		showInfoDialog.show(getFragmentManager(), "show info");
	}
	
	private String getScanFilterMsg()
	{
		StringBuffer strBuffer=new StringBuffer();
		strBuffer.append("Broadcast Type:"+getBroadcastType().toString().toLowerCase()+"\r\n");
		String deviceTypes="all";
		if(getDeviceTypes().size()<6)
		{
			int i=0;
			for(DeviceType type:getDeviceTypes())
			{
				strBuffer.append("Device Type:  "+type.toString().toLowerCase()+"\r\n");
			}
		}
		else strBuffer.append(deviceTypes+"\r\n");
		return strBuffer.toString();
	}
	
	/**
	 * @param deviceTypes
	 * @return
	 */
	private List<DeviceType> parseDeviceTypes(Set<String> deviceTypes) 
	{
		List<DeviceType> list=new ArrayList<DeviceType>();
		for(String value:deviceTypes)
		{
			if(value.equals(DeviceType.FAT_SCALE.toString()))
			{
				list.add(DeviceType.FAT_SCALE);
			}
			else if(value.equals(DeviceType.HEIGHT_RULER.toString()))
			{
				list.add(DeviceType.HEIGHT_RULER);
			}
			else if(value.equals(DeviceType.KITCHEN_SCALE.toString()))
			{
				list.add(DeviceType.KITCHEN_SCALE);
			}
			else if(value.equals(DeviceType.PEDOMETER.toString()))
			{
				list.add(DeviceType.PEDOMETER);
			}
			else if(value.equals(DeviceType.SPHYGMOMANOMETER.toString()))
			{
				list.add(DeviceType.SPHYGMOMANOMETER);
			}
			else if(value.equals(DeviceType.WEIGHT_SCALE.toString()))
			{
				list.add(DeviceType.WEIGHT_SCALE);
			}
			System.err.println("my device type multi choose:"+value);
		}
		return list;
	}

	/**
	 * 获取当前设置的扫描设备类型
	 * @return
	 */
	private List<DeviceType> getDeviceTypes()
	{	
		PreferenceManager.setDefaultValues(getActivity(), R.xml.setting, false);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		Set<String> deviceTypes=prefs.getStringSet("device_type", null);
		if(deviceTypes==null || deviceTypes.size()==0)
		{
			//返回扫描所有的设备类型
			mScanDeviceType=new ArrayList<DeviceType>();
			mScanDeviceType.add(DeviceType.SPHYGMOMANOMETER);
			mScanDeviceType.add(DeviceType.FAT_SCALE);
			mScanDeviceType.add(DeviceType.WEIGHT_SCALE);
			mScanDeviceType.add(DeviceType.HEIGHT_RULER);
			mScanDeviceType.add(DeviceType.PEDOMETER);
			mScanDeviceType.add(DeviceType.KITCHEN_SCALE);
		}
		else
		{
			mScanDeviceType=parseDeviceTypes(deviceTypes);
		}
		return mScanDeviceType;
	}
	
	/**
	 * 获取当前设置的广播类型
	 * @return
	 */
	private BroadcastType getBroadcastType()
	{
		PreferenceManager.setDefaultValues(getActivity(), R.xml.setting, false);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		String broadcastType=prefs.getString("broadcast_type", null);
		if(broadcastType==null)
		{
			//返回默认的广播类型，ALL
			mBroadcastType=BroadcastType.ALL;
		}
		else
		{
			mBroadcastType=BroadcastType.ALL;
			if(Integer.valueOf(broadcastType)==1)
			{
				mBroadcastType=BroadcastType.PAIR;
			}
			else if(Integer.valueOf(broadcastType)==2)
			{
				mBroadcastType=BroadcastType.NORMAL;
			}
		}
		return mBroadcastType;
	}

	/**
	 * 获取当前网络的SSID
	 * @return
	 */
	private String getWifiNetworkSSID()
	{
		String ssid=null;
		WifiManager wifiManager = (WifiManager)getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo=wifiManager.getConnectionInfo();
		if (wifiInfo!=null && wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) 
		{
			ssid= wifiInfo.getSSID();
		}
		
		if(ssid==null || ssid.length()==0)
		{
			ssid="no network";
		}
		else
		{
			ssid=ssid.replace('"', '%').replace("%", "");
		}
		return ssid;
	}
	
	/**
	 * set product user info on pairing mode
	 * @param device
	 */
	private void setProductUserInfoOnPairingMode(LsDeviceInfo lsDevice) 
	{
		if(lsDevice==null)
		{
			return ;
		}
		/**
	      * in some old products, such as A2, A3, 
	      * you can change the device's settings info before pairing  
		  */
		if(DeviceTypeConstants.FAT_SCALE.equalsIgnoreCase(lsDevice.getDeviceType())
	 	||DeviceTypeConstants.WEIGHT_SCALE.equalsIgnoreCase(lsDevice.getDeviceType()))
		{
		    /**
			  * optional step,set product user info in pairing mode
			  */
			WeightUserInfo weightUserInfo=new WeightUserInfo();
		    weightUserInfo.setAge(34);					//user age
			weightUserInfo.setHeight((float) 1.78); 	//unit of measurement is m
		    weightUserInfo.setGoalWeight(78);			//unit of measurement is kg
			weightUserInfo.setUnit(UnitType.UNIT_KG);	//measurement unit
			weightUserInfo.setSex(SexType.FEMALE);		//user gender
			weightUserInfo.setAthlete(true);			//it is an athlete 		   
			weightUserInfo.setAthleteActivityLevel(3);	//athlete level
			//set device macAddress
			weightUserInfo.setMacAddress(lsDevice.getMacAddress());
			//calling interface
			LsBleManager.getInstance().setProductUserInfo(weightUserInfo);
		 }
	}
	
	@SuppressWarnings("null")
	private void showDeviceUserInfo(List<DeviceUserInfo> userList) 
	{
		ContextThemeWrapper ctw = new ContextThemeWrapper(getActivity(), android.R.style.Theme_Holo_Light);
		// Strings to Show In Dialog with Radio Buttons
		final CharSequence[] items ;
		if(userList!=null && userList.size()>0)
		{
			items=new String[userList.size()];
			for(DeviceUserInfo  user:userList)
			{
				if(user!=null)
				{
					user.getDeviceId();
					int index=userList.indexOf(user);
					if(user.getUserName().isEmpty())
					{
						items[index]=user.getUserNumber()+"   "+"unknow";
					}
					else 
					{
						items[index]=user.getUserNumber()+"   "+user.getUserName();
					}
				}	
			}
		}
		else items=new String[2];
		// Creating and Building the Dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(ctw);
		builder.setTitle("Select User Number");
		builder.setSingleChoiceItems(items, -1,new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog, int which) 
			{
				//不通过解除绑定，直接更换设备的用户
				userListDialog.dismiss();
				showInputDialog(which+1);
			}
		});
		userListDialog = builder.create();
		userListDialog.show();
	}
	
	private void showInputDialog(final int userNumber)
	{
		ContextThemeWrapper ctw = new ContextThemeWrapper(getActivity(), android.R.style.Theme_Holo_Light);
		AlertDialog.Builder builder = new AlertDialog.Builder(ctw);
		builder.setTitle("Enter your name(Less than 16 characters)");
		// Set up the input
		final EditText input = new EditText(ctw);
		// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		builder.setView(input);
		// Set up the buttons
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				String userName=input.getText().toString();
				if(userName!=null && userName.length()<16)
				{
					LsBleManager.getInstance().bindDeviceUser(currentPairingDevice.getMacAddress(),userNumber, userName.trim());
				}
				else
				{
					dialog.cancel();
					Toast.makeText(getActivity(), "Error,entering invalid...", Toast.LENGTH_LONG).show();
				}

			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				showDeviceUserInfo(mDeviceUserList);
			}
		});
		builder.create();
		builder.show();
	}

}
