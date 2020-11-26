package com.gnet.lanchat;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.gnet.lan_manager.utils.IpUtil;


public class MainActivity extends AppCompatActivity {


    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        refreshIP();
    }

    private void initView() {
        findViewById(R.id.masterBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LanChatActivity.Companion.openChat(view.getContext(), false);
            }
        });

        findViewById(R.id.slaveBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LanChatActivity.Companion.openChat(view.getContext(), true);
            }
        });

        textView = (TextView) findViewById(R.id.text1);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshIP();
            }
        });
    }

    private void refreshIP() {
        String ip1 = IpUtil.getHostIP();

        textView.setText("本机IP: " + ip1);
    }
}
