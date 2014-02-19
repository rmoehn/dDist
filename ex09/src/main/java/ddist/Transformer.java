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
            String recStr             = recInsert.getText();
            int recOffs               = recInsert.getOffset();
            int recLen                = recStr.length();

            TextInsertEvent locInsert = (TextInsertEvent) locTE;
            String locStr             = locInsert.getText();
            int locOffs               = locInsert.getOffset();
            int locLen                = locStr.length();

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
        else if (recTE instanceof TextRemoveEvent
                && locTE instanceof TextInsertEvent) {
            TextRemoveEvent recRemove = (TextRemoveEvent) recTE;
            int remOffs               = recRemove.getOffset();
            int remLen                = recRemove.getLength();

            TextInsertEvent locInsert = (TextInsertEvent) locTE;
            String insStr             = locInsert.getText();
            int insOffs               = locInsert.getOffset();
            int insLen                = insStr.length();

            if (insOffs <= remOffs) {
                transRecTE = new TextRemoveEvent(remOffs + insLen, remLen);
                transLocTE = new TextInsertEvent(insOffs, insStr);
            }
            else {
                // If we want to insert into a region that gets deleted
                if (insOffs < remOffs + remLen) {
                    transRecTE = new TextRemoveEvent(remOffs, remLen +insLen);

                    // Throw away the insert -- unelegant, but easy
                    transLocTE = new TextInsertEvent(insOffs, "");
                }
                else {
                    transRecTE = new TextRemoveEvent(remOffs, remLen);
                    transLocTE = new TextInsertEvent(insOffs -remLen, insStr);
                }
            }
        }
        else if (recTE instanceof TextInsertEvent
                && locTE instanceof TextRemoveEvent) {
            TextInsertEvent recInsert = (TextInsertEvent) recTE;
            String insStr             = recInsert.getText();
            int insOffs               = recInsert.getOffset();
            int insLen                = insStr.length();

            TextRemoveEvent locRemove = (TextRemoveEvent) locTE;
            int remOffs               = locRemove.getOffset();
            int remLen                = locRemove.getLength();

            if (insOffs <= remOffs) {
                transRecTE = new TextInsertEvent(insOffs, insStr);
                transLocTE = new TextRemoveEvent(remOffs + insLen, remLen);
            }
            else {
                // If we want to insert into a region that gets deleted
                if (insOffs < remOffs + remLen) {
                    // Throw away the insert -- unelegant, but easy
                    transRecTE = new TextInsertEvent(insOffs, "");

                    transLocTE = new TextRemoveEvent(remOffs, remLen +insLen);
                }
                else {
                    transRecTE = new TextInsertEvent(insOffs -remLen, insStr);
                    transLocTE = new TextRemoveEvent(remOffs, remLen);
                }
            }
        }
        else if (recTE instanceof TextRemoveEvent
                && locTE instanceof TextRemoveEvent) {
            TextRemoveEvent recRemove = (TextRemoveEvent) recTE;
            int recOffs               = recRemove.getOffset();
            int recLen                = recRemove.getLength();

            TextRemoveEvent locRemove = (TextRemoveEvent) locTE;
            int locOffs               = locRemove.getOffset();
            int locLen                = locRemove.getLength();

            if (locOffs > recOffs) {
                if (locOffs >= recOffs + recLen) {
                    transRecTE = new TextRemoveEvent(recOffs, recLen);
                    transLocTE = new TextRemoveEvent(locOffs -recLen, locLen);
                }
                else { // Removals overlap
                    // rec contains loc
                    if (locOffs + locLen < recOffs + recLen) {
                        transRecTE = new TextRemoveEvent(
                                         recOffs,
                                         recLen - locLen
                                     );
                        transLocTE = new TextRemoveEvent(locOffs, 0);
                    }
                    else {
                        transRecTE = new TextRemoveEvent(
                                         recOffs,
                                         locOffs - recOffs
                                     );
                        transLocTE = new TextRemoveEvent(
                                         recOffs,
                                         locOffs + locLen - (recOffs + recLen)
                                     );
                    }
                }
            }
            else if (recOffs > locOffs) {
                if (recOffs >= locOffs + locLen) {
                    transRecTE = new TextRemoveEvent(recOffs -locLen, recLen);
                    transLocTE = new TextRemoveEvent(locOffs, locLen);
                }
                else {
                    if (recOffs + recLen < locOffs + locLen) {
                        transRecTE = new TextRemoveEvent(locOffs, 0);
                        transLocTE = new TextRemoveEvent(
                                         recOffs,
                                         recLen - locLen
                                     );
                    }
                    else {
                        transRecTE = new TextRemoveEvent(
                                         locOffs,
                                         recOffs + recLen - (locOffs + locLen)
                                     );
                        transLocTE = new TextRemoveEvent(
                                         locOffs,
                                         recOffs - locOffs
                                     );
                    }
                }
            }
            else { // recOffs == locOffs -- nearly contained in another case
                if (recLen < locLen) {
                    transRecTE = new TextRemoveEvent(recOffs, 0);
                    transLocTE = new TextRemoveEvent(locOffs, locLen -recLen);
                }
                else if (locLen < recLen) {
                    transRecTE = new TextRemoveEvent(recOffs, recLen -locLen);
                    transLocTE = new TextRemoveEvent(locOffs, 0);
                }
                else { // equal length for both
                    transRecTE = new TextRemoveEvent(recOffs, recLen);
                    transLocTE = new TextRemoveEvent(locOffs, 0);
                }
            }
        }
        else {
            throw new IllegalArgumentException("Got wrong events.");
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
