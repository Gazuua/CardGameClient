package com.example.gameclient;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class LobbyFragment extends Fragment {

    FragmentChangerCallback fragmentChanger;
    ListView listView;
    List<String> roomList; // 스트링 형식 지정해서 방제 :: 인원수 로 나타나도록 하기

    public LobbyFragment() {
        // Required empty public constructor
        roomList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_lobby, container, false);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, roomList);

        // 로그인을 통해 onCreateView 호출 시 서버에서 방 리스트를 새로 받아오도록 해야 함.
        // HashMap으로 고유번호 부여 및 네트워크 모듈과의 통신 효율화 필요
        roomList.clear();
        roomList.add("1번방 :: 1/4");
        roomList.add("2번방 :: 1/4");
        roomList.add("3번방 :: 1/4");
        roomList.add("4번방 :: 1/4");

        listView = (ListView) view.findViewById(R.id.roomListView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(view.getContext(), i+"번방", Toast.LENGTH_SHORT).show();
                // 게임매니저의 Room이랑 연동해서 서버에 입장신호 보내고 받을때까지 일단 대기하는 코드 작성
                // 성공하면 아래 코드를 실행하고
                fragmentChanger.onEnterRoom();
                // 실패하면 AlertDialog 실행
            }
        });

        return view;
    }

    public void setFragmentChanger(FragmentChangerCallback callback)
    {
        this.fragmentChanger = callback;
    }
}
