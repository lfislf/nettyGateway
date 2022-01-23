package io.github.lfislf.backend;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// 创建了一个固定大小的线程池处理请求
public class BackendServer extends Thread {

    private String name;
    private int port;

    public BackendServer(String name, int port) {
        this.name = name;
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        BackendServer server01 = new BackendServer("server01", 8801);
        server01.start();
        BackendServer server02 = new BackendServer("server02", 8802);
        server02.start();
        server01.join();
        server02.join();
    }

    public void run() {
        ExecutorService executorService = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors() * 2);
        try {
            final ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                try {
                    final Socket socket = serverSocket.accept();
                    executorService.execute(() -> service(socket));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void service(Socket socket) {
        try {
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
            printWriter.println("HTTP/1.1 200 OK");
            printWriter.println("Content-Type:text/html;charset=utf-8");
            String body = "Hello! I'm " + name;
            printWriter.println("Content-Length:" + body.getBytes().length);
            printWriter.println();
            printWriter.write(body);
            printWriter.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}