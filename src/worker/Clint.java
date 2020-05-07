package worker;

import Messages.*;
import Routers.*;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Clint {
    private Socket socket;
    private BufferedReader bufferedReader;  //通道的输入Reader
    private DataOutputStream dataOutputStream;  //通道的Data输出流
    private SimpleDateFormat sdf = new SimpleDateFormat("kk:mm:ss");

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
    public String send(Message message, int targetPort) {
        try {
            this.connect(targetPort);
            dataOutputStream.writeBytes(message.toString() + System.getProperty("line.separator"));

            //接收回应
            String echoWord = bufferedReader.readLine();
            switch (echoWord) {
                case "ACK":
                case "QUIT":
                    QuitMsg quitMsg = new QuitMsg(Main.localRouter.port, socket.getPort(), MsgType.QUIT, "quit");
                    dataOutputStream.writeBytes(quitMsg.toString());
                    break;
                case "ERROR":
            }
            this.close();
            return echoWord;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
