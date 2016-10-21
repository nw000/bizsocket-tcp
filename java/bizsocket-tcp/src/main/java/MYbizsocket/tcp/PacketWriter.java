package MYbizsocket.tcp;

import bizsocket.logger.Logger;
import bizsocket.logger.LoggerFactory;
import okio.BufferedSink;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by dxjf on 16/10/20.
 */
public class PacketWriter {

    private final SocketConnection connection;
    private Thread writerThread;

    private BufferedSink writer;

    private final BlockingQueue<Packet> queue = new ArrayBlockingQueue<Packet>(500, true);

    private volatile boolean done = false;

    private final Logger logger = LoggerFactory.getLogger(PacketWriter.class.getSimpleName());


    public PacketWriter(SocketConnection connection) {
        this.connection = connection;
        this.init();

    }

    private void init() {
        this.writer = connection.getWriter();
        done = false;
    }

    public void setWriter(BufferedSink writer) {
        this.writer = writer;
    }

    public void startUp() {
        done = false;
        if (writer != null && writerThread.isAlive()) {
            return;
        }
        logger.debug("writer thread startup");
        writerThread = new Thread() {
            @Override
            public void run() {
                writePackets(this);
            }
        };

    }

    public void shutdown() {
        if (this.done) {
            return;
        }
        this.done = true;

        synchronized (queue) {
            queue.notifyAll();
        }

        if (writerThread != null) {
            writerThread.interrupt();
            writerThread = null;
        }
    }

    private void writePackets(Thread thread) {
        while (!this.done && this.writerThread != null) {
            Packet packet = nextPacket();
            if (packet != null && !this.done && writerThread != null) {
                byte[] bytes = packet.toBytes();
                try {
                    writer.write(bytes);
                    writer.flush();

                    connection.notifySendSuccessful(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                    if (!this.done && writerThread != null) {
                        connection.handleReadWriteError(e);
                    }
                }
            }
        }
    }

    private Packet nextPacket() {
        return null;
    }

    public void sendPacket(Packet packet) {
        if (this.done) {
            return;
        }
        try {
            queue.put(packet);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        synchronized (queue) {
            queue.notifyAll();
        }
    }

    public void clearQueue() {
        if (queue != null) {
            queue.clear();
        }

    }
}
