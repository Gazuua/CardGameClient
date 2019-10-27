package client;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

public class NetworkManager implements Runnable {
    // 로깅 인스턴스 ===========================================================
    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkManager.class);
    private static final Logger FILELOGGER = LoggerFactory.getLogger("file");

    // 멤버 객체 및 변수 ===========================================================
    private Socket socket;

    private Thread startThread; // ANR 방지를 위해 전용 스레드로 작업 진행

    private Reader reader;
    private Thread readerThread;

    private Writer writer;
    private Thread writerThread;

    // Singleton 패턴에 기반한 생성자 ===========================================================
    private NetworkManager() {
        LOGGER.debug("구성 중...");
        startThread = new Thread(this);
        LOGGER.debug("구성 완료");
    };

    private static class Singleton {
        private static final NetworkManager instance = new NetworkManager();
    }

    public static NetworkManager getInstance() {
        return Singleton.instance;
    }

    // public 메서드 ===========================================================
    @Override
    public void run() {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress("121.132.103.138", 55248), 5000);
            FILELOGGER.info("서버에 접속됨 : {}/55248", InetAddress.getLocalHost().getAddress());

            socket.setSoLinger(true, 0);
            socket.setTcpNoDelay(true);

            reader = new Reader(socket.getInputStream());
            readerThread = new Thread(reader);
            readerThread.start();

            writer = new Writer(socket.getOutputStream());
            writerThread = new Thread(writer);
            writerThread.start();

            while(true)
            {
                writeRequest("HELLO!", (short)0);
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() {
        startThread.start();
    }

    public void writeRequest(String content, short type) {
        Packet packet = new Packet(content, type);
        writer.writeRequest(packet);
    }

    public void shutdownGracefully() {

    }

    // protected 메서드 ===========================================================

}
