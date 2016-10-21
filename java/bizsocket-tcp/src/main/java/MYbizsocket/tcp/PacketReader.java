package MYbizsocket.tcp;

import bizsocket.logger.Logger;
import bizsocket.logger.LoggerFactory;
import okio.BufferedSource;

import java.io.IOException;

/**
 * Created by dxjf on 16/10/20.
 */
public class PacketReader {

    private Thread readerThread;

    private final SocketConnection connection;

    private BufferedSource reader;

    private volatile boolean done = false;

    private final Logger logger = LoggerFactory.getLogger(PacketReader.class.getSimpleName());


    public PacketReader(SocketConnection connection) {
        this.connection = connection;
        this.init();
    }

    private void init() {
        this.reader = connection.getReader();
    }

    public synchronized void startUp() {
        if (!this.done && readerThread != null) {
            return;
        }
        done = false;
        if (readerThread != null && readerThread.isAlive()) {
            return;
        }
        logger.debug("reader thread startup");
        readerThread = new Thread() {
            @Override
            public void run() {
                parsePackets(this);
            }
        };

        readerThread.setName("packet Reader");
        readerThread.setDaemon(true);
        readerThread.start();

    }

    public synchronized void shutdown() {
        if (this.done) {
            return;
        }
        this.done = true;

        logger.debug("reader thread shutdown");
        if (readerThread != null) {
            readerThread.interrupt();
            readerThread = null;
        }
    }


    public void setReader(BufferedSource reader) {
        this.reader = reader;
    }

    private void parsePackets(Thread thread) {
        while ((!this.done && this.readerThread == thread)) {
            Packet packet = null;
            try {
                packet = connection.getPacketFactory().buildPacket(reader);
                if (packet != null && !this.done && this.readerThread == reader) {
                    connection.handlerReceivedPacket(packet);
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (!done && this.readerThread == thread) {
                    connection.handleReadWriteError(e);
                }
            }
        }

    }

}















































