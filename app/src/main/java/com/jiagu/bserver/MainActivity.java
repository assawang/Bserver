package com.jiagu.bserver;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jiagu.util.HttpRequest;
import com.jiagu.util.PayCommonUtil;
import com.google.zxing.*;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.SortedMap;
import java.util.TreeMap;
public class MainActivity extends AppCompatActivity
{
    HashMap<Object,Object> hm=new HashMap<Object,Object>();
    final int SUCCESS=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Looper mainLooper = Looper.getMainLooper();
        Button bt_getQrcode = (Button) findViewById(R.id.bt_getQrcode);
        final ImageView im_qrcode=(ImageView)findViewById(R.id.iv_qrcode);

        final EHandler eHandler=new EHandler();
        final Bitmap[] bm = {null};
        bt_getQrcode.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "正在获取", Toast.LENGTH_LONG).show();
                try{

                    Thread thread=new Thread(){
                        @Override
                        public void run(){
                            try{
                                hm=makeOrder();
                                bm[0] =createQRImage((String)hm.get("code_url"),300,300);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    };
                    thread.start();
                    thread.join();
                    im_qrcode.setImageBitmap(bm[0]);

                    Thread  thread1=new Thread(){
                        @Override
                        public void run(){
                            try{
                                queryOrder();
                                Message msg=eHandler.obtainMessage(SUCCESS);
                                eHandler.sendMessage(msg);
                            }catch (Exception e){
                                e.printStackTrace();
                            }

                        }
                    };
                    thread1.start();
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });
    }


    public HashMap makeOrder() {
        String appid = "wx3f8f87562cbee92b";
        String mchid = "1374012602";
        String key = "GIMIS20160809100000GIMISWEIXINZF";

        String currTime = PayCommonUtil.getCurrTime();
        String strTime = currTime.substring(8, currTime.length());
        String strRandom = PayCommonUtil.buildRandom(4) + "";
        String nonce_str = strTime + strRandom;



        SortedMap<Object, Object> packageParams = new TreeMap<Object, Object>();

        packageParams.put("appid", appid);
        packageParams.put("mch_id", mchid);
        packageParams.put("nonce_str", nonce_str);
        packageParams.put("body", "测试商品");
        packageParams.put("out_trade_no", nonce_str);

        packageParams.put("total_fee", "1");
        packageParams.put("spbill_create_ip", "10.14.125.1");
        packageParams.put("notify_url", "http://wxpay.weixin.qq.com/pub_v2/pay/notify.v2.php");
        packageParams.put("trade_type", "NATIVE");

        String sign = PayCommonUtil.createSign("UTF-8", packageParams, key);
        packageParams.put("sign", sign);


        String requestXML = PayCommonUtil.getRequestXml(packageParams);
        String sss = requestXML;
        System.out.println("requestXML===="+requestXML);
        String returnXML = HttpRequest.post("https://api.mch.weixin.qq.com/pay/unifiedorder").send(sss).body();
        System.out.println(returnXML);

        SAXBuilder sb = new SAXBuilder();
        Document doc = null;
        Element ele = null;
        try {
            doc = sb.build(new java.io.ByteArrayInputStream(returnXML.getBytes()));
            ele = doc.getRootElement();
            System.out.println("URL:=====" + ele.getChild("code_url").getValue());

        } catch (Exception e) {
            e.printStackTrace();
        }
        hm.put("code_url",ele.getChild("code_url").getValue());
        hm.put("out_trade_no",nonce_str);
        return hm;

    }

    /**
     * 生成二维码 要转换的地址或字符串,可以是中文
     *
     * @param url
     * @param width
     * @param height
     * @return
     */
    public Bitmap createQRImage(String url, final int width, final int height) {
        try {
            // 判断URL合法性
            if (url == null || "".equals(url) || url.length() < 1) {
                return null;
            }
            Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            // 图像数据转换，使用了矩阵转换
            BitMatrix bitMatrix = new QRCodeWriter().encode(url,
                    BarcodeFormat.QR_CODE, width, height, hints);
            int[] pixels = new int[width * height];
            // 下面这里按照二维码的算法，逐个生成二维码的图片，
            // 两个for循环是图片横列扫描的结果
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * width + x] = 0xff000000;
                    } else {
                        pixels[y * width + x] = 0xffffffff;
                    }
                }
            }
            // 生成二维码图片的格式，使用ARGB_8888
            Bitmap bitmap = Bitmap.createBitmap(width, height,
                    Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            System.out.println("bitmap created!=================================");
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    void queryOrder(){
        String appid="wx3f8f87562cbee92b";
        String mchid="1374012602";
        String key="GIMIS20160809100000GIMISWEIXINZF";
        SortedMap<Object,Object> packageParams2=new TreeMap<Object, Object>();
        packageParams2.put("appid", appid);
        packageParams2.put("mch_id", mchid);
        packageParams2.put("out_trade_no", hm.get("out_trade_no"));
        packageParams2.put("nonce_str", "1add1a30ac87aa2sb72f57a2375d8fec");
        String sign2 = PayCommonUtil.createSign("UTF-8", packageParams2,key);
        packageParams2.put("sign", sign2);

        String requestXML2 = PayCommonUtil.getRequestXml(packageParams2);
        System.out.println(requestXML2);
        String sss=requestXML2;
        int code=HttpRequest.post("https://api.mch.weixin.qq.com/pay/orderquery").send(sss).code();
        System.out.println(code+"========================");
        String returnXML2=HttpRequest.post("https://api.mch.weixin.qq.com/pay/orderquery").send(sss).body();
        System.out.println(requestXML2);

        SAXBuilder sb = new SAXBuilder();
        Document doc = null;
        Element ele = null;
        try {
            doc = sb.build(new java.io.ByteArrayInputStream(returnXML2.getBytes()));
            ele = doc.getRootElement();
            System.out.println("state:=====" + ele.getChild("trade_state").getValue());
            while(!ele.getChild("trade_state").getValue().equals("SUCCESS")){
                returnXML2=HttpRequest.post("https://api.mch.weixin.qq.com/pay/orderquery").send(sss).body();
                doc = sb.build(new java.io.ByteArrayInputStream(returnXML2.getBytes()));
                ele = doc.getRootElement();
                System.out.println("state:=====" + ele.getChild("trade_state").getValue());
                Thread.sleep(2000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class EHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch(msg.what)
            {
                case  SUCCESS://在收到消息时，对界面进行更新
                    TextView tv=(TextView)findViewById((R.id.tv_state));
                    tv.setText("支付成功！");
                    WaitWater.actionStart(MainActivity.this);
                    break;
            }
        }
    }
}
