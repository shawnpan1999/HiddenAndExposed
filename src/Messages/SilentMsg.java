package Messages;

/***
 * 静默消息：接收方无需作出回应、也无需设置繁忙时间的消息
 */
public class SilentMsg extends Message{
    public MsgType type = MsgType.SILENT;

    public SilentMsg(int fromPort, int toPort, MsgType type, String text) {
        super(fromPort, toPort, type, text);
    }
}
