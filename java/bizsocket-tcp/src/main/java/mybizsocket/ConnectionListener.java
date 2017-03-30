package mybizsocket;

/**
 * Created by nw on 17/3/28.
 */
public interface ConnectionListener {

    void connected(SocketConnection connection);

    void connectionClosed();

    void connectionClosedOnError(Exception e);

    void reconnectingIn(int seconds);


}
