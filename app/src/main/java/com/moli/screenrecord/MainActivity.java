package com.moli.screenrecord;

import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Toast;

import com.github.faucamp.simplertmp.RtmpHandler;

import net.ossrs.yasea.ScreenPublisher;
import net.ossrs.yasea.SrsEncodeHandler;
import net.ossrs.yasea.SrsRecordHandler;

import java.io.IOException;
import java.net.SocketException;

public class MainActivity extends AppCompatActivity implements OnClickListener, RtmpHandler.RtmpListener,
        SrsRecordHandler.SrsRecordListener, SrsEncodeHandler.SrsEncodeListener {
    private static final int REQUEST_CODE = 1;
    private static final String TAG = "MainActivity";
    private ScreenPublisher mPublisher;
    private MediaProjectionManager mMediaProjectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        findViewById(R.id.start).setOnClickListener(this);
        findViewById(R.id.stop).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start:
                mMediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                Intent captureIntent = mMediaProjectionManager != null ? mMediaProjectionManager.createScreenCaptureIntent() : null;
                startActivityForResult(captureIntent, REQUEST_CODE);
                break;
            case R.id.stop:
                if (mPublisher != null) {
                    mPublisher.stopPublish();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        MediaProjection mediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);
        if (mediaProjection == null) {
            Log.e("@@", "media projection is null");
            return;
        }
        mPublisher = new ScreenPublisher(mediaProjection);
        mPublisher.setEncodeHandler(new SrsEncodeHandler(this));
        mPublisher.setRtmpHandler(new RtmpHandler(this));
        mPublisher.setRecordHandler(new SrsRecordHandler(this));
        mPublisher.setPreviewResolution(640, 480);
        mPublisher.setOutputResolution(640, 480);
        mPublisher.switchToHardEncoder();//软解码
        mPublisher.setVideoSmoothMode();//低码率，速度快
        mPublisher.setScreenRecord();//录屏和相机区分
        String rtmpUrl = "";//你的rtmp推流地址
        mPublisher.startPublish(rtmpUrl);
        mPublisher.startScreen();//开始录屏
        Toast.makeText(getApplicationContext(), "开启录屏直播", Toast.LENGTH_SHORT).show();
//        moveTaskToBack(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPublisher != null) {
            mPublisher.stopPublish();
        }

    }

    private void handleException(Exception e) {
        try {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            mPublisher.stopPublish();
        } catch (Exception e1) {
            //
        }
    }

    // Implementation of SrsRtmpListener.

    @Override
    public void onRtmpConnecting(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRtmpConnected(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRtmpVideoStreaming() {
    }

    @Override
    public void onRtmpAudioStreaming() {
    }

    @Override
    public void onRtmpStopped() {
//        Toast.makeText(getApplicationContext(), "Stopped", Toast.LENGTH_SHORT).show();
        Log.e(TAG, "Stopped");
    }

    @Override
    public void onRtmpDisconnected() {
//        Toast.makeText(getApplicationContext(), "Disconnected", Toast.LENGTH_SHORT).show();
        Log.e(TAG, "Disconnected");
    }

    @Override
    public void onRtmpVideoFpsChanged(double fps) {
        Log.i(TAG, String.format("Output Fps: %f", fps));
    }

    @Override
    public void onRtmpVideoBitrateChanged(double bitrate) {
        int rate = (int) bitrate;
        if (rate / 1000 > 0) {
            Log.i(TAG, String.format("Video bitrate: %f kbps", bitrate / 1000));
        } else {
            Log.i(TAG, String.format("Video bitrate: %d bps", rate));
        }
    }

    @Override
    public void onRtmpAudioBitrateChanged(double bitrate) {
        int rate = (int) bitrate;
        if (rate / 1000 > 0) {
            Log.i(TAG, String.format("Audio bitrate: %f kbps", bitrate / 1000));
        } else {
            Log.i(TAG, String.format("Audio bitrate: %d bps", rate));
        }
    }

    @Override
    public void onRtmpSocketException(SocketException e) {
        handleException(e);
    }

    @Override
    public void onRtmpIOException(IOException e) {
        handleException(e);
    }

    @Override
    public void onRtmpIllegalArgumentException(IllegalArgumentException e) {
        handleException(e);
    }

    @Override
    public void onRtmpIllegalStateException(IllegalStateException e) {
        handleException(e);
    }

    // Implementation of SrsRecordHandler.

    @Override
    public void onRecordPause() {
//        Toast.makeText(getApplicationContext(), "Record paused", Toast.LENGTH_SHORT).show();
        Log.e(TAG, "Record paused");
    }

    @Override
    public void onRecordResume() {
//        Toast.makeText(getApplicationContext(), "Record resumed", Toast.LENGTH_SHORT).show();
        Log.e(TAG, "Record resumed");
    }

    @Override
    public void onRecordStarted(String msg) {
//        Toast.makeText(getApplicationContext(), "Recording file: " + msg, Toast.LENGTH_SHORT).show();
        Log.e(TAG, "Recording file: " + msg);
    }

    @Override
    public void onRecordFinished(String msg) {
//        Toast.makeText(getApplicationContext(), "MP4 file saved: " + msg, Toast.LENGTH_SHORT).show();
        Log.e(TAG, "MP4 file saved: " + msg);
    }

    @Override
    public void onRecordIOException(IOException e) {
        handleException(e);
    }

    @Override
    public void onRecordIllegalArgumentException(IllegalArgumentException e) {
        handleException(e);
    }

    // Implementation of SrsEncodeHandler.

    @Override
    public void onNetworkWeak() {
//        Toast.makeText(this, "Network weak", Toast.LENGTH_SHORT).show();
        Log.e(TAG, "Network weak");
    }

    @Override
    public void onNetworkResume() {
//        Toast.makeText(getApplicationContext(), "Network resume", Toast.LENGTH_SHORT).show();
        Log.e(TAG, "Network resume");
    }

    @Override
    public void onEncodeIllegalArgumentException(IllegalArgumentException e) {
        handleException(e);
    }
}
