package com.example.gameclient;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import client.NetworkManager;

// GameActivity :: 로그인 성공 이후 클라이언트 상태를 다루는 액티비티
// 이 액티비티 진입 이후부터는 모든 화면이동은 프래그먼트의 교체로 진행된다.
public class GameActivity extends AppCompatActivity {

    static final int PLACE_LOBBY = 0;
    static final int PLACE_ROOM = 1;
    static final int PLACE_INGAME = 2;

    FragmentManager fragmentManager;
    FragmentChanger fragmentChanger;

    LobbyFragment lobbyFragment;
    RoomFragment roomFragment;
    GameFragment gameFragment;

    int place;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        fragmentManager = getSupportFragmentManager();
        fragmentChanger = new FragmentChanger();

        lobbyFragment = new LobbyFragment();
        lobbyFragment.setFragmentChanger(fragmentChanger);
        roomFragment = new RoomFragment();
        roomFragment.setFragmentChanger(fragmentChanger);
        gameFragment = new GameFragment();
        gameFragment.setFragmentChanger(fragmentChanger);

        place = PLACE_LOBBY;
        fragmentManager.beginTransaction()
                .add(R.id.gameFragmentContainer, lobbyFragment)
                .commit();
    }

    class FragmentChanger implements FragmentChangerCallback
    {
        @Override
        public void onEnterRoom() {
            place = PLACE_ROOM;
            fragmentManager.beginTransaction()
                    .remove(lobbyFragment)
                    .add(R.id.gameFragmentContainer, roomFragment)
                    .commit();
        }

        @Override
        public void onExitRoom() {
            place = PLACE_LOBBY;
            fragmentManager.beginTransaction()
                    .remove(roomFragment)
                    .add(R.id.gameFragmentContainer, lobbyFragment)
                    .commit();
        }

        @Override
        public void onStartGame() {
            place = PLACE_INGAME;
            fragmentManager.beginTransaction()
                    .remove(roomFragment)
                    .add(R.id.gameFragmentContainer, gameFragment)
                    .commit();
        }

        @Override
        public void onEndGame() {
            place = PLACE_ROOM;
            fragmentManager.beginTransaction()
                    .remove(gameFragment)
                    .add(R.id.gameFragmentContainer, roomFragment)
                    .commit();
        }
    }

    // 뒤로 버튼을 짧은 시간 안에 두 번 연속 누르면 종료
    private long lastTimeBackPressed;
    @Override
    public void onBackPressed()
    {
        switch(place)
        {
            // 현재 로비에 있을 경우 뒤로 두 번 누르면 게임 종료
            case PLACE_LOBBY:
                if (System.currentTimeMillis() - lastTimeBackPressed < 1500) {
                    NetworkManager.getInstance().shutdown();
                    finish();
                }
                else {
                    Toast.makeText(this, "'뒤로' 버튼을 한 번 더 누르시면 종료합니다.", Toast.LENGTH_SHORT).show();
                    lastTimeBackPressed = System.currentTimeMillis();
                }
                break;

            // 현재 방 안에 있을 경우 뒤로 두 번 누르면 방에서 퇴장
            case PLACE_ROOM:
                if (System.currentTimeMillis() - lastTimeBackPressed < 1500)
                    fragmentChanger.onExitRoom();
                else {
                    Toast.makeText(this, "'뒤로' 버튼을 한 번 더 누르시면 방에서 퇴장합니다.", Toast.LENGTH_SHORT).show();
                    lastTimeBackPressed = System.currentTimeMillis();
                }
                break;

            // 현재 게임 중일 경우 퇴장 불가를 통지
            case PLACE_INGAME:
                Toast.makeText(this, "게임 중에는 퇴장하실 수 없습니다.", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
