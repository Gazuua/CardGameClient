package com.example.gameclient;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import client.ClientInfo;
import client.NetworkManager;
import client.NetworkResponseCallback;
import client.Packet;

public class LobbyFragment extends Fragment {

    NetworkResponseCallback lobbyFragmentCallback;
    FragmentChangerCallback fragmentChanger;
    ListView listView;
    List<String> roomList; // 스트링 형식 지정해서 방제 :: 인원수 로 나타나도록 하기

    // 요청을 중복해서 보내면 데이터가 꼬일 수 있으므로 간이 locking 변수로 사용
    boolean isWaiting = false;

    public LobbyFragment() {
        // Required empty public constructor
        roomList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_lobby, container, false);

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, roomList);
        final Handler handler = new Handler();

        final TextView idText = view.findViewById(R.id.userIDText);
        final TextView scoreText = view.findViewById(R.id.scoreText);
        final TextView moneyText = view.findViewById(R.id.moneyText);
        final Button createButton = view.findViewById(R.id.roomCreateButton);
        final Button refreshButton = view.findViewById(R.id.refreshButton);

        lobbyFragmentCallback = new NetworkResponseCallback() {
            @Override
            public void onRecv(String content, short type) {
                // lobbyFragment에서는 아래 4개의 응답이 서버로부터 도착한다.
                switch(type)
                {
                    // 로비 입장 시 자기 자신의 정보를 요청하는 패킷에 대한 응답이다.
                    case Packet.PACKET_TYPE_USER_INFO_RES: {
                        // 원래 있어서는 안 되는 경우이지만 에러가 걸려서 null값이 오면 그냥 아무것도 안 하게 둔다.
                        if (content.equals("null")) return;

                        // split 메서드로 한방에 나누어서 클라이언트의 정보를 저장한다
                        final String[] info = content.split("/");
                        ClientInfo.getInstance().setId(info[0]);
                        ClientInfo.getInstance().setWin(Integer.parseInt(info[1]));
                        ClientInfo.getInstance().setLose(Integer.parseInt(info[2]));
                        ClientInfo.getInstance().setMoney(Integer.parseInt(info[3]));
                        ClientInfo.getInstance().setRoom(Integer.parseInt(info[4]));

                        // 저장했으면 각 뷰에 정보를 셋팅한다.
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                idText.setText(info[0]);
                                scoreText.setText("전적\n" + info[1] + "승 " + info[2] + "패");
                                moneyText.setText("소지금\n" + info[3] + "원");
                            }
                        });
                    }
                        break;

                    // 로비 입장 시 방 목록을 요청하는 패킷에 대한 응답이다.
                    case Packet.PACKET_TYPE_ROOM_LIST_RES: {
                        // 방이 없어서 리스트가 없는 경우 null이 올 때도 있다.
                        // 이 경우 그냥 아무것도 안 하고 빠져나가 준다.
                        if (content.equals("null")) {
                            isWaiting = false;
                            return;
                        }

                        // split 메서드로 한방에 나누어서 일단 리스트부터 저장한다
                        String[] info = content.split("/");

                        // roomList에 데이터를 추가한다.
                        roomList.clear();
                        for (int i = 0; i < info.length; i += 3) {
                            String title = info[i] + " :: " + info[i + 1] + " :: " + info[i + 2] + "/4";
                            roomList.add(title);
                        }

                        // 리스트뷰에 보이도록 설정한다.
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (listView.getAdapter() == null)
                                    listView.setAdapter(adapter);
                                else adapter.notifyDataSetChanged();

                                isWaiting = false;
                            }
                        });
                    }
                        break;

                    // 방 만들기 요청에 대한 응답
                    case Packet.PACKET_TYPE_ROOM_MAKE_RES: {
                        // 자신이 만든 방 번호를 싱글톤 클래스에 셋팅해 주고
                        int num = Integer.parseInt(content);
                        ClientInfo.getInstance().setRoom(num);
                        // 해당하는 방으로 입장한다.
                        fragmentChanger.onEnterRoom();
                        isWaiting = false;
                    }
                        break;

                    // 방 입장 요청에 대한 응답
                    case Packet.PACKET_TYPE_ENTER_ROOM_RES: {
                        // 성공 시 방에 입장한다.
                        if(content.equals("success")) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    fragmentChanger.onEnterRoom();
                                    isWaiting = false;
                                }
                            });
                        }
                        // 실패하면 싱글톤 클래스에 임의로 설정했던 방 번호를 지우고, toast로 입장불가 메세지 띄우기
                        else
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    ClientInfo.getInstance().setRoom(-1);
                                    Toast.makeText(getActivity(),
                                            "방에 입장할 수 없습니다.", Toast.LENGTH_SHORT).show();
                                    isWaiting = false;
                                }
                            });
                    }
                        break;
                }
            }
        };

        // 위의 콜백 구현체를 등록한다
        NetworkManager.getInstance().setResponseCallback(lobbyFragmentCallback);

        // 방 만들기 버튼 클릭시
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isWaiting) return;
                // 방 제목 입력하는 창을 하나 주고 방만들기 요청 패킷을 보낸다.
                // 방 제목이 겹쳐도 어차피 인덱스로 구분하므로 요청이 실패하는 일은 없지만
                // 일단 서버에서 관리해야 하므로 요청 후 응답을 대기해준다.
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                // 방 제목을 입력할 수 있도록 커스텀 뷰의 xml 레이아웃을 인플레이터에서 받아온다.
                final View createDialog = getActivity().getLayoutInflater().inflate(R.layout.dialog_createroom, null);


                                builder.setTitle("방 만들기")
                                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String title = ((EditText) createDialog.findViewById(R.id.createRoomText)).getText().toString();
                                // 방 제목 형식이 틀릴 경우 빠꾸먹인다.
                                if( title.equals("") || title.contains("/") )
                                {
                                    AlertDialog.Builder failBuilder = new AlertDialog.Builder(getActivity());
                                    failBuilder.setTitle("형식 오류")
                                            .setMessage("방 제목은 1글자 이상이어야 하며, 특수문자는 허용되지 않습니다.")
                                            .setNeutralButton("확인", null)
                                            .create()
                                            .show();

                                    return;
                                }
                                // 형식 검사에 통과했다면 방을 만들어 준다.
                                else
                                {
                                    isWaiting = true;

                                    // 방 만들기 요청을 위한 방 제목을 패킷에 실어서 날려 준다.
                                    NetworkManager.getInstance().writeRequest(title, Packet.PACKET_TYPE_ROOM_MAKE_REQ);
                                }

                            }
                        })
                        .setNegativeButton("취소", null)
                        .setCancelable(false)
                        .setView(createDialog)
                        .create()
                        .show();
            }
        });

        // 새로고침 버튼 클릭시 (자기 정보 / 방 리스트 업데이트)
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isWaiting) return;

                isWaiting = true;
                // 자신의 아이디를 이용하여 서버에 자신의 정보 및 방 목록을 요청한다.
                NetworkManager.getInstance()
                        .writeRequest(ClientInfo.getInstance().getId(), Packet.PACKET_TYPE_ENTER_LOBBY_REQ);
            }
        });

        // 리스트뷰 초기화 및 아이템 클릭시 입장 설정
        listView = (ListView) view.findViewById(R.id.roomListView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            // 리스트뷰 아이템을 클릭하면 해당하는 방에 입장한다.
            // 인덱스에 해당하는 방의 맨 앞 숫자( :: 이전 문자열)에 해당하는 방으로의 입장을 서버에 요청한다.
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                if(isWaiting) return;

                isWaiting = true;
                // (방 번호 ::) 형식이므로, 맨 앞에 :: 먼저 찾고 그 앞 한 칸까지 substring으로 잘라준다.
                final String roomNumber = roomList.get(i).substring(0, roomList.get(i).indexOf("::") - 1);

                // 방 번호를 잘랐으면 우선 ClientInfo에 임의로 추가한다
                // 실패 시 다시 -1으로 집어넣어 줘야 한다.
                ClientInfo.getInstance().setRoom(Integer.parseInt(roomNumber));

                // 서버에 방 입장 요청을 한다.
                NetworkManager.getInstance().writeRequest(roomNumber, Packet.PACKET_TYPE_ENTER_ROOM_REQ);
            }
        });

        // 로비 입장 시 유저 정보와 방 리스트가 필요하다.
        isWaiting = true;

        // 서버에 정보를 요청하자.
        NetworkManager.getInstance()
                .writeRequest(ClientInfo.getInstance().getId(), Packet.PACKET_TYPE_ENTER_LOBBY_REQ);

        return view;
    }

    public void setFragmentChanger(FragmentChangerCallback callback)
    {
        this.fragmentChanger = callback;
    }
}
