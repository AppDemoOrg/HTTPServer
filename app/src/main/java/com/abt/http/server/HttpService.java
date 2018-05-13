package com.abt.http.server;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;

import fi.iki.elonen.NanoHTTPD;

public class HttpService extends Service {

	private HttpServerImpl mHttpServer;
	private static final String KEYSTORE_PWD = "ssl123";

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		/*mHttpServer = new HttpServerImpl();
        try {
			mHttpServer.start();
		} catch (IOException e) {
        	e.printStackTrace();
		}*/
		startHttpServer();
	}

	/** START_STICKY表示Service被销毁后将被系统重启，但是onStartCommand中的intent为null */
	@SuppressLint("WrongConstant")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		flags = START_STICKY;
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (null != mHttpServer) {
			mHttpServer.stop();
		}
	}

/*	private void createMySSLFactory() throws Exception {
		InputStream inputStream = null;
		SSLContext ctx = SSLContext.getInstance("TLS"); // 选择安全协议的版本
		KeyManagerFactory keyManagers = KeyManagerFactory.getInstance(
				KeyManagerFactory.getDefaultAlgorithm());
		inputStream = getResources().openRawResource(R.raw.startHttpServer);
		//选择keystore的储存类型，andorid只支持BKS
		KeyStore ks = KeyStore.getInstance("BKS");
		ks.load(inputStream, KEYSTORE_PWD.toCharArray());
		keyManagers.init(ks, KEYSTORE_PWD.toCharArray());
		ctx.init(keyManagers.getKeyManagers(), null, null);
		SSLServerSocketFactory serverSocketFactory = ctx.getServerSocketFactory();
		soudboxServer.makeSecure(serverSocketFactory,null);
	}*/

	@SuppressLint("LongLogTag")
	private void startHttpServer() {
		try {
			AssetManager am = getAssets();
			InputStream ins = am.open("android.kbs");
			HttpServerImpl httpServer = new HttpServerImpl();

			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			keyStore.load(ins, "123456".toCharArray());

			//读取证书,注意这里的密码必须设置
			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			keyManagerFactory.init(keyStore, "android".toCharArray());

			httpServer.makeSecure(NanoHTTPD.makeSSLSocketFactory(keyStore, keyManagerFactory), null);
			httpServer.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
		} catch (IOException e) {
			Log.e("IOException", "Couldn't start server:\n" + e.getMessage());
		} catch (NumberFormatException e) {
			Log.e("NumberFormatException", e.getMessage());
		} catch (KeyStoreException | NoSuchAlgorithmException e) {
			Log.e("HTTPSException", "HTTPS certificate error:\n " + e.getMessage());
		} catch (UnrecoverableKeyException e) {
			Log.e("UnrecoverableKeyException", "UnrecoverableKeyException" + e.getMessage());
		} catch (CertificateException e) {
			e.printStackTrace();
		}
	}

}
