package Messages;

/***
 * 退出消息：接收方接收后断开与发送方的链接
 */
public class QuitMsg extends Message{
    public MsgType type = MsgType.QUIT;

    public QuitMsg(int fromPort, int toPort, MsgType type, String text) {
        super(fromPort, toPort, type, text);
    }
}
