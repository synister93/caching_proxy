package main;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

/**
 * To keep data storing in case when the proxy stops itself, we may use storing to files. Each server handler thread may write the received data
 * to its own file (i.e. file with certain number) together with adding the data to the sender's queue. At next step, the sender sends data to the server and if 200 status
 * is received, offset is incremented and stored in the other file of the thread which put this data to the sender's queue. (We add offset to data structure as well)
 * Then, if proxy downs for some reason, it starts and loads data from files, puts to the sender's queue data messages which have no committed offset (per each thread).
 * Technically, it just compares data file's lines numbers and last committed offset for the current thread
 * Background thread may remove lines with numbers which are not more than last committed offset from data files with some retention policy
 */
public class ProxyServerEmulator {

    private final BlockingQueue<Data> INCOMING_REQUESTS_QUEUE = new ArrayBlockingQueue<>(Sender.MAX_QUEUE_SIZE);
    private final int NUMBER_OF_THREADS = Runtime.getRuntime().availableProcessors() * 2;
    private final ExecutorService SERVER_HANDLER_POOL = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    private final AtomicBoolean STOPPED = new AtomicBoolean();
    private final Sender SENDER = new Sender(STOPPED);

    public void run() {
        SENDER.run();
        IntStream.range(0, NUMBER_OF_THREADS).forEach(d -> runRequestHandler(STOPPED));
    }

    public void stop() {
        STOPPED.set(true);
        SERVER_HANDLER_POOL.shutdown();
        SENDER.stop();
    }

    public void addDataFromRequest(Data data) throws InterruptedException {
        INCOMING_REQUESTS_QUEUE.put(data);
    }

    private void runRequestHandler(AtomicBoolean stopped) {
        SERVER_HANDLER_POOL.execute(() -> {
            while (!stopped.get()) {
                try {
                    Data nextData = INCOMING_REQUESTS_QUEUE.take();
                    SENDER.addRawData(nextData);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
