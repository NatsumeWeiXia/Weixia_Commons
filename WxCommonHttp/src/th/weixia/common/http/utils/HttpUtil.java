package th.weixia.common.http.utils;

import android.content.Context;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.zip.GZIPInputStream;

public class HttpUtil
{
    private static int ConnectionTimeout = 30;// 连接超时  
    private static int ReadTimeOut = 30;// 读超时  

	private static DefaultHttpClient httpClient = null;

	private static TrustAllSSLSocketFactory socketFactory = null;
	
	public synchronized static DefaultHttpClient createHttpClient(Context context)
	{
		
		if (httpClient == null)
		{
			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setContentCharset(params, "utf-8");
			HttpProtocolParams.setHttpElementCharset(params, "utf-8");
//			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
//			HttpProtocolParams.setUserAgent(params, "HttpComponents/1.1");

			HttpConnectionParams.setConnectionTimeout(params, ConnectionTimeout * 1000);
			HttpConnectionParams.setSoTimeout(params, ReadTimeOut * 1000);

			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			
			if (socketFactory == null)
			{
				try
				{
					socketFactory = new TrustAllSSLSocketFactory();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			schemeRegistry.register(new Scheme("https", socketFactory, 443));

			ClientConnectionManager manager = new ThreadSafeClientConnManager(params, schemeRegistry);

			httpClient = new DefaultHttpClient(manager, params);
		}
		
		return httpClient;
	}
	
	/**  
     * 对服务器返回的内容进行解压并返回解压后的内容  
     * @param is  
     * @return  
     * @throws IOException
     * @throws UnsupportedEncodingException
     */  
	public static String unGzip(InputStream is)
	{
		try
		{
			GZIPInputStream in = new GZIPInputStream(is);
			ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
			int len = -1;
			byte[] buffer = new byte[1024];
			while ((len = in.read(buffer)) != -1)
			{
				arrayOutputStream.write(buffer, 0, len);
			}
			in.close();
			arrayOutputStream.close();
			is.close();
			return new String(arrayOutputStream.toByteArray(), "utf-8");
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}
}
