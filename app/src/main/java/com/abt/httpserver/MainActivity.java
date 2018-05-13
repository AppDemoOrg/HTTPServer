package com.abt.httpserver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

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

        String tips = new String("请在PC浏览器中输入:\n\n"+
                IPAddressUtils.getLocalIP()+":"+
                HttpServerImpl.DEFAULT_SERVER_PORT);

        Toast.makeText(MainActivity.this, tips, Toast.LENGTH_SHORT).show();
        mTips.setText(tips);

        Intent intent = new Intent(this, HttpService.class);
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
