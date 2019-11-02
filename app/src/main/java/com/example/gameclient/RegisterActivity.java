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

import client.NetworkManager;
import client.NetworkResponseCallback;
import client.Packet;

public class RegisterActivity extends AppCompatActivity {

    boolean isWaiting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        final EditText idText = (EditText) findViewById(R.id.idRegText);
        final EditText pwText = (EditText) findViewById(R.id.pwRegText);
        final Button submitButton = (Button) findViewById(R.id.registerSubmitButton);
        final Handler handler = new Handler();

        isWaiting = false;

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 이미 회원가입 요청에 대한 응답을 기다리고 있을 경우 아무것도 하지 않고 리턴
                if(isWaiting)
                    return;

                String id = idText.getText().toString();
                String pw = pwText.getText().toString();
                // 아이디 문자열에 "/"가 포함되어 있을 경우 패킷 구분자로 쓸 수 없으므로 오류처리.
                if (id.contains("/")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    builder.setTitle("형식 오류")
                            .setMessage("아이디에 특수문자는 사용할 수 없습니다.")
                            .setNeutralButton("확인", null)
                            .create()
                            .show();
                    return;
                }

                if (id.length() >= 4 && pw.length() >= 4)
                    isWaiting = true;
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    builder.setTitle("형식 오류")
                            .setMessage("아이디 및 비밀번호는 4글자 이상이어야 합니다.")
                            .setNeutralButton("확인", null)
                            .create()
                            .show();

                    return;
                }

                // 서버에 보내고 결과값 받기
                String encodedString = id + "/" + pw;
                Log.d("회원가입 요청", encodedString);
                NetworkManager.getInstance().setResponseCallback(new NetworkResponseCallback() {
                    @Override
                    public void onRecv(String content) {
                        Log.d("회원가입 응답", content);
                        // 회원가입 응답에 대한 response 받고 그에 대한 처리를 한다
                        // 성공 시 LoginActivity로 돌아간다.
                        if(content.equals("success")) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    finish();
                                }
                            });
                        }
                        // 실패 시 응답코드에 맞는 처리 (중복된 아이디가 있을 경우, 혹은 특수문자 '/' 포함시 실패)
                        else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                                    builder.setTitle("회원가입 실패")
                                            .setMessage("중복되거나 잘못된 형식의 아이디입니다. 다른 아이디를 입력해 주세요.")
                                            .setNeutralButton("확인", null)
                                            .create()
                                            .show();

                                    idText.setText("");

                                    isWaiting = false;
                                }
                            });
                        }
                    }
                });

                NetworkManager.getInstance().writeRequest(encodedString, Packet.PACKET_TYPE_REGISTER_REQ);
            }
        });
    }
}
