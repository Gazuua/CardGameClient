package com.example.gameclient;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import client.ClientInfo;
import client.NetworkManager;
import client.NetworkResponseCallback;
import client.Packet;

public class RoomFragment extends Fragment {

    FragmentChangerCallback fragmentChanger;
    NetworkResponseCallback roomFragmentCallback;

    String roomName;
    int roomNumber;

    public RoomFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_room, container, false);
        final TextView roomTitleText = view.findViewById(R.id.roomTitleText);
        final GridLayout userInfoContainer = view.findViewById(R.id.roomInfoContainer);

        final Handler handler = new Handler();

        // 이 프래그먼트에 진입했다는 것은 방에 들어왔다는 뜻이므로 가장 먼저 방 번호를 설정한다.
        roomNumber = ClientInfo.getInstance().getRoom();

        Button startButton = view.findViewById(R.id.roomStartButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 방 인원이 2명 이상이면 시작 가능
                fragmentChanger.onStartGame();
            }
        });

        roomFragmentCallback = new NetworkResponseCallback() {
            @Override
            public void onRecv(String content, short type) {
                switch(type)
                {
                    // roomFragment에서는 아래와 같은 응답을 받아 처리할 수 있다.

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
                                for(int i=0; i<info.length; i+=2)
                                {
                                    // 컨테이너에 담을 유저 리스트를 하나씩 차곡차곡 쌓아준다.
                                    View inflatedView = getActivity().getLayoutInflater().
                                            inflate(R.layout.layout_userlist, null);

                                    userInfoContainer.addView(inflatedView);

                                    // inflate된 유저 리스트 뷰에 내용물을 채워준다.
                                    TextView nameText = inflatedView.findViewById(R.id.userListNameText);
                                    TextView moneyText = inflatedView.findViewById(R.id.userListMoneyText);
                                    nameText.setText(info[i]);
                                    moneyText.setText(info[i+1]);
                                }
                            }
                        });
                        break;
                }
            }
        };

        NetworkManager.getInstance().setResponseCallback(roomFragmentCallback);

        // 처음 입장 시 방 정보를 요청하는 ROOM_INFO_REQ 패킷을 날린다.
        NetworkManager.getInstance().writeRequest("" + roomNumber, Packet.PACKET_TYPE_ROOM_INFO_REQ);

        return view;
    }

    public void setFragmentChanger(FragmentChangerCallback callback)
    {
        this.fragmentChanger = callback;
    }
}
