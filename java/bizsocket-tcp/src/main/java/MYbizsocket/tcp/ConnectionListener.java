package MYbizsocket.tcp;

/**
 * Created by dxjf on 16/10/19.
 */
public interface ConnectionListener {

    void connected(SocketConnection socketConnection);

    void connectionClosed();

    void connectionClosedOnError(Exception e);

    void reconnectingIn(int seconds);
}
