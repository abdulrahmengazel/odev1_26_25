package Main;

public class Main {

    public static void main(String[] args) {
        ChatServer.main(args);

        new Thread(() -> {
            try { Thread.sleep(800); } catch (InterruptedException ignored) {}
            ChatClient.main(args);
        }, "client-1").start();

        new Thread(() -> {
            try { Thread.sleep(1600); } catch (InterruptedException ignored) {}
            ChatClient.main(args);
        }, "client-2").start();

        new Thread(() -> {
            try { Thread.sleep(2400); } catch (InterruptedException ignored) {}
            ChatClient.main(args);
        }, "client-3").start();
    }
}
