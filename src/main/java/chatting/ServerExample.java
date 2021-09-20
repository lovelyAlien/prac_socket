package chatting;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerExample extends Application {
    ExecutorService executorService;
    ServerSocket serverSocket;
    List<Client> connections = new Vector<Client>();

    void startServer() {
        //서버 시작 코드
        executorService= Executors.newFixedThreadPool(
                //PC의 CPU가 지원하는 core의 수
                //core의 수 만큼 스레드 생성해서 사용
                Runtime.getRuntime().availableProcessors()
        );

        try {
                    serverSocket= new ServerSocket();
                    serverSocket.bind(new InetSocketAddress("localhost", 5001));
        } catch (IOException e) {
            if(!serverSocket.isClosed()){
                    stopServer();
            }
            return;
        }

        Runnable runnable=new Runnable(){

            @Override
            public void run() {

                Platform.runLater(()->{
                    displayText("[서버 시작]");
                    btnStartStop.setText("stop");
                });

                while(true){
                    try {
                        //서버에서 연결 요청 받은 클라이언트의 소켓 정보
                        Socket socket=serverSocket.accept();
                        //서버에 연결된 클라이언트 IP: 현재 수행하는 Thread Pool의 Thread
                        String message="[연결 수락: "+ socket.getRemoteSocketAddress()+": "+Thread.currentThread().getName()+"]";
                        Platform.runLater(()->{
                            displayText(message);
                        });

                        //connections에 Client, 즉, 받은 socket을 담는다.
                        Client client= new Client(socket);
                        connections.add(client);

                        //연결 개수 출력

                        Platform.runLater(()->{
                            displayText("[연결 개수: "+ connections.size()+"]");
                        });
                    } catch (IOException e) {
                        if(!serverSocket.isClosed()){
                            stopServer();
                        }
                        break;
                    }

                }




            }


        };//runnable end

        executorService.submit(runnable);

    }

    void stopServer(){
        //서버 종료 코드
    }

    class Client {
//        데이터 통신 코드
        public Client(Socket socket){

        }
    }


    //////////////////////////////////////////////////////
    TextArea txtDisplay;
    Button btnStartStop;

    @Override
    public void start(Stage primaryStage) throws Exception {

        BorderPane root = new BorderPane();
        root.setPrefSize(500, 300);

        txtDisplay = new TextArea();
        txtDisplay.setEditable(false);
        BorderPane.setMargin(txtDisplay, new Insets(0,0,2,0));
        root.setCenter(txtDisplay);

        btnStartStop = new Button("start");
        btnStartStop.setPrefHeight(30);
        btnStartStop.setMaxWidth(Double.MAX_VALUE);
        btnStartStop.setOnAction(e->{
            if(btnStartStop.getText().equals("start")) {
                startServer();
            } else if(btnStartStop.getText().equals("stop")){
                stopServer();
            }
        });
        root.setBottom(btnStartStop);

        Scene scene = new Scene(root);

//        System.out.println(getClass().getResource("/app.css"));


        scene.getStylesheets().add(getClass().getResource("/app.css").toString());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Server");
        primaryStage.setOnCloseRequest(event->stopServer());
        primaryStage.show();
    }

    void displayText(String text) {
        txtDisplay.appendText(text + "\n");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
