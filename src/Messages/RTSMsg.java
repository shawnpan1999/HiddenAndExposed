package Messages;

import java.text.SimpleDateFormat;

public class RTSMsg extends Message{
    public MsgType type = MsgType.RTS;
    public int timeout;    //超时时间(毫秒)

    public RTSMsg(int fromPort, int toPort, MsgType type, String text, int timeout) {
        super(fromPort, toPort, type, text);
        this.timeout = timeout;
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        //RTS 信息比其他信息要多一条 timeout
        builder.append(fromPort).append("&").append(toPort).append("&").append(type).append("&").append(text).append("&").append(timeout);
        return builder.toString();
    }
}
