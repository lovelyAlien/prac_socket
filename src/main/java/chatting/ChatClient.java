package chatting;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ChatClient extends  Application{
    Socket socket;
    void startClient(){
        Thread thread=new Thread(){
            @Override
            public void run() {
                socket=new Socket();
                try {
                    socket.connect(new InetSocketAddress("localhost", 5001));
                    Platform.runLater(()->{
                        displayText("[연결 완료: " +socket.getRemoteSocketAddress()+ "]");
                        btnConn.setText("stop");
                        btnSend.setDisable(false);
                    });
                } catch (IOException e) {
                    Platform.runLater(()->displayText("[서버 통신 안됨]"));
                        if(!socket.isClosed()) stopClient();
                        return;
                }
                //예외 발생 넘어감=연결 성공
                //서버가 보낸 데이터 받음
                receive();
            }
        };
        thread.start();
    }

    void stopClient(){
        try{
            Platform.runLater(()-> {
                displayText("[연결 끊음]");
                btnConn.setText("start");
                btnSend.setDisable(true);
            });
            //소켓 닫음
            if(socket!=null && !socket.isClosed()){
                socket.close();
            }
        }
        catch(Exception e){}
    }

    //항상 서버가 보낸 데이터를 받아야 하므로 무한루프 작성
    void receive(){

        while(true){
            try{
                byte[] byteArr= new byte[100];
                InputStream inputStream = socket.getInputStream();
                //서버가 데이터를 보내기 전까지 blocking
                int readByCount= inputStream.read(byteArr);
                //서버가 소켓을 닫게 되면, 정상적으로 닫음=-1/ 비정상적으로 닫음=exception
                if(readByCount==-1){
                    throw new IOException();
                }
                String data= new String(byteArr, 0, readByCount, "UTF-8");
                Platform.runLater(()->displayText("[받기 완료]"+ data));

            }
            catch(Exception e){ //예외처리
                Platform.runLater(()->displayText("[서버 통신 안됨]"));
                stopClient();
                break;
            }
        }
    };
    void send(String data){
        //서버로 데이터를 보낼 때, 별도의 thread를 만들어서 보내줌. 데이터를 보내는 시간이 오래걸리면 UI가 멈춰있기 때문에,
        //javafx.applicaton thread가 통신코드 실행하지 않도록 해야한다.
        Thread thread= new Thread(){
            @Override
            public void run() {
                try {
                    byte[] byteArr= data.getBytes("UTF-8");
                    OutputStream outputStream = socket.getOutputStream();
                    outputStream.write(byteArr);
                    //(Ip+port) 어떻게 알고 서버에 보내지?
                    outputStream.flush();
                    Platform.runLater(()->displayText("[보내기 완료]"));
                } catch (Exception e) {
                    Platform.runLater(()->displayText("[서버 통신 안됨]"));
                    stopClient();
                }
            }
        };
        thread.start();

    };

    //////////////////////////////////////////////////////
    TextArea txtDisplay;
    TextField txtInput;
    Button btnConn, btnSend;

    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane root = new BorderPane();
        root.setPrefSize(500, 300);

        txtDisplay = new TextArea();
        txtDisplay.setEditable(false);
        BorderPane.setMargin(txtDisplay, new Insets(0,0,2,0));
        root.setCenter(txtDisplay);

        BorderPane bottom = new BorderPane();
        txtInput = new TextField();
        txtInput.setPrefSize(60, 30);
        BorderPane.setMargin(txtInput, new Insets(0,1,1,1));

        btnConn = new Button("start");
        btnConn.setPrefSize(60, 30);
        btnConn.setOnAction(e->{
            if(btnConn.getText().equals("start")) {
                startClient();
            } else if(btnConn.getText().equals("stop")){
                stopClient();
            }
        });

        btnSend = new Button("send");
        btnSend.setPrefSize(60, 30);
        btnSend.setDisable(true);
        btnSend.setOnAction(e->send(txtInput.getText()));

        bottom.setCenter(txtInput);
        bottom.setLeft(btnConn);
        bottom.setRight(btnSend);
        root.setBottom(bottom);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/app.css").toString());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Client");
        primaryStage.setOnCloseRequest(event->stopClient());
        primaryStage.show();
    }

    void displayText(String text) {
        txtDisplay.appendText(text + "\n");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
