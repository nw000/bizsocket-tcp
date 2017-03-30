package mybizsocket;

/**
 * Created by nw on 17/3/28.
 */
public interface Connection {

    void connection() throws Exception;

    void disConnection();

    boolean isConnected();
}
