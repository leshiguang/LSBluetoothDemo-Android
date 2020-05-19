/**
 * 
 */
package com.bluetooth.demo.ui;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bluetooth.demo.R;
import com.bluetooth.demo.R.anim;
import com.bluetooth.demo.R.id;
import com.bluetooth.demo.R.layout;
import com.bluetooth.demo.R.menu;
import com.bluetooth.demo.R.string;
import com.bluetooth.demo.database.AsyncTaskRunner;
import com.bluetooth.demo.database.SettingInfoManager;
import com.bluetooth.demo.ui.adapter.PairedDeviceAdapter;
import com.bluetooth.demo.ui.adapter.PairedDeviceListItem;
import com.bluetooth.demo.utils.ApplicationProfiles;
import com.bluetooth.demo.utils.DeviceDataUtils;
import com.lifesense.ble.LsBleManager;
import com.lifesense.ble.ReceiveDataCallback;
import com.lifesense.ble.bean.BloodPressureData;
import com.lifesense.ble.bean.KitchenScaleData;
import com.lifesense.ble.bean.LsDeviceInfo;
import com.lifesense.ble.bean.PedometerData;
import com.lifesense.ble.bean.WeightData_A2;
import com.lifesense.ble.bean.WeightData_A3;
import com.lifesense.ble.bean.WeightUserInfo;
import com.lifesense.ble.bean.constant.DeviceConnectState;
import com.lifesense.ble.bean.constant.DeviceTypeConstants;
import com.lifesense.ble.bean.constant.ManagerStatus;
import com.lifesense.ble.bean.constant.SexType;
import com.lifesense.ble.bean.constant.UnitType;

/**
 * @author CaiChiXiang
 *
 */
@SuppressLint({ "RtlHardcoded", "DefaultLocale" })
public class PairedDevicesFragment extends Fragment {

	private final int CELL_DEFAULT_HEIGHT = 200;

	private List<PairedDeviceListItem> mDeviceListItems;
	private List<LsDeviceInfo> myDeviceList;
	private PairedDeviceAdapter deviceAdapter;
	private ListView mListView;
	private View fragmentRootView;
	private TextView emptyTextView;
	private Switch autoSyncDataSwitch;
	private boolean isConnectedDevice;
	private Handler mainHandler;
	private TextView mAppVersionTextView;
	
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
			updateDeviceConnectState(broadcastId,connectState);
		}

		@Override
		public void onReceiveWeightData_A3(WeightData_A3 wData) 
		{
			/**
			 * Weight Scale Measurement Data
			 * A3 product
			 */
			parseMeasureData(wData.getBroadcastId(),wData);
		}

		@Override
		public void onReceiveUserInfo(WeightUserInfo proUserInfo) 
		{
			/**
			 * Weight Scale Product User Info
			 * A3 product
			 */
			parseMeasureData(proUserInfo.getMacAddress(),proUserInfo);
		}
		@Override
		public void onReceiveWeightDta_A2(WeightData_A2 wData) 
		{
			// TODO Auto-generated method stub
			parseMeasureData(wData.getBroadcastId(),wData);
		}

		@Override
		public void onReceiveBloodPressureData(BloodPressureData bpData) 
		{
			parseMeasureData(bpData.getBroadcastId(),bpData);
		}

		@Override
		public void onReceivePedometerData(PedometerData pData) 
		{
			parseMeasureData(pData.getBroadcastId(),pData);
		}

		@Override
		public void onReceiveKitchenScaleData(KitchenScaleData kiScaleData) 
		{
			// TODO Auto-generated method stub
			parseMeasureData(kiScaleData.getDeviceId(),kiScaleData);

		}

		@Override
		public void onReceiveDeviceInfo(LsDeviceInfo lsDevice) 
		{
			if(lsDevice==null || getActivity() == null)
			{
				return ;
			}
			Log.e("LS-BLE", "Demo-Update Device Info:"+lsDevice.toString());
			//update and save device information
			AsyncTaskRunner runner = new AsyncTaskRunner(getActivity(),lsDevice);
			runner.execute();
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) 
	{
		fragmentRootView=inflater.inflate(R.layout.activity_main, container, false);
		autoSyncDataSwitch=(Switch) fragmentRootView.findViewById(R.id.auto_sync_data_switch);
		mListView =(ListView)fragmentRootView.findViewById(R.id.paired_device_list_view);
		emptyTextView=(TextView)fragmentRootView.findViewById(R.id.no_device_text_view);
		mAppVersionTextView=(TextView)fragmentRootView.findViewById(R.id.app_version_text_view);
		setHasOptionsMenu(true);
		//get app version name
		mAppVersionTextView.setText(ApplicationProfiles.getAppVersion(getActivity().getApplicationContext()));
		return fragmentRootView;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		mainHandler=new Handler(getActivity().getMainLooper());
		mDeviceListItems = new ArrayList<PairedDeviceListItem>();
		myDeviceList=SettingInfoManager.getPairedDeviceInfo(getActivity().getApplicationContext());
		if(myDeviceList!=null && myDeviceList.size()>0)
		{
			for(LsDeviceInfo device:myDeviceList)
			{

				PairedDeviceListItem deviceItem=new PairedDeviceListItem(device, CELL_DEFAULT_HEIGHT, 0);
				mDeviceListItems.add(deviceItem);
			}
		}
		deviceAdapter = new PairedDeviceAdapter(getActivity(), R.layout.device_list_view_item, mDeviceListItems);

	}

	@Override
	public void onStart() 
	{
		super.onStart();
		if(LsBleManager.getInstance().getLsBleManagerStatus() ==ManagerStatus.DATA_RECEIVE)
		{
			return ;
		}
		mListView.setAdapter(deviceAdapter);
		mListView.setDivider(null);
		//初始化list view 的点击事件
		initListViewSelectAction();
		if(mDeviceListItems==null || mDeviceListItems.size()==0)
		{
			mListView.setVisibility(View.GONE);
			emptyTextView.setVisibility(View.VISIBLE);
		}
		//init
		initSwitchAction();
	}

	@Override
	public void onResume() 
	{
		super.onResume();
		
	}


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.main, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_add_device_item) 
		{
			SearchFragment searchFragment=new SearchFragment();
//			TestFragment testFragment=new TestFragment();
			FragmentManager fragmentManager = getFragmentManager();
			FragmentTransaction ft=fragmentManager.beginTransaction();
			ft.setCustomAnimations(R.anim.slide_in_left,R.anim.slide_out_right,0,0);
			ft.replace(R.id.frame_container, searchFragment).commit();	
			getActivity().getActionBar().setTitle("Search Device");
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * 初始化list view的选择事件
	 */
	private void initListViewSelectAction() 
	{
		mListView.setOnItemClickListener(new OnItemClickListener() 
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) 
			{
				PairedDeviceListItem tempItem=(PairedDeviceListItem) parent.getAdapter().getItem(position);
				Bundle args = new Bundle();
				args.putParcelable("DeviceInfo", tempItem.getDeviceInfo());
				
				DeviceFragment showDeviceFragment=new DeviceFragment();
				showDeviceFragment.setArguments(args);
				FragmentManager fragmentManager = getFragmentManager();
				FragmentTransaction ft=fragmentManager.beginTransaction();
				ft.setCustomAnimations(R.anim.slide_in_left,R.anim.slide_out_right,0,0);
				ft.replace(R.id.frame_container, showDeviceFragment).commit();	
				getActivity().getActionBar().setTitle("Show Device");
			}

		});
		mListView.setOnItemLongClickListener(new OnItemLongClickListener() 
		{

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) 
			{
				final PairedDeviceListItem tempItem=(PairedDeviceListItem) parent.getAdapter().getItem(position);
				String delBroadcastId=tempItem.getDeviceInfo().getBroadcastID();
				String title=tempItem.getDeviceInfo().getDeviceName()+tempItem.getDeviceInfo().getBroadcastID();
				String msg=getResources().getString(R.string.message_delete_prompt);
				showPromptDialog(title, msg,delBroadcastId,tempItem);
				return true;
			}

		});
	}

	private void initSwitchAction()
	{
		// set the switch to ON
		autoSyncDataSwitch.setChecked(false);
		// attach a listener to check for changes in state
		autoSyncDataSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() 
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView,final boolean isChecked) 
			{
				if(!LsBleManager.getInstance().isOpenBluetooth())
				{
					Toast.makeText(getActivity(), "failed to sync measuring data,bluetooth unavailable...", Toast.LENGTH_SHORT).show();
					return ;
				}
				if(!LsBleManager.getInstance().isSupportLowEnergy())
				{
					Toast.makeText(getActivity(), "failed to sync measuring data,unsupport...", Toast.LENGTH_SHORT).show();
					return ;
				}
				
				mainHandler.post(new Runnable() 
				{
					@Override
					public void run() {

						if (isChecked) 
						{
							boolean isSuccess=setMeasureDevice();
							if(isSuccess)
							{		
								deviceAdapter.notifyDataSetChanged(); 	
								isConnectedDevice=false;
								//set measure device
								setMeasureDevice();
								LsBleManager.getInstance().startDataReceiveService(mDataCallback);
								Toast.makeText(getActivity(), "start auto syncing measurement data", Toast.LENGTH_SHORT).show();
							}
							else
							{
								autoSyncDataSwitch.setChecked(false);
								Toast.makeText(getActivity(), "failed to syncing measurement data,no devices...", Toast.LENGTH_SHORT).show();
							}
						} 
						else 
						{
							LsBleManager.getInstance().stopDataReceiveService();
							Toast.makeText(getActivity(), "stop auto syncing measurement data", Toast.LENGTH_SHORT).show();
							deviceAdapter.notifyDataSetChanged(); 	
						}
					}
				});
			}
		});
	}
	
	

	/**
	 * 显示提示对话框
	 * @param context
	 * @param title
	 * @param message
	 */
	private void showPromptDialog(String title, String message,final String delBroadcastId,final PairedDeviceListItem delItem) 
	{
		ContextThemeWrapper ctw = new ContextThemeWrapper(getActivity(), android.R.style.Theme_Holo_Light);
		AlertDialog.Builder promptDialog= new AlertDialog.Builder(ctw)
		.setTitle(title)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				//断开连接
				LsBleManager.getInstance().deleteMeasureDevice(delBroadcastId);
				//更新本地保存的数据库
				boolean delete=SettingInfoManager.deletePairedDeviceInfo(getActivity(), delBroadcastId);
				if(delete)
				{
					//重置当前的设备列表
					myDeviceList=SettingInfoManager.getPairedDeviceInfo(getActivity().getApplicationContext());
					mDeviceListItems.remove(delItem);
					deviceAdapter.notifyDataSetChanged();
					Toast.makeText(getActivity(), "Deleted successfully!", Toast.LENGTH_SHORT).show();
					if(mDeviceListItems==null || mDeviceListItems.size()==0)
					{
						mListView.setVisibility(View.GONE);
						emptyTextView.setVisibility(View.VISIBLE);
					}
				}
			}
		})
		.setNegativeButton("Cancel", new  DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		})
		.setMessage(message);
		promptDialog.create().show();
	}

	/**
	 * set measurement device
	 */
	protected boolean setMeasureDevice() 
	{
		if(myDeviceList==null || myDeviceList.size()==0)
		{
			return false;
		}
		//clear old device
		LsBleManager.getInstance().setMeasureDevice(null);
		
		for(LsDeviceInfo device:myDeviceList)
		{
			System.err.println("add measure device:"+device.toString());
			LsBleManager.getInstance().addMeasureDevice(device);
			//set product user info on data syncing mode
			setProductUserInfoOnSyncingMode(device);
		}
		return true;
	}
	
	/**
	 * 解析测量数据
	 * @param obj
	 */
	private void parseMeasureData(final String broadcastID,final Object obj)
	{
		if(obj==null)
		{
			return ;
		}
		mainHandler.post(new Runnable() 
		{	
			@Override
			public void run() 
			{
				Log.e("LS-Demo", "unhandle measure data:"+obj.toString());
				//save caching data
				DeviceDataUtils.addDeviceMeasurementData(broadcastID, obj);
				
				//update new data tips
				List<Object> newDatas=DeviceDataUtils.getDeviceMeasurementData(broadcastID);
				updateNewMeasurementDataNotify(broadcastID, newDatas.size());
			}
		});
	};
	
	/**
	 * 更新测量数据提示信息
	 * @param broadcastID
	 * @param newDataCount
	 */
	private void updateNewMeasurementDataNotify(final String broadcastID,final int newDataCount)
	{
		if(mDeviceListItems==null || mDeviceListItems.size()==0)
		{
			return ;
		}
		mainHandler.post(new Runnable() 
		{
			
			@Override
			public void run() 
			{
				for (PairedDeviceListItem item : mDeviceListItems) 
				{
					LsDeviceInfo dev=item.getDeviceInfo();
					if (dev != null && dev.getBroadcastID().equalsIgnoreCase(broadcastID)) 
					{
						final View view = mListView.getChildAt(deviceAdapter.getPosition(item));
						TextView newDataView = (TextView) view.findViewById(R.id.new_data_text_view);
						newDataView.setVisibility(View.VISIBLE);
						newDataView.setText(newDataCount+" New Data");
					}
				}
			}
		});
	}
	
	/**
	 * 更新设备的连接状态
	 * @param connectState
	 */
	private void updateDeviceConnectState(final String broadcastID,final DeviceConnectState connectState) 
	{
		if(mDeviceListItems==null || mDeviceListItems.size()==0)
		{
			return ;
		}
		mainHandler.post(new Runnable() 
		{
			@Override
			public void run() 
			{
				for (PairedDeviceListItem item : mDeviceListItems) 
				{
					LsDeviceInfo dev=item.getDeviceInfo();
					if (dev != null && dev.getBroadcastID().equalsIgnoreCase(broadcastID)) 
					{
						final View view = mListView.getChildAt(deviceAdapter.getPosition(item));
						TextView stateTextView = (TextView) view.findViewById(R.id.device_mac_text_view);
						ProgressBar connectingProgressBar=(ProgressBar) view.findViewById(R.id.connecting_progress_bar);
						connectingProgressBar.setVisibility(View.GONE);
						String stateStr="Mac: ["+dev.getMacAddress()+"]";
						stateTextView.setTextColor(Color.BLACK);
						if(connectState==DeviceConnectState.CONNECTED_SUCCESS 
								&& LsBleManager.getInstance().getLsBleManagerStatus()==ManagerStatus.DATA_RECEIVE)
						{
							stateStr="Connect Success";
							stateTextView.setTextColor(Color.parseColor("#006400"));
							connectingProgressBar.setVisibility(View.GONE);
							stateTextView.setText(stateStr);
						}
						else if(LsBleManager.getInstance().getLsBleManagerStatus()==ManagerStatus.DATA_RECEIVE)
						{
							stateTextView.setText("Disconnect");
							stateTextView.setTextColor(Color.RED);
							connectingProgressBar.setVisibility(View.VISIBLE);
						}
						else
						{
							stateTextView.setText(stateStr);
						}
					}
				}				
			}
		});
	}
	
	/**
	 * set product user info on data syncing mode
	 * @param device
	 */
	private void setProductUserInfoOnSyncingMode(LsDeviceInfo lsDevice) 
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
}
