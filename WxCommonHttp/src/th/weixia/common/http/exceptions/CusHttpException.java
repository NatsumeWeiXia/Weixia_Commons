package th.weixia.common.http.exceptions;

public class CusHttpException extends Exception
{

	public static final int NULL_SESSIION_EX=800;

	public static final int CONNECT_EX =600;

	public static final int NULL_PORT_EX=800;
	
	public static final int BAD_GATEWAY=502;
	
	public static final int RELOGIN_EX=900;
	
	public static final int PARAMETER_EX=901;
	
	public static final int CONN_REFUSED_EX=902;
	
	public static final int NET_EX =1006;//网络不通畅
	
	public static final int SOCKET_EX =700;//网络不通畅/地址填写错误/己方关闭连接后的网络读写异常/连接断开后的读和写操作 异常 （不在使用，用1001代替）

	//InterruptedIOException
	public static final int CONNECT_TIMEOUT_EX =1001;//请求超时---现加上 //网络不通畅/地址填写错误/己方关闭连接后的网络读写异常/连接断开后的读和写操作 异常
	public static final int SOCKET_TIMEOUT_EX =1002; //响应超时
	public static final int INTERRUPTEDID_EX=1007;   //终止
	
	//IOException
	public static final int UNKNOWNHOST_EX=1005;     //网络地址/域名解析 错误
	
	//other
	public static final int OTHER_EX=1003;  //其他
	
	private static final long serialVersionUID = -1678400716831548251L;
	
	public static final int FORBIDDEN_EX=1008;  //非法请求

	private int exNo;
	
	public CusHttpException(int exNo)
	{
		this.exNo=exNo;
	}
	
	public CusHttpException(String message)
    {
        super(message);
    }

    public CusHttpException(Throwable t)
    {
        super(t);
    }

    public CusHttpException(String message, Throwable t)
    {
        super(message, t);
    }

	public int getExNo()
	{
		return exNo;
	}
}
