package com.coretronic.ssdpexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.coretronic.ssdpexample.ssdp.SSDPConstants;
import com.coretronic.ssdpexample.ssdp.SSDPSearchMsg;
import com.coretronic.ssdpexample.ssdp.SSDPSocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import static com.coretronic.ssdpexample.ssdp.SSDPConstants.*;

public class MainActivity extends AppCompatActivity {
    static final String TAG = MainActivity.class.getSimpleName();

    private WifiManager.MulticastLock multicastLock;
    private List<String> listReceive = new ArrayList<String>();
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.test_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMSearchMessage();
            }
        });
        textView = findViewById(R.id.textView);
        try {
            SSDPSocket.listNetworkInterface();
        }catch (SocketException e){
            e.printStackTrace();
        }
        new Thread(){
            @Override
            public void run() {
                SendMSearchMessage();
            }
        }.start();
    }

    /**
     * 获取组锁，使用后记得及时释放，否则会增加耗电。为了省电，Android设备默认关闭
     */
    private void acquireMultiLock() {
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        multicastLock = wm.createMulticastLock("multicastLock");
        multicastLock.setReferenceCounted(true);
        multicastLock.acquire();//使用后，需要及时关闭
    }

    /**
     * 释放组锁
     */
    private void releaseMultiLock() {
        if (null != multicastLock) {
            multicastLock.release();
        }
    }
    SSDPSocket sock = null;
    private void SendMSearchMessage() {
        acquireMultiLock();
        SSDPSearchMsg searchMsg = new SSDPSearchMsg(SSDPConstants.ST_ALL);

        try {
            //发送
            sock = new SSDPSocket();
            sock.send(searchMsg.toString());
            Log.i(TAG, "要发送的消息为：" + searchMsg.toString());
            //接收
            listReceive.clear();
            while (true) {
                DatagramPacket dp = sock.receive(); // Here, I only receive the same packets I initially sent above
                String c = new String(dp.getData()).trim();
                String ip = dp.getAddress().toString().trim();
                Log.e(TAG, "接收到的消息为：\n" + c + "\n来源IP地址：" + ip);
                listReceive.add(c);
                break;
//                //接收时候一遍后，直接跳出循环
//                if (listReceive.contains(c)) break;
//                else listReceive.add(c);
            }
            sock.close();
            releaseMultiLock();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //显示接收结果
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < listReceive.size(); i++) {
                    sb.append(i).append("\r\t").append(listReceive.get(i))
                            .append(NEWLINE).append("-----------------------").append(NEWLINE);
                }
                String s = sb.toString();
                textView.setText(s);
                Log.d(TAG, "result = " + s);
            }
        });
    }

}