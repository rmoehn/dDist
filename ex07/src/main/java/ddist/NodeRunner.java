package ddist;

public class NodeRunner {

    public static void main(final String[] args) {
        String localHostname = "";
        int localPort;
        String firstNodeHostname = "";
        int firstNodePort;
        ChordNode node = new ChordNode();

        if (args.length == 2) {
            localHostname = args[0];
            localPort = Interger.parseInt(args[1]);
            node.newNetwork(localHostname, localPort);
        }

        if (args.length == 4) {
            firstNodeHostname = args[2];
            firstNodePort = Interger.parseInt(args[3]);
            node.join(localHostname, localPort, firstNodeHostname, firstNodePort);
        } 

    }
}
