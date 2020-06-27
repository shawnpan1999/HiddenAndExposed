package Stations;

import Messages.*;
import worker.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.concurrent.*;

public class Main {
    public static int[] DEFAULT_PORT = new int[]{8081, 8082, 8083, 8084};
    public static int[] DEFAULT_LOC = new int[]{0, 3, 6, 9};
    public static Date[] DEFAULT_DATE = new Date[]{new Date(), new Date(), new Date(), new Date()};
    public static int[] DEFAULT_RANGE = new int[]{5, 5, 5, 5};
    public static boolean RTS_OPEN = false;    //RTS功能是否开启
    public static boolean RANDOM_OPEN = false;    //是否允许随机发送状态

    public static Station localStation;
    public static ArrayList<Station> stations = new ArrayList<>();  //其他路由器的信息表
    public static Server server;
    public static Clint clint;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("无效参数！");
            return;
        }
        configureRouter(args);
        //输入指令
        boolean continueInput = true;
        while (continueInput) {
            System.out.print(">>> ");
            Scanner scan = new Scanner(System.in);
            String instruction = scan.next();
            int target, min, max; String text; long time;
            try {
                switch (instruction) {
                    case "send":
                        //指定接收端口，会对所有范围内的路由器广播
                        //send 8081 abc 5000
                        target = scan.nextInt();
                        text = scan.next();
                        time = scan.nextLong();
                        if (RTS_OPEN) {
                            sendWithRTS(target, text, time);
                        } else {
                            sendNormal(target, text, time);
                        }
                        break;
                    case "sendRandom":
                        //指定端口，间隔随机时间发送一次数据
                        //sendRandom 8081 abc 2000 2000 5000
                        target = scan.nextInt();
                        text = scan.next();
                        time = scan.nextLong();
                        min = scan.nextInt();
                        max = scan.nextInt();
                        RANDOM_OPEN = true;
                        //另开线程去执行发送循环
                        new Thread(() -> {
                            if (RTS_OPEN) {
                                sendWithRTSRandom(target, text, time, min, max);
                            } else {
                                sendRandom(target, text, time, min, max);
                            }
                        }).start();
                        break;
                    case "stop":
                        if (localStation.state >= 2) {
                            localStation.state = 0;
                        }
                        RANDOM_OPEN = false;
                        break;
                    case "state":
                        //打印当前路由器的信息
                        localStation.printState();
                        break;
                    case "RTS":
                        if (RTS_OPEN) {
                            RTS_OPEN = false;
                            System.out.println("关闭RTS");
                        } else {
                            RTS_OPEN = true;
                            System.out.println("开启RTS");
                        }
                        break;
                    default:
                        System.out.println("非法指令，请重新输入！");
                        break;
                }
            } catch (InputMismatchException e) {
                System.out.println("非法指令，请重新输入！");
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
        if (Main.localStation.isBusy()) {
            String nowTime = sdf.format(new Date());
            System.out.println(nowTime + " | 信道正忙，不发送消息");
            //检测一下目标真实信道状态
            String realState = clint.send(new ProbeMsg(Main.localStation.port, target, MsgType.PROBE, text), target);
            if (realState.equals("0")) {
                System.out.println(nowTime + " | " + target + " 实际上并没有繁忙，发生暴露站问题");
                Main.localStation.state = 3;
            }
            return false;
        } else {
            for (Station station : Main.stations) {
                if (Math.abs(station.location - Main.localStation.location) > Main.localStation.range) {
                    continue;   //如果所选的路由器在广播范围外，则不对他发送
                }
                clint.send(new NormalMsg(Main.localStation.port, target, MsgType.NORMAL, text, busyTimeMillis), station.port);
            }
            return true;
        }
    }

    /***
     * RTS发送：先发RTS预约信道，预约成功再发信息
     * @param target    目标端口
     * @param text    发送文本
     * @param busyTimeMillis    占用信道持续时间
     * @return    是否发送成功
     */
    public static boolean sendWithRTS(int target, String text, long busyTimeMillis) {
        int timeout = 500;
        SimpleDateFormat sdf = new SimpleDateFormat("kk:mm:ss");
        RTSMsg rtsMsg = new RTSMsg(Main.localStation.port, target, MsgType.RTS, text, timeout);
        Future<String> echoFuture = sendRTS(rtsMsg, target);
        try {
            String echoWord = echoFuture.get(timeout, TimeUnit.MILLISECONDS);   //在规定时间内接收消息
            if (echoWord.equals("CTS")) {
                //如果收到回复 CTS ，则发送接下来的信息
                NormalMsg normalMsg = new NormalMsg(Main.localStation.port, target, MsgType.NORMAL, text, busyTimeMillis);
                clint.send(normalMsg, target);
                return true;
            } else {
                return false;
            }
        } catch(Exception e) {
            //超出时间，表明信道预约失败
            String nowTime = sdf.format(new Date());
            System.out.println(nowTime + " | [" + Main.localStation.port + "] RTS预约失败：服务端无回复");
            clint.close();
            return false;
        }
    }

    public static void sendWithRTSRandom(int target, String text, long busyTimeMillis, int min, int max) {
        int timeout = 500;
        SimpleDateFormat sdf = new SimpleDateFormat("kk:mm:ss");
        RTSMsg rtsMsg = new RTSMsg(Main.localStation.port, target, MsgType.RTS, text, timeout);
        while (RANDOM_OPEN) {
            double random = Math.random() * (max - min) + min;  //min~max秒的随机发送间隔
            try {
                Thread.sleep(Math.round(random));
            } catch(Exception e) {
                e.printStackTrace();
            }

            Future<String> echoFuture = sendRTS(rtsMsg, target);    //发一个RTS
            try {
                String echoWord = echoFuture.get(timeout, TimeUnit.MILLISECONDS);   //在规定时间内接收消息
                if (echoWord.equals("CTS")) {
                    //如果收到回复 CTS ，则发送接下来的信息
                    NormalMsg normalMsg = new NormalMsg(Main.localStation.port, target, MsgType.NORMAL, text, busyTimeMillis);
                    clint.send(normalMsg, target);
                } else {
                    continue;
                }
            } catch(Exception e) {
                //超出时间，表明信道预约失败
                String nowTime = sdf.format(new Date());
                System.out.println(nowTime + " | [" + Main.localStation.port + "] RTS预约失败：服务端无回复");
                clint.close();  //手动关闭一下clint
                continue;
            }
        }
        System.out.println("随机发送结束");
    }

    public static Future<String> sendRTS(Message msg, int target) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        return executor.submit(() -> {
            return clint.send(msg, target);
        });
    }

    public static void sendRandom(int target, String text, long busyTimeMillis, int min, int max) {
        while(RANDOM_OPEN) {
            sendNormal(target, text, busyTimeMillis);
            double random = Math.random() * (max - min) + min;  //min~max秒的随机发送间隔
            try {
                Thread.sleep(Math.round(random));
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("随机发送结束");
    }

    public static void configureRouter(String[] args) {
        Station a = new Station(DEFAULT_PORT[0], DEFAULT_LOC[0], DEFAULT_DATE[0], DEFAULT_RANGE[0]);
        Station b = new Station(DEFAULT_PORT[1], DEFAULT_LOC[1], DEFAULT_DATE[1], DEFAULT_RANGE[1]);
        Station c = new Station(DEFAULT_PORT[2], DEFAULT_LOC[2], DEFAULT_DATE[2], DEFAULT_RANGE[2]);
        Station d = new Station(DEFAULT_PORT[3], DEFAULT_LOC[3], DEFAULT_DATE[3], DEFAULT_RANGE[3]);
        Main.stations.add(a); Main.stations.add(b);
        Main.stations.add(c); Main.stations.add(d);
        switch (args[0]) {
            case "A":
                Main.localStation = a;
                Main.stations.remove(0);
                break;
            case "B":
                Main.localStation = b;
                Main.stations.remove(1);
                break;
            case "C":
                Main.localStation = c;
                Main.stations.remove(2);
                break;
            case "D":
                Main.localStation = d;
                Main.stations.remove(3);
                break;
            default:
                break;
        }
        Main.server = new Server(Main.localStation.port);
        new Thread(server).start();    //服务线程开启
        Main.clint = new Clint();
    }
}
