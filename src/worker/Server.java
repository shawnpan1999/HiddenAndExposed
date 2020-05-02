package worker;

import Messages.*;
import Routers.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable{
    public int port;  //服务器端口

    public void run() {  //监听端口，接收请求
        try {
            System.out.println("[" + this.port + "] 服务端口启动");
            System.out.print(">>> ");
            ServerSocket serverSocket = new ServerSocket(port); //其实这里面就做了绑定 bind() 的操作
            while(true) {
                //在死循环内不断接受请求，每次接收到一个连接请求就新建一个socket，并开一条线程去处理
                Socket socket = serverSocket.accept();
                new Thread(new ServerHandler(socket)).start();  //ServerHandler处理接收到的输入，每个客户一个线程
            }
            //serverSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Server(int port) {
        this.port = port;
    }

}
