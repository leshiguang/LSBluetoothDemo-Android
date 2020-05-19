/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bluetooth.demo.ui.adapter;

import com.lifesense.ble.bean.LsDeviceInfo;
import com.lifesense.ble.bean.constant.DeviceConnectState;

/**
 * This custom object is used to populate the list adapter. It contains a reference
 * to an image, title, and the extra text to be displayed. Furthermore, it keeps track
 * of the current state (collapsed/expanded) of the corresponding item in the list,
 * as well as store the height of the cell in its collapsed state.
 */
public class PairedDeviceListItem  implements OnSizeChangedListener{

	private LsDeviceInfo mDeviceInfo;
    private boolean mIsExpanded;
    private int mImgResource;
    private int mCollapsedHeight;
    private int mExpandedHeight;
    private int recordCount; 
    private DeviceConnectState connectState;
    

    public PairedDeviceListItem(LsDeviceInfo deviceInfo, int collapsedHeight, int count) {

    	mDeviceInfo=deviceInfo;
        mCollapsedHeight = collapsedHeight;
        mIsExpanded = false;
        mExpandedHeight = -1;
        recordCount=count;
    }

    

    public LsDeviceInfo getDeviceInfo() {
		return mDeviceInfo;
	}



	public void setDeviceInfo(LsDeviceInfo mDeviceInfo) {
		this.mDeviceInfo = mDeviceInfo;
	}



	public boolean isExpanded() {
		return mIsExpanded;
	}



	public void setExpanded(boolean mIsExpanded) {
		this.mIsExpanded = mIsExpanded;
	}



	public int getImgResource() {
		return mImgResource;
	}



	public void setImgResource(int mImgResource) {
		this.mImgResource = mImgResource;
	}



	public int getCollapsedHeight() {
		return mCollapsedHeight;
	}



	public void setCollapsedHeight(int mCollapsedHeight) {
		this.mCollapsedHeight = mCollapsedHeight;
	}



	public int getExpandedHeight() {
		return mExpandedHeight;
	}



	public void setExpandedHeight(int mExpandedHeight) {
		this.mExpandedHeight = mExpandedHeight;
	}


	





	public int getRecordCount() {
		return recordCount;
	}



	public void setRecordCount(int recordCount) {
		this.recordCount = recordCount;
	}



	@Override
    public void onSizeChanged(int newHeight) {
        setExpandedHeight(newHeight);
    }



	/**
	 * @return the connectState
	 */
	public DeviceConnectState getConnectState() {
		return connectState;
	}



	/**
	 * @param connectState the connectState to set
	 */
	public void setConnectState(DeviceConnectState connectState) {
		this.connectState = connectState;
	}
	
}
