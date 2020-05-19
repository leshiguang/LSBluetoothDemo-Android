/**
 * 
 */
package com.bluetooth.demo.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.text.TextUtils;

import com.lifesense.ble.bean.LsDeviceInfo;

/**
 * @author sky
 *
 */
public class FileUtils {

	private static final String UPGRADE_FILE_PATH="Lifesense/files";
	
	/**
	 * 将应用内置的升级文件保存到手机的根目录，或SD卡中
	 * @param upgradeFile
	 */
	public static File saveUpgradeFile(Context context,String fileName)
	{
		if(fileName==null || fileName.length()==0)
		{
			return null;
		}
		AssetManager assetManager = context.getAssets();
		if(assetManager==null)
		{
			return null;
		}
		File upgradeFile=null;
		try 
		{
			File[] oldFiles=getUpgradeFile(context);
			if(oldFiles!=null && oldFiles.length>0)
			{
				//比较文件名是否相同，若相同则直接返回
				for(File tempFile:oldFiles)
				{
					if(fileName.endsWith(tempFile.getName()))
					{
						System.err.println("has the same file >>"+tempFile.getAbsolutePath());
						return tempFile;
					}
				}
			}
			//手机目录未存在目标文件，重新保存
			String savePath=createPortraitUrl(context.getApplicationContext(), UPGRADE_FILE_PATH);
			//File file = new File("android.resource://com.bluetooth.demo/assets/"+filename);
			InputStream in = null;
			OutputStream out = null;
			in = assetManager.open(fileName);
			if(in!=null)
			{
				out = new FileOutputStream(savePath+fileName);
				byte[] buffer = new byte[1024];
				int read;
				while((read = in.read(buffer)) != -1)
				{
					out.write(buffer, 0, read);
				}
				in.close();
				in = null;
				out.flush();
				out.close();
				out = null;
			}
			upgradeFile=new File(savePath+fileName);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return upgradeFile;
	}

	/**
	 * 从应用中读取已内置的升级文件
	 * @return
	 */
	@SuppressWarnings("null")
	public static List<File> getUpgradeFileFromAssets(Context context)
	{
		AssetManager assetManager =context.getAssets();
		if(assetManager==null)
		{
			return null;
		}
		List<File> upgradeFiles=null;
		try 
		{
			String[] filePaths=assetManager.list("");
			if(filePaths==null || filePaths.length==0)
			{
				return null;
			}
			upgradeFiles=new ArrayList<File>();
			for(String filename:filePaths)
			{
				if(filename!=null 
						&& (filename.endsWith(".hex") ||filename.endsWith(".lsf")))
				{
					File file = new File("android.resource://com.bluetooth.demo/assets/"+filename);
					upgradeFiles.add(file);
				}
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		return upgradeFiles;
	}



	/**
	 * 创建log文件目录
	 * @param context
	 * @return
	 */
	public static String createPortraitUrl(Context context,String filePath) 
	{
		try 
		{
			boolean sdCardExist = Environment.getExternalStorageState().equals(
					android.os.Environment.MEDIA_MOUNTED);
			StringBuffer url = new StringBuffer();
			if (sdCardExist) 
			{
				url.append(Environment.getExternalStorageDirectory().getPath()
						+ File.separator);
			} 
			else 
			{
				url.append(context.getFilesDir().getAbsolutePath()
						+ File.separator);
			}
			url.append(filePath + File.separator);
			File file = new File(url.toString());
			if (!file.exists())
			{
				file.mkdirs();
				System.err.println("sky-test,create file path >>"+url.toString());
			}
			return url.toString();
		} catch (Exception e) 
		{
			e.printStackTrace();
			return "";
		}
	}
	
	/**
	 * 从手机默认的路径中获取升级文件
	 * @return
	 */
	private static File[] getUpgradeFile(Context appContext)
	{
		String filePath=createPortraitUrl(appContext.getApplicationContext(), UPGRADE_FILE_PATH);
		if(filePath==null || filePath.length()<1)
		{
			return null;
		}
		File file=new File(filePath);
		if(file.exists() && file.isDirectory())
		{
			File[] upgradefiles=file.listFiles();
			return upgradefiles;
		}
		else 
		{
			return null;
		}
	}
	
	/**
	 * get upgrade file with device information
	 * @return
	 */
	@SuppressWarnings("null")
	public static List<File> getUpgradeFileFromAssets(Context context,LsDeviceInfo lsDevice)
	{
		AssetManager assetManager =context.getAssets();
		if(assetManager==null)
		{
			return null;
		}
		List<File> upgradeFiles=null;
		try 
		{
			String[] filePaths=assetManager.list("");
			if(filePaths==null || filePaths.length==0)
			{
				return null;
			}
			upgradeFiles=new ArrayList<File>();
			for(String filename:filePaths)
			{
				if(isSupportedUpgrade(lsDevice, filename))
				{
					File file = new File("android.resource://com.bluetooth.demo/assets/"+filename);
					upgradeFiles.add(file);
				}
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		return upgradeFiles;
	}
	
	public static List<File> getUpgradeFileFromSDcard()
	{
		final List<File> fileList = new ArrayList<File>();
		String path = Environment.getExternalStorageDirectory().getPath();
		File[] fileArray = new File(path).listFiles();
		for (File file : fileArray) 
		{
			if (file.isFile() 
					&& (file.getName().endsWith(".hex") 
							|| file.getName().endsWith(".lsf") 
							|| file.getName().endsWith(".bin")))
			{
				fileList.add(file);
			}
		}
		return fileList;
	}

	/**
	 * check the device is supported upgrade ,if support then filter invalid file
	 * @param firmwareVersion
	 * @param fileName
	 * @return
	 */
	private static boolean isSupportedUpgrade(LsDeviceInfo lsDevice,String fileName)
	{
		if(lsDevice==null)
		{
			return false;
		}
		String firmwareVersion=lsDevice.getFirmwareVersion();
		String deviceName=lsDevice.getDeviceName();
		if(TextUtils.isEmpty(fileName))
		{
			return false;
		}
		if((fileName.endsWith(".hex") || fileName.endsWith(".bin")) )
		{
			if(firmwareVersion!=null && firmwareVersion.startsWith("A0"))
			{
				return true;
			}
			if(deviceName!=null 
					&& (deviceName.equalsIgnoreCase("my mambo") 
							|| deviceName.equalsIgnoreCase("mambo hr")
							|| deviceName.equalsIgnoreCase("mambo")))
			{
				return true;
			}
		}
		if(fileName.endsWith(".lsf"))
		{
			if(fileName.startsWith("417") && lsDevice.getModelNumber().startsWith("417"))
			{
				return true;
			}
			if(fileName.startsWith("418") &&  lsDevice.getModelNumber().startsWith("418"))
			{
				return true;
			}
			if(fileName.startsWith("415") && lsDevice.getModelNumber().startsWith("415"))
			{
				return true;
			}
			if(fileName.startsWith("422") && lsDevice.getModelNumber().startsWith("422"))
			{
				return true;
			}
		}
		return false;
	}

}
