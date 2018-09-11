package th.weixia.common.http.rsp;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

import java.util.HashMap;

public abstract class BaseResponse
{
	protected int msgno; // 消息号

	protected String resultcode = "";

	protected String resultmessage = "";
	
	protected boolean isNetError=false;
	
	protected boolean isInteralError=false;
	
	protected int interalErrorNo;

	public abstract void init(HttpResponse httpresponse);

	/**
	 * 得到服务器返回的http头
	 * 
	 * @param response
	 * @return 得到http头
	 */
	protected HashMap<String, String> getHttpReponseHead(HttpResponse response)
	{
		HashMap<String, String> hm = new HashMap<String, String>();
		Header[] headers = response.getAllHeaders();
		for (int i = 0; i < headers.length; i++)
		{
			hm.put(headers[i].getName(), headers[i].getValue());
		}
		return hm;
	}

	public int getMsgno()
	{
		return msgno;
	}

	public void setMsgno(int msgno)
	{
		this.msgno = msgno;
	}

	public boolean isOK()
	{
		return true;
	}

	public boolean isNetError()
	{
		return isNetError;
	}

	public void setNetError(boolean isNetError)
	{
		this.isNetError = isNetError;
	}
	
	public int getInteralErrorNo()
	{
		return interalErrorNo;
	}

	public void setInteralErrorNo(int interalErrorNo)
	{
		isInteralError=true;
		this.interalErrorNo = interalErrorNo;
	}

	public boolean isInteralError()
	{
		return isInteralError;
	}

	public void setInteralError(boolean isInteralError)
	{
		this.isInteralError = isInteralError;
	}

	public String getResultcode()
	{
		return resultcode;
	}

	public void setResultcode(String resultcode)
	{
		this.resultcode = resultcode;
	}

	public String getResultmessage()
	{
		return resultmessage;
	}

	public void setResultmessage(String resultmessage)
	{
		this.resultmessage = resultmessage;
	}

	public static final int UAA_UPLOAD = 1;
}
