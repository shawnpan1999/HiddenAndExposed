package Routers;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Router {
    public int port;            //所在端口
    public int location;        //所在位置(横坐标)
    public Date lastBusyDate;   //繁忙期限(即过了这个时间就是空闲状态)
    public int range;           //广播范围

    public Router(int port, int location, Date lastBusyDate, int range) {
        this.port = port;
        this.location = location;
        this.lastBusyDate = lastBusyDate;
        this.range = range;
    }
    public void setLastBusyDate(Date lastBusyDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
        System.out.println("[" + Main.localRouter.port + "] 信道繁忙时间更新为: " + sdf.format(lastBusyDate));
        this.lastBusyDate = lastBusyDate;
    }
    public boolean isBusy() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
        return lastBusyDate.getTime() > System.currentTimeMillis();
    }
}
