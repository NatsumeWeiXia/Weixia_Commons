package th.weixia.common.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import com.fiberhome.wx.commonlog.BuildConfig;

import android.util.Log;

public class WxLogger {
	
	private String TAG = "WxLoger";
	
	private String PATH;
	
	private boolean enableLogFile = true;
	
	private boolean enableConsole = true;
	
	private boolean DEBUG = BuildConfig.DEBUG;
	
	public WxLogger(String tag, String path, boolean enableFile, boolean enableConsole) {
		TAG = tag;
		PATH = path;
		this.enableLogFile = enableFile;
		this.enableConsole = enableConsole;
	}
	
	public void d(String msg) {
		d(TAG, msg);
	}
	
	public void d(String tag,String msg) {
		if(DEBUG && enableConsole)
			Log.d(tag, msg);
		if (enableLogFile)
			_L.debugMessage(PATH, TAG, msg);
	}
	
	public void i(String msg) {
		i(TAG, msg);
	}
	
	public void i(String tag,String msg) {
		if(DEBUG && enableConsole)
			Log.i(tag, msg);
		if (enableLogFile)
			_L.debugMessage(PATH, TAG, msg);
	}
	
	public void w(String msg) {
		w(TAG, msg);
	}
	
	public void w(String tag,String msg) {
		if(DEBUG && enableConsole) 
			Log.w(tag, msg);
		if (enableLogFile)
			_L.debugMessage(PATH, TAG, msg);
	}
	
	public void e(String msg) {
		e(TAG, msg);
	}
	
	public void e(String tag, String msg) {
		if(DEBUG && enableConsole) 
			Log.e(tag, msg);
		if (enableLogFile)
			_L.debugMessage(PATH, TAG, msg);
	}
	
	public void e(Throwable tr) {
		e(TAG, " ", tr);
	}
	
	public void e(String tag,  String msg, Throwable tr) {
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
