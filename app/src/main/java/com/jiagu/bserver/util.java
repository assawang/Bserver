package com.jiagu.bserver;

import java.io.FileDescriptor;

/**
 * Created by Administrator on 2016/11/7.
 */

public class util
{
    static
    {
        System.loadLibrary("serial_port");
    }

    public native static FileDescriptor UartOpen(String path, int baudrate, int flags);
    public native static void UartClose(FileDescriptor fd);
}
