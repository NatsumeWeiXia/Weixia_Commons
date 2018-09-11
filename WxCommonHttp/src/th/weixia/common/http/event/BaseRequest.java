package th.weixia.common.http.event;

import android.content.Context;

import org.apache.http.Header;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.message.BasicHeader;

import th.weixia.common.http.utils.TrustAllSSLSocketFactory;

import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public abstract class BaseRequest {
	
	private final String HTTPS = "https://";

	// domain
	private final String PROPERTY_USER_AGENT = "User-Agent";
	private final String PROPERTY_CONNECTION = "Connection";
	private final String PROPERTY_CONTENT_TYPE = "Content-Type";
	private final String PROPERTY_SECRETFLAG = "Secretflag";
	private final String PROPERTY_KEEP_ALIVE = "keep-alive";

	protected int msgo;
	
	protected ArrayList<Header> headList = new ArrayList<Header>();

	protected String mUrl;
	
	public BaseRequest(String url) {
		this.mUrl = url;
	}
	
	public String getUrl() {
		mUrl = getEmpServerUrl();
		return mUrl;
	}

	public void setMsgo(int msgo) {
		this.msgo = msgo;
	}
	
	public int getMsgo() {
		return msgo;
	}
	
	public ArrayList<Header> getHttpReqHead(Context context) {
		headList.add(new BasicHeader(PROPERTY_USER_AGENT, "GAEA-Client"));
		headList.add(new BasicHeader(PROPERTY_CONNECTION, PROPERTY_KEEP_ALIVE));
		headList.add(new BasicHeader(PROPERTY_CONTENT_TYPE, "application/x-www-form-urlencoded"));

		headList.add(new BasicHeader(PROPERTY_SECRETFLAG, "false"));
		
		return headList;
	}

	public String getHttpReqBody() {
		return null;
	}

	public String getEmpServerUrl() {
		String url = this.mUrl;

		if (url.startsWith(HTTPS)) {
			try {
				HttpsURLConnection
						.setDefaultSSLSocketFactory(new TrustAllSSLSocketFactory()
								.getFactory());
				HttpsURLConnection
						.setDefaultHostnameVerifier(new AllowAllHostnameVerifier());
			}

			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return url.toString();
	}
}
