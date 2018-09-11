package th.weixia.common.log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.util.Log;

public class _L
{
	/**
	 * 打印日志到外存储卡
	 */
	public static synchronized void debugMessage(String fileDirPath, String fileName, String logMessage) 
	{

		File fi = new File(fileDirPath);
		if (!fi.exists()) {
			fi.mkdirs();
		} else if (fi.isFile()) {
			fi.delete();
			fi.mkdirs();
		}
		
		String filePath = fileDirPath + "/" + fileName + ".log" ;
		
		fi = new File(filePath);
		if (fi != null) {
			try {
				long len = fi.length();
				if (len > 3 * 1024 * 1024) {
					File nfi = new File(fileDirPath + "/" + fileName + System.currentTimeMillis() + ".log");
					fi.renameTo(nfi);
					fi = new File(filePath);
					fi.delete();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				if (!fi.exists()) {
					boolean isSuc = fi.createNewFile();
					if (!isSuc) {
						Log.d("L.class", "LogL.debugMessage_isSuc=" + isSuc);
					}
				}
			} catch (Exception e) {
				Log.e("L.class", "LogL.debugMessage_Exception2=" + e.getMessage());
			}
		}

		try {
			FileOutputStream os = new FileOutputStream(fi, true);
			StringBuffer sb = new StringBuffer();
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd:hh.mm.ss.SSS", Locale.getDefault());
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(System.currentTimeMillis());
			String strTime = format.format(calendar.getTime());
			sb.append("    " + strTime);
			sb.append("   " + logMessage).append("    ");
			sb.append("\r\n");
			try {
				os.write(sb.toString().getBytes("UTF-8"));
			} catch (UnsupportedEncodingException ex) {
				os.write(sb.toString().getBytes());
			}
			os.close();
		} catch (Exception e) {
			Log.e("_L.class ","Log.debugMessage_Exception3=" + e.getMessage());
		}
	}
}
