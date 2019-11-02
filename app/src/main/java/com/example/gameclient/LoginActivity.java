package com.example.gameclient;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import client.NetworkManager;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText idText = (EditText) findViewById(R.id.idText);
        final EditText pwText = (EditText) findViewById(R.id.pwText);
        final Button loginButton = (Button) findViewById(R.id.loginSubmitButton);
        final Button registerButton = (Button) findViewById(R.id.registerButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = idText.getText().toString();
                String pw = pwText.getText().toString();
                // 서버에 보내고 결과값 반환때까지 대기
                // 성공 시 게임 액티비티로 이동 및 finish()
                // 액티비티 구조 짜는 동안은 그냥 통과시키기
                Intent intent = new Intent(getApplicationContext(), GameActivity.class);
                startActivity(intent);
                finish();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    // 뒤로 버튼을 짧은 시간 안에 두 번 연속 누르면 종료
    private long lastTimeBackPressed;
    @Override
    public void onBackPressed()
    {
        if (System.currentTimeMillis() - lastTimeBackPressed < 1500 ) {
            NetworkManager.getInstance().shutdown();
            finish();
        }
        else {
            Toast.makeText(this, "'뒤로'버튼을 한 번 더 누르시면 종료합니다.", Toast.LENGTH_SHORT).show();
            lastTimeBackPressed = System.currentTimeMillis();
        }
    }
}
