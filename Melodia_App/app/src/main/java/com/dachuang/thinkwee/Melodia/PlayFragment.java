package com.dachuang.thinkwee.Melodia;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by thinkwee on 2017/3/9.
 */

public class PlayFragment extends Fragment {
    private View v;
    private FloatingActionButton bt_play;
    private MediaPlayer player;
    private ImageView showpic;
    private boolean playflag = false;
    private String md5;
    private ImageLoader imageLoader;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_play, container, false);
        bt_play = (FloatingActionButton) v.findViewById(R.id.bt_play);
        bt_play.setImageResource(R.drawable.ic_play_arrow_white_24dp);
        showpic = (ImageView) v.findViewById(R.id.pic);
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getActivity())
                .build();
        ImageLoader.getInstance().init(config);
        imageLoader = ImageLoader.getInstance();
        return v;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        showpic.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Bitmap drawingCache = getViewBitmap(showpic);
                if (drawingCache == null) {
                    Log.i("play", "no img to save");
                } else {
                    try {
                        File imageFile = new File(Environment.getExternalStorageDirectory(), "saveImageview.jpg");
                        Toast toast = Toast.makeText(getActivity(),
                                "", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.TOP, 0, 200);
                        toast.setText("分享图片");
                        toast.show();
                        FileOutputStream outStream;
                        outStream = new FileOutputStream(imageFile);
                        drawingCache.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                        outStream.flush();
                        outStream.close();

                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(imageFile));
                        sendIntent.setType("image/png");
                        getActivity().startActivity(Intent.createChooser(sendIntent, "分享到"));

                    } catch (IOException e) {
                        Log.i("play", "share img wrong");
                    }
                }
                return true;
            }
        });

        bt_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player != null) {
                    player.pause();
                }
                if (!playflag) {
                    playflag = true;
                    bt_play.setImageResource(R.drawable.ic_stop_white_24dp);
                    bt_play.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    YoYo.with(Techniques.Tada)
                            .duration(400)
                            .repeat(5)
                            .playOn(bt_play);
                    File midiFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/Melodia.mid");
                    player = MediaPlayer.create(getActivity(), Uri.fromFile(midiFile));
                    player.start();
                } else {
                    YoYo.with(Techniques.RubberBand)
                            .duration(1000)
                            .repeat(1)
                            .playOn(bt_play);
                    bt_play.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                    bt_play.setBackgroundColor(getResources().getColor(R.color.mainPink));
                    playflag = false;
                    if (player != null) {
                        player.stop();
                    }
                }

            }
        });

    }

    private Bitmap getViewBitmap(View view) {
        if (view == null) {
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    public void init() {
        md5 = getArguments().getString("md5");
        final String imageUri = "http://60.10.6.106/server/" + md5 + "_1.png";
        Log.i("play", "pngfile: " + imageUri);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                //execute the task
                imageLoader.displayImage(imageUri, showpic);
            }
        }, 2000);

    }


}
