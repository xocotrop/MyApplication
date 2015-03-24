package com.example.codal.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.IOException;


public class PreviewActivity extends Activity implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, TextureView.SurfaceTextureListener {

    private SurfaceView surfaceView;
    private SurfaceHolder holder;
    private MediaPlayer mediaPlayer;
    String arquivo;
    private TextureView textureView;

    private float mVideoWidth;
    private float mVideoHeight;
    private Button btnOk;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        /*surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        holder = surfaceView.getHolder();*/
        textureView = (TextureView) findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(this);
        btnOk = (Button) findViewById(R.id.btnOK);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(getBaseContext(), ListaActivity.class);
                startActivity(it);
                finish();
            }
        });
        mediaPlayer = new MediaPlayer();

        File dir = new File(Environment.getExternalStorageDirectory(), this.getClass().getPackage().getName());

        File arquivo = new File(dir, "final.mp4");
        this.arquivo = arquivo.getAbsolutePath();
        calculateVideoSize();
/*        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying())
                {
                    mediaPlayer.pause();
                } else {
                    mediaPlayer.start();
                }
            }
        });*/
        textureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying())
                {
                    mediaPlayer.pause();
                } else {
                    mediaPlayer.start();
                }
            }
        });
        //holder.addCallback(this);
        mediaPlayer.setOnPreparedListener(this);



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_preview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mediaPlayer.setDisplay(holder);
        try {
            create43RatioSurface(surfaceView);
            mediaPlayer.setDataSource(arquivo);
            //ViewGroup.LayoutParams params = surfaceView.getLayoutParams();
            //params.height = params.width;
            //surfaceView.setLayoutParams(params);

            holder.setFixedSize(400, 400);
            mediaPlayer.prepareAsync();
            //surfaceView.invalidate();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    private void create43RatioSurface(SurfaceView surfaceView) {


        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int height = 0;
        int width = 0;

        if(metrics.widthPixels < metrics.heightPixels){
            width = metrics.widthPixels;
            height= (metrics.widthPixels/4) * 3 ;
        } else {
            height= metrics.heightPixels;
            width= (metrics.heightPixels/4) * 3 ;
        }

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);

        surfaceView.setLayoutParams(layoutParams);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Surface s = new Surface(surface);
        mediaPlayer.setSurface(s);
        ViewGroup.LayoutParams layoutParams = textureView.getLayoutParams();
        layoutParams.height = textureView.getWidth();
        textureView.setLayoutParams(layoutParams);
        int viewWidth = textureView.getWidth();
        int viewHeight = viewWidth;
        float scaleX = 1.0f;
        float scaleY = 1.0f;

        if (mVideoWidth > viewWidth && mVideoHeight > viewHeight) {
            scaleX = (float)mVideoWidth / (float)viewWidth;
            scaleY = mVideoHeight / viewHeight;
        } else if (mVideoWidth < viewWidth && mVideoHeight < viewHeight) {
            scaleY = viewWidth / mVideoWidth;
            scaleX = viewHeight / mVideoHeight;
        } else if (viewWidth > mVideoWidth) {
            scaleY = (viewWidth / mVideoWidth) / (viewHeight / mVideoHeight);
        } else if (viewHeight > mVideoHeight) {
            scaleX = (viewHeight / mVideoHeight) / (viewWidth / mVideoWidth);
        } else if(mVideoWidth > viewWidth){
            scaleY = (float)mVideoWidth / (float)viewWidth;

        }

        // Calculate pivot points, in our case crop from center
        int pivotPointX = viewWidth / 2;
        int pivotPointY = viewHeight / 2;

        Matrix matrix = new Matrix();
        matrix.setScale(scaleX, scaleY, 0, 0);

        textureView.setTransform(matrix);

        try {
            mediaPlayer.setDataSource(arquivo);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //ViewGroup.LayoutParams params = surfaceView.getLayoutParams();
        //params.height = params.width;
        //surfaceView.setLayoutParams(params);

        //holder.setFixedSize(400, 400);
        mediaPlayer.prepareAsync();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    private void calculateVideoSize() {
        try {
            //AssetFileDescriptor afd = getAssets().openFd(FILE_NAME);
            MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
            metaRetriever.setDataSource(arquivo);
            String height = metaRetriever
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            String width = metaRetriever
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            mVideoHeight = Float.parseFloat(height);
            mVideoWidth = Float.parseFloat(width);

        } catch (NumberFormatException e) {
            Log.d("TAGGG", e.getMessage());
        }
    }
}
