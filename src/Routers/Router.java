package Routers;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Router {
    public int port;            //所在端口
    public int location;        //所在位置(横坐标)
    public Date lastBusyDate;   //繁忙期限(即过了这个时间就是空闲状态)
    public int range;           //广播范围
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");

    public Router(int port, int location, Date lastBusyDate, int range) {
        this.port = port;
        this.location = location;
        this.lastBusyDate = lastBusyDate;
        this.range = range;
    }
    public void setLastBusyDate(Date lastBusyDate) {
        this.lastBusyDate = lastBusyDate;
    }
    public boolean isBusy() {
        return lastBusyDate.getTime() > System.currentTimeMillis();
    }
    public void printState() {
        System.out.println("所在端口： " + port);
        System.out.println("所在位置： " + location);
        if (isBusy()) {
            System.out.println("繁忙状态： " + "繁忙    (持续至" + sdf.format(lastBusyDate) + ")");
        } else {
            System.out.println("繁忙状态： " + "空闲");
        }
        System.out.println("广播范围： " + range);
    }
}
