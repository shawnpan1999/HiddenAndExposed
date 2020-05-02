package worker;

import Messages.*;
import Routers.*;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class Clint {
    Socket socket;
    BufferedReader bufferedReader;  //通道的输入Reader
    DataOutputStream dataOutputStream;  //通道的Data输出流

    private void connect(int port) {
        try {
            socket = new Socket(InetAddress.getByName("127.0.0.1"), port); //连接到目标端口 Server (需要 Server 先开启)

            //初始化通道
            //同一个通道，服务端的输出流就是客户端的输入流；服务端的输入流就是客户端的输出流
            InputStream inputStream = socket.getInputStream();    //开启通道的输入流
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            OutputStream outputStream = socket.getOutputStream();  //开启通道的输出流
            dataOutputStream = new DataOutputStream(outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void close() {
        try {
            dataOutputStream.close();
            bufferedReader.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * 直接向一个端口发送指定 Message，只发一次，发完就退出 QUIT
     * @param message 指定的 Message 信息
     * @param targetPort 指定端口
     */
    public boolean sendOnce(Message message, int targetPort) {
        try {
            this.connect(targetPort);
            dataOutputStream.writeBytes(message.toString() + System.getProperty("line.separator"));

            //接收回应
            String echoWord = bufferedReader.readLine();
            if (echoWord == null) {
                System.out.println("[" + Main.localRouter.port + "] 通道关闭");
                return false;
            } else {
                QuitMsg quitMsg = new QuitMsg(Main.localRouter.port, socket.getPort(), MsgType.QUIT, "quit");
                dataOutputStream.writeBytes(quitMsg.toString());
            }
            this.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
