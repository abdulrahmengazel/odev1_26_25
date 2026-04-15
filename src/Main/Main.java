package Main;

public class Main {

    public static void main(String[] args) {


        new Thread(() -> {
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            ChatServer.main(args);
        }, "server").start();

        new Thread(() -> {
            try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
            ChatClient.main(args);
        }, "client-1").start();

        new Thread(() -> {
            try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
            ChatClient.main(args);
        }, "client-2").start();

        new Thread(() -> {
            try { Thread.sleep(4000); } catch (InterruptedException ignored) {}
            ChatClient.main(args);
        }, "client-3").start();
    }
}
