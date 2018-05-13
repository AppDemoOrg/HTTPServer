package com.abt.http.server;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.io.IOException;

public class HttpService extends Service {

	private HttpServerImpl mHttpServer;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		mHttpServer = new HttpServerImpl();
        try {
			mHttpServer.start();
		} catch (IOException e) {
        	e.printStackTrace();
		}
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

}
