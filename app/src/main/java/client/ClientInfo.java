package client;

public class ClientInfo {

    private String id;
    private int win;
    private int lose;
    private int money;
    private int room;

    // Singleton 패턴에 기반한 생성자 ===========================================================
    // 클라이언트에서 정보는 내껏만 가지고 있으면 되므로 싱글톤으로 설정함.
    private ClientInfo() {
        id = new String("");
    }

    private static class Singleton {
        private static final ClientInfo instance = new ClientInfo();
    }

    public static ClientInfo getInstance() {
        return ClientInfo.Singleton.instance;
    }

    // Getter & Setter ===========================================================
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getWin() {
        return win;
    }

    public void setWin(int win) {
        this.win = win;
    }

    public int getLose() {
        return lose;
    }

    public void setLose(int lose) {
        this.lose = lose;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public int getRoom() {
        return room;
    }

    public void setRoom(int room) {
        this.room = room;
    }
}
