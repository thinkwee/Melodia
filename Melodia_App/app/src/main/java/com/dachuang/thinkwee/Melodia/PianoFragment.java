package com.dachuang.thinkwee.Melodia;

import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by thinkwee on 2017/5/16.
 */

public class PianoFragment extends Fragment {
    private View v;
    private FloatingActionButton pianobt, samplebt;
    private SeekBar seekBaroctave, seekBarpianospeed;
    private TextView tvoctave, tvpianospeed;
    private byte[] param;
    private Socket soc;
    private boolean isconnected = false;
    private StartThread st;
    private EditText etpianoaddr, etpianoport;
    private String pianoaddr;
    private int pianoport;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_piano, container, false);
        pianobt = (FloatingActionButton) v.findViewById(R.id.pianobt);
        pianobt.setImageResource(R.drawable.ic_insert_emoticon_black_24dp);
        samplebt = (FloatingActionButton) v.findViewById(R.id.samplebt);
        samplebt.setImageResource(R.drawable.ic_style_black_24dp);

        seekBaroctave = (SeekBar) v.findViewById(R.id.SeekbarOctave);
        seekBarpianospeed = (SeekBar) v.findViewById(R.id.SeekbarPianoSpeed);
        tvoctave = (TextView) v.findViewById(R.id.tv_octave);
        tvpianospeed = (TextView) v.findViewById(R.id.tv_pianospeed);
        etpianoaddr = (EditText) v.findViewById(R.id.et_pianoaddr);
        etpianoport = (EditText) v.findViewById(R.id.et_pianoport);
        param = new byte[3];
        param[0] = 0x30;
        param[1] = 0x01;
        param[2] = 0x00;
        return v;
    }

    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        seekBaroctave.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                switch (progress) {
                    case 0:
                        tvoctave.setText("升两个八度");
                        param[2] = 24;
                        break;
                    case 1:
                        tvoctave.setText("升一个八度");
                        param[2] = 12;

                        break;
                    case 2:
                        tvoctave.setText("正常");
                        param[2] = 0;

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

        seekBarpianospeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                switch (progress) {
                    case 0:
                        tvpianospeed.setText("1/4倍速");
                        param[1] = 4;
                        break;
                    case 1:
                        tvpianospeed.setText("1/2倍速");
                        param[1] = 2;

                        break;
                    case 2:
                        tvpianospeed.setText("正常");
                        param[1] = 1;
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

        pianobt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isconnected) {
                    pianoaddr = etpianoaddr.getText().toString();
                    pianoport = Integer.valueOf(etpianoport.getText().toString());
                    param[0] = 0x30;
                    StartThread st = new StartThread();
                    st.start();
                    while (!isconnected) ;
                    MsgThread ms = new MsgThread();
                    ms.start();
                    YoYo.with(Techniques.Wobble)
                            .duration(300)
                            .repeat(6)
                            .playOn(seekBaroctave);
                    while (soc.isConnected()) ;
                    try {
                        soc.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    isconnected = false;
                    Log.i("piano", "socket closed");
                }


            }
        });

        samplebt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pianoaddr = etpianoaddr.getText().toString();
                pianoport = Integer.valueOf(etpianoport.getText().toString());
                param[0] = 0x31;
                StartThread st = new StartThread();
                st.start();
                while (!isconnected) ;
                MsgThread ms = new MsgThread();
                ms.start();
                YoYo.with(Techniques.Wobble)
                        .duration(300)
                        .repeat(6)
                        .playOn(seekBaroctave);
                while (soc.isConnected()) ;
                try {
                    soc.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                isconnected = false;
                Log.i("piano", "socket closed");

            }
        });


    }

    private class StartThread extends Thread {
        @Override
        public void run() {
            try {
                soc = new Socket(pianoaddr, pianoport);
                if (soc.isConnected()) {//成功连接获取soc对象则发送成功消息
                    Log.i("piano", "piano is Connected");
                    if (!isconnected)
                        isconnected = !isconnected;

                } else {
                    Snackbar.make(pianobt, "启动电子琴教学失败", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                    Log.i("piano", "Connect Failed");
                    soc.close();
                }
            } catch (IOException e) {
                Snackbar.make(pianobt, "启动电子琴教学失败", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
                Log.i("piano", "Connect Failed");
                e.printStackTrace();
            }
        }
    }

    private class MsgThread extends Thread {
        @Override
        public void run() {
            try {
                OutputStream os = soc.getOutputStream();
                os.write(param);
                os.flush();
                Log.i("piano", "piano msg send successful");
                Snackbar.make(pianobt, "正在启动启动电子琴教学", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();

                soc.close();
            } catch (IOException e) {
                Log.i("piano", "piano msg send successful failed");
                Snackbar.make(pianobt, "启动电子琴教学失败", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
                e.printStackTrace();
            }

        }
    }

}
