package ddist;

public class Transformer {
    public Transformer() { }

    public TransformedPair transform(JupiterEvent received, JupiterEvent
            local) {
        MyTextEvent recTE       = (MyTextEvent) received.getContainedEvent();
        JupiterTime recTS       = received.getTimestamp();
        boolean isRecFromServer = received.isFromServer();
        MyTextEvent transRecTE  = null;

        MyTextEvent locTE       = (MyTextEvent) local.getContainedEvent();
        JupiterTime locTS       = local.getTimestamp();
        boolean isLocFromServer = local.isFromServer();
        MyTextEvent transLocTE  = null;

        /*
         * Take care that all if's have else's!
         */

        if (recTE instanceof TextInsertEvent
                && locTE instanceof TextInsertEvent) {
            TextInsertEvent recInsert = (TextInsertEvent) recTE;
            String recStr = recInsert.getText();
            int recOffs   = recInsert.getOffset();
            int recLen    = recStr.length();

            TextInsertEvent locInsert = (TextInsertEvent) locTE;
            String locStr = locInsert.getText();
            int locOffs   = locInsert.getOffset();
            int locLen    = locStr.length();

            if (locOffs < recOffs) {
                transRecTE = new TextInsertEvent(recOffs + locLen, recStr);
                transLocTE = new TextInsertEvent(locOffs, locStr);
            }
            else if (locOffs > recOffs) {
                transRecTE = new TextInsertEvent(recOffs, recStr);
                transLocTE = new TextInsertEvent(locOffs + recLen, locStr);
            }
            else { // locOffs == recOffs
                if (received.isFromServer()) {
                    transRecTE = new TextInsertEvent(recOffs, recStr);
                    transLocTE = new TextInsertEvent(locOffs +recLen, locStr);
                }
                else { // local is from server
                    transRecTE = new TextInsertEvent(recOffs +locLen, recStr);
                    transLocTE = new TextInsertEvent(locOffs, locStr);
                }
            }
        }

        return new TransformedPair(
            new JupiterEvent(
                transRecTE,
                recTS,
                isRecFromServer
            ),
            new JupiterEvent(
                transLocTE,
                locTS,
                isLocFromServer
            )
        );
    }
}
