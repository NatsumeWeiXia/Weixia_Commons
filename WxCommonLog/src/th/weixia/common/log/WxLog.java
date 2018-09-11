package th.weixia.common.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import com.fiberhome.wx.commonlog.BuildConfig;

import android.util.Log;

@Deprecated
public class WxLog {
	
	private static String TAG = "WxLog";
	
	private static String PATH;
	
	public static boolean enableLogFile = true;
	
	public static boolean enableConsole = true;
	
	public static boolean DEBUG = BuildConfig.DEBUG;
	
	@Deprecated
	public static void init(String tag, String path) {
		TAG = tag;
		PATH = path;
	}
	
	@Deprecated
	public static void d(String msg) {
		d(TAG, msg);
	}
	
	@Deprecated
	public static void d(String tag,String msg) {
		if(DEBUG && enableConsole)
			Log.d(tag, msg);
		if (enableLogFile)
			_L.debugMessage(PATH, TAG, msg);
	}
	
	@Deprecated
	public static void i(String msg) {
		i(TAG, msg);
	}
	
	@Deprecated
	public static void i(String tag,String msg) {
		if(DEBUG && enableConsole)
			Log.i(tag, msg);
		if (enableLogFile)
			_L.debugMessage(PATH, TAG, msg);
	}
	
	@Deprecated
	public static void w(String msg) {
		w(TAG, msg);
	}
	
	@Deprecated
	public static void w(String tag,String msg) {
		if(DEBUG && enableConsole) 
			Log.w(tag, msg);
		if (enableLogFile)
			_L.debugMessage(PATH, TAG, msg);
	}
	
	@Deprecated
	public static void e(String msg) {
		e(TAG, msg);
	}
	
	@Deprecated
	public static void e(String tag, String msg) {
		if(DEBUG && enableConsole) 
			Log.e(tag, msg);
		if (enableLogFile)
			_L.debugMessage(PATH, TAG, msg);
	}
	
	@Deprecated
	public static void e(Throwable tr) {
		e(TAG, " ", tr);
	}
	
	@Deprecated
	public static void e(String tag,  String msg, Throwable tr) {
		Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        tr.printStackTrace(printWriter);
        Throwable cause = tr.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
		
		if(DEBUG && enableConsole) 
			Log.e(tag, msg + "/n" + result);
		if (enableLogFile)
			_L.debugMessage(PATH, TAG, msg + "/n" + result);
	}
}
