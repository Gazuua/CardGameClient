package com.example.gameclient;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import client.ClientInfo;
import client.NetworkManager;
import client.NetworkResponseCallback;
import client.Packet;

public class LoginActivity extends AppCompatActivity {

    NetworkResponseCallback loginActivityCallback;

    boolean isWaiting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText idText = (EditText) findViewById(R.id.idText);
        final EditText pwText = (EditText) findViewById(R.id.pwText);
        final Button loginButton = (Button) findViewById(R.id.loginSubmitButton);
        final Button registerButton = (Button) findViewById(R.id.registerButton);
        final Handler handler = new Handler();

        isWaiting = false;

        loginActivityCallback = new NetworkResponseCallback() {
            // LoginActivity의 네트워크 응답 콜백 클래스이다.
            // 이 화면에서 클라이언트가 받게 되는 응답의 종류는
            // LOGIN_RES 이다.
            @Override
            public void onRecv(String content, short type) {

                switch(type)
                {
                    // 로그인 패킷에 대한 응답이다.
                    case Packet.PACKET_TYPE_LOGIN_RES:
                        Log.d("로그인 응답", content);
                        // 로그인 응답에 대한 response 받고 그에 대한 처리를 한다
                        // 성공 시 클라이언트 정보 입력 및 게임 액티비티로 이동
                        if(content.equals("success")) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    // 액티비티 이동
                                    Intent intent = new Intent(getApplicationContext(), GameActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        }
                        // 실패 시 응답코드에 맞는 처리 (아이디 / 비밀번호가 틀렸습니다 안내)
                        else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    // 로그인에 실패했기 때문에 아이디를 다시 비워준다.
                                    ClientInfo.getInstance().setId("");

                                    // 알림창을 띄워준다.
                                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                    builder.setTitle("로그인 실패")
                                            .setMessage("잘못된 아이디 혹은 비밀번호를 입력하셨습니다. 다시 시도하십시오.")
                                            .setNeutralButton("확인", null)
                                            .create()
                                            .show();

                                    idText.setText("");
                                    pwText.setText("");

                                    isWaiting = false;
                                }
                            });
                        }
                        break;
                }
            }
        };

        // 바로 위의 콜백 구현체를 네트워크 매니저에 등록한다.
        NetworkManager.getInstance().setResponseCallback(loginActivityCallback);

        // 로그인 버튼을 누르면 로그인 요청이 진행된다.
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 이미 로그인 요청에 대한 응답 대기 중일 경우 아무것도 하지 않고 반환한다.
                if (isWaiting)
                    return;

                final String id = idText.getText().toString();
                final String pw = pwText.getText().toString();
                if (id.length() >= 4 && pw.length() >= 4)
                    isWaiting = true;
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setTitle("형식 오류")
                            .setMessage("아이디 및 비밀번호는 4글자 이상이어야 합니다.")
                            .setNeutralButton("확인", null)
                            .create()
                            .show();

                    return;
                }

                // 형식 검증이 통과되었으므로 일단 아이디를 ClientInfo에 입력한 후 대기한다
                ClientInfo.getInstance().setId(id);

                // 서버에 로그인 요청 패킷을 보내는 동작
                // 응답은 미리 설정해 둔 콜백 메서드를 통해 스레드에서 실행된다
                String encodedString = id + "/" + pw;
                Log.d("로그인 요청", encodedString);
                NetworkManager.getInstance().writeRequest(encodedString, Packet.PACKET_TYPE_LOGIN_REQ);
            }
        });

        // 회원가입 버튼을 누르면 아래 코드가 실행된다.
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 로그인 요청 대기 중인데 함부로 회원가입 액티비티로 넘어가지 않도록 처리
                if (isWaiting)
                    return;

                // 버튼을 누르면 회원가입 액티비티로 넘어간다.
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
