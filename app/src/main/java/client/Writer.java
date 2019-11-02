package client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Queue;

public class Writer implements Runnable {
    // 로깅 인스턴스 ===========================================================
    private static final Logger LOGGER = LoggerFactory.getLogger(Writer.class);
    private static final Logger FILELOGGER = LoggerFactory.getLogger("file");

    // 멤버 객체 및 변수 ===========================================================
    private OutputStream outputStream;
    private Queue<byte[]> writeQueue;
    private ShutdownCallback shutdownCallback;

    // 상태 변수 ===========================================================
    private boolean bStop;

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
                if (writeQueue.isEmpty())
                    synchronized (this) {
                    wait();
                }

                byte[] sendData;

                synchronized (writeQueue) {
                    sendData = writeQueue.poll();
                }
                LOGGER.debug("보내는 메세지 : " + new String(sendData));
                outputStream.write(sendData);
                outputStream.flush();
            } catch(IOException e) {
                FILELOGGER.info("WRITER -> 서버와의 연결이 끊어졌습니다. : {}", e);
                shutdownCallback.shutdown();
                return;
            } catch(InterruptedException e) {
                FILELOGGER.info("writerThread 종료됨");
                shutdownCallback.shutdown();
                return;
            } catch (Exception e) {
                FILELOGGER.error("Writer -> 알 수 없는 오류 발생 : {}", e);
                shutdownCallback.shutdown();
                return;
            }
        }
    }

    public void writeRequest(Packet packet) {
        synchronized (writeQueue) {
            writeQueue.add(packet.Encode());
        }
        synchronized (this) {
            notify();
        }
    }

    public void threadStopRequest() {
        bStop = true;
    }

    public void setShutdownCallback(ShutdownCallback callback) {
        this.shutdownCallback = callback;
    }
}
