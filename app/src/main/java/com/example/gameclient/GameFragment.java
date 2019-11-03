package com.example.gameclient;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class GameFragment extends Fragment {

    FragmentChangerCallback fragmentChanger;

    // 클라이언트의 게임 로직은 따로 모듈 만들 필요 없이 이 클래스에 붙여도 될 것 같다.
    // 서버도 Room 클래스 하나하나마다 게임모듈 하나씩 붙여놓으면 될듯.
    public GameFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_game, container, false);

        return view;
    }

    // 게임 시작시 초기화메서드 init()
    public void initialize() {

    }

    // 게임 끝나면 다시 방으로 나가는 메서드 escape()
    public void escape() {
        fragmentChanger.onExitRoom();
    }

    public void setFragmentChanger(FragmentChangerCallback callback)
    {
        this.fragmentChanger = callback;
    }
}
