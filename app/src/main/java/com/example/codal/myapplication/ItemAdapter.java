package com.example.codal.myapplication;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

/**
 * Created by Codal on 19/03/15.
 */
public class ItemAdapter extends RecyclerView.Adapter {

    private Context context;
    private LayoutInflater inflater;
    private List<String> lista;

    public ItemAdapter(Context context, List<String> lista){
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.lista = lista;

    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = inflater.inflate(R.layout.item_lista, viewGroup, false);
        MyViewHolder myViewHolder = new MyViewHolder(v);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {

        MyViewHolder v = (MyViewHolder) viewHolder;

        /*if(v.mp != null){
            v.mp.stop();
            v.mp.setSurface(null);
            v.mp.release();

            v.pronto = false;
        }*/
        v.pronto = false;

       v.arquivo = lista.get(i);
        v.topo.setText(v.arquivo);
       v.preparar();



    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    private class MyViewHolder extends RecyclerView.ViewHolder implements TextureView.SurfaceTextureListener, MediaPlayer.OnPreparedListener {

        private TextureView textura;
        private TextView topo;
        private MediaPlayer mp;
        private boolean pronto = false;
        private String arquivo;
        private float mVideoWidth;
        private float mVideoHeight;
        private ScrollView scrollView;
        public MyViewHolder(View itemView) {
            super(itemView);

            textura = (TextureView) itemView.findViewById(R.id.textureView);
            topo = (TextView) itemView.findViewById(R.id.titulo);
            scrollView = (ScrollView) itemView.findViewById(R.id.scroll);
            scrollView.setEnabled(false);
        }

        public void preparar(){
            if(!pronto) {

                //topo.setText(arquivo);

                calculateVideoSize();

                mp = new MediaPlayer();

                textura.setSurfaceTextureListener(this);
                pronto = true;
            }
        }

        @Override
        public void onPrepared(MediaPlayer mp) {
            mp.start();
        }

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            Surface s = new Surface(textura.getSurfaceTexture());

            if(mp == null){
                mp = new MediaPlayer();
            }
            ViewGroup.LayoutParams layoutParams = textura.getLayoutParams();
            layoutParams.height = textura.getWidth();
            textura.setLayoutParams(layoutParams);

            int viewWidth = textura.getWidth();
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
            scaleX = (float) mVideoHeight / (float)viewHeight;
            scaleY = (float)mVideoWidth / (float)viewWidth;
            // Calculate pivot points, in our case crop from center
            int pivotPointX = viewWidth / 2;
            int pivotPointY = viewHeight / 2;

            Matrix matrix = new Matrix();
            matrix.setScale(scaleX, scaleY, 0, 0);

            textura.setTransform(matrix);
            textura.invalidate();

            try {

                mp.setDataSource(arquivo);
                mp.setSurface(s);

            } catch (IOException e) {
                e.printStackTrace();
            }
            mp.prepareAsync();
            mp.setOnPreparedListener(this);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            if(mp != null){
                mp.stop();
                mp.setSurface(null);
                mp.release();
                mp = null;
                pronto = false;
                //mp = null;
//                mp.setSurface(null);
                //textura = null;
            }
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
}
