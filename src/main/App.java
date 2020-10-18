package main;

public class App {

    /**
     * It may send not exactly 50000 items because the sender may be stopped before server pool, but it's enough for task purposes
     */
    public static void main(String[] ar) throws InterruptedException {
        ProxyServerEmulator proxyServerEmulator = new ProxyServerEmulator();
        proxyServerEmulator.run();
        for (int i = 0; i < 50000; i++) {
            byte[] rawData = new byte[10];
            long ts = System.currentTimeMillis();
            proxyServerEmulator.addDataFromRequest(new Data(ts, rawData));
        }
        proxyServerEmulator.stop();
    }
}
