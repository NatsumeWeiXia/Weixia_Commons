package th.weixia.common.http.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

public class ApnManager
{
	public static final String STRING_3GWAP = "3GWAP";
	
	private static final String ID = "_id";
	private static final String APN = "apn";

	public static final Uri PREFERRED_APN_URI;

	private String mApn; // 接入点名称

	private String mPort; // 端口号

	private String mProxy; // 代理服务器

	private boolean mUseWap; // 是否正在使用WAP

	static
	{
		PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn"); // 取得当前设置的APN
	}

	public ApnManager(Context context)
	{
		checkNetworkType(context);
	}

	/**
	 * 获得当前设置的APN相关参数
	 * 
	 * @param context
	 */
	private void checkApn(Context context)
	{
		// ContentResolver contentResolver = context.getContentResolver();
		// Uri uri = PREFERRED_APN_URI;
		// String[] apnInfo = new String[3];
		// apnInfo[0] = "apn";
		// apnInfo[1] = "proxy";
		// apnInfo[2] = "port";
		//
		// Cursor cursor = contentResolver.query(uri, apnInfo, null, null,
		// null);
		// if (cursor != null)
		// {
		// while (cursor.moveToFirst())
		// {
		// this.mApn = cursor.getString(cursor.getColumnIndex("apn"));
		// this.mProxy = cursor.getString(cursor.getColumnIndex("proxy"));
		// this.mPort = cursor.getString(cursor.getColumnIndex("port"));
		//
		// // 代理为空
		// if ((this.mProxy == null) || (this.mProxy.length() <= 0))
		// {
		// String apn = this.mApn.toUpperCase();
		//
		// // 中国移动WAP设置：APN：CMWAP；代理：10.0.0.172；端口：80
		// // 中国联通WAP设置：APN：UNIWAP；代理：10.0.0.172；端口：80
		// // 中国联通WAP设置（3G）：APN：3GWAP；代理：10.0.0.172；端口：80
		// if ((apn.equals("CMWAP")) || (apn.equals("UNIWAP")) ||
		// (apn.equals("3GWAP")))
		// {
		// this.mUseWap = true;
		// this.mProxy = "10.0.0.172";
		// this.mPort = "80";
		// break;
		// }
		//
		// // 中国电信WAP设置：APN(或者接入点名称)：CTWAP；代理：10.0.0.200；端口：80
		// if (apn.equals("CTWAP"))
		// {
		// this.mUseWap = true;
		// this.mProxy = "10.0.0.200";
		// this.mPort = "80";
		// break;
		// }
		// }
		// this.mPort = "80";
		// this.mUseWap = true;
		// break;
		// }
		// }
		//
		// this.mUseWap = false;
		// cursor.close();
		String apnName = getApnName(context);
		if (apnName != null)
		{
			String apn = apnName.toUpperCase();
			this.mApn=apn;

			// 中国移动WAP设置：APN：CMWAP；代理：10.0.0.172；端口：80
			// 中国联通WAP设置：APN：UNIWAP；代理：10.0.0.172；端口：80
			if ((apn.equals("CMWAP")) || (apn.equals("UNIWAP")))
			{
				this.mUseWap = true;
				this.mProxy = "10.0.0.172";
				this.mPort = "80";
			}

			// 中国联通WAP设置（3G）：APN：3GWAP；代理：10.0.0.172；端口：80
			if (apn.equals(STRING_3GWAP))
			{
				this.mUseWap = true;
				this.mProxy = "10.0.0.172";
				this.mPort = "80";
			}
			
			// 中国电信WAP设置：APN(或者接入点名称)：CTWAP；代理：10.0.0.200；端口：80
			if (apn.equals("CTWAP"))
			{
				this.mUseWap = true;
				this.mProxy = "10.0.0.200";
				this.mPort = "80";
			}
		}
		else
		{
			this.mUseWap = false;
		}
	}

	/**
	 * 返回当前网络接入点
	 * 
	 * @param context
	 * @return 接入点名字，类似于ctwap,ctnet；如果返回为空，不做处理
	 */
	public static String getApnName(Context context)
	{
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = manager.getActiveNetworkInfo();
		if (networkInfo == null || networkInfo.getType() == ConnectivityManager.TYPE_WIFI)
		{
			return null;
		}

		String result = null;
		// 找到第一个处于连接状态的接入点即可
		String extrainfo = networkInfo.getExtraInfo();
		// extrainfo 类似于 uinwap:gsm/cmwap:gsm/ctwap:cdma
		// type: MOBILE[CDMA - EvDo rev. A], state: CONNECTED/CONNECTED, reason:
		// (unspecified), extra: #777:CDMA, roaming: false, failover: false,
		// isAvailable: true
		// 在XT800上得到的extra:#777:CDMA,这里需要在设置里面更改接入点名字或者重新创建对应的ctwap/ctnet接入点
		if (extrainfo != null)
		{
			int end = extrainfo.indexOf(":");
			if (end > 0)
			{
				result = extrainfo.substring(0, end);
				if (result != null && result.startsWith("#777"))
				{
					// XT800手机返回的即是这个值，做特别处理或者在设置中更改名字即可
					result = getPreferredApnName(context);
				}
			}
			else
			{
				result = extrainfo;
			}
		}
		if (result != null && result.startsWith("#777"))
		{
			result = getPreferredApnName(context);
		}
		if (result != null)
		{
			return result.toLowerCase();
		}
		return null;
	}

	public static String getPreferredApnName(Context context)
	{
		Cursor cursor = context.getContentResolver().query(PREFERRED_APN_URI, new String[]
		{
			ID, APN, "user"
		}, null, null, null);
		if (cursor == null)
		{
			return "";
		}
		cursor.moveToFirst();
		if (!cursor.isAfterLast())
		{
			String name = cursor.getString(1);
			String usr = cursor.getString(2);
			if (name != null && name.equalsIgnoreCase("#777"))
			{
				// 为XT800做特别处理，查看user类型
				if (usr != null && usr.indexOf("ctwap") != -1)
				{
					return "ctwap";
				}
				else if (usr != null && usr.indexOf("ctnet") != -1)
				{
					return "ctnet";
				}
				else if (usr != null && usr.indexOf("@") != -1)
				{
					return "vpdn";
				}
			}
		}
		cursor.close();
		return null;
	}

	/**
	 * 检测当前使用的网络类型是WIFI还是WAP
	 * 
	 * @param context
	 */
	private void checkNetworkType(Context context)
	{
		NetworkInfo networkInfo = ((ConnectivityManager) context.getApplicationContext().getSystemService(
				Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		if (networkInfo != null)
		{
			if (!"wifi".equals(networkInfo.getTypeName().toLowerCase()))
			{
				checkApn(context);
				return;
			}
			this.mUseWap = false;
		}
	}

	/**
	 * 判断当前网络连接状态
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetworkConnected(Context context)
	{
		NetworkInfo networkInfo = ((ConnectivityManager) context.getApplicationContext().getSystemService(
				"connectivity")).getActiveNetworkInfo();
		if (networkInfo != null)
		{
			return networkInfo.isConnectedOrConnecting();
		}
		return false;
	}

	public String getApn()
	{
		return this.mApn;
	}

	public String getProxy()
	{
		return this.mProxy;
	}

	public String getProxyPort()
	{
		return this.mPort;
	}

	public boolean isWapNetwork()
	{
		return this.mUseWap;
	}
}