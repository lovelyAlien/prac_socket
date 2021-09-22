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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer extends Application {
    ExecutorService executorService;
    ServerSocket serverSocket;
    List<Client> connections = new Vector<Client>();

    void startServer() {
        //서버 시작 코드
        executorService = Executors.newFixedThreadPool(
                //PC의 CPU가 지원하는 core의 수
                //core의 수 만큼 스레드 생성해서 사용
                Runtime.getRuntime().availableProcessors()
        );

        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress("localhost", 5001));
        } catch (IOException e) {
            if (!serverSocket.isClosed()) {
                stopServer();
            }
            return;
        }

        Runnable runnable = new Runnable() {

            @Override
            public void run() {

                Platform.runLater(() -> {
                    displayText("[서버 시작]");
                    btnStartStop.setText("stop");
                });

                while (true) {
                    try {
                        //서버에서 연결 요청 받은 클라이언트의 소켓 정보
                        Socket socket = serverSocket.accept();
                        //서버에 연결된 클라이언트 IP: 현재 수행하는 Thread Pool의 Thread
                        String message = "[연결 수락: " + socket.getRemoteSocketAddress() + ": " + Thread.currentThread().getName() + "]";
                        Platform.runLater(() -> {
                            displayText(message);
                        });

                        //connections에 Client, 즉, 받은 socket을 담는다.
                        Client client = new Client(socket);
                        connections.add(client);

                        //연결 개수 출력

                        Platform.runLater(() -> {
                            displayText("[연결 개수: " + connections.size() + "]");
                        });
                    } catch (IOException e) {
                        if (!serverSocket.isClosed()) {
                            stopServer();
                        }
                        break;
                    }

                }


            }


        };//runnable end

        executorService.submit(runnable);

    }//startServer end


    void stopServer() {
        //서버 종료 코드
        try {
            Iterator<Client> iterator = connections.iterator();
            while (iterator.hasNext()) {
                Client client = iterator.next();
                client.socket.close();
                iterator.remove();
            }


            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }

            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdown();
            }
            Platform.runLater(() -> {
                        displayText("[서버 멈춤]");
                        btnStartStop.setText("start");
                    }
            );
        } catch (Exception e) {
        }
        ;

    }//stopServer end

    class Client {
        //        데이터 통신 코드
        Socket socket;

        public Client(Socket socket) {
            this.socket = socket;
            receive();
        }

        //클라이언트에서 보낸 데이터를 항상 받을 준비를 해야하므로
        //별도의 쓰레드에서 클라이언트의 데이터를 받을 준비를 한다.
        //쓰레드 풀의 쓰레드가 receive()을 호출하기 위해 읽기 작업을 정의한다.
        void receive() {
            //이 작업 객체를 작업 큐에 저장되도록 하기 위해서 sumbit 호출
            //executorService 내부의 쓰레드가 run 작업을 실행
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    //클라이언트에서 보낸 데이터 저장
                    while (true) {
                        byte[] byteArr = new byte[100];
                        try {
                            InputStream inputStream = socket.getInputStream();
                            int readByteCount = inputStream.read(byteArr);
                            if (readByteCount == -1) {
                                throw new IOException();
                            }
                            String message = "[요청 처리: " + socket.getRemoteSocketAddress() + ": " + Thread.currentThread().getName() + "]";
                            Platform.runLater(() -> displayText(message));

                            String data = new String(byteArr, 0, readByteCount, "UTF-8");

                            //하나의 클라이언트가 보낸 데이터를 모든 클라이언트에게 다 보내야 한다.
                            //connections에 저장된 클라이언트의 send() 호출해서 모든 클라이언트에게 데이터를 보낸다.
                            for (Client client : connections) {
                                client.send(data);
                            }


                        } catch (Exception e) {
                            //연결이 안된 클라이언트 제거
                            try {
                                connections.remove(Client.this);
                                String message = "[클라이언트 통신 안됨: " + socket.getRemoteSocketAddress() + ": " + Thread.currentThread().getName() + "]";
                                Platform.runLater(() -> displayText(message));
                                socket.close();
                                break;
                            } catch (IOException ioException) {}
                        }
                    }
                }
            };
            executorService.submit(runnable);

        }//receive end

        void send(String data) {
            Runnable runnable= new Runnable(){
                @Override
                public void run() {
                    try {
                        byte[] bytes=data.getBytes("UTF-8");
                        OutputStream outputStream= socket.getOutputStream();
                        outputStream.write(bytes);
                        outputStream.flush();

                    } catch (Exception e) {
                        //클라이언트 통신이 안됨
                        try {
                            String message = "[클라이언트 통신 안됨: " + socket.getRemoteSocketAddress() + ": " + Thread.currentThread().getName() + "]";
                            Platform.runLater(() -> displayText(message));
                            connections.remove(Client.this);
                            socket.close();
                        } catch (IOException ioException) { }

                    }

                }
            };
            //보내는 작업을 큐에 저장
            executorService.submit(runnable);


        }//send end

    }//Client end


    //////////////////////////////////////////////////////
    TextArea txtDisplay;
    Button btnStartStop;

    @Override
    public void start(Stage primaryStage) throws Exception {

        BorderPane root = new BorderPane();
        root.setPrefSize(500, 300);

        txtDisplay = new TextArea();
        txtDisplay.setEditable(false);
        BorderPane.setMargin(txtDisplay, new Insets(0, 0, 2, 0));
        root.setCenter(txtDisplay);

        btnStartStop = new Button("start");
        btnStartStop.setPrefHeight(30);
        btnStartStop.setMaxWidth(Double.MAX_VALUE);
        btnStartStop.setOnAction(e -> {
            if (btnStartStop.getText().equals("start")) {
                startServer();
            } else if (btnStartStop.getText().equals("stop")) {
                stopServer();
            }
        });
        root.setBottom(btnStartStop);

        Scene scene = new Scene(root);

//        System.out.println(getClass().getResource("/app.css"));


        scene.getStylesheets().add(getClass().getResource("/app.css").toString());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Server");
        primaryStage.setOnCloseRequest(event -> stopServer());
        primaryStage.show();
    }

    void displayText(String text) {
        txtDisplay.appendText(text + "\n");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
