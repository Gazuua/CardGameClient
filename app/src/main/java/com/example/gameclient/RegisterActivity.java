package com.example.gameclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        final EditText idText = (EditText) findViewById(R.id.idRegText);
        final EditText pwText = (EditText) findViewById(R.id.pwRegText);
        final Button submitButton = (Button) findViewById(R.id.registerSubmitButton);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = idText.getText().toString();
                String pw = pwText.getText().toString();
                // 서버에 보내고 결과값 받기
                // 액티비티 구조 짜는 동안은 그냥 finish() 해주기
                finish();
            }
        });
    }
}
