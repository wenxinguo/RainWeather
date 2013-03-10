package com.dddpeter.app.rainweather.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileOperator {
	/**
	 * 
	 *TODO：保存文件
	 *Author：Andy.Liu
	 *Create Time：2012-7-10 上午08:42:40
	 *TAG：@param str	文件的内容
	 *TAG：@param filePath	保存路径
	 *Return：void
	 */
	public static void saveFile(String str,String filePath,String fileName){
		FileOutputStream fos = null;
		try {
			File dir = new File(filePath);
			if(!dir.exists())
				dir.mkdirs();
		    File file=new File(filePath+fileName);
			fos = new FileOutputStream(file);
			fos.write(str.getBytes("UTF-8"));
			fos.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			try {
				if(null!=fos)
				fos.close();
				System.gc();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	/**
	 * 
	 *TODO：读取文件
	 *Author：Andy.Liu
	 *Create Time：2012-7-10 上午08:48:40
	 *TAG：@param filePath
	 *TAG：@return
	 *Return：String
	 */
	public static String readFile(String filePath){
		FileInputStream fis = null;
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		byte[] mByte = new byte[512];
		try {
			fis = new FileInputStream(new File(filePath));
			int x;
			while((x=fis.read())!=-1){
				baos.write(x);
			}
			baos.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
				try {
					baos.close();
					if(null!=fis)
					fis.close();
					System.gc();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return new String(baos.toByteArray());
	}
	
	
	
}
