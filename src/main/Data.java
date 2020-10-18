package main;

public class Data {

    public final long timestamp;
    public final byte[] rawData;

    public Data(long timestamp, byte[] rawData) {
        this.timestamp = timestamp;
        this.rawData = rawData;
    }
}
