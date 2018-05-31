package com.example.lenovo.busq;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class IndexActivity extends AppCompatActivity {
    private Button btMap;
    private Button btLine;
    private Button btStation;
    private Button btTrans;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
        btMap = findViewById(R.id.map);
        btLine = findViewById(R.id.line);
        btStation = findViewById(R.id.station);
        btTrans = findViewById(R.id.trans);
        btMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(IndexActivity.this,MainActivity.class);
                startActivity(i);
            }
        });
        btLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(IndexActivity.this,LineActivity.class);
                startActivity(i);
            }
        });
        btStation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(IndexActivity.this,StationActivity.class);
                startActivity(i);
            }
        });
        btTrans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(IndexActivity.this,TransActivity.class);
                startActivity(i);
            }
        });
    }
}
