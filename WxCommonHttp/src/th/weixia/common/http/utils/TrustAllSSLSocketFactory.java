package th.weixia.common.http.utils;

import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class TrustAllSSLSocketFactory extends SSLSocketFactory
{
	private javax.net.ssl.SSLSocketFactory factory;

	public TrustAllSSLSocketFactory() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException,
			UnrecoverableKeyException
	{
		super(null);
		try
		{
			SSLContext sslcontext = SSLContext.getInstance("TLS");
			sslcontext.init(null, new TrustManager[]
			{
				new TrustAllManager()
			}, null);
			factory = sslcontext.getSocketFactory();
			setHostnameVerifier(new AllowAllHostnameVerifier());
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
//		getTm();
	}

	public javax.net.ssl.SSLSocketFactory getFactory()
	{
		return factory;
	}

	public static SocketFactory getDefault() throws KeyManagementException, NoSuchAlgorithmException,
			KeyStoreException, UnrecoverableKeyException
	{
		return new TrustAllSSLSocketFactory();
	}

	public Socket createSocket() throws IOException
	{
		return factory.createSocket();
	}

	public Socket createSocket(InetAddress inaddr, int i, InetAddress inaddr1, int j) throws IOException
	{
		return factory.createSocket(inaddr, i, inaddr1, j);
	}

	public Socket createSocket(InetAddress inaddr, int i) throws IOException
	{
		return factory.createSocket(inaddr, i);
	}

	public Socket createSocket(String s, int i, InetAddress inaddr, int j) throws IOException
	{
		return factory.createSocket(s, i, inaddr, j);
	}

	public Socket createSocket(String s, int i) throws IOException
	{
		return factory.createSocket(s, i);
	}

	public String[] getDefaultCipherSuites()
	{
		return factory.getDefaultCipherSuites();
	}

	public Socket createSocket(Socket socket, String s, int i, boolean flag) throws IOException
	{
		Socket sslSocket = factory.createSocket(socket, s, i, flag);
		if (sslSocket instanceof SSLSocket)
		{
			((SSLSocket) sslSocket).setEnabledCipherSuites(getSupportedCipherSuites());
		}
		return sslSocket;
	}

	public String[] getSupportedCipherSuites()
	{
		return factory.getDefaultCipherSuites();
		// return new String[]
		// {
		// "SSL_RSA_WITH_3DES_EDE_CBC_SHA",
		// "TLS_RSA_WITH_AES_256_CBC_SHA",
		// "TLS_RSA_WITH_AES_128_CBC_SHA",
		// "SSL_RSA_WITH_RC4_128_MD5",
		// "SSL_RSA_WITH_RC4_128_SHA",
		// "SSL_RSA_WITH_DES_CBC_SHA",
		// "SSL_RSA_EXPORT_WITH_RC4_40_MD5",
		// "SSL_RSA_EXPORT_WITH_RC2_CBC_40_MD5",
		// "SSL2_RC4_128_EXPORT40_WITH_MD5",
		// "SSL2_RC2_CBC_128_CBC_EXPORT40_WITH_MD5",
		// "SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA",
		// "SSL_DHE_DSS_WITH_DES_CBC_SHA",
		// "TLS_DHE_DSS_EXPORT1024_WITH_DES_CBC_SHA"
		// };
	}

	public static class TrustAllManager implements X509TrustManager
	{
		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException
		{
			return;
		}

		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException
		{
			return;
		}

		public X509Certificate[] getAcceptedIssuers()
		{
			return null;
		}
	}

	public static class MyHostnameVerifier implements HostnameVerifier
	{

		@Override
		public boolean verify(String arg0, SSLSession arg1)
		{
			return true;
		}
	}
}