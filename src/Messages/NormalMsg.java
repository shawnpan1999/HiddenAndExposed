package Messages;

import java.text.SimpleDateFormat;
import java.util.Date;

/***
 * 普通消息：接收方对消息作出回应，并根据消息携带的繁忙时间更新自身的繁忙时间
 */
public class NormalMsg extends Message{
    public MsgType type = MsgType.NORMAL;
    public Date lastBusyDate;   //接收方要根据这个Date设置信道的繁忙时间

    public NormalMsg(int fromPort, int toPort, MsgType type, String text) {
        super(fromPort, toPort, type, text);
        this.lastBusyDate = new Date();    //缺省没有繁忙时间0
    }
    public NormalMsg(int fromPort, int toPort, MsgType type, String text, Date lastBusyDate) {
        super(fromPort, toPort, type, text);
        this.lastBusyDate = lastBusyDate;
    }
    public NormalMsg(int fromPort, int toPort, MsgType type, String text, long busyTimeMillis) {
        super(fromPort, toPort, type, text);
        this.lastBusyDate = new Date(System.currentTimeMillis() + busyTimeMillis);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        //NORMAL 信息比其他信息要多一条 lastBusyDate
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
        builder.append(fromPort).append("&").append(toPort).append("&").append(type).append("&").append(text).append("&").append(sdf.format(lastBusyDate));
        return builder.toString();
    }
}
