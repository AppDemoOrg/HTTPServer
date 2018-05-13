package com.abt.httpserver;

import android.app.Application;
import android.util.Log;

/**
 * Created by hwq on 2017/10/27 0027.
 */
public class LauncherApp extends Application {

    private static final String TAG = LauncherApp.class.getSimpleName();
    private static LauncherApp mLauncherApp;

    public static LauncherApp getInstance() {
        return mLauncherApp;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mLauncherApp = this;
        //initCommonModule();
        Log.d(TAG, "initCommonModule()");
        //初始化设置
        //CameraSetting.initialize();
        //updateDeviceName();
        //startService();

        //在这里初始化
        //Bugtags.start("5ddba78610d41d70b6aed3d6bb67e2ee", this, Bugtags.BTGInvocationEventBubble);
    }

    /*private final void startService(){
        Intent intent = new Intent(this, SystemService.class);
        this.startService(intent);
    }*/

    //初始化公共模块
    /*private final void initCommonModule(){
        GlobalConfig.initialize(this);
    }

    private void updateDeviceName() {
        String name = CameraSetting.Device.getDeviceName();
        if (!TextUtils.isEmpty(name)) {
            SignalConstant._VARIABLE = name;
        }
        SignalConstant.LOCAL_DEVICE_NAME =
                SignalConstant.DEVICE_+SignalConstant._VARIABLE;
        Log.d(TAG, "SignalConstant.LOCAL_DEVICE_NAME : "
                + SignalConstant.LOCAL_DEVICE_NAME);
    }

    public final int getCurrentPower(){
        Intent intent = this.registerReceiver( null ,
                new IntentFilter( Intent.ACTION_BATTERY_CHANGED ) ) ;
        return intent.getIntExtra( SystemConstant.LEVEL , 0 );//电量（0-100）
    }

    public final boolean getIsCharging(){
        Intent intent = this.registerReceiver( null ,
                new IntentFilter( Intent.ACTION_BATTERY_CHANGED ) ) ;
        return intent.getBooleanExtra( SystemConstant.IS_CHARGING , false );//电量（0-100）
    }*/
}
