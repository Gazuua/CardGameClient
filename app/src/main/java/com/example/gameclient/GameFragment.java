package com.example.gameclient;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import client.ClientInfo;
import client.NetworkManager;
import client.NetworkResponseCallback;
import client.Packet;
import game.Card;

public class GameFragment extends Fragment {

    NetworkResponseCallback gameFragmentCallback;
    FragmentChangerCallback fragmentChanger;

    LinearLayout userInfoContainer;
    ImageView cardOneView;
    ImageView cardTwoView;
    TextView cardText;
    TextView betMoneyText;
    TextView allMoneyText;
    Button dieButton;
    Button callButton;
    Button doubleButton;

    int roomUser;

    int stdMoney;
    boolean bDie;
    String endString;

    Card myCardOne;
    Card myCardTwo;

    final int GAME_BETTING_NEUTRAL = 0;
    final int GAME_BETTING_CALL = 1;
    final int GAME_BETTING_DOUBLE = 2;
    final int GAME_BETTING_ALLIN = 3;
    final int GAME_BETTING_DIE = 4;

    // 망통~한끗
    final int CARD_MANGTONG = 0;
    final int CARD_HANKUT = 1;
    final int CARD_DUKUT = 2;
    final int CARD_SEKUT = 3;
    final int CARD_NEKUT = 4;
    final int CARD_DASUTKUT = 5;
    final int CARD_YUSUTKUT = 6;
    final int CARD_ILGOPKUT = 7;
    final int CARD_YUDULKUT = 8;
    final int CARD_GABO = 9;

    // 세륙~알리
    final int CARD_SERYUK = 101;
    final int CARD_JANGSA = 102;
    final int CARD_JANGBBING = 103;
    final int CARD_GUBBING = 104;
    final int CARD_ALLI = 105;

    // 삥땡~장땡
    final int CARD_BBINGTTAENG = 201;
    final int CARD_YITTAENG = 202;
    final int CARD_SAMTTAENG = 203;
    final int CARD_SATTAENG = 204;
    final int CARD_OTTAENG = 205;
    final int CARD_YUKTTAENG = 206;
    final int CARD_CHILTTAENG = 207;
    final int CARD_PALTTAENG = 208;
    final int CARD_GUTTAENG = 209;
    final int CARD_JANGTTAENG = 210;

    // 일삼, 일팔광땡
    final int CARD_ILSAM_GWANGTTAENG = 301;
    final int CARD_ILPAL_GWANGTTAENG = 302;

    // 무적의 삼팔광땡
    final int CARD_SAMPAL_GWANGTTAENG = 99999;

    public GameFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_game, container, false);

        final Handler handler = new Handler();

        bDie = false;

        endString = "";

        userInfoContainer = view.findViewById(R.id.gameOtherStateContainer);
        cardOneView = view.findViewById(R.id.gameCardView_1);
        cardTwoView = view.findViewById(R.id.gameCardView_2);
        cardText = view.findViewById(R.id.gameCardText);
        betMoneyText = view.findViewById(R.id.gameBettingMoneyText);
        allMoneyText = view.findViewById(R.id.gameMoneyText);
        dieButton = view.findViewById(R.id.gameDieButton);
        callButton = view.findViewById(R.id.gameCallButton);
        doubleButton = view.findViewById(R.id.gameDoubleButton);

        gameFragmentCallback = new NetworkResponseCallback() {
            @Override
            public void onRecv(String content, short type) {

                // 게임 프래그먼트에서는 아래와 같은 응답에 대한 콜백이 설정된다.
                switch(type)
                {
                    // 방에 있는 유저 리스트를 받는 패킷이다.
                    // 퇴장, 정보 업데이트 등의 상황에 서버에서 임의로 날아온다.
                    case Packet.PACKET_TYPE_GAME_USER_RES: {
                        final String[] info = content.split("/");
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                // 우선 컨테이너를 싹 비워주고
                                userInfoContainer.removeAllViews();
                                roomUser = 0;
                                for (int i = 0; i < info.length; i += 2) {
                                    // 컨테이너에 담을 유저 리스트를 하나씩 차곡차곡 쌓아준다.
                                    View inflatedView = ((LayoutInflater) view.getContext()
                                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).
                                            inflate(R.layout.layout_gameuserlist, null);

                                    userInfoContainer.addView(inflatedView);

                                    // inflate된 유저 리스트 뷰에 내용물을 채워준다.
                                    TextView nameText = inflatedView.findViewById(R.id.gameUserNameText);
                                    TextView bettingText = inflatedView.findViewById(R.id.gameUserBettingText);
                                    nameText.setText(info[i]);
                                    bettingText.setText(info[i + 1]);

                                    roomUser++;

                                    view.invalidate();
                                }
                            }
                        });
                    }
                    break;

                    // 기준 판돈, 전체 판돈을 받아오는 패킷이다.
                    case Packet.PACKET_TYPE_GAME_INFO_RES: {
                        final String[] info = content.split("/");

                        stdMoney = Integer.parseInt(info[0]);

                        betMoneyText.setText(info[0]);
                        allMoneyText.setText(info[1]);

                        view.invalidate();
                        }
                        break;

                    // 카드 정보를 받아오는 패킷이다.
                    // 최초 입장 시 1번만 받아온다.
                    case Packet.PACKET_TYPE_GAME_CARD_RES: {
                        final String[] info = content.split("/");
                        myCardOne = new Card(Integer.parseInt(info[0]), Boolean.parseBoolean(info[1]));
                        myCardTwo = new Card(Integer.parseInt(info[2]), Boolean.parseBoolean(info[3]));
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                setCardPairs();
                                setCardViews();
                                view.invalidate();
                            }
                        });
                    }
                        break;

                    // 누군가가 더블 베팅을 하면 날아오는 패킷이다.
                    // 이 패킷을 받으면 다이 상태가 아닌 한 버튼을 다시 활성화해 주면 된다.
                    case Packet.PACKET_TYPE_GAME_BETTING_DOUBLE_RES: {
                        // 참고로 이 패킷이 올 때는 반드시 방 정보가 Refresh된다.
                        if(bDie) return;

                        Log.d("누군가의 더블베팅으로 인한", "버튼 초기화");
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                dieButton.setClickable(true);
                                callButton.setClickable(true);
                                doubleButton.setClickable(true);
                                view.invalidate();
                            }
                        });
                    }
                        break;

                    // 하아ㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏ
                    // 이 패킷을 받았다면 게임이 끝났음을 의미한다.
                    // 이름/족보/다이여부/이름----- 순서로 데이터가 날아오니
                    // AlertDialog 에서 승리를 표현해주자.
                    // 프래그먼트의 모든 것을 정리해 주고 방으로 돌아가자.
                    case Packet.PACKET_TYPE_GAME_SET_RES: {
                        final String[] info = content.split("/");
                        for(int i=0; i<info.length; i+=3){
                            endString += info[i] + " :: " + getJokbo(Integer.parseInt(info[i+1]));
                            if(Boolean.parseBoolean(info[i+2]))
                                endString += "(다이)";
                            endString += "\r\n";
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("게임 끝")
                                        .setMessage(endString)
                                        .setCancelable(false)
                                        .setNeutralButton("확인", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                bDie = false;
                                                stdMoney = 0;
                                                escape();
                                            }
                                        }).create().show();
                            }
                        });
                    }
                        break;
                }
            }
        };

        NetworkManager.getInstance().setResponseCallback(gameFragmentCallback);

        // 콜백 설정이 완료되면 게임방에 들어왔음을 서버에 알린다.
        NetworkManager.getInstance().writeRequest(ClientInfo.getInstance().getRoom() + "",
                Packet.PACKET_TYPE_ENTER_GAME_REQ);

        // 다이 버튼을 누르면
        dieButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 모든 버튼을 잠그고 승리 시 취득금액을 0원으로 셋팅하며
                // 서버에 나 죽었다고 알린다.
                bDie = true;
                dieButton.setClickable(false);
                callButton.setClickable(false);
                doubleButton.setClickable(false);

                allMoneyText.setText("0");

                NetworkManager.getInstance().writeRequest("" + GAME_BETTING_DIE,
                        Packet.PACKET_TYPE_GAME_BETTING_REQ);
            }
        });

        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 콜과 더블 버튼만 잠그고 서버에 알린 후 페이즈 진행을 기다린다.
                callButton.setClickable(false);
                doubleButton.setClickable(false);

                NetworkManager.getInstance().writeRequest("" + GAME_BETTING_CALL,
                        Packet.PACKET_TYPE_GAME_BETTING_REQ);
            }
        });

        doubleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 콜과 더블 버튼만 잠그고 서버에 알린 후 페이즈 진행을 기다린다.
                callButton.setClickable(false);
                doubleButton.setClickable(false);

                NetworkManager.getInstance().writeRequest("" + GAME_BETTING_DOUBLE,
                        Packet.PACKET_TYPE_GAME_BETTING_REQ);
            }
        });

        return view;
    }

    // 해당 족보가 무엇인지 텍스트에 설정하는 메서드
    // 세륙, 장사, 장삥, 구삥, 알리, 땡, 광땡 등의 특수족보만 제외하고
    // 전부 더한 다음 10의 자리로 나누어 끗이라고 하면 된다.
    public void setCardPairs() {
        // 세륙
        if( (myCardOne.getNumber() == 4 && myCardTwo.getNumber() == 6)
                || (myCardOne.getNumber() == 6 && myCardTwo.getNumber() == 4) ) {
            cardText.setText("세륙");
            return;
        }

        // 장사
        if( (myCardOne.getNumber() == 10 && myCardTwo.getNumber() == 4)
                || (myCardOne.getNumber() == 4 && myCardTwo.getNumber() == 10) ) {
            cardText.setText("장사");
            return;
        }

        // 장삥
        if( (myCardOne.getNumber() == 10 && myCardTwo.getNumber() == 1)
                || (myCardOne.getNumber() == 1 && myCardTwo.getNumber() == 10) ) {
            cardText.setText("장삥");
            return;
        }

        // 구삥
        if( (myCardOne.getNumber() == 9 && myCardTwo.getNumber() == 1)
                || (myCardOne.getNumber() == 1 && myCardTwo.getNumber() == 9) ) {
            cardText.setText("구삥");
            return;
        }

        // 알리
        if( (myCardOne.getNumber() == 1 && myCardTwo.getNumber() == 2)
                || (myCardOne.getNumber() == 2 && myCardTwo.getNumber() == 1) ) {
            cardText.setText("알리");
            return;
        }

        // 땡 (두 카드 번호가 같을 떄
        if( myCardOne.getNumber() ==  myCardTwo.getNumber() )
        {
            switch(myCardOne.getNumber())
            {
                case 1:
                    cardText.setText("삥땡");
                    break;
                case 2:
                    cardText.setText("이땡");
                    break;
                case 3:
                    cardText.setText("삼땡");
                    break;
                case 4:
                    cardText.setText("사땡");
                    break;
                case 5:
                    cardText.setText("오땡");
                    break;
                case 6:
                    cardText.setText("육땡");
                    break;
                case 7:
                    cardText.setText("칠땡");
                        break;
                case 8:
                    cardText.setText("팔땡");
                    break;
                case 9:
                    cardText.setText("구땡");
                    break;
                case 10:
                    cardText.setText("장땡");
                    break;
            }
            return;
        }

        // 일삼광땡
        if( (myCardOne.getNumber() == 1 && myCardOne.getSpecial())
                && (myCardTwo.getNumber() == 3 && myCardTwo.getSpecial()) ) {
            cardText.setText("일삼광땡");
            return;
        }

        // 일삼광땡
        if( (myCardOne.getNumber() == 3 && myCardOne.getSpecial())
                && (myCardTwo.getNumber() == 1 && myCardTwo.getSpecial()) ) {
            cardText.setText("일삼광땡");
            return;
        }

        // 일팔광땡
        if( (myCardOne.getNumber() == 1 && myCardOne.getSpecial())
                && (myCardTwo.getNumber() == 8 && myCardTwo.getSpecial()) ) {
            cardText.setText("일팔광땡");
            return;
        }

        // 일팔광땡
        if( (myCardOne.getNumber() == 8 && myCardOne.getSpecial())
                && (myCardTwo.getNumber() == 1 && myCardTwo.getSpecial()) ) {
            cardText.setText("일팔광땡");
            return;
        }

        // 삼팔광땡
        if( (myCardOne.getNumber() == 3 && myCardOne.getSpecial())
                && (myCardTwo.getNumber() == 8 && myCardTwo.getSpecial()) ) {
            cardText.setText("삼팔광땡");
            return;
        }

        // 삼팔광땡
        if( (myCardOne.getNumber() == 8 && myCardOne.getSpecial())
                && (myCardTwo.getNumber() == 3 && myCardTwo.getSpecial()) ) {
            cardText.setText("삼팔광땡");
            return;
        }

        // 끗자리 설정
        int sum = myCardOne.getNumber() + myCardTwo.getNumber();
        if(sum >= 10)
            sum -= 10;

        switch(sum)
        {
            case 0:
                cardText.setText("망통");
                break;
            case 1:
                cardText.setText("한끗");
                break;
            case 2:
                cardText.setText("두끗");
                break;
            case 3:
                cardText.setText("세끗");
                break;
            case 4:
                cardText.setText("네끗");
                break;
            case 5:
                cardText.setText("다섯끗");
                break;
            case 6:
                cardText.setText("여섯끗");
                break;
            case 7:
                cardText.setText("일곱끗");
                break;
            case 8:
                cardText.setText("여덟끗");
                break;
            case 9:
                cardText.setText("갑오");
                break;
        }
    }

    // 카드 이미지를 셋팅하는 메서드
    // 그때그때 직접 하기 때문에 상당히 길다
    public void setCardViews() {

        switch(myCardOne.getNumber()) {
            case 1:
                if (myCardOne.getSpecial())
                    cardOneView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.cardones));
                else
                    cardOneView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.cardone));
                break;

            case 2:
                if (myCardOne.getSpecial())
                    cardOneView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.cardtwos));
                else
                    cardOneView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.cardtwo));
                break;

            case 3:
                if (myCardOne.getSpecial())
                    cardOneView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.cardthrees));
                else
                    cardOneView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.cardthree));
                break;

            case 4:
                if (myCardOne.getSpecial())
                    cardOneView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.cardfours));
                else
                    cardOneView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.cardfour));
                break;

            case 5:
                if (myCardOne.getSpecial())
                    cardOneView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.cardfives));
                else
                    cardOneView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.cardfive));
                break;

            case 6:
                if (myCardOne.getSpecial())
                    cardOneView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.cardsixs));
                else
                    cardOneView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.cardsix));
                break;

            case 7:
                if (myCardOne.getSpecial())
                    cardOneView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.cardsevens));
                else
                    cardOneView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.cardseven));
                break;

            case 8:
                if (myCardOne.getSpecial())
                    cardOneView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.cardeights));
                else
                    cardOneView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.cardeight));
                break;

            case 9:
                if (myCardOne.getSpecial())
                    cardOneView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.cardnines));
                else
                    cardOneView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.cardnine));
                break;

            case 10:
                if (myCardOne.getSpecial())
                    cardOneView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.cardtens));
                else
                    cardOneView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.cardten));
                break;
        }

        switch(myCardTwo.getNumber()) {
            case 1:
                if (myCardTwo.getSpecial())
                    cardTwoView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.cardones));
                else
                    cardTwoView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.cardone));
                break;

            case 2:
                if (myCardTwo.getSpecial())
                    cardTwoView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.cardtwos));
                else
                    cardTwoView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.cardtwo));
                break;

            case 3:
                if (myCardTwo.getSpecial())
                    cardTwoView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.cardthrees));
                else
                    cardTwoView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.cardthree));
                break;

            case 4:
                if (myCardTwo.getSpecial())
                    cardTwoView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.cardfours));
                else
                    cardTwoView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.cardfour));
                break;

            case 5:
                if (myCardTwo.getSpecial())
                    cardTwoView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.cardfives));
                else
                    cardTwoView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.cardfive));
                break;

            case 6:
                if (myCardTwo.getSpecial())
                    cardTwoView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.cardsixs));
                else
                    cardTwoView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.cardsix));
                break;

            case 7:
                if (myCardTwo.getSpecial())
                    cardTwoView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.cardsevens));
                else
                    cardTwoView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.cardseven));
                break;

            case 8:
                if (myCardTwo.getSpecial())
                    cardTwoView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.cardeights));
                else
                    cardTwoView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.cardeight));
                break;

            case 9:
                if (myCardTwo.getSpecial())
                    cardTwoView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.cardnines));
                else
                    cardTwoView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.cardnine));
                break;

            case 10:
                if (myCardTwo.getSpecial())
                    cardTwoView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.cardtens));
                else
                    cardTwoView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.cardten));
                break;
        }
    }

    public String getJokbo(int code)
    {
        switch(code)
        {
            case CARD_SERYUK:
                return "세륙";
            case CARD_JANGSA:
                return "장사";
            case CARD_JANGBBING:
                return "장삥";
            case CARD_GUBBING:
                return "구삥";
            case CARD_ALLI:
                return "알리";
            case CARD_BBINGTTAENG:
                return "삥땡";
            case CARD_YITTAENG:
                return "이땡";
            case CARD_SAMTTAENG:
                return "삼땡";
            case CARD_SATTAENG:
                return "사땡";
            case CARD_OTTAENG:
                return "오땡";
            case CARD_YUKTTAENG:
                return "육땡";
            case CARD_CHILTTAENG:
                return "칠땡";
            case CARD_PALTTAENG:
                return "팔땡";
            case CARD_GUTTAENG:
                return "구땡";
            case CARD_JANGTTAENG:
                return "장땡";
            case CARD_ILSAM_GWANGTTAENG:
                return "일삼광땡";
            case CARD_ILPAL_GWANGTTAENG:
                return "일팔광땡";
            case CARD_SAMPAL_GWANGTTAENG:
                return "삼팔광땡";
            case CARD_MANGTONG:
                return "망통";
            case CARD_HANKUT:
                return "한끗";
            case CARD_DUKUT:
                return "두끗";
            case CARD_SEKUT:
                return "세끗";
            case CARD_NEKUT:
                return "네끗";
            case CARD_DASUTKUT:
                return "다섯끗";
            case CARD_YUSUTKUT:
                return "여섯끗";
            case CARD_ILGOPKUT:
                return "일곱끗";
            case CARD_YUDULKUT:
                return "여덟끗";
            case CARD_GABO:
                return "갑오";
            default:
                return null;
        }
    }

    // 게임 끝나면 다시 방으로 나가는 메서드 escape()
    public void escape() {
        fragmentChanger.onEndGame();
    }

    public void setFragmentChanger(FragmentChangerCallback callback)
    {
        this.fragmentChanger = callback;
    }
}
