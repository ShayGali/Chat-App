package com.example.chatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText editText = findViewById(R.id.editText);

        findViewById(R.id.enter_button).setOnClickListener(v ->{
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("name", editText.getText().toString());
            startActivity(intent);
        });

    }
}