/**
 * 
 */
package com.bluetooth.demo.ui.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bluetooth.demo.R;
import com.lifesense.ble.LsBleManager;
import com.lifesense.ble.bean.LsDeviceInfo;
import com.lifesense.ble.bean.constant.DeviceConnectState;
import com.lifesense.ble.bean.constant.DeviceTypeConstants;
import com.lifesense.ble.bean.constant.ManagerStatus;
import com.lifesense.ble.bean.constant.ProtocolType;



/**
 * @author CaiChiXiang
 *
 */
public class PairedDeviceAdapter extends ArrayAdapter<PairedDeviceListItem>{

	private List<PairedDeviceListItem> mData;
	private int mLayoutViewResourceId;
	private PairedDeviceListItem currentSelectItem;
	private Context appContext;
	private boolean isConnected;

	public PairedDeviceAdapter(Context context, int layoutViewResourceId,
			List<PairedDeviceListItem> data) {
		super(context, layoutViewResourceId, data);
		this.appContext=context;
		mData = data;
		mLayoutViewResourceId = layoutViewResourceId;
		isConnected=false;

	}

	/**
	 * Populates the item in the listview cell with the appropriate data. This method
	 * sets the thumbnail image, the title and the extra text. This method also updates
	 * the layout parameters of the item's view so that the image and title are centered
	 * in the bounds of the collapsed view, and such that the extra text is not displayed
	 * in the collapsed state of the cell.
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		currentSelectItem = mData.get(position);
		final LsDeviceInfo pairedDevice=currentSelectItem.getDeviceInfo();
		
		if(convertView == null) 
		{
			LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
			convertView = inflater.inflate(mLayoutViewResourceId, parent, false);
		}

		LinearLayout linearLayout = (LinearLayout)(convertView.findViewById(R.id.item_linear_layout));
		LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams
				(AbsListView.LayoutParams.MATCH_PARENT,AbsListView.LayoutParams.WRAP_CONTENT);//
		linearLayout.setLayoutParams(linearLayoutParams);


		ImageView imgView = (ImageView)convertView.findViewById(R.id.device_image_view);
		TextView protocolTypeView = (TextView)convertView.findViewById(R.id.protocol_type_text_view);
		TextView deviceNameView = (TextView)convertView.findViewById(R.id.device_name_text_view);
		TextView userNumberView = (TextView)convertView.findViewById(R.id.user_number_text_view);
		TextView macAddressView= (TextView)convertView.findViewById(R.id.device_mac_text_view);
		ProgressBar connectingProgressBar=(ProgressBar)convertView.findViewById(R.id.connecting_progress_bar);
		TextView newDataTextView=(TextView)convertView.findViewById(R.id.new_data_text_view);
		newDataTextView.setVisibility(View.GONE);
		connectingProgressBar.setVisibility(View.GONE);
		String protocolStr=pairedDevice.getProtocolType();
		if(protocolStr.startsWith("WECHAT")){
			protocolStr="wechat";
		}
		protocolTypeView.setText(getProtocolName(protocolStr));

		if("GENERIC_FAT".equals(pairedDevice.getProtocolType())
				||"A4".equals(pairedDevice.getProtocolType())
				||"KITCHEN_PROTOCOL".equals(pairedDevice.getProtocolType()))
		{
			deviceNameView.setText(pairedDevice.getDeviceName());
		}
		else
		{
			deviceNameView.setText(pairedDevice.getDeviceName()+":"+pairedDevice.getBroadcastID());
		}

		userNumberView.setText("UserNumber:"+pairedDevice.getDeviceUserNumber());
		DeviceConnectState connectState=LsBleManager.getInstance().checkDeviceConnectState(pairedDevice.getBroadcastID());
		String stateStr="Mac: ["+pairedDevice.getMacAddress()+"]";
		macAddressView.setTextColor(Color.BLACK);
		if(connectState==DeviceConnectState.CONNECTED_SUCCESS 
				&& LsBleManager.getInstance().getLsBleManagerStatus()==ManagerStatus.DATA_RECEIVE)
		{
			isConnected=true;
			stateStr="Connect Success";
			macAddressView.setTextColor(Color.parseColor("#006400"));
			connectingProgressBar.setVisibility(View.GONE);
		}
		else if(LsBleManager.getInstance().getLsBleManagerStatus()==ManagerStatus.DATA_RECEIVE)
		{
			connectingProgressBar.setVisibility(View.VISIBLE);
			if(isConnected)
			{
				isConnected=false;
				stateStr="Disconnect";
				macAddressView.setTextColor(Color.RED);
			}
		}
		//设置连接状态
		macAddressView.setText(stateStr);
		String deviceType=pairedDevice.getDeviceType();
		int imageResource=getDeviceImage(deviceType);
		imgView.setImageBitmap(getCroppedBitmap(BitmapFactory.decodeResource(getContext()
				.getResources(),imageResource, null)));

		convertView.setLayoutParams(new ListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,
				AbsListView.LayoutParams.WRAP_CONTENT));
        
		return convertView;
	}

	/**
	 * Crops a circle out of the thumbnail photo.
	 */
	public Bitmap getCroppedBitmap(Bitmap bitmap) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
				Config.ARGB_8888);

		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

		Canvas canvas = new Canvas(output);

		final Paint paint = new Paint();
		paint.setAntiAlias(true);

		int halfWidth = bitmap.getWidth()/2;
		int halfHeight = bitmap.getHeight()/2;

		canvas.drawCircle(halfWidth, halfHeight, Math.max(halfWidth, halfHeight), paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));

		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}

	private String getProtocolName(String protocolStr)
	{
		if(ProtocolType.WECHAT_CALL_PEDOMETER.toString().equals(protocolStr)
				||ProtocolType.WECHAT_PEDOMETER.toString().equals(protocolStr)
				||ProtocolType.WECHAT_WEIGHT_SCALE.toString().equals(protocolStr))
		{
			protocolStr="Wechat";
		}
		else if(ProtocolType.GENERIC_FAT.toString().equals(protocolStr))
		{
			protocolStr="78A2";
		}
		else if(ProtocolType.KITCHEN_PROTOCOL.toString().equals(protocolStr))
		{
			protocolStr="Kitchen";
		}
		else if(ProtocolType.BLOOD_PRESSURE_COMMAND_START_PROTOCOL.toString().equals(protocolStr))
		{
			protocolStr="78E9";
		}

		return protocolStr;
	}

	private int getDeviceImage(String deviceType)
	{
		int imageResource=android.R.drawable.ic_menu_help;//default device image

		if(DeviceTypeConstants.FAT_SCALE.equals(deviceType))
		{
			imageResource=R.drawable.ic_device_at_scale;
		}
		else if(DeviceTypeConstants.PEDOMETER.equals(deviceType))
		{
			imageResource=R.drawable.ic_device_pedometer;
		}
		else if(DeviceTypeConstants.HEIGHT_RULER.equals(deviceType))
		{
			imageResource=R.drawable.ic_device_height;
		}
		else if(DeviceTypeConstants.SPHYGMOMAN_METER.equals(deviceType))
		{
			imageResource=R.drawable.ic_device_blood_pressure;
		}
		else if(DeviceTypeConstants.KITCHEN_SCALE.equals(deviceType))
		{
			imageResource=R.drawable.ic_device_kitchen_scale;
		}
		else if(DeviceTypeConstants.WEIGHT_SCALE.equals(deviceType))
		{
			imageResource=R.drawable.ic_device_weight_scale;
		}
		return imageResource;
	}



}
