package com.abt.http;

import android.app.Application;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

/**
 * Created by hwq on 2017/10/27 0027.
 */
public class ServerApp extends Application {

    private static ServerApp mLauncherApp;

    public static ServerApp getInstance() {
        return mLauncherApp;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.addLogAdapter(new AndroidLogAdapter());
        mLauncherApp = this;
    }

}
