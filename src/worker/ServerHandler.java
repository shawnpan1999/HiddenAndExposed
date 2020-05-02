package worker;

import Messages.*;
import Routers.*;

import java.io.*;
import java.net.Socket;

public class ServerHandler implements Runnable {
    Socket socket;

    public ServerHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

            boolean quitFlag = false;
            while (!quitFlag) {
                String getWord = bufferedReader.readLine();
                Message getMsg = Message.parse(getWord);    //从得到的 getWord 中解析出 Message
                //TODO: 根据收到各种不同类型的 Message 做相应的动作
                switch(getMsg.type) {
                    case PROBE:
                        //TODO: 探查消息的返回
                        break;
                    case SILENT:
                        //静默消息本地显示即可
                        System.out.println("[" + Main.localRouter.port + "] 收到来自 " + getMsg.fromPort + " 的信息:" + getWord);
                        break;
                    case NORMAL:
                        //普通消息要本地显示，作出回应，设置信道繁忙时间
                        System.out.println("[" + Main.localRouter.port + "] 收到来自 " + getMsg.fromPort + " 的信息:" + getWord);
                        if (getMsg.toPort == Main.localRouter.port) {
                            //是发给我的消息，就要检查隐蔽站问题
                            if (Main.localRouter.isBusy()) {
                                //如果当前信道忙，则发生隐蔽站问题，且中断连接
                                System.out.println("[" + Main.localRouter.port + "] 当前信道正忙！ ");
                                System.out.println("【发生隐蔽站问题】");
                                quitFlag = true;
                            } else {
                                //信道不忙则正常回应消息
                                String echoWord = "get message successfully. Type = " + getMsg.type;
                                Message echoMsg = new SilentMsg(Main.localRouter.port, socket.getPort(), MsgType.SILENT, echoWord);  //回应的消息使用静默消息即可
                                Main.localRouter.setLastBusyDate(((NormalMsg)getMsg).lastBusyDate);    //更新繁忙时间
                                dataOutputStream.writeBytes(echoMsg.toString() + System.getProperty("line.separator"));    //toString 后写到输出流
                            }
                        } else {
                            //不是发给我的消息，只要更新一下当前信道的繁忙时间
                            Main.localRouter.setLastBusyDate(((NormalMsg)getMsg).lastBusyDate);    //更新繁忙时间
                        }
                        break;
                    case QUIT:
                        //退出消息则设置退出条件为 true
                        quitFlag = true;
                        break;
                    default:
                        quitFlag = true;
                        break;
                }
            }
            bufferedReader.close();
            // 关闭包装类，会自动关闭包装类中所包装的底层类。所以不用调用ips.close()
            dataOutputStream.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
