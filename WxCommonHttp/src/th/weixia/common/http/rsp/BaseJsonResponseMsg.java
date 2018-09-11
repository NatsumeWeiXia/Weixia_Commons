package th.weixia.common.http.rsp;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import th.weixia.common.http.utils.HttpUtil;

public class BaseJsonResponseMsg extends BaseResponse
{
	private HttpResponse httpresponse;

	protected String strResult;
	
	/**
	 * 最外层的对象
	 */
	protected JSONObject jso;

	public BaseJsonResponseMsg()
	{
	}

	@Override
	public void init(HttpResponse httpresponse)
	{
		this.httpresponse = httpresponse;
		try
		{
			String string = getHttpReponseHead(httpresponse).get("Content-Encoding");

			if(string!=null && string.equals("gzip"))
			{
				strResult = HttpUtil.unGzip(httpresponse.getEntity().getContent());
			}
			else
			{
				HttpEntity entity = httpresponse.getEntity();
				strResult = EntityUtils.toString(entity,"utf-8");
			}
			if (null != strResult)
			{

				jso = new JSONObject(strResult);
				if (jso.has("resultcode"))
				{
					resultcode = (String) jso.get("resultcode");
				} else if (jso.has("code"))
				{
					resultcode = (String) jso.get("code");
				}

				if (jso.has("resultmessage"))
				{
					resultmessage = (String) jso.get("resultmessage");
				} else if (jso.has("message"))
				{
					resultmessage = (String) jso.get("message");
				}else if (jso.has("msg"))
				{
					resultmessage = (String) jso.get("msg");
				}
			}
		}
		catch (Exception e)
		{
			Log.e(this.getClass().getName(), "presponse ( Error ):  " + httpresponse.getStatusLine().toString(), e);
		}
	}

	/**
	 * HTTP返回对象
	 * 
	 * @return
	 */
	public HttpResponse getHttpresponse()
	{
		return httpresponse;
	}
	
	@Override
	public boolean isOK()
	{
		if(!resultcode.isEmpty())
		{
			if("0".equals(resultcode))
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean isSessionTimeOut()
	{
		if(!resultcode.isEmpty())
		{
			if("-100".equals(resultcode))
			{
				return true;
			}
		}
		return false;

	}
}
