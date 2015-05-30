package org.baswell.httproxy.servers;

import org.baswell.httproxy.ProxiedRequest;
import org.baswell.httproxy.ServerSocketAcceptLoop;
import org.baswell.httproxy.SimpleIODirector;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class IOServer
{
  public static void main(String[] args) throws Exception
  {
    final SSLContext sslContext = SSLContext.getInstance("TLSv");

    sslContext.init(null, new TrustManager[]{new X509TrustManager()
    {
      @Override
      public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException
      {}

      @Override
      public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException
      {}

      @Override
      public X509Certificate[] getAcceptedIssuers()
      {
        return null;
      }
    }}, new SecureRandom());


    ThreadPoolExecutor threadPool = new ThreadPoolExecutor(250, 2000, 25, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    ServerSocket serverSocket = new ServerSocket(9090);
    ServerSocketAcceptLoop acceptLoop = new ServerSocketAcceptLoop(new SimpleIODirector("localhost", 8080) {
      public Socket connectToProxiedHost(ProxiedRequest request) throws IOException
      {
        return sslContext.getSocketFactory().createSocket("localhost", 44301);
      }
    }, threadPool);
    acceptLoop.start(serverSocket);
  }
}