package th.weixia.common.http;

import android.content.Context;
import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import th.weixia.common.http.event.BaseRequest;
import th.weixia.common.http.exceptions.CusHttpException;
import th.weixia.common.http.rsp.BaseJsonResponseMsg;
import th.weixia.common.http.rsp.BaseResponse;
import th.weixia.common.http.utils.ApnManager;
import th.weixia.common.http.utils.HttpUtil;
import th.weixia.common.http.utils.NetworkUtil;
import th.weixia.common.http.utils.TrustAllSSLSocketFactory;

import java.io.InterruptedIOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class WxHttpHandler
{
	private static final String TAG = WxHttpHandler.class.getSimpleName();
	private Context context;
	private DefaultHttpClient httpclient;// 默认的HTTP客户端
	private static TrustAllSSLSocketFactory socketFactory = null;
	private HttpUriRequest postMethod = null;

	public WxHttpHandler(Context context)
	{
		this.context = context;
		httpclient = HttpUtil.createHttpClient(context);
	}

	/**
	 * 
	 * @param request
	 * @param response
	 * @throws CusHttpException
	 */
	public void sendMessage(BaseRequest request, BaseResponse response) throws CusHttpException
	{
		String url = request.getUrl();

		ArrayList<Header> headers = request.getHttpReqHead(context);
		String httpReqBody = request.getHttpReqBody();
		ByteArrayEntity reqBody;

		reqBody = new ByteArrayEntity(httpReqBody.getBytes());
		
		requestURL(url, headers, reqBody, response, true);

		return;
	}

	HttpHost target = null;
	HttpHost proxy = null;

	/**
	 * @param url
	 * @param headers
	 * @param httpbody
	 * @return -1:正常错误；-2：主动取消
	 * @throws CusHttpException
	 * @throws ClientProtocolException
	 * @throws SocketTimeoutException
	 */
	public void requestURL(String url, ArrayList<Header> headers, HttpEntity httpbody, BaseResponse response,
						   boolean isPostHttp) throws CusHttpException
	{
		// 不需要去检查是否是正确的URL
		if (url.startsWith("https://"))
		{
			// 这里需要解析出Port并设置为自动接收所有证书
			try
			{
				if (socketFactory == null)
				{
					socketFactory = new TrustAllSSLSocketFactory();
				}
				Scheme sch = new Scheme("https", socketFactory, Integer.valueOf(url.split(":")[2].split("/")[0]));

				SchemeRegistry schemeRegistry = httpclient.getConnectionManager().getSchemeRegistry();
				schemeRegistry.unregister("https");
				schemeRegistry.register(sch);
			}
			catch (IllegalArgumentException e)
			{
				e.printStackTrace();
				response.setInteralErrorNo(CusHttpException.CONNECT_TIMEOUT_EX);
				response.setResultmessage(getToastMsg(CusHttpException.CONNECT_TIMEOUT_EX));
				throw new CusHttpException(CusHttpException.CONNECT_TIMEOUT_EX);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		ApnManager apnManager = new ApnManager(context);
		boolean mUseWap = apnManager.isWapNetwork();

		if (mUseWap)
		{
			String mProxy = apnManager.getProxy();
			String mPort = apnManager.getProxyPort();
			if (proxy == null)
				proxy = new HttpHost(mProxy, Integer.valueOf(mPort));
			if (target == null)
				target = new HttpHost(mProxy, Integer.valueOf(mPort));

			httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
		}
		else
		{
			target = null;
		}
		if (isPostHttp)
		{
			try
			{
				postMethod = new HttpPost(url);
			}
			catch (IllegalArgumentException e)
			{
				e.printStackTrace();
				response.setInteralErrorNo(CusHttpException.UNKNOWNHOST_EX);
				response.setResultmessage(getToastMsg(CusHttpException.UNKNOWNHOST_EX));
				return;
			}
		}
		else
		{
			postMethod = new HttpGet(url);
		}

		postMethod.setHeaders(headers.toArray(new Header[headers.size()]));

		if (postMethod == null)
		{
			response.setResultmessage(getToastMsg(CusHttpException.CONNECT_EX));
			return;
		}
		if (isPostHttp)
		{
			if (postMethod instanceof HttpPost)
			{
				HttpPost hp = (HttpPost) postMethod;

				hp.setEntity(httpbody);
			}
		}
		else
		{
		}

		doResponse(response, apnManager);
	}

	private void doResponse(BaseResponse response, ApnManager apnManager)
		throws CusHttpException
	{
		HttpResponse response_ = null;

		try
		{
			httpclient.setCookieStore(null); // android的DefaultHttpClient会自动添加cookie,就会跟我们自己写的重复了,所以设置为null

			if (target != null)
			{
				response_ = httpclient.execute(target, postMethod);
			}
			else
			{
				response_ = httpclient.execute(postMethod);
			}

			int statusCode = response_.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK)
			{
				response.init(response_);

				if (response instanceof BaseJsonResponseMsg)
				{
					BaseJsonResponseMsg baseRes = (BaseJsonResponseMsg) response;
					if (baseRes.isSessionTimeOut())
					{
						Log.d(TAG, "isSessionTimeOut");

						if (isNeedAutoLogin())
						{
							Log.d(TAG, "isNeedAutoLogin");
							doLoginAgain();
						}
						else
						{
							Log.d(TAG, "forward LoginActivity");
							// 跳转登录界面
							throw new CusHttpException(CusHttpException.RELOGIN_EX);
						}
					}
				}
			}
			else
			{
				Log.e(TAG, "response error status code:" + response_.getStatusLine().getReasonPhrase());

				if (statusCode == HttpStatus.SC_BAD_GATEWAY)
				{
					if (apnManager.getApn().equals(ApnManager.STRING_3GWAP))
					{
						// throw new
						// CusHttpException(CusHttpException.BAD_GATEWAY);
						response.setInteralErrorNo(CusHttpException.BAD_GATEWAY);
						response.setResultmessage(getToastMsg(CusHttpException.BAD_GATEWAY));
					}
				}
				else if (statusCode == HttpStatus.SC_FORBIDDEN)
				{
					if (apnManager.getApn().equals(ApnManager.STRING_3GWAP))
					{
						// throw new
						// CusHttpException(CusHttpException.BAD_GATEWAY);
						response.setInteralErrorNo(CusHttpException.BAD_GATEWAY);
						response.setResultmessage(getToastMsg(CusHttpException.BAD_GATEWAY));
					}
				}
				else
				{
					response.setInteralErrorNo(CusHttpException.CONNECT_EX);
					response.setResultmessage(getToastMsg(CusHttpException.CONNECT_EX));
				}

			}

		}
		catch (CusHttpException e)
		{
			throw e;
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
			response.setInteralErrorNo(CusHttpException.SOCKET_TIMEOUT_EX);
			response.setResultmessage(getToastMsg(CusHttpException.SOCKET_TIMEOUT_EX));
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
			if (NetworkUtil.isNetworkAvailable(context))
			{
				response.setInteralErrorNo(CusHttpException.CONNECT_TIMEOUT_EX);
				response.setResultmessage(getToastMsg(CusHttpException.CONNECT_TIMEOUT_EX));
			}
			else
			{
				response.setInteralErrorNo(CusHttpException.NET_EX);
				response.setResultmessage(getToastMsg(CusHttpException.NET_EX));
			}
		}
		catch (ConnectTimeoutException e)
		{
			e.printStackTrace();
			if (NetworkUtil.isNetworkAvailable(context))
			{
				response.setInteralErrorNo(CusHttpException.CONNECT_TIMEOUT_EX);
				response.setResultmessage(getToastMsg(CusHttpException.CONNECT_TIMEOUT_EX));
			}
			else
			{
				response.setNetError(true);
				response.setInteralErrorNo(CusHttpException.NET_EX);
				response.setResultmessage(getToastMsg(CusHttpException.NET_EX));
			}
		}
		catch (SocketTimeoutException se)
		{
			se.printStackTrace();
			if (apnManager.getApn() != null && apnManager.getApn().equals(ApnManager.STRING_3GWAP))
			{
				response.setInteralErrorNo(CusHttpException.BAD_GATEWAY);
				response.setResultmessage(getToastMsg(CusHttpException.BAD_GATEWAY));
			}
			else
			{
				if (NetworkUtil.isNetworkAvailable(context))
				{
					response.setInteralErrorNo(CusHttpException.SOCKET_TIMEOUT_EX);
					response.setResultmessage(getToastMsg(CusHttpException.SOCKET_TIMEOUT_EX));
				}
				else
				{
					response.setNetError(true);
					response.setInteralErrorNo(CusHttpException.NET_EX);
					response.setResultmessage(getToastMsg(CusHttpException.NET_EX));
				}
			}
		}
		catch (SocketException e)
		{
			e.printStackTrace();
			if (NetworkUtil.isNetworkAvailable(context))
			{
				response.setInteralErrorNo(CusHttpException.CONNECT_TIMEOUT_EX);
				response.setResultmessage(getToastMsg(CusHttpException.CONNECT_TIMEOUT_EX));
			}
			else
			{
				response.setNetError(true);
				response.setInteralErrorNo(CusHttpException.NET_EX);
				response.setResultmessage(getToastMsg(CusHttpException.NET_EX));
			}
		}
		catch (ClientProtocolException ce)
		{
			ce.printStackTrace();
			response.setInteralErrorNo(CusHttpException.CONNECT_EX);
			response.setResultmessage(getToastMsg(CusHttpException.CONNECT_EX));
		}
		catch (InterruptedIOException e)
		{
			e.printStackTrace();
			response.setInteralErrorNo(CusHttpException.INTERRUPTEDID_EX);
			response.setResultmessage(getToastMsg(CusHttpException.INTERRUPTEDID_EX));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			response.setInteralErrorNo(CusHttpException.OTHER_EX);
			response.setResultmessage(getToastMsg(CusHttpException.OTHER_EX));
		}
		finally
		{
			postMethod = null;
			httpclient.getParams().removeParameter(ConnRoutePNames.DEFAULT_PROXY);
		}
	}

	private boolean isNeedAutoLogin()
	{
		return false;
	}

	private void doLoginAgain()
	{

	}

	private String getToastMsg(int exNo)
	{
		String str = "";

		if (exNo == CusHttpException.CONNECT_EX)
		{
			str = "已与服务端断开连接";
		}
		else if (exNo == CusHttpException.NET_EX)
		{
			str = "请检查网络配置";
		}
		else if (exNo == CusHttpException.SOCKET_EX)
		{
			str = "已与服务端断开连接";
		}
		else if (exNo == CusHttpException.BAD_GATEWAY)
		{
			str = "亲，还不支持 当前设置的 3GWAP 网络额,请把 接入点网络 修改为3GNET.有问题请咨询客服人员.";
		}
		else if (exNo == CusHttpException.CONN_REFUSED_EX)
		{
			str = "已与服务端断开连接";
		}
		else if (exNo == CusHttpException.PARAMETER_EX)
		{
			str = "请检查当前地址和端口是否填写正确。";
		}
		// NEW
		else if (exNo == CusHttpException.CONNECT_TIMEOUT_EX)
		{
			str = "已与服务端断开连接";
		}
		else if (exNo == CusHttpException.SOCKET_TIMEOUT_EX)
		{
			str = "已与服务端断开连接";
		}
		else if (exNo == CusHttpException.INTERRUPTEDID_EX)
		{
			str = "已与服务端断开连接";
		}
		else if (exNo == CusHttpException.OTHER_EX)
		{
			str = "已与服务端断开连接";
		}
		else if (exNo == CusHttpException.UNKNOWNHOST_EX)
		{
			str = "请检查当前地址和端口";
		}

		return str;
	}
}
