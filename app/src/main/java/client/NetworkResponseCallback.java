package client;

// 뒤늦게 구조가 틀려먹었다는 것을 알고 새로 만든 ResponseCallback이다.
// 이 구조로 짜면 Reader에서 switch로 구분할 필요 없이 바로 액티비티나 프래그먼트에서
// 동작을 구분하고 행동할 수 있으며 한 번만 설정해 놔도 이곳으로 오는 모든 패킷을 처리한다.
// 나중에 다른 프로젝트 할 때는 삽질하지 말자 ㅜㅜ
public interface NetworkResponseCallback {
    public abstract void onRecv(String content, short type);
}
