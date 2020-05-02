package Messages;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class Message {
    public int fromPort;    //消息发送方
    public int toPort;      //消息接收方
    public MsgType type;    //消息类型
    public String text;     //消息正文

    public Message(int fromPort, int toPort, MsgType type, String text) {
        this.fromPort = fromPort;
        this.toPort = toPort;
        this.type = type;
        this.text = text;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(fromPort).append("&").append(toPort).append("&").append(type).append("&").append(text);
        return builder.toString();
    }

    /***
     * 解析 String 变为 Message
     * @param str 字符串，格式如 fromPort&toPort&type&text&time[&lastBusyTime]
     * @return msg 解析的 Message
     */
    public static Message parse(String str) {
        Message msg;
        String[] fields = str.split("&");
        int fromPort = Integer.parseInt(fields[0]);
        int toPort = Integer.parseInt(fields[1]);
        MsgType type = MsgType.valueOf(fields[2]);
        String text = fields[3];
        switch(fields[2]) {
            case "PROBE":
                msg = new ProbeMsg(fromPort, toPort, type, text);
                break;
            case "SILENT":
                msg = new SilentMsg(fromPort, toPort, type, text);
                break;
            case "NORMAL":
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
                try {
                    msg = new NormalMsg(fromPort, toPort, type, text, sdf.parse(fields[4]));
                } catch (ParseException e) {
                    //如果出错就 QUIT
                    msg = new QuitMsg(0, 0, MsgType.QUIT, "error");
                    e.printStackTrace();
                }
                break;
            case "QUIT":
                msg = new QuitMsg(fromPort, toPort, type, text);
                break;
            default:
                msg = new QuitMsg(0, 0, MsgType.QUIT, "error");
                break;
        }
        return msg;
    }
}
