package com.c.speeddemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class DashBoardActivity extends AppCompatActivity {

    public static final int MSG_CONNECTING=0;
    public static final int MSG_CONNECTED=1;
    public static final int MSG_CONNECT_FAIL =2;
    public static final int MSG_CONNECT_DATE =3;
    private TextView mSpeed;
    private BluetoothSocket mBlueSocket ;
    private BluetoothDevice mDevice;
    private InputStream mBlueinputStream;
    private OutputStream mBlueoutputStream;
    private Thread mWorkerThread = new Thread(new WorkerThread());
    private boolean mWorkerExit=false;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandle = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_CONNECTING:
                    mSpeed.setText("等待连接中...");
                    break;
                case MSG_CONNECTED:
                    mSpeed.setText("连接成功");
                    mWorkerThread.start();
                    break;
                case MSG_CONNECT_FAIL:
                    mSpeed.setText("连接失败");
                    break;
                case MSG_CONNECT_DATE:
                    String data = (String) msg.getData().get("data");
                    /*
                        车速数据原始格式 41 0D XX\r\r>
                        XX 是转速的16进制行驶
                     */
                    assert data != null;
                    String[] str =data.split(" ");
                    char[] hexdata = str[2].toCharArray();
                    Integer speed =charToByte(hexdata[0])*16+charToByte(hexdata[1]);
                    mSpeed.setText(speed.toString());
            }
        }
    };

    private byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);
        Intent intent = getIntent();
        mDevice=intent.getParcelableExtra("device");
        Log.i("DashBoardActivity device", mDevice.getName());
        mSpeed = (TextView) findViewById(R.id.tv_speed_show);
        mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        Thread connectThread=new Thread(new ConnectThread());
        connectThread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mBlueSocket!=null){
            try {
                mBlueSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(mWorkerThread.isAlive()){
            mWorkerExit=true;
        }
    }

    class ConnectThread implements Runnable {
        public void run() {

            Message message = new Message();
            message.what = DashBoardActivity.MSG_CONNECTING;
            DashBoardActivity.this.mHandle.sendMessage(message);

            Message message_fail=new Message();
            message_fail.what=DashBoardActivity.MSG_CONNECT_FAIL;
            // 开始建立蓝牙连接
            UUID uuid= UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

            try {
                mBlueSocket =mDevice.createRfcommSocketToServiceRecord(uuid);
                mBlueSocket.connect();
                Message message_ok=new Message();
                message_ok.what=MSG_CONNECTED;
                mBlueinputStream = mBlueSocket.getInputStream();
                mBlueoutputStream = mBlueSocket.getOutputStream();
                DashBoardActivity.this.mHandle.sendMessage(message_ok);
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    mBlueSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                DashBoardActivity.this.mHandle.sendMessage(message_fail);
            }

        }
    }

    class WorkerThread implements Runnable{

        private void write(String str){
            try {
                byte[] bytes=str.getBytes();
                mBlueoutputStream.write(bytes);
                Log.i("Write:",str);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private String read(){
            try {
                String str= "";
                boolean isEnd=false;
                int times =0;
                while (!isEnd) {
                    times+=1;
                    Thread.sleep(10);
                    byte[] bytes = new byte[1024];
                    mBlueinputStream.read(bytes);
                    str=str+new String(bytes);
                    if(str.contains(">")){
                            isEnd=true;
                    }
                    if(times>20){
                        isEnd=true;
                    }
                }
                Log.i("Read:", str);
                return str;
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        public void run() {
            write("ATE0\r");
            read();
            while (!mWorkerExit){
                try {
                    write("010D\r\n");
                    Thread.sleep(120);
                    String str = read();
                    if(str==null || str.length()==0)
                        continue;
                    Message msg = new Message();
                    msg.what=DashBoardActivity.MSG_CONNECT_DATE;
                    Bundle bundle=new Bundle();
                    bundle.putString("data",str);
                    msg.setData(bundle);
                    DashBoardActivity.this.mHandle.sendMessage(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }
    /**
     * 得到车速、显示
     */
    private void getDashSpeed() {
        Intent intent = getIntent();
        String dash_speed = intent.getStringExtra("DASH_SPEED");
        mSpeed.setText(dash_speed);
    }
}
