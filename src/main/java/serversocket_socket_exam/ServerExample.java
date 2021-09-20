package serversocket_socket_exam;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerExample {
    public static void main(String[] args) {
        ServerSocket serverSocket= null;



        try {
            serverSocket=new ServerSocket();
            serverSocket.bind(new InetSocketAddress("localhost", 5001));

            while(true){

                System.out.println("[연결 기다림]");
                Socket socket= serverSocket.accept();
                //클라이언트 소켓 정보
                InetSocketAddress isa= (InetSocketAddress) socket.getRemoteSocketAddress();
                System.out.println("[연결 수락함]"+ isa.getHostName());

                byte[] bytes= null;
                String message= null;

                InputStream is=socket.getInputStream();
                bytes=new byte[100];

                //bytes에 읽은 데이터 저장
                //데이터의 바이트 수 반환
                int readByteCount= is.read(bytes);
                message=new String(bytes, 0, readByteCount, "UTF-8");
                System.out.println("[데이터 받기 성공] "+ message);

                //클라이언트에 데이터 보내기
                OutputStream os=socket.getOutputStream();
                message="Hello Client! It is your message";

                bytes=message.getBytes("UTF-8");
                os.write(bytes);
                os.flush();
                System.out.println("[데이터 보내기 성공]");

                is.close();
                os.close();
                socket.close();


            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(!serverSocket.isClosed()){
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
