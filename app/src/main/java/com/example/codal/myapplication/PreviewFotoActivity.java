package com.example.codal.myapplication;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class PreviewFotoActivity extends Activity {
    private ImageView imagem;
    private int altura;
    private int largura;
    private float escala;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_foto);
        Bundle b = getIntent().getExtras();
        if(b != null){

            largura = b.getInt("largura");
            escala = b.getFloat("escala");
            altura = largura;
        }

        imagem = (ImageView) findViewById(R.id.imagem);

        File dir = new File(Environment.getExternalStorageDirectory(), this.getClass().getPackage().getName());
        File imagemarquivo = null;
        for(File arquivo : dir.listFiles()){
            if(arquivo.getAbsolutePath().contains(".jpg")){
                imagemarquivo = arquivo.getAbsoluteFile();
                break;
            }
        }
        Bitmap img = BitmapFactory.decodeFile(imagemarquivo.getAbsolutePath());

        Matrix matrix = new Matrix();
        matrix.setRotate(90);
        img = Bitmap.createBitmap(img, 0,0, img.getWidth(), img.getHeight(), matrix, true);


        int imgAltura = img.getHeight();
        int imgLargura = img.getWidth();
        int calcAltura = (imgAltura * largura) / imgLargura;

        Bitmap result = Bitmap.createScaledBitmap(img, largura, calcAltura, true);
        result = Bitmap.createBitmap(result, 0, 0,largura, altura);
        try {
            FileOutputStream fos = new FileOutputStream(imagemarquivo.getAbsolutePath());
            result.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();

            MediaStore.Images.Media.insertImage(getContentResolver(), imagemarquivo.getAbsolutePath(), imagemarquivo.getName(), imagemarquivo.getName());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        Bitmap result = Bitmap.createBitmap(img, 0,0, altura, largura, matrix, true);
        imagem.setImageBitmap(result);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_preview_foto, menu);
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
}
