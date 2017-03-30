package mybizsocket;

import bizsocket.logger.Logger;
import bizsocket.logger.LoggerFactory;
import okio.BufferedSource;

/**
 * Created by nw on 17/3/23.
 */
public class PacketReader {

    private BufferedSource buffer;
    private volatile boolean done = false;
    private static final Logger LOGGER = LoggerFactory.getLogger(PacketReader.class.getSimpleName());
    private final SocketConnection connection;
    private Thread readThread;


    public PacketReader(SocketConnection connection) {
        this.connection = connection;
        buffer = connection.getInputStream();
        this.done = false;
    }

    public synchronized void startUp() {
        if (!done && readThread != null) {
            return;
        }
        done = false;
        if (readThread == null && readThread.isAlive()) {
            return;
        }
        readThread = new Thread(){
            @Override
            public void run() {
                PacketReader.this.parsePackets(this);
            }
        };
        readThread.setName("packetReader");
        LOGGER.debug("readThread start");
        readThread.setDaemon(true);
        readThread.start();
    }

    private void parsePackets(Thread thread) {
        try {
            while (!done && thread == readThread && !readThread.isInterrupted()) {
                Packet packet = WPBPacket.build(buffer);
                if (packet != null && !done && thread == readThread) {

                }
            }
        } catch (Exception e) {

        }

    }


    public void shutDown() {
        if (done) {
            return;
        }
        LOGGER.debug("readthread shutdown");
        this.done = true;
        if (readThread == null) {
            readThread.interrupt();
            readThread = null;
        }
    }

    public void setBuffer(BufferedSource buffer) {
        this.buffer = buffer;
    }
}
