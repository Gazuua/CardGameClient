package client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Reader implements Runnable {
    // 로깅 인스턴스 ===========================================================
    private static final Logger LOGGER = LoggerFactory.getLogger(Reader.class);
    private static final Logger FILELOGGER = LoggerFactory.getLogger("file");

    // 멤버 객체 및 변수 ===========================================================
    private InputStream inputStream;
    private Queue<Packet> readQueue;
    private ShutdownCallback shutdownCallback;

    // 상태 변수 ===========================================================
    private boolean bStop;
    private boolean bHead;
    private boolean bEnd;
    private short type;
    private short size;

    // 생성자 ===========================================================
    public Reader(InputStream stream) {
        this.inputStream = stream;
        readQueue = new LinkedList<>();

        bStop = true;
        initialize();
    }

    // public 메서드 ===========================================================
    @Override
    public void run() {
        bStop = false;
        while(!bStop) {
            try {
                // 최초 헤더 받기 (8바이트)
                byte[] headerData = new byte[8];
                int recvByte = 0;
                do {
                    int temp = inputStream.read(headerData);
                    if ( temp == -1 ) {
                        shutdownCallback.shutdown();
                        return;
                    }
                    recvByte += temp;
                } while (recvByte != 8);

                recvByte = 0;

                // 헤더 문자열 검증(4바이트)
                if(headerData[0] == '\0')
                    if(headerData[1] == 'e')
                        if(headerData[2] == 'n')
                            if(headerData[3] == 'd')
                                bHead = true;

                // 올바르지 못한 프로토콜은 바로 연결 종료하여 준다.
                if (!bHead) {
                    shutdownCallback.shutdown();
                    return;
                }

                // 패킷 타입 복사(2바이트)
                byte[] btype = new byte[2];
                btype[0] = headerData[4];
                btype[1] = headerData[5];
                type = byteArrayToShort(btype);

                // 패킷 크기 복사(2바이트)
                byte[] bsize = new byte[2];
                bsize[0] = headerData[6];
                bsize[1] = headerData[7];
                size = byteArrayToShort(bsize);

                // 데이터 받기
                byte[] recvData = new byte[size + 4]; // 끝 문자열까지 받기 위함
                do {
                    int temp = inputStream.read(recvData);
                    if ( temp == -1 ) {
                        shutdownCallback.shutdown();
                        return;
                    }
                    recvByte += temp;
                } while (recvByte != size + 4);

                // 끝 문자열 검증(4바이트)
                if(recvData[recvData.length - 1] == '\0')
                    if(recvData[recvData.length - 2] == 'e')
                        if(recvData[recvData.length - 3] == 'n')
                            if(recvData[recvData.length - 4] == 'd')
                                bEnd = true;

                // 올바르지 못한 프로토콜은 바로 연결 종료하여 준다.
                if(!bEnd) {
                    shutdownCallback.shutdown();
                    return;
                }

                // 패킷 내 순수 데이터 영역만 분할
                byte[] content = new byte[size];
                for(int i=0; i<content.length; i++)
                    content[i] = recvData[i];

                // 작업 가능한 큐에 추가
                readQueue.add(new Packet(new String(content), type));

                // 분석 및 작업
                Packet packet = readQueue.poll();
                switch(packet.getType())
                {
                    case Packet.PACKET_TYPE_STANDARD:
                        LOGGER.debug("받은 메세지 :: " + packet.getContent());
                        break;

                    default:
                        break;
                }

                initialize();

            } catch(IOException e) {
                FILELOGGER.info("READER -> 서버와의 연결이 끊어졌습니다. : {}", e);
                shutdownCallback.shutdown();
                return;
            } catch (Exception e) {
                FILELOGGER.error("Reader -> 알 수 없는 오류 발생 : {}", e);
                shutdownCallback.shutdown();
                return;
            }
        }
    }

    public void threadStopRequest() {
        bStop = true;
    }

    public void setShutdownCallback(ShutdownCallback callback) {
        this.shutdownCallback = callback;
    }

    // private 메서드 ===========================================================
    private void initialize() {
        bHead = false;
        bEnd = false;
        type = 0;
        size = 0;
    }

    private short byteArrayToShort(byte[] arr)
    {
        return (short)(((arr[0] & 0xFF) << 8) + (arr[1] & 0xFF));
    }
}
