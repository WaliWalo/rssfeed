package com.example.user.rssfeed;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


    }
    EditText editText;
    public void on_click(View view) {
        Intent intent = new Intent(this,MainActivity.class);
        editText = findViewById(R.id.editText);
        String feed = editText.getText().toString();
        intent.putExtra("feed",feed);
        startActivity(intent);
    }
}
