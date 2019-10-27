package client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;

public class Writer implements Runnable {
    // 로깅 인스턴스 ===========================================================
    private static final Logger LOGGER = LoggerFactory.getLogger(Writer.class);
    private static final Logger FILELOGGER = LoggerFactory.getLogger("file");

    // 멤버 객체 및 변수 ===========================================================
    private OutputStream outputStream;
    private Queue<byte[]> writeQueue;

    // 상태 변수 ===========================================================
    private boolean bStop;
    private int i = 0;

    // 생성자 ===========================================================
    public Writer(OutputStream stream) {
        this.outputStream = stream;
        writeQueue = new LinkedList<>();
        bStop = true;
    }

    // public 메서드 ===========================================================
    @Override
    public void run() {
        bStop = false;
        while(!bStop) {
            try {
                while(writeQueue.isEmpty());

                byte[] sendData = writeQueue.poll();
                LOGGER.debug("보내는 메세지 : " + new String(sendData));
                outputStream.write(sendData);
                outputStream.flush();
            } catch(IOException e) {
                FILELOGGER.info("WRITER -> 서버와의 연결이 끊어졌습니다. : {}", e);
                return;
            } catch(Exception e) {

            }
        }
    }

    public void writeRequest(Packet packet) {
        writeQueue.add(packet.Encode());
    }
}
