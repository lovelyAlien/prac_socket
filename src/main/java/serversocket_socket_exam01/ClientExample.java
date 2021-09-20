package serversocket_socket_exam01;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientExample {

    public static void main(String[] args) {
        Socket socket = null;


        try {
            socket = new Socket();
            System.out.println("[연결 요청]");
            socket.connect(new InetSocketAddress("localhost", 5001));
            System.out.println("[연결 성공]");


            //서버에 데이터를 보내기 위해 OutputStream 사용
            byte[] bytes=null;
            String message= null;
            OutputStream os=socket.getOutputStream();
            message="Hello Server! It is your message";

            //UTF-8 인코딩
            bytes=message.getBytes("UTF-8");
            os.write(bytes);
            //서버에 데이터 전송
            os.flush();
            System.out.println("[데이터 보내기 성공]");

            //서버에서 보낸 데이터 받기
            InputStream is=socket.getInputStream();
            bytes=new byte[100];
            int readByteCount= is.read(bytes);
            message=new String(bytes,0,readByteCount,"UTF-8");
            System.out.println("[데이터 받기 성공] "+message);

            os.close();
            is.close();
            //아래 조건문 코드에서 socket.close() 됨

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
