package inetaddress;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class InetAddressExample {

    public static void main(String[] args) {
        try {
            InetAddress local= InetAddress.getLocalHost();
            System.out.println("내 컴퓨터 IP 주소: "+ local.getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
