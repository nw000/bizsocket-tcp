package MYbizsocket.tcp;

/**
 * Created by dxjf on 16/10/19.
 */
public interface Connection {
    void connect() throws Exception;

    void disconnect();

    boolean isConnected();
}
