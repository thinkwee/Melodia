package com.dachuang.thinkwee.Melodia;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sackcentury.shinebuttonlib.ShineButton;
import com.victor.loading.newton.NewtonCradleLoading;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by thinkwee on 2017/3/9.
 */

public class UploadFragment extends Fragment {
    private View v;
    private FloatingActionButton soc_connect;
    private Socket soc;
    private String IPget = "";
    private int port = 2017;
    private String TAG = "Upload";
    private boolean running = false;
    private Handler myhandler;
    private Bitmap bmp = null;
    private EditText tv_ipaddr, tv_port;
    private SeekBar sex, speed;
    private static final int SIZE = 4096;
    private TextView tvsex, tvspeed;
    private String MidiFileName;
    private int lowf = 50, highf = 500, interval = 100;
    private File midiFile;
    private String md5;
    private int startflag = 0;
    private ShineButton turntoplaybt;
    boolean isjustani = true;
    private FragmentManager manager;
    private FragmentTransaction ft;
    private NewtonCradleLoading loading;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_upload, container, false);
        turntoplaybt = (ShineButton) v.findViewById(R.id.bt_turntoplay);
        loading = (NewtonCradleLoading) v.findViewById(R.id.loadingview);
        turntoplaybt.setShapeResource(R.drawable.ic_check_circle_black_24dp);
        loading.setLoadingColor(getResources().getColor(R.color.colorPrimary));
//        loading.start();
        turntoplaybt.setVisibility(View.INVISIBLE);
        manager = getFragmentManager();
        return v;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        soc_connect = (FloatingActionButton) getActivity().findViewById(R.id.socketconnect);
        FloatingActionButton soc_send = (FloatingActionButton) getActivity().findViewById(R.id.socketsend);
        soc_connect.setImageResource(R.drawable.ic_backup_black_24dp);
        soc_send.setImageResource(R.drawable.ic_menu_send);
        tv_ipaddr = (EditText) getActivity().findViewById(R.id.tv_ipaddr);
        sex = (SeekBar) getActivity().findViewById(R.id.seekBarsex);
        speed = (SeekBar) getActivity().findViewById(R.id.seekBarspeed);
        tvsex = (TextView) getActivity().findViewById(R.id.tv_sex);
        tvspeed = (TextView) getActivity().findViewById(R.id.tv_speed);
        tv_port = (EditText) getActivity().findViewById(R.id.tv_port);

        myhandler = new MyHandler();

        turntoplaybt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isjustani) {
                    PlayFragment playfragment = new PlayFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("md5", md5);
                    playfragment.setArguments(bundle);
                    playfragment.init();
                    ft = manager.beginTransaction();
                    ft.replace(R.id.frame, playfragment);
//                    ft.addToBackStack(null);
                    turntoplaybt.setVisibility(View.INVISIBLE);
                    ft.commit();
                }

            }
        });

        soc_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!running) {
                    running = true;
                    IPget = tv_ipaddr.getText().toString();
                    port = Integer.valueOf(tv_port.getText().toString());
                    soc_connect.setImageResource(R.drawable.ic_cancel_white_24dp);
                    startflag = 0;
                    StartThread st = new StartThread();
                    Log.i(TAG, "bt_connect");
                    st.start();
                } else {
                    running = false;
                    Log.i(TAG, "bt_disconnect");
                    soc_connect.setImageResource(R.drawable.ic_backup_black_24dp);
                    OutputStream os = null;
                    try {
                        soc.close();
                        Message msg = myhandler.obtainMessage();
                        msg.what = 3;
                        myhandler.sendMessage(msg);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        Message msg = myhandler.obtainMessage();
                        msg.what = 4;
                        myhandler.sendMessage(msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        soc_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (running) {
                    MsgThread ms = new MsgThread();
                    Message msg = myhandler.obtainMessage();
                    msg.what = 2;
                    myhandler.sendMessage(msg);
                    ms.start();
                } else {
                    Message msg = myhandler.obtainMessage();
                    msg.what = 6;
                    myhandler.sendMessage(msg);
                }
            }
        });

        sex.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()

        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                switch (progress) {
                    case 0:
                        tvsex.setText("高音");
                        lowf = 200;
                        highf = 700;
                        break;
                    case 1:
                        tvsex.setText("中音");
                        lowf = 100;
                        highf = 500;
                        break;
                    case 2:
                        tvsex.setText("低音");
                        lowf = 50;
                        highf = 400;
                        break;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        speed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()

        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                switch (progress) {
                    case 0:
                        tvspeed.setText("慢速");
                        interval = 250;
                        break;
                    case 1:
                        tvspeed.setText("中速");
                        interval = 200;
                        break;
                    case 2:
                        tvspeed.setText("快速");
                        interval = 100;
                        break;
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    private class StartThread extends Thread {
        @Override
        public void run() {
            try {
                soc = new Socket(IPget, port);
                if (soc.isConnected()) {//成功连接获取soc对象则发送成功消息
//                    Log.i(TAG, "isConnected");
                    Message msg = myhandler.obtainMessage();
                    msg.what = 0;
                    myhandler.sendMessage(msg);
                } else {
                    soc.close();
                    Message msg = myhandler.obtainMessage();
                    msg.what = 7;
                    myhandler.sendMessage(msg);
                }
            } catch (IOException e) {
                Log.i(TAG, "Connect Failed");
                e.printStackTrace();
            }
        }
    }

    private class MsgThread extends Thread {
        @Override
        public void run() {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/Melodia.wav");
            FileInputStream reader = null;
            try {
                reader = new FileInputStream(file);
                int len = reader.available();
                byte[] buff = new byte[len];
                reader.read(buff);
                String data = Base64.encodeToString(buff, Base64.DEFAULT);
                String senda = makejson(1, "a", data).toString();
                Log.i(TAG, "request1: " + senda);
                OutputStream os = null;
                InputStream is = null;
                DataInputStream in = null;
                try {
                    os = soc.getOutputStream();
                    BufferedReader bra = null;
                    os.write(senda.getBytes());
                    os.write("endbidou1".getBytes());
                    os.flush();
                    Log.i(TAG, "request1 send successful");
                    if (soc.isConnected()) {
                        is = soc.getInputStream();
                        bra = new BufferedReader(new InputStreamReader(is));
                        md5 = bra.readLine();
                        Log.i(TAG, "md5: " + md5);
                        bra.close();
                    } else
                        Log.i(TAG, "socket closed while reading");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                soc.close();
                startflag = 1;

                StartThread st = new StartThread();
                st.start();

                while (soc.isClosed()) ;

                String sendb = makejson(2, md5, "request2").toString();
                Log.i(TAG, "request2: " + sendb);
                os = soc.getOutputStream();
                os.write(sendb.getBytes());
                os.write("endbidou1".getBytes());
                os.flush();
                Log.i(TAG, "request2 send successful");

                is = soc.getInputStream();
                byte buffer[] = new byte[1024 * 100];
                is.read(buffer);
                Log.i(TAG, "midifilecontent: " + buffer.toString());
                soc.close();
                File filemid = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/Melodia.mid");
                FileOutputStream writer = null;
                writer = new FileOutputStream(filemid);
                writer.write(buffer);
                writer.close();
                Message msg = myhandler.obtainMessage();
                msg.what = 1;
                myhandler.sendMessage(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }


    private class MyHandler extends Handler {//在主线程处理Handler传回来的message

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Log.i(TAG, "连接成功");
                    if (startflag == 0)
                        Snackbar.make(soc_connect, "连接成功", Snackbar.LENGTH_SHORT)
                                .setAction("Action", null).show();

                    break;
                case 1:
//                    String strrecv = (String) msg.obj;
                    Log.i(TAG, "MIDI转换完成");
                    loading.stop();
                    Snackbar.make(soc_connect, "MIDI转换完成", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                    turntoplaybt.setVisibility(View.VISIBLE);
                    isjustani = true;
                    turntoplaybt.performClick();
                    isjustani = false;
                    loading.setVisibility(View.INVISIBLE);
                    break;
                case 2:
                    Log.i(TAG, "MIDI转换开始");
                    loading.setLoadingColor(R.color.mainPink);
                    loading.setVisibility(View.VISIBLE);
                    loading.start();
                    break;
                case 3:
                    Log.i(TAG, "服务器安全断开");
                    Snackbar.make(soc_connect, "服务器安全断开", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                    break;
                case 4:
                    Log.i(TAG, "断开连接中发生错误");
                    Snackbar.make(soc_connect, "断开连接中发生错误", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                    break;
                case 5:

                    break;
                case 6:
                    Snackbar.make(soc_connect, "未连接到服务器", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                    break;
                case 7:
                    Snackbar.make(soc_connect, "无法连接服务器", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                default:
                    break;
            }
        }

    }


    private JSONObject makejson(int request, String identifycode, String data) {
        if (identifycode == "a") {
            try {
                JSONObject pack = new JSONObject();
                pack.put("request", request);
                JSONObject config = new JSONObject();
                config.put("n", lowf);
                config.put("m", highf);
                config.put("w", interval);
                pack.put("config", config);
                pack.put("data", data);
                return pack;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            try {
                JSONObject pack = new JSONObject();
                pack.put("request", request);
                pack.put("config", "");
                pack.put("data", identifycode);
                return pack;
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return null;
    }

}
