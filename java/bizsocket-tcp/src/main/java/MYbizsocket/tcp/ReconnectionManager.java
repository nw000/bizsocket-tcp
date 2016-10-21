package MYbizsocket.tcp;

import bizsocket.logger.Logger;
import bizsocket.logger.LoggerFactory;

/**
 * Created by dxjf on 16/10/20.
 */
public class ReconnectionManager {
    private Logger logger = LoggerFactory.getLogger(ReconnectionManager.class.getSimpleName());

    private int RANDOM_BASE = 5;

    private SocketConnection connection;

    private boolean done = false;

    private boolean needRecnect = false;

    private ReconnectHandler reconnectHandler;

    private ReconnectionManager.ReconnectionThread reconnectionThread;

    public void bind(SocketConnection connection) {
        this.connection = connection;
        this.connection.addConnectionListener(connectionListener);
    }


    public void unbind() {
        if (connection != null) {
            this.connection.removeConnectionListener(connectionListener);
        }
        this.connection = null;

    }

    public boolean isNeedRecnect() {
        return needRecnect;
    }

    public boolean isReconnectionAllowed() {
        return !this.done;
    }

    public void setDone(boolean done) {
        this.done = done;

        if (done) {
            if (connection != null) {
                connection.removeConnectionListener(connectionListener);
                connection = null;
            }
            if (reconnectionThread != null) {
                reconnectionThread.interrupt();
                reconnectionThread = null;
            }
        }
    }

    public ReconnectHandler getReconnectHandler() {
        return reconnectHandler;
    }

    public void setReconnectHandler(ReconnectHandler reconnectHandler) {
        this.reconnectHandler = reconnectHandler;
    }

    public synchronized void reconnect() {
        if (!this.isReconnectionAllowed()) {
            return;
        }
        if (this.reconnectionThread != null && this.reconnectionThread.isAlive()) {
            return;
        }
        this.reconnectionThread = new ReconnectionThread();
        this.reconnectionThread.setName("Reconnection Manager");
        this.reconnectionThread.setDaemon(true);
        this.reconnectionThread.start();

    }


    class ReconnectionThread extends Thread {
        @Override
        public void run() {

        }
    }

    private ConnectionListener connectionListener = new ConnectionListener() {
        @Override
        public void connected(SocketConnection socketConnection) {
            logger.debug("SocketConnection connected");

            done = true;

            needRecnect = false;


        }

        @Override
        public void connectionClosed() {

        }

        @Override
        public void connectionClosedOnError(Exception e) {

        }

        @Override
        public void reconnectingIn(int seconds) {

        }
    };

    public void setReconnectHandler(SocketConnection socketConnection) {

    }


    class ReconnectionThread extends Thread {
        private int attempts = 0;

        ReconnectionThread() {
        }

        ;

        public void resetAttempts() {
            this.attempts = 0;
        }

        private int timeDelay() {
            ++this.attempts;
            return this.attempts > 9 ? ReconnectionManager.this.RANDOM_BASE * 3 :
                    ReconnectionManager.this.RANDOM_BASE;
        }

        @Override
        public void run() {

        }
    }

    public interface ReconnectHandler {
        void doReconnect(SocketConnection connection);
    }

}
