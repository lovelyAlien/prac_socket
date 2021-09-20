package serversocketchannel_socketchannel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;

public class Client {

    public static void main(String[] args) {
        SocketChannel socketChannel = null;

        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(true);
            System.out.println("[연결 요청]");
            socketChannel.connect(new InetSocketAddress("localhost", 5001));
            System.out.println("[연결 성공]");


        } catch (IOException e) {
            e.printStackTrace();
        }


        if (socketChannel.isOpen()) {
            try {
                socketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
