package Stations;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Station {
    public int port;            //所在端口
    public int location;        //所在位置(横坐标)
    public Date lastBusyDate;   //繁忙期限(即过了这个时间就是空闲状态)
    public int range;           //广播范围
    public int state = 0;       //0 正常-空闲/正常-繁忙，2 隐蔽站节点，3 暴露站节点
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");

    public Station(int port, int location, Date lastBusyDate, int range) {
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
    public int getState() {
        //返回0-空闲；1-繁忙；2-隐蔽站；3-暴露站
        if (state == 0) {
            if (isBusy()) {
                return 1;
            }
            return 0;
        }
        return state;
    }
    public void printState() {
        System.out.println("所在端口： " + port);
        System.out.println("所在位置： " + location);
        System.out.println("状态编号： " + state);
        if (isBusy()) {
            System.out.println("繁忙状态： " + "繁忙    (持续至" + sdf.format(lastBusyDate) + ")");
        } else {
            System.out.println("繁忙状态： " + "空闲");
        }
        System.out.println("广播范围： " + range);
    }
}
