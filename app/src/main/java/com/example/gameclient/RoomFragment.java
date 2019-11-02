package com.example.gameclient;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class RoomFragment extends Fragment {

    FragmentChangerCallback fragmentChanger;

    public RoomFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_room, container, false);

        // 입장 & 게임 1회 종료로 인한 onCreateView시 방 위의 유저 리스트 받아오기 (메서드로 구현 후 호출)

        Button startButton = view.findViewById(R.id.roomStartButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 방 인원이 2명 이상이면 시작
                fragmentChanger.onStartGame();
            }
        });

        return view;
    }

    public void setFragmentChanger(FragmentChangerCallback callback)
    {
        this.fragmentChanger = callback;
    }
}
