package com.example.codal.myapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.codal.myapplication.View.CameraSurface;
import com.example.codal.myapplication.util.CameraHelper;
import com.example.codal.myapplication.util.UtilCamera;
import com.googlecode.mp4parser.BasicContainer;
import com.googlecode.mp4parser.DataSource;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class MainActivity extends Activity implements SurfaceHolder.Callback, View.OnTouchListener {
    private CameraSurface surfaceView;
    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private final String URL = BuildConfig.var_URL;
    private Button btnGravar;
    private Button btnTirarFoto;
    private MediaRecorder mediaRecorder;
    private static final int TEMPO_MAXIMO = 11 * 1000;
    private boolean gravando = false;
    private SeekBar seekBar;
    private int tempoGravado = 0;
    private int numeroVideo = 0;
    private File pasta;
    private ImageView switchCam;
    private Button btnFinalizar;
    private ProgressBar progressBar;
    CamcorderProfile cp = null;
    private Handler handler = new Handler();
    private Activity activity;
    private ProgressDialog progressDialog;
    FrameLayout frameLayout;
    int mVideoWidth;
    int mVideoHeight;
    private static final int MODO_FOTO = 1;
    private static final int MODO_VIDEO = 2;
    private LinearLayout containerOpcoes;
    int alturaSurfaceView;
    int larguraSurfaceView;

    //private TextureView textureView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!UtilCamera.isDeviceSupportCamera(this)) {
            Toast.makeText(this, "Seu dispositivo não tem suporte a câmera", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        activity = this;
        surfaceView = (CameraSurface) findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        btnGravar = (Button) findViewById(R.id.btnGravar);
        btnTirarFoto = (Button) findViewById(R.id.tirarFoto);
        seekBar = (SeekBar) findViewById(R.id.seek);
        switchCam = (ImageView) findViewById(R.id.switchCam);
        btnFinalizar = (Button) findViewById(R.id.btnFinalizar);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        containerOpcoes = (LinearLayout) findViewById(R.id.containerOpcoes);


        //textureView = (TextureView) findViewById(R.id.textureView);
        //textureView.setSurfaceTextureListener(this);

        //frameLayout = (FrameLayout) findViewById(R.id.frameSurface);

        //surfaceView = new CameraSurface(this);


        /*ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(frameLayout.getWidth(), frameLayout.getHeight());
        //params.width = frameLayout.getWidth();
        //params.height = frameLayout.getHeight();
        surfaceView.setLayoutParams(params);

        frameLayout.addView(surfaceView);*/
//        frameLayout.notify();

        if (UtilCamera.quantidadeCameras() <= 1) {
            switchCam.setVisibility(View.GONE);
        }


        //seekBar.setMax(TEMPO_MAXIMO);
        progressBar.setMax(TEMPO_MAXIMO);

        btnFinalizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ThreadMerge().execute();
            }
        });

        switchCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera = UtilCamera.switchCam(camera, activity);
                UtilCamera.initCamera(camera, surfaceHolder);
                mediaRecorder.setPreviewDisplay(null);
                mediaRecorder.release();
                mediaRecorder = null;
                camera.startPreview();
                prepareRecord();
                //UtilCamera.initCamera(camera, surfaceHolder);
            }
        });

        //seekBar.setClickable(false);
        seekBar.setEnabled(false);
        btnGravar.setOnTouchListener(this);
        btnTirarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (camera != null) {
                    //camera.stopPreview();
                    //camera.unlock();
                    camera.takePicture(new Camera.ShutterCallback() {
                        @Override
                        public void onShutter() {
                            AudioManager mgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                            mgr.playSoundEffect(AudioManager.FLAG_PLAY_SOUND);
                        }
                    }, new Camera.PictureCallback() {
                        @Override
                        public void onPictureTaken(byte[] data, Camera camera) {

                        }
                    }, new Camera.PictureCallback() {
                        @Override
                        public void onPictureTaken(byte[] data, Camera camera) {
                            try {

                                Camera.Parameters parameters = camera.getParameters();
                                int picH = parameters.getPictureSize().height;
                                int picW = parameters.getPictureSize().width;
                                int preH = parameters.getPreviewSize().height;
                                int preW = parameters.getPreviewSize().width;
                                float scale = ((float) (picH * preW)) / ((float) (picW * preH));

                                Camera.Size size = UtilCamera.getOptimalPreviewSize(camera.getParameters().getSupportedPreviewSizes(), larguraSurfaceView, alturaSurfaceView);

                                FileOutputStream fos = new FileOutputStream(criarArquivo(MODO_FOTO));
                                fos.write(data);
                                fos.close();
                                Intent it = new Intent(getBaseContext(), PreviewFotoActivity.class);
                                it.putExtra("largura", size.width);
                                it.putExtra("escala", scale);
//                                it.putExtra("altura", surfaceView.getHeight());
                                startActivity(it);
                                overridePendingTransition(0, 0);
                                finish();
                            } catch (FileNotFoundException e) {
                                if (camera != null) {
                                    camera.release();
                                    camera = null;
                                }
                                e.printStackTrace();
                            } catch (IOException e) {
                                if (camera != null) {
                                    camera.release();
                                    camera = null;
                                }
                                e.printStackTrace();

                            }
                        }
                    });
                }
            }
        });

        pasta = new File(Environment.getExternalStorageDirectory(), this.getClass().getPackage().getName());
        if (!pasta.exists() && !pasta.mkdir()) {
            Toast.makeText(this, "Erro ao criar a pasta", Toast.LENGTH_SHORT).show();
        }

        surfaceHolder.addCallback(this);
        /*camera = Camera.open();
        try {
            camera.setPreviewDisplay(surfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setTitle("Aguarde");
        progressDialog.setMessage("Processando vídeo");
        progressDialog.setCancelable(false);

    }

    private void prepareRecord() {
        if (mediaRecorder == null) {

            mediaRecorder = new MediaRecorder();
            //camera.unlock();

            mediaRecorder.setCamera(camera);

            if (true) {
//            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){

                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
                int orientacao = UtilCamera.getCameraDisplayOrientation(this, UtilCamera.posicaoAtualCamera);
                Log.d("Orientacao", orientacao + "");

                if (UtilCamera.posicaoAtualCamera == UtilCamera.CAMERA_FRONT) {
                    orientacao += 180;
                }
                Log.d("Orientacao", orientacao + "");
                mediaRecorder.setOrientationHint(orientacao);
                CamcorderProfile cp = CamcorderProfile.get(UtilCamera.posicaoAtualCamera, CamcorderProfile.QUALITY_HIGH);
                mediaRecorder.setVideoEncodingBitRate(cp.videoBitRate);
                mediaRecorder.setVideoFrameRate(cp.videoFrameRate);
                mediaRecorder.setAudioChannels(cp.audioChannels);

                Camera.Size size = UtilCamera.getOptimalPreviewSize(camera.getParameters().getSupportedVideoSizes(), 1024, 768);
                mediaRecorder.setVideoSize(size.width, size.height);
            } else {
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
                mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

                mediaRecorder.setProfile(cp);
                //mediaRecorder.setVideoSize(640,640);
                //mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                //mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

                //mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
                //mediaRecorder.setOutputFile(Environment.getExternalStorageDirectory().getPath()+‌​‌​"/test.3gp");

            /*mediaRecorder.setMaxDuration(15000);
            mediaRecorder.setMaxFileSize(50000000);
            mediaRecorder.setVideoSize(640, 640);
            mediaRecorder.setVideoFrameRate(30);
            mediaRecorder.setVideoEncodingBitRate(3000000);*/

//            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
                //          mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                //mediaRecorder.setMaxFileSize(5 * 1024);
                mediaRecorder.setOrientationHint(UtilCamera.getCameraDisplayOrientation(this, UtilCamera.posicaoAtualCamera));

//            mediaRecorder.setVideoSize(640, 480);
                // mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());


            }

            try {

                FileOutputStream fos = new FileOutputStream(criarArquivo(MODO_VIDEO));
                mediaRecorder.setOutputFile(fos.getFD());

                //camera.startPreview();
//                mediaRecorder.prepare();
                //camera.stopPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private File criarArquivo(int tipoArquivo) {

        File dir;
        if (tipoArquivo == MODO_VIDEO) {

            dir = new File(Environment.getExternalStorageDirectory(), this
                    .getClass().getPackage().getName() + "/temp");
        } else {
            dir = new File(Environment.getExternalStorageDirectory(), this.getClass().getPackage().getName());
        }
        File file;

        if (!dir.exists() && !dir.mkdirs()) {
            Log.wtf("TAG",
                    "Failed to create storage directory: "
                            + dir.getAbsolutePath());
            Toast.makeText(getBaseContext(), "not record", Toast.LENGTH_SHORT);
            file = null;
        } else {
            if (tipoArquivo == MODO_VIDEO) {
                file = new File(dir.getAbsolutePath(), "video_" + numeroVideo + ".3gp");
            } else {
                file = new File(dir.getAbsolutePath(), "imagem_" + numeroVideo + ".jpg");
            }
        }
        return file;


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
        } else if (
                id == R.id.action_preview) {
            Intent it = new Intent(getBaseContext(), PreviewActivity.class);
            startActivity(it);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        camera = UtilCamera.getCameraInstance(camera, UtilCamera.CAMERA_BACK, activity);
        alturaSurfaceView = surfaceView.getHeight();
        larguraSurfaceView = surfaceView.getWidth();
        Camera.Parameters parameters = camera.getParameters();
        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        Camera.Size optimalSize = CameraHelper.getOptimalPreviewSize(supportedPreviewSizes, larguraSurfaceView, alturaSurfaceView);

        cp = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        cp.videoFrameWidth = optimalSize.width;
        cp.videoFrameHeight = optimalSize.height;
        mVideoHeight = optimalSize.height;
        mVideoWidth = optimalSize.width;
        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            Log.d("Video", "Tem auto focus mode video");
        } else {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            Log.d("Video", "Tem auto focus mode imagem");
        }

        //parameters.set( "cam_mode", 1 );
        parameters.setPreviewSize(cp.videoFrameWidth, cp.videoFrameHeight);

        //camera = UtilCamera.initCamera(camera, surfaceHolder, activity);

        try {

            camera.setPreviewDisplay(surfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Camera.Size size = UtilCamera.getOptimalPreviewSize(camera.getParameters().getSupportedPictureSizes(), larguraSurfaceView, alturaSurfaceView);
        parameters.setPictureSize(optimalSize.width, optimalSize.height);


        camera.setParameters(parameters);
        if (!alterou) {
            alterou = true;
            ViewGroup.LayoutParams layoutParams = surfaceView.getLayoutParams();
            float ratio = 0f;
            //if(optimalSize.width < optimalSize.height){
            //ratio = optimalSize.width / optimalSize.height;
            //} else {
            ratio = optimalSize.width / optimalSize.height;
            int calc = (int) ((optimalSize.width * surfaceView.getWidth()) / optimalSize.height);
            //}

            Log.d("TAMANHO 1", "W: " + optimalSize.width + " H: " + optimalSize.height + " CALC: " + calc);
            layoutParams.height = calc;

            surfaceView.setLayoutParams(layoutParams);

        }
        Log.d("TAMANHO 2", "W: " + surfaceView.getWidth() + " H: " + surfaceView.getHeight());


        camera.startPreview();
        prepareRecord();

    }

    boolean alterou = false;

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, final int width, final int height) {


//        ResizeWidthAnimation animation = new ResizeWidthAnimation(containerOpcoes, alturaSurfaceView - larguraSurfaceView);
//        animation.setDuration(2000);
//        containerOpcoes.setAnimation(animation);
//        animation.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                Log.d("Tamanho final", containerOpcoes.getHeight() + "");
//                Log.d("Tamanho final 2 ", surfaceView.getMeasuredHeight() + "");
//                surfaceView.setTranslationY(5);
//                surfaceView.requestLayout();
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//
//            }
//        });
        ViewGroup.LayoutParams layoutParams = containerOpcoes.getLayoutParams();
        Log.d("TAMANHO", width + " - " + height);
        layoutParams.height = alturaSurfaceView - larguraSurfaceView;

        containerOpcoes.setLayoutParams(layoutParams);

        /*ViewGroup.LayoutParams params = surfaceView.getLayoutParams();
        params.height = surfaceView.getWidth();
        surfaceView.setLayoutParams(params);*/


    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        UtilCamera.destroy(camera);
    }

    public class ResizeWidthAnimation extends Animation {
        private int mHeight;
        private int mStartHeight;
        private View mView;

        public ResizeWidthAnimation(View view, int width) {
            mView = view;
            mHeight = width;
            mStartHeight = view.getHeight();

        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            int newHeight = mStartHeight + (int) ((mHeight - mStartHeight) * interpolatedTime);

            mView.getLayoutParams().height = newHeight;
            mView.requestLayout();
        }

        @Override
        public void initialize(int width, int height, int parentWidth, int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }
    }


    private long tempo;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                tempo = System.currentTimeMillis();
                iniciarGravacao();
                break;
            case MotionEvent.ACTION_UP:
                pararGravacao();
                break;
        }

        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gravando) {

            try {
                tempoGravado -= 200;
                mediaRecorder.stop();
            } catch (RuntimeException e) {

            }
            Log.d("TAG", "Parou a gravação");
            mediaRecorder.setPreviewDisplay(null);
            mediaRecorder.release();
            gravando = false;
            mediaRecorder = null;
            numeroVideo++;
            if (camera != null) {
                camera.lock();
            }

            //prepareRecord();
        }
        camera.release();
        camera = null;
        //UtilCamera.destroy(camera);


    }

    private void atualizarSeekBar() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (gravando) {
                    tempoGravado += 200;
                    if (TEMPO_MAXIMO >= tempoGravado) {
                        Log.d("TAG", "Gravando");
                        //seekBar.setProgress(tempoGravado);
                        progressBar.setProgress(tempoGravado);
                        handler.postDelayed(this, 200);
                    } else {
                        //GravacaoTask g = new GravacaoTask();
                        //g.execute(false);
                        pararGravacao();
                    }
                }
            }
        });
    }

    private void pararGravacao() {
        if (gravando) {

            try {
                tempoGravado -= 200;
                mediaRecorder.stop();
            } catch (RuntimeException e) {

            }
            Log.d("TAG", "Parou a gravação");
            mediaRecorder.setPreviewDisplay(null);
            mediaRecorder.release();
            gravando = false;
            mediaRecorder = null;
            numeroVideo++;
            if (camera != null) {
                camera.lock();
            }

            prepareRecord();
        }
    }

    private void iniciarGravacao() {
        if (!gravando && TEMPO_MAXIMO >= tempoGravado) {
            camera.unlock();
            gravando = true;
            try {
                mediaRecorder.prepare();
                mediaRecorder.start();
                atualizarSeekBar();
                Log.d("TAG", "Gravando, rodando o seek");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getBaseContext(), "Tempo máximo atingido", Toast.LENGTH_SHORT).show();
        }
    }


    private class GravacaoTask extends AsyncTask<Boolean, Void, Void> {

        @Override
        protected Void doInBackground(Boolean... params) {
            if (params[0]) {

                //UtilCamera.destroy(camera);
                if (!gravando && TEMPO_MAXIMO >= tempoGravado) {
                    camera.unlock();
                    gravando = true;
                    try {

                        mediaRecorder.prepare();
                        mediaRecorder.start();
                        atualizarSeekBar();
                        Log.d("TAG", "Gravando, rodando o seek");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getBaseContext(), "Tempo máximo atingido", Toast.LENGTH_SHORT).show();
                }


            } else {

                if (gravando) {

                    try {
                        mediaRecorder.stop();
                    } catch (RuntimeException e) {

                    }
                    Log.d("TAG", "Parou a gravação");
                    mediaRecorder.setPreviewDisplay(null);
                    mediaRecorder.release();
                    gravando = false;
                    mediaRecorder = null;
                    numeroVideo++;
                    if (camera != null) {
                        camera.lock();
                    }


                    prepareRecord();
                }
                //UtilCamera.initCamera(camera, surfaceHolder);
            }
            return null;
        }
    }

    private class ThreadMerge extends AsyncTask<Void, Void, Void> {
        int qtd;
        File files[] = null;
        File dir;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dir = new File(Environment.getExternalStorageDirectory(), this
                    .getClass().getPackage().getName() + "/temp");
            files = dir.listFiles();
            qtd = files.length;

            progressDialog.show();

        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                String paths[] = new String[qtd];
                List<Movie> inMovies = new ArrayList<>();
                MovieCreator mc = new MovieCreator();
                for (int i = 0; i < qtd; i++) {
                    File arquivo = new File(dir, "video_" + i + ".3gp");
                    if (arquivo.length() <= 0) {
                        arquivo.delete();
                        continue;
                    }
                    paths[i] = arquivo.getAbsolutePath();

                    //InputStream input = new FileInputStream(paths[i]);
                    DataSource rChannel = new FileDataSourceImpl(paths[i]);
                    inMovies.add(MovieCreator.build(rChannel));

                    //inMovies[i] = MovieCreator.build(Channels.newChannel(NovaCameraActivity.class.getResourceAsStream(paths[i])));
                }
                // MovieCreator.build(Channels.newChannel(new FileInputStream(files[0])));
                List<Track> videoTracks = new LinkedList<Track>();
                List<Track> audioTracks = new LinkedList<Track>();
                for (Movie m : inMovies) {
                    for (Track t : m.getTracks()) {
                        if (t.getHandler().equals("soun")) {
                            audioTracks.add(t);
                        }
                        if (t.getHandler().equals("vide")) {
                            videoTracks.add(t);
                        }
                    }
                }

                Movie result = new Movie();
                if (audioTracks.size() > 0) {
                    result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
                }
                if (videoTracks.size() > 0) {
                    result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
                }
                File dir = new File(Environment.getExternalStorageDirectory(), this
                        .getClass().getPackage().getName());
                BasicContainer out = (BasicContainer) new DefaultMp4Builder().build(result);
                FileChannel fc = new RandomAccessFile(dir + "/final.mp4", "rw").getChannel();
                out.writeContainer(fc);
                fc.close();

                for (int i = 0; i < qtd; i++) {
                    files[i].delete();
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (progressDialog != null) {
                progressDialog.dismiss();
            }

            Intent it = new Intent(getBaseContext(), PreviewActivity.class);
            startActivity(it);
            finish();
            /*shutdown();
            Intent it = new Intent(getBaseContext(), MergeActivity.class);
            startActivity(it);
            finish();*/
        }
    }


}
