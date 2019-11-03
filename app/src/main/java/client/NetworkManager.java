package client;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class NetworkManager implements Runnable {
    // 로깅 인스턴스 ===========================================================
    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkManager.class);
    private static final Logger FILELOGGER = LoggerFactory.getLogger("file");

    // 멤버 객체 및 변수 ===========================================================
    private Socket socket;

    private Thread startThread; // ANR 방지를 위해 전용 스레드로 초기화 작업 진행
    private ConnectionListener connectionListener;
    private ShutdownClass shutdownCallbackClass;

    private Reader reader;
    private Thread readerThread;

    private Writer writer;
    private Thread writerThread;

    // Singleton 패턴에 기반한 생성자 ===========================================================
    private NetworkManager() {
        startThread = new Thread(this);
    }

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
            socket.connect(new InetSocketAddress("119.193.229.120", 55248), 2000);
            connectionListener.onSuccess();
            FILELOGGER.info("서버에 접속됨 : {}/55248", InetAddress.getLocalHost().getAddress());

            socket.setTcpNoDelay(true);

            shutdownCallbackClass = new ShutdownClass();

            reader = new Reader(socket.getInputStream());
            reader.setShutdownCallback(shutdownCallbackClass);
            readerThread = new Thread(reader);
            readerThread.start();

            writer = new Writer(socket.getOutputStream());
            writer.setShutdownCallback(shutdownCallbackClass);
            writerThread = new Thread(writer);
            writerThread.start();

            // 테스트용
            writeRequest("HELLO!", Packet.PACKET_TYPE_STANDARD);

        } catch (Exception e) {
            connectionListener.onFail();
            e.printStackTrace();
        }
    }

    public void start() {
        startThread.start();
    }

    public void shutdown() {
        try {
            reader.threadStopRequest();
            writer.threadStopRequest();
            Thread.sleep(500);
            synchronized (socket) {
                if (!socket.isClosed())
                    socket.close();
            }
        } catch(Exception e) {
            FILELOGGER.error("소켓 연결 종료 중 알 수 없는 예외 발생 : {}", e);
            System.exit(-1);
        }
    }

    public void writeRequest(String content, short type) {
        Packet packet = new Packet(content, type);
        writer.writeRequest(packet);
    }

    public void setConnectionListener(ConnectionListener listener) {
        this.connectionListener = listener;
    }

    public void setResponseCallback(NetworkResponseCallback callback) {
        reader.setResponseCallback(callback);
    }

    class ShutdownClass implements ShutdownCallback
    {
        @Override
        public void shutdown() {
            try {
                reader.threadStopRequest();
                writer.threadStopRequest();
                synchronized (socket) {
                    if (!socket.isClosed())
                        socket.close();
                }
            } catch(Exception e) {
                FILELOGGER.error("소켓 연결 종료 중 알 수 없는 예외 발생 : {}", e);
                System.exit(-1);
            }
        }
    }
}
