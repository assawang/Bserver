package com.jiagu.bserver;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Administrator on 2016/11/6.
 */

public class WaitWater extends AppCompatActivity
{
    public static void actionStart(Context context)
    {
        Intent intent = new Intent(context,WaitWater.class);
        context.startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wait_boiled);
        ActivityCollector.addActivity(this);

        SerialPortUtil sendProt = new SerialPortUtil();
        sendProt.openUrt("COM3",115200);

        String send_str = "020001330103";
        byte[] mBuffer = send_str.getBytes();
        sendProt.sendBuffer(mBuffer);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}
