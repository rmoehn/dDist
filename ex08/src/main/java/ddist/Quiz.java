package ddist;

public class Quiz {
    public static void main (String[] args) {
        String mode = args[0];
        String port = args[1];

        if (args.length != 2) {
            throw new IllegalArgumentException("Wrong number of arguments.");
        }

        if ( mode.equals("server") ) {
            (new QuizServer(port)).run();
        }
        else if ( mode.equals("client") ) {
            (new QuizClient(port)).run();
        }
        else {
            throw new IllegalArgumentException("Strange argument: " ++ mode);
        }
    }
}
