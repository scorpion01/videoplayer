package byteshaft.com.recorder;

import android.app.Activity;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import java.io.IOException;

public class MainActivity extends Activity implements SurfaceHolder.Callback,
        View.OnClickListener, View.OnLongClickListener {

    private Camera mCamera;
    private SurfaceHolder mHolder;
    private MediaRecorder mediaRecorder;
    private Button rec;
    private int seekPosition;
    private MediaPlayer mediaPlayer;
    private boolean isRecording = false;
    private String filePath = (Environment.getExternalStoragePublicDirectory("Example.mp4").getAbsolutePath());
    private VideoOverlay overlay;

    private static class CAMERA {
        private static class ORIENTATION {
            static int PORTRAIT = 90;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rec = (Button) findViewById(R.id.recorder);
        rec.setOnClickListener(this);
        SurfaceView display = (SurfaceView) findViewById(R.id.display);
        mHolder = display.getHolder();
        mHolder.addCallback(this);
        display.setOnClickListener(this);
        display.setOnLongClickListener(this);
        Button videoPlayer = (Button) findViewById(R.id.button);
        videoPlayer.setOnClickListener(this);
        overlay = new VideoOverlay(getApplicationContext());

    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.recorder:
                if (!isRecording) {
                    recordVideo();
                    isRecording = true;
                    rec.setText("stop");
                } else {
                    mediaRecorder.stop();
                    mediaRecorder.release();
                    mCamera.release();
                    mCamera = null;
                    isRecording = false;
                    rec.setText("record");
                }
                break;
            case R.id.display:
                playVideo();
                break;
            case R.id.button:
                startPlayback();
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.display:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    seekPosition = mediaPlayer.getCurrentPosition();
                    startPlayback();
                }
        }
        return false;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        openCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    private void openCamera() {
        mCamera = Camera.open();
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.setDisplayOrientation(CAMERA.ORIENTATION.PORTRAIT);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void recordVideo() {
        mediaRecorder = new MediaRecorder();
        if (mCamera == null) {
           openCamera();
        }
        mCamera.unlock();
        mediaRecorder.setCamera(mCamera);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        CamcorderProfile camcorderProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P);
        mediaRecorder.setProfile(camcorderProfile);
        mediaRecorder.setOrientationHint(90);
        mediaRecorder.setOutputFile(filePath);
        try {
            mediaRecorder.setPreviewDisplay(mHolder.getSurface());
            mediaRecorder.prepare();

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mediaRecorder.start();
    }

    private void playVideo() {
        Uri uri1 = Uri.parse(filePath);
        mediaPlayer = new MediaPlayer();
        if (mediaPlayer.isPlaying()){
            mediaPlayer.reset();
        }

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setDisplay(mHolder);

        try {
            mediaPlayer.setDataSource(getApplicationContext(), uri1);
            mediaPlayer.prepare();
        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            e.printStackTrace();
        }

        mediaPlayer.start();
    }

    private void startPlayback() {
        overlay.setVideoFile(filePath);
        overlay.setVideoStartPosition(seekPosition);
        overlay.startPlayback();
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }
}
