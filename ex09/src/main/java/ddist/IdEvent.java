package ddist;

public class IdEvent implements Event {
    private static final long serialVersionUID = 7402001329032617567L;
    public static final int NOT_SET = -1;

    private int _senderId;

    public IdEvent() {
        _senderId       = NOT_SET;
    }

    public void setSenderId(int senderId) {
        _senderId = senderId;
    }


    public int getSenderId() {
        if (_senderId == NOT_SET) {
            throw new IllegalStateException("senderId is not set in event.");
        }
        return _senderId;
    }
}
