package com.abt.httpserver;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

public class HttpService extends Service {

	private static String TAG = HttpService.class.getSimpleName();
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
			Log.d(TAG, "onCreate http start error :", e);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mHttpServer.stop();
		Log.d(TAG, "onDestroy");
	}

	@Override
	public void onStart(Intent intent, int startid) {
		super.onStart(intent, startid);
		Log.d(TAG, "onStart");
	}

	@SuppressLint("WrongConstant")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		flags = START_STICKY;
		return super.onStartCommand(intent, flags, startId);
	}
}
