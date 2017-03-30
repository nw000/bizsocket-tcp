package mybizsocket;

import bizsocket.logger.Logger;
import bizsocket.logger.LoggerFactory;
import okio.BufferedSink;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by nw on 17/3/23.
 */
public class PacketWriter {

    private final SocketConnection socketConnection;
    private Thread writerThread;
    private final BufferedSink buffer;
    private ArrayBlockingQueue<Packet> queue = new ArrayBlockingQueue<Packet>(500, true);
    private final Logger logger = LoggerFactory.getLogger(PacketWriter.class.getSimpleName());
    private volatile boolean done = false;


    public PacketWriter(SocketConnection socketConnection) {
        this.socketConnection = socketConnection;
        buffer = socketConnection.getOutputStream();
        done = false;
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

    }

    public synchronized void startUp() {
        done = false;
        if (writerThread != null && writerThread.isAlive()) {
            return;
        }
        writerThread = new Thread() {
            @Override
            public void run() {
                consumePacket(this);
            }
        };
        writerThread.setName("writerThread");
        writerThread.setDaemon(true);
        writerThread.start();
    }

    public void shutDown() {
        if (this.done) {
            return;
        }
        logger.debug("writer thread shutdown");
        this.done = true;
        if (writerThread != null) {
            writerThread.interrupt();
            writerThread = null;
        }
    }


    public Packet nextPacket() {
        try {
            Packet take = queue.take();
            return take;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void consumePacket(Thread thread) {
        try {
            while (!thread.isInterrupted() && thread == writerThread) {
                try {
                    Packet packet = nextPacket();
                    if (packet != null && thread == writerThread) {
                        buffer.write(packet.toBytes());
                        buffer.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {

        }

    }

    public void clearQueue() {
        if (queue != null) {
            queue.clear();
        }
    }


}
