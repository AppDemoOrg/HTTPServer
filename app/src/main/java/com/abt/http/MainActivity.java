package com.abt.http;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.abt.http.server.HttpServerImpl;
import com.abt.http.server.HttpService;
import com.abt.http.util.IPAddressUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends Activity {

    @BindView(R.id.tips)
    TextView mTips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        ButterKnife.bind(this);

        Intent intent = new Intent(this, HttpService.class);
        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        String tips = new String("请在PC浏览器中输入:\n\n"+
                IPAddressUtils.getLocalIP()+":"+HttpServerImpl.DEFAULT_SERVER_PORT);
        mTips.setText(tips);
    }

}
