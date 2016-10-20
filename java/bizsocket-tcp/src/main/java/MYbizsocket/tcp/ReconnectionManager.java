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

    public void bind(SocketConnection connection) {
        this.connection = connection;
        //this.connection
    }

    public void setReconnectHandler(SocketConnection socketConnection) {

    }

    public void unbind() {

    }


    class ReconnectionThread extends Thread {
        private int attempts = 0;

        ReconnectionThread() {};

        public void resetAttempts() {
            this.attempts = 0;
        }

        private int timeDelay() {
            ++ this.attempts;
            return this.attempts > 9 ? ReconnectionManager.this.RANDOM_BASE * 3:
                    ReconnectionManager.this.RANDOM_BASE;
        }

        @Override
        public void run() {

        }
    }

}
