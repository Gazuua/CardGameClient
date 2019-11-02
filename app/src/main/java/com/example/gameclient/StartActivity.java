package com.example.gameclient;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import client.ConnectionListener;
import client.NetworkManager;

// StartActivity :: 게임을 맨 처음 시작하면 나타나는 화면
// 다음과 같은 흐름을 가지고 있다.
// 1. 소켓 연결 및 각종 모듈을 동작하며 풀 스크린 이미지와 Tap To Start 안내문구 표시
// 2. 이미지를 탭하면 로그인 액티비티로 이동 후 자신은 종료
public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final View startView = findViewById(R.id.startView);
        final View textView = findViewById(R.id.textView);
        final View.OnClickListener startViewClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        };
        final Handler handler = new Handler();

        // NetworkManager에서 연결이 되었는지 여부에 따라 조건 분기
        NetworkManager.getInstance().setConnectionListener(new ConnectionListener() {

            // 연결이 되었으면 다음 화면으로 넘어갈 수 있도록 알리고 클릭리스너 설정
            @Override
            public void onSuccess() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "서버에 연결되었습니다.", Toast.LENGTH_SHORT)
                                .show();
                        startView.setOnClickListener(startViewClickListener);
                        textView.setOnClickListener(startViewClickListener);
                    }
                });
            }

            // 연결 실패 시 대화 상자를 띄우고 확인을 누르면 프로세스 종료
            @Override
            public void onFail() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this);
                        builder.setTitle("연결 실패")
                                .setMessage("서버와의 통신 연결에 실패하였습니다.")
                                .setCancelable(false)
                                .setNeutralButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        NetworkManager.getInstance().shutdown();
                                        finish();
                                    }
                                }).create().show();
                    }
                });
            }
        });
        NetworkManager.getInstance().start();
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
