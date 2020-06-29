package Messages;

/***
 * 探查消息：接收端会发回当前站点的端口、繁忙时间等消息
 */
public class ProbeMsg extends Message{
    public MsgType type = MsgType.PROBE;

    public ProbeMsg(int fromPort, int toPort, MsgType type, String text) {
        super(fromPort, toPort, type, text);
    }
}
