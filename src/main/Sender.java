package main;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Sender {

    public static final int MAX_QUEUE_SIZE = 1000;
    private final BlockingQueue<Data> MESSAGES_QUEUE = new ArrayBlockingQueue<>(MAX_QUEUE_SIZE);
    private final ExecutorService SEND_DATA_EXECUTOR = Executors.newSingleThreadExecutor();
    private final int MAX_BATCH_SIZE = 100;
    private final int MAX_POSSIBLE_DELAY_IN_SECONDS = 30;
    private final Random ERRORS_EMULATOR = new Random();
    private final int ERROR_VALUE = 5;
    private final int INITIAL_DELAY_IN_SECONDS = 1;
    private final AtomicInteger CURRENT_DELAY = new AtomicInteger(INITIAL_DELAY_IN_SECONDS);
    private final AtomicBoolean STOPPED;

    public void addRawData(Data data) throws InterruptedException {
        MESSAGES_QUEUE.put(data);
    }

    public Sender(AtomicBoolean stopped) {
        this.STOPPED = stopped;
    }

    public void run() {
        SEND_DATA_EXECUTOR.execute(() -> {
            try {
                startSendingData();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public void stop() {
        SEND_DATA_EXECUTOR.shutdown();
    }

    private void startSendingData() throws InterruptedException {
        while (!STOPPED.get()) {
            List<Data> arrayData = new ArrayList<>();
            MESSAGES_QUEUE.drainTo(arrayData, MAX_BATCH_SIZE);
            try {
                sendDataToServerEmulation(arrayData);
                CURRENT_DELAY.set(INITIAL_DELAY_IN_SECONDS);
            } catch (ServerException ex) {
                ex.printStackTrace(); //Some logging should be here
                TimeUnit.SECONDS.sleep(CURRENT_DELAY.get());
                int nextDelay = CURRENT_DELAY.get() * 2; //Some kind of expotentional backoff
                if (nextDelay <= MAX_POSSIBLE_DELAY_IN_SECONDS) CURRENT_DELAY.set(nextDelay);
            }
        }
        MESSAGES_QUEUE.clear();
    }

    /**
     * @param nextDataBatch - batch to be sent on the server
     * @throws ServerException - here may be some http exceptions set
     */
    private void sendDataToServerEmulation(List<Data> nextDataBatch) throws ServerException {
        int nextRandom = ERRORS_EMULATOR.nextInt(6);
        if (nextRandom == ERROR_VALUE) throw new ServerException("Error sending data!", 500);
        System.out.println(String.format("Sent data batch of size %d", nextDataBatch.size()));
    }

}
