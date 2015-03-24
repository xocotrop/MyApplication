package com.example.codal.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ListaActivity extends Activity {

    private RecyclerView recyclerView;
    private ItemAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        List<String> lista = popularLista();
        adapter = new ItemAdapter(this, lista);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    public List<String> popularLista(){
        List<String> lista = new ArrayList<>();

        File pasta = new File(Environment.getExternalStorageDirectory(), this.getClass().getPackage().getName());

        File arquivo = new File(pasta, "final.mp4");

        for(int i = 0; i < 20; i++){
            String a = arquivo.getAbsolutePath();
            lista.add(a);
        }
        return lista;

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_lista, menu);
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
