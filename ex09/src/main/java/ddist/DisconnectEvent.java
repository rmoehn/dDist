package ddist;

/**
 * Event indicating that someone wants to disconnect.
 */
public class DisconnectEvent extends MyTextEvent {
    /*
     * There are six threads that have to be notified when one user wants to
     * disconnect:
     *
     *  MI: the main thread of the initiator of the disconnect
     *  MR: the main thread of the receiver of the disconnect
     *  II: the thread responsible for incoming events at the initiator
     *  IR: the thread responsible for incoming events at the receiver
     *  OI: the thread responsible for outgoing events at the initiator
     *  OR: the thread responsible for outgoing events at the receiver
     *
     * The process of disconnection uses the normal communication paths
     * between the threads and the editors. It looks like this:
     *
     * MI -> OI ------------------------> IR -> MR
     *                                     |
     *                                     v
     *       II <------------------------ OR
     *
     *  1. The main thread at the initiator creates a DisconnectEvent and puts
     *     it in the queue for outgoing events (outqueue). It also takes the
     *     necessary actions to put the editor in a disconnected state.
     *
     *  2. The thread OI, responsible for taking elements from the outqueue and
     *     sending them to the other editor, takes the DisconnectEvent from
     *     the outqueue, sends it to the other editor and shuts down, because
     *     of the special event.
     *
     *  3. The thread IR, responsible for receiving events from the other
     *     editor and putting them in the queue for incoming events (inqueue),
     *     receives the event and puts it in the inqueue. After this, the
     *     socket is only used by one of the threads in each editor. Those
     *     threads have to close it. IR indicates this to the threads coming
     *     after it by setting the _shouldClose flag in the DisconnectEvent.
     *     It puts the DisconnectEvent in the local outqueue and it shuts down.
     *
     *  4. OR takes the DisconnectEvent event from the outqueue and sends it
     *     on. The DisconnectEvent now indicates that the socket should be
     *     closed. OR complies and then shuts itself down.
     *
     *  5. II receives the DisconnectEvent event from the network, shuts the
     *     socket down as told and terminates.
     */

    private static final long serialVersionUID = -3411878142976145233L;

    // Indicates whether threads that see this event may close the socket
    private boolean _shouldClose = false;

    public DisconnectEvent() {
        super(-1);
    }

    public void setShouldClose() {
        _shouldClose = true;
    }

    public boolean shouldClose() {
        return _shouldClose;
    }
}
