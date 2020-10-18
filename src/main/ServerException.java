package main;

public class ServerException extends Exception {

    public final int code;

    public ServerException(String message, int code) {
        super(message);
        this.code = code;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + " Status code is " + code;
    }
}
