package com.jiagu.bserver;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Administrator on 2016/11/9.
 */

public class SerialPortUtil
{
    private String TAG = "SerialPortUtil";
    private SerialPort mSerialPort;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadThread mReadThread;
   // private String path = "COM3";
   // private int baudrate = 115200;
    private static SerialPortUtil portUtil;
    private OnDataReceiveListener onDataReceiveListener = null;
    private boolean isStop = false;

    public interface OnDataReceiveListener
    {
        public void onDataReceive(byte[] buffer, int size);
    }

    public void setOnDataReceiveListener(OnDataReceiveListener dataReceiveListener)
    {
        onDataReceiveListener = dataReceiveListener;
    }
    /*
     * 初始化串口信息
     */
    public void openUrt(String path,int baudrate)
    {
        try
        {
            mSerialPort = new SerialPort(new File(path), baudrate, 0);
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    public void recieve()
    {
        mReadThread = new ReadThread();
        isStop = false;
        mReadThread.start();
    }
    /*
    public void onCreate()
    {
        //try
        //{
            //mSerialPort = new SerialPort(new File(path), baudrate,0);
            //mOutputStream = mSerialPort.getOutputStream();
           // mInputStream = mSerialPort.getInputStream();

            mReadThread = new ReadThread();
            isStop = false;
            mReadThread.start();
        //}
        //catch (Exception e)
        //{
        //    e.printStackTrace();
        //}
    }*/
    /**
     * 发送指令到串口
     *
     * @param cmd
     * @return
     */
    public boolean sendCmds(String cmd)
    {
        boolean result = true;
        byte[] mBuffer = (cmd+"\r\n").getBytes();
        try
        {
            if (mOutputStream != null)
            {
                mOutputStream.write(mBuffer);
            }
            else
            {
                result = false;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public boolean sendBuffer(byte[] mBuffer)
    {
        boolean result = true;
        byte[] mBufferTemp = new byte[mBuffer.length];
        System.arraycopy(mBuffer, 0, mBufferTemp, 0, mBuffer.length);
        try
        {
            if (mOutputStream != null)
            {
                mOutputStream.write(mBufferTemp);
            }
            else
            {
                result = false;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    private class ReadThread extends Thread
    {
        @Override
        public void run()
        {
            super.run();
            while (!isStop && !isInterrupted())
            {
                int size;
                try
                {
                    if (mInputStream == null)
                        return;
                    byte[] buffer = new byte[512];
                    size = mInputStream.read(buffer);
                    if (size > 0)
                    {
                        String readData = new String(buffer);
                        Log.d(TAG,readData);
                        if (null != onDataReceiveListener)
                        {
                            onDataReceiveListener.onDataReceive(buffer, size);
                        }
                    }
                    Thread.sleep(10);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }
    /**
     * 关闭串口
     */
    public void closeSerialPort()
    {
        isStop = true;
        if (mReadThread != null)
        {
            mReadThread.interrupt();
        }
        if (mSerialPort != null)
        {
            mSerialPort.Close();
        }
    }
}
