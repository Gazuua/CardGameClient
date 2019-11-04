package com.example.gameclient;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import ch.qos.logback.core.net.server.Client;
import client.ClientInfo;
import client.NetworkManager;
import client.NetworkResponseCallback;
import client.Packet;

public class RoomFragment extends Fragment {

    FragmentChangerCallback fragmentChanger;
    NetworkResponseCallback roomFragmentCallback;

    GridLayout userInfoContainer;

    String roomName;
    int roomNumber;
    int roomUser;

    boolean isWaiting;

    public RoomFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_room, container, false);
        final TextView roomTitleText = view.findViewById(R.id.roomTitleText);

        final ScrollView scrollView = view.findViewById(R.id.roomScrollView);
        final TextView chatTextView = view.findViewById(R.id.roomChattingView);
        final EditText editText = view.findViewById(R.id.roomEditText);
        final Button sendButton = view.findViewById(R.id.roomChattingSendButton);
        final Button startButton = view.findViewById(R.id.roomStartButton);

        userInfoContainer = view.findViewById(R.id.roomInfoContainer);

        isWaiting = false;
        final Handler handler = new Handler();

        // 이 프래그먼트에 진입했다는 것은 방에 들어왔다는 뜻이므로 가장 먼저 방 번호를 설정한다.
        roomNumber = ClientInfo.getInstance().getRoom();

        Log.d("여기는 몇번방?", roomNumber + "번방!!");

        roomFragmentCallback = new NetworkResponseCallback() {
            @Override
            public void onRecv(String content, short type) {

                // roomFragment에서는 아래와 같은 응답을 받아 처리할 수 있다.
                switch(type)
                {
                    // 방 제목이 무엇인지 응답받는 패킷이다.
                    // 초기화, 입장, 게임 종료 등의 상황에 서버에서 임의로 날아온다.
                    case Packet.PACKET_TYPE_ROOM_INFO_RES:
                        // 방 제목이 null이 아닌 이상 화면에 셋팅해주면 된다.
                        if(!content.equals("null")) {
                            roomName = content;
                            roomTitleText.setText(roomName);
                        }
                        break;

                    // 방에 있는 유저 리스트를 받는 패킷이다.
                    // 초기화, 입장, 게임 종료 등의 상황에 서버에서 임의로 날아온다.
                    case Packet.PACKET_TYPE_ROOM_USER_RES:
                        final String[] info = content.split("/");
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                // 우선 컨테이너를 싹 비워주고
                                userInfoContainer.removeAllViews();
                                roomUser = 0;
                                for(int i=0; i<info.length; i+=2)
                                {
                                    // 컨테이너에 담을 유저 리스트를 하나씩 차곡차곡 쌓아준다.
                                    View inflatedView = ((LayoutInflater) view.getContext()
                                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).
                                            inflate(R.layout.layout_userlist, null);

                                    userInfoContainer.addView(inflatedView);

                                    // inflate된 유저 리스트 뷰에 내용물을 채워준다.
                                    TextView nameText = inflatedView.findViewById(R.id.userListNameText);
                                    TextView moneyText = inflatedView.findViewById(R.id.userListMoneyText);
                                    nameText.setText(info[i]);
                                    moneyText.setText(info[i+1]);

                                    roomUser++;

                                    view.invalidate();
                                }
                            }
                        });
                        break;

                    case Packet.PACKET_TYPE_ROOM_CHAT_RES:
                        final String msg = content;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                chatTextView.append(msg + "\r\n");
                                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                                view.invalidate();
                            }
                        });
                        break;

                    case Packet.PACKET_TYPE_ROOM_START_RES:
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                fragmentChanger.onStartGame();
                            }
                        });
                        break;
                }
            }
        };

        // 콜백 구현체부터 설정해 준다.
        NetworkManager.getInstance().setResponseCallback(roomFragmentCallback);

        // 송신 버튼을 누르면 EditText의 내용을 서버로 보낸다.
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = editText.getText().toString();

                // 공백이면 그냥 아무것도 안 한다.
                if (text.equals("")) return;

                // 공백이 아니면 보내준다.
                String msg = ClientInfo.getInstance().getId() + " :: " + text;
                NetworkManager.getInstance().writeRequest(msg, Packet.PACKET_TYPE_ROOM_CHAT_REQ);
                editText.setText("");
            }
        });

        // 시작 버튼을 누르면 시작한다.
        // 아무나 시작할 수 있게 해놨다.
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 방 인원이 2명 이상이면 시작 가능
                if(isWaiting) return;

                if (roomUser >= 2) {
                    isWaiting = true;
                    NetworkManager.getInstance().writeRequest
                            ("" + roomNumber, Packet.PACKET_TYPE_ROOM_START_REQ);
                }
                else {
                    Toast.makeText(getContext(), "게임을 시작하려면 2명 이상이어야 합니다.", Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });

        // 처음 입장 시 방 정보를 요청하는 ROOM_INFO_REQ 패킷을 날린다.
        NetworkManager.getInstance().writeRequest("" + roomNumber, Packet.PACKET_TYPE_ROOM_INFO_REQ);

        return view;
    }

    // 방에서 나갈 땐 서버에 통지만 하면 된다.
    public void onExitRoom() {
        NetworkManager.getInstance().writeRequest("" + roomNumber, Packet.PACKET_TYPE_EXIT_ROOM_REQ);
        ClientInfo.getInstance().setRoom(-1);
    }

    public void setFragmentChanger(FragmentChangerCallback callback)
    {
        this.fragmentChanger = callback;
    }
}
