package Main;

public class Main {

    public static void main(String[] args) {
        ChatServer.main(args);

        // Optional: you can uncomment these lines if you want to automatically start clients too.
         ChatClient.main(args);
         ChatClient.main(args);
         ChatClient.main(args);
    }
}
