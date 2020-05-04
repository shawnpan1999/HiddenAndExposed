package Routers;

import Messages.*;
import worker.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class Main {
    public static int[] DEFAULT_PORT = new int[]{8081, 8082, 8083, 8084};
    public static int[] DEFAULT_LOC = new int[]{0, 3, 6, 9};
    public static Date[] DEFAULT_DATE = new Date[]{new Date(), new Date(), new Date(), new Date()};
    public static int[] DEFAULT_RANGE = new int[]{5, 5, 5, 5};

    public static Router localRouter;
    public static ArrayList<Router> routers = new ArrayList<>();  //其他路由器的信息表
    public static Server server;
    public static Clint clint;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("无效参数！");
            return;
        }
        configureRouter(args);
        //输入指令
        boolean inputInstruction = true;
        while (inputInstruction) {
            System.out.print(">>> ");
            inputInstruction = false;
            Scanner scan = new Scanner(System.in);
            String instruction = scan.next();
            int target, min, max; String text; long time;
            switch (instruction) {
                case "send":
                    //指定接收端口，会对所有范围内的路由器广播
                    //send 8081 abc 5000
                    target = scan.nextInt();
                    text = scan.next();
                    time = scan.nextLong();
                    sendNormal(target, text, time);
                    inputInstruction = true;
                    break;
                case "sendRandom":
                    //指定端口，间隔随机时间发送一次数据
                    //sendRandom 8081 abc 5000
                    target = scan.nextInt();
                    text = scan.next();
                    time = scan.nextLong();
                    min = scan.nextInt();
                    max = scan.nextInt();
                    sendRandom(target, text, time, min, max);
                    break;
                case "state":
                    //打印当前路由器的信息
                    localRouter.printState();
                    inputInstruction = true;
                    break;
                default:
                    System.out.println("非法的指令，请重新输入！");
                    inputInstruction = true;
                    break;
            }
        }
    }

    /***
     * 广播发送，但是只有指定的端口会处理信息，其他的会更新信道繁忙时间
     * @param target    目标端口
     * @param text    发送文本
     * @param busyTimeMillis    占用信道持续时间
     * @return    是否发送成功
     */
    public static boolean sendNormal(int target, String text, long busyTimeMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("kk:mm:ss");
        if (Main.localRouter.isBusy()) {
            String nowTime = sdf.format(new Date());
            System.out.println(nowTime + " | [" + Main.localRouter.port + "] 信道正忙，不发送消息");
            return false;
        } else {
            for (Router router : Main.routers) {
                if (Math.abs(router.location - Main.localRouter.location) > Main.localRouter.range) {
                    continue;   //如果所选的路由器在广播范围外，则不对他发送
                }
                clint.send(new NormalMsg(Main.localRouter.port, target, MsgType.NORMAL, text, busyTimeMillis), router.port);
            }
            return true;
        }
    }

    public static void sendRandom(int target, String text, long busyTimeMillis, int min, int max) {
        while(sendNormal(target, text, busyTimeMillis)) {
            double random = Math.random() * (max - min) + min;  //min~max秒的随机发送间隔
            try {
                Thread.sleep(Math.round(random) * 1000);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void configureRouter(String[] args) {
        Router a = new Router(DEFAULT_PORT[0], DEFAULT_LOC[0], DEFAULT_DATE[0], DEFAULT_RANGE[0]);
        Router b = new Router(DEFAULT_PORT[1], DEFAULT_LOC[1], DEFAULT_DATE[1], DEFAULT_RANGE[1]);
        Router c = new Router(DEFAULT_PORT[2], DEFAULT_LOC[2], DEFAULT_DATE[2], DEFAULT_RANGE[2]);
        Router d = new Router(DEFAULT_PORT[3], DEFAULT_LOC[3], DEFAULT_DATE[3], DEFAULT_RANGE[3]);
        Main.routers.add(a); Main.routers.add(b);
        Main.routers.add(c); Main.routers.add(d);
        switch (args[0]) {
            case "A":
                Main.localRouter = a;
                Main.routers.remove(0);
                break;
            case "B":
                Main.localRouter = b;
                Main.routers.remove(1);
                break;
            case "C":
                Main.localRouter = c;
                Main.routers.remove(2);
                break;
            case "D":
                Main.localRouter = d;
                Main.routers.remove(3);
                break;
            default:
                break;
        }
        Main.server = new Server(Main.localRouter.port);
        new Thread(server).start();    //服务线程开启
        Main.clint = new Clint();
    }
}
