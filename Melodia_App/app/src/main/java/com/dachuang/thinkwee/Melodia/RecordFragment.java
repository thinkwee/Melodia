package com.dachuang.thinkwee.Melodia;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sackcentury.shinebuttonlib.ShineButton;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by thinkwee on 2017/3/9.
 */

public class RecordFragment extends Fragment {
    private View v;
    private float recLen = 0;
    private TextView tv_record;
    private FloatingActionButton fabrecord;
    private info.abdolahi.CircularMusicProgressBar pb;
    private Boolean isRecording = false;
    private Boolean isUploadingIcon = false;
    private Boolean isPlaying = false;
    private Boolean isPressUpload = true;
    private ShineButton shinebt, uploadbt;
    private RecordTask recorder;
    private boolean shinebtstatus = false;
    private String TAG = "touch";
    private static int audioRate = 8000;
    private PlayTask player;
    private File pcmFile;
    private float recTime = 0;
    //WAV文件
    private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    private int bufferSize = AudioRecord.getMinBufferSize(audioRate, channelConfig, audioEncoding);
    //wav文件目录
    private String WavFileName;
    //pcm文件目录
    private String PcmFileName;
    private float ox, oy;
    private FragmentManager manager;
    private FragmentTransaction ft;

    Handler handler = new Handler();

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_record, container, false);
        pb = (info.abdolahi.CircularMusicProgressBar) v.findViewById(R.id.progressbar);
        tv_record = (TextView) v.findViewById(R.id.tv_record);
        shinebt = (ShineButton) v.findViewById(R.id.bt_shine);
        uploadbt = (ShineButton) v.findViewById(R.id.bt_upload);
        tv_record.setText("点击按钮开始录音");
        fabrecord = (FloatingActionButton) v.findViewById(R.id.fabrecord);
        shinebt.setShapeResource(R.drawable.ic_face_black_24dp);
        uploadbt.setShapeResource(R.drawable.ic_check_circle_black_24dp);
        uploadbt.setVisibility(View.INVISIBLE);
        manager = getFragmentManager();

        //创建文件,注意这里的格式为.pcm

        PcmFileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/Melodia.pcm";
        pcmFile = new File(PcmFileName);
        WavFileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/Melodia.wav";

        fabrecord.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        uploadbt.setVisibility(View.INVISIBLE);
                        if (isUploadingIcon) {
                            isPressUpload = false;
                            uploadbt.performClick();
                            isPressUpload = true;
                            isUploadingIcon = !isUploadingIcon;
                        }

                        Log.i(TAG, "ACTION_DOWN");
                        if (!shinebtstatus) {
                            shinebt.performClick();
                            shinebtstatus = true;
                        }
                        ox = event.getX();
                        oy = event.getY();

                        isRecording = true;
                        recLen = 0;
                        recTime = 0;
                        pb.setValue(0);
                        fabrecord.setImageResource(R.drawable.ic_stop_white_24dp);
                        Snackbar.make(fabrecord, "开始录音", Snackbar.LENGTH_SHORT)
                                .setAction("Action", null).show();

                        recorder = new RecordTask();
                        recorder.execute();
                        handler.postDelayed(runrecord, 0);

                        break;
                    case MotionEvent.ACTION_UP:
                        handler.removeCallbacks(runrecord);
                        Log.i(TAG, "ACTION_UP");
                        if (shinebtstatus) {
                            shinebt.performClick();
                            shinebtstatus = false;
                        }
                        float x1 = event.getX();
                        float y1 = event.getY();
                        float dis1 = (x1 - ox) * (x1 - ox) + (y1 - oy) * (y1 - oy);

                        isRecording = false;
                        pb.setValue(0);
                        fabrecord.setImageResource(R.drawable.ic_fiber_manual_record_white_24dp);
                        if (dis1 > 30000) {
                            Snackbar.make(fabrecord, "取消录音", Snackbar.LENGTH_SHORT)
                                    .setAction("Action", null).show();
                        } else {
                            if (!isUploadingIcon) {
                                uploadbt.setVisibility(View.VISIBLE);
                                isPressUpload = false;
                                uploadbt.performClick();
                                isPressUpload = true;
                                isUploadingIcon = !isUploadingIcon;
                            } else {

                            }

                            Snackbar.make(fabrecord, "录音完成", Snackbar.LENGTH_SHORT)
                                    .setAction("Action", null).show();
                            handler.postDelayed(runreplay, 0);
                            replay();
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float x2 = event.getX();
                        float y2 = event.getY();
                        float dis2 = (x2 - ox) * (x2 - ox) + (y2 - oy) * (y2 - oy);
                        if (dis2 > 30000) {
                            fabrecord.setImageResource(R.drawable.ic_cancel_white_24dp);
                        } else {
                            fabrecord.setImageResource(R.drawable.ic_stop_white_24dp);
                        }
                        break;
                }
                return true;
            }
        });

        uploadbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPressUpload) {
                    isUploadingIcon = !isUploadingIcon;
                    UploadFragment uploadfragment = new UploadFragment();
                    ft = manager.beginTransaction();
                    ft.replace(R.id.frame, uploadfragment);
//                    ft.addToBackStack(null);
                    ft.commit();
                }
            }
        });


        return v;
    }

    private void replay() {
        isPlaying = true;
        Snackbar.make(fabrecord, "开始重放", Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
        player = new PlayTask();
        player.execute();
    }

    private void stopreplay() {
        isPlaying = false;
        handler.removeCallbacks(runreplay);
        Snackbar.make(fabrecord, "结束重放", Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
    }


    Runnable runrecord = new Runnable() {
        @Override
        public void run() {
            recTime += 0.1;
            recLen += 0.3;
            pb.setValue(recLen);
            recLen += 0.3;
            pb.setValue(recLen);
            tv_record.setText("" + (int) recTime + "秒");
            handler.postDelayed(this, 100);
        }
    };

    Runnable runreplay = new Runnable() {
        @Override
        public void run() {
            if (recTime >= 0 && recLen >= 0) {
                recTime -= 0.1;
                recLen -= 0.3;
                pb.setValue(recLen);
                recLen -= 0.3;
                pb.setValue(recLen);
                tv_record.setText("" + (int) recTime + "秒");
                handler.postDelayed(this, 100);
            }
        }
    };

    private class RecordTask extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... arg0) {
            isRecording = true;
            try {
                //开通输出流到指定的文件
                DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(pcmFile)));
                //根据定义好的几个配置，来获取合适的缓冲大小
                int bufferSize = AudioRecord.getMinBufferSize(audioRate, channelConfig, audioEncoding);
                //实例化AudioRecord
                AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.MIC, audioRate, channelConfig, audioEncoding, bufferSize);
                //定义缓冲
                short[] buffer = new short[bufferSize];

                //开始录制
                record.startRecording();

                int r = 0; //存储录制进度
                //定义循环，根据isRecording的值来判断是否继续录制
                while (isRecording) {
                    //从bufferSize中读取字节，返回读取的short个数
                    //这里老是出现buffer overflow，不知道是什么原因，试了好几个值，都没用，TODO：待解决
                    int bufferReadResult = record.read(buffer, 0, buffer.length);
                    //循环将buffer中的音频数据写入到OutputStream中
                    for (int i = 0; i < bufferReadResult; i++) {
                        dos.writeShort(buffer[i]);
                    }
                    publishProgress(new Integer(r)); //向UI线程报告当前进度
                    r++; //自增进度值
                }
                //录制结束
                record.stop();
                convertWaveFile();
                dos.close();
            } catch (Exception e) {
                // TODO: handle exception
            }
            return null;
        }
    }

    private class PlayTask extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... arg0) {
            isPlaying = true;
            int bufferSize = AudioTrack.getMinBufferSize(audioRate, channelConfig, audioEncoding);
            short[] buffer = new short[bufferSize / 4];
            try {
                //定义输入流，将音频写入到AudioTrack类中，实现播放
                DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(pcmFile)));
                //实例AudioTrack
                AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, audioRate, channelConfig, audioEncoding, bufferSize, AudioTrack.MODE_STREAM);
                //开始播放
                track.play();
                //由于AudioTrack播放的是流，所以，我们需要一边播放一边读取
                while (isPlaying && dis.available() > 0) {
                    int i = 0;
                    while (dis.available() > 0 && i < buffer.length) {
                        buffer[i] = dis.readShort();
                        i++;
                    }
                    //然后将数据写入到AudioTrack中
                    track.write(buffer, 0, buffer.length);

                }

                //播放结束
                track.stop();
                dis.close();
                stopreplay();
            } catch (Exception e) {
                // TODO: handle exception
            }
            return null;
        }
    }

    public void convertWaveFile() {
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = audioRate;
        int channels = 1;
        long byteRate = 16 * audioRate * channels / 8;
        byte[] data = new byte[bufferSize];
        try {
            in = new FileInputStream(PcmFileName);
            out = new FileOutputStream(WavFileName);
            totalAudioLen = in.getChannel().size();
            //由于不包括RIFF和WAV
            totalDataLen = totalAudioLen + 36;
            WriteWaveFileHeader(out, totalAudioLen, totalDataLen, longSampleRate, channels, byteRate);
            while (in.read(data) != -1) {
                out.write(data);
            }
            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen, long totalDataLen, long longSampleRate,
                                     int channels, long byteRate) throws IOException {
        byte[] header = new byte[45];
        header[0] = 'R'; // RIFF
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);//数据大小
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';//WAVE
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        //FMT Chunk
        header[12] = 'f'; // 'fmt '
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';//过渡字节
        //数据大小
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        //编码方式 10H为PCM编码格式
        header[20] = 1; // format = 1
        header[21] = 0;
        //通道数
        header[22] = (byte) channels;
        header[23] = 0;
        //采样率，每个通道的播放速度
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        //音频数据传送速率,采样率*通道数*采样深度/8
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        // 确定系统一次要处理多少个这样字节的数据，确定缓冲区，通道数*采样位数
        header[32] = (byte) (1 * 16 / 8);
        header[33] = 0;
        //每个样本的数据位数
        header[34] = 16;
        header[35] = 0;
        //Data chunk
        header[36] = 'd';//data
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        header[44] = 0;
        out.write(header, 0, 45);
    }

}
