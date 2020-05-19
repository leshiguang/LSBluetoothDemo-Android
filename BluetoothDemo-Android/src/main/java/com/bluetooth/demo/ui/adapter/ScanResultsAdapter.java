/**
 * 
 */
package com.bluetooth.demo.ui.adapter;

import java.util.ArrayList;
import java.util.List;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bluetooth.demo.R;
import com.lifesense.ble.bean.LsDeviceInfo;
import com.lifesense.ble.bean.constant.DeviceTypeConstants;

/**
 * @author CaiChiXiang
 *
 */
@SuppressLint("DefaultLocale")
public class ScanResultsAdapter extends ArrayAdapter{

	private ArrayList<LsDeviceInfo> modelsArrayList;
	private ArrayList<LsDeviceInfo> scanResutls;
	
	private Context context;

	@SuppressWarnings("unchecked")
	public ScanResultsAdapter(Context context, ArrayList<LsDeviceInfo> modelsArrayList) {

		super(context, R.layout.scan_list_item, modelsArrayList);

		this.context = context;
		this.modelsArrayList = modelsArrayList;
		this.scanResutls=new ArrayList<LsDeviceInfo>();
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		// 1. Create inflater
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// 2. Get rowView from inflater
		View rowView=null;
		if(modelsArrayList.size()!=0)
		{
			rowView =inflater.inflate(R.layout.scan_list_item, parent, false);
			TextView nameView = (TextView) rowView.findViewById(R.id.name);
			TextView addressView=(TextView) rowView.findViewById(R.id.address);
			TextView infoImageView= (TextView) rowView.findViewById(R.id.info_image_view);
			TextView pairingTextView=(TextView) rowView.findViewById(R.id.pairing_text_view);
			ImageView deviceImage=(ImageView) rowView.findViewById(R.id.unpair_device_image_view);
			TextView serviceTextView=(TextView) rowView.findViewById(R.id.services);

			infoImageView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					LsDeviceInfo deInfo=modelsArrayList.get(position);
				}
			});

			String sensorName=modelsArrayList.get(position).getDeviceName();
			String macAddress=modelsArrayList.get(position).getMacAddress();

			String deviceType=modelsArrayList.get(position).getDeviceType();

			deviceImage.setImageResource(getDeviceImage(deviceType));

			nameView.setText("Device : "+sensorName);
			addressView.setText("Address : "+macAddress);

			String broadcastId=modelsArrayList.get(position).getBroadcastID();
			List<String> services=modelsArrayList.get(position).getServiceUuid();

			serviceTextView.setText("Service :"+getServiceTitle(services));
			if(modelsArrayList.get(position).getPairStatus()==1)
			{
				infoImageView.setVisibility(View.GONE);
				pairingTextView.setBackgroundColor(Color.RED);
				pairingTextView.setVisibility(View.VISIBLE);
			}
			if(modelsArrayList.get(position).getPairStatus()==0)
			{
				infoImageView.setTextColor(Color.RED);
				infoImageView.setText(modelsArrayList.get(position).getRssi()+"");
			}
			else
			{
				infoImageView.setTextColor(Color.DKGRAY);
				infoImageView.setText(modelsArrayList.get(position).getRssi()+"");
			}


		}

		// 5. retrn rowView
		return rowView;
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

	/**
	 * @param services
	 * @return
	 */
	private String getServiceTitle(List<String> services) 
	{
		if(services!=null)
		{
			StringBuffer stringBuffer=new StringBuffer();
			for(String str:services)
			{
				stringBuffer.append(" ");
				stringBuffer.append(str);
				stringBuffer.append(",");
			}
			String title=stringBuffer.toString();
			if(title.endsWith(","))
			{
				title=title.substring(0,title.lastIndexOf(","));
			}
			return title;
		}
		else return "null";
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

	public Filter getScanFilter(){
		return scanFilter;
	}
	
	private Filter scanFilter=new Filter() {
		
		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			if(results==null || results.values==null){
				return ;
			}
			Log.e("LS-BLE", "publishResults >> "+results.values);
			for(LsDeviceInfo lsDevice:(ArrayList<LsDeviceInfo>) results.values){
				add(lsDevice);
			}
			notifyDataSetChanged();		
		}
		
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			Log.e("LS-BLE", "filtering with >> "+constraint);
			FilterResults results = new FilterResults();
			ArrayList<LsDeviceInfo> filters = new ArrayList<LsDeviceInfo>();
			// El prefijo tiene que ser mayor que 0 y existir
			if (constraint != null && constraint.toString().length() > 0) 
			{
				String key=constraint.toString().toUpperCase();
				for (int index = 0; index < scanResutls.size(); index++) {
					LsDeviceInfo device = scanResutls.get(index);
					if(checkScanFilter(key, device)){
						filters.add(device);
					}
				}
				results.values = filters;
				results.count = filters.size();
			} else {
				synchronized (scanResutls) {
					results.values = scanResutls;
					results.count = scanResutls.size();
				}
			}
			return results;
		}
	};
	
	public void addScanResults(LsDeviceInfo device)
	{
		scanResutls.clear();
		if(device!=null){
			scanResutls.add(device);
		}
	}
	
	/**
	 * 检测扫描过滤条件
	 * @param filter
	 * @param device
	 * @return
	 */
	private boolean checkScanFilter(String filter,LsDeviceInfo device)
	{
		if(filter==null){
			return true;
		}
		if(device==null){
			return false;
		}
		String key=filter.toUpperCase();
		if(key.startsWith("-")){
			//rssi filter
			int rssi=device.getRssi();
			if(rssi >= Integer.parseInt(key)){
				return true;
			}
			else{
				return false;
			}
		}
		else{
			//device name filter
			String name = device.getDeviceName().toUpperCase();
			if(name.startsWith(key) 
					|| name.contains(key) 
					|| name.equalsIgnoreCase(key) 
					|| name.endsWith(key)){
				return true;
			}
			else{
				return false;
			}
		}
	}

}
