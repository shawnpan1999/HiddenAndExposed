package Routers;

import Messages.*;
import worker.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class Main {
    public static Router localRouter;
    public static ArrayList<Router> routers;  //其他路由器的信息表
    public static Server server;
    public static Clint clint;

    public static void main(String[] args) {
        configureRouter(args);

        //输入指令
        boolean inputInstruction = true;
        while (inputInstruction) {
            inputInstruction = false;
            Scanner scan = new Scanner(System.in);
            String instruction = scan.next();
            int target; String text; long time;
            switch (instruction) {
                case "sendTo":
                    //指定端口，直接单次发送
                    //sendTo 8081 abc 5000
                    target = scan.nextInt();
                    text = scan.next();
                    time = scan.nextInt();
                    sendNormal(target, text, time);
                    break;
                case "randomSendTo":
                    //指定端口，间隔随机时间发送一次数据
                    //randomSendTo 8081 abc 5000
                    target = scan.nextInt();
                    text = scan.next();
                    time = scan.nextInt();
                    randomSend(target, text, time);
                    break;
                case "check":
                    //获取其他路由器(8081~8084)的信息
                    break;
                default:
                    System.out.println("非法的指令，请重新输入！");
                    System.out.print(">>> ");
                    inputInstruction = true;
                    break;
            }
        }
    }

    /***
     * 向范围内的所有路由广播
     */
    public void sendBroadcast(int target, String text, long busyTimeMillis) {
        try {
            //TODO: 未测试
            for (Router router : Main.routers) {
                if (Math.abs(router.location - Main.localRouter.location) > Main.localRouter.range) {
                    continue;   //如果所选的路由器在广播范围外，则不对他发送
                }
                sendNormal(target, text, busyTimeMillis);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean sendNormal(int target, String text, long busyTimeMillis) {
        //sendOnce 的返回值用于记录本次发送是否成功
        return clint.sendOnce(new NormalMsg(Main.localRouter.port, target, MsgType.NORMAL, text, busyTimeMillis), target);
    }

    public static void randomSend(int target, String text, long busyTimeMillis) {
        while(sendNormal(target, text, busyTimeMillis)) {
            double random = Math.random() * 3 + 2;  //2-5秒的随机发送间隔
            try {
                Thread.sleep(Math.round(random) * 1000);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void configureRouter(String[] args) {
        //参数列表：port location range
        //默认配置：
        int port = 8081;
        int location = 1;
        int range = 5;
        //覆盖配置：
        switch (args.length) {
            case 3:
                range = Integer.parseInt(args[2]);
            case 2:
                location = Integer.parseInt(args[1]);
            case 1:
                port = Integer.parseInt(args[0]);
            default:
                break;
        }
        Date lastBusyDate = new Date(); //初始繁忙期限为当前时间

        Main.localRouter = new Router(port, location, lastBusyDate, range);
        Main.server = new Server(port);
        new Thread(server).start();    //服务线程开启
        Main.clint = new Clint();
    }
}
