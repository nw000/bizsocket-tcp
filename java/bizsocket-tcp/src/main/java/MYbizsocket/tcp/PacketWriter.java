package MYbizsocket.tcp;

import okio.BufferedSink;

/**
 * Created by dxjf on 16/10/20.
 */
public class PacketWriter {
    private BufferedSink writer;

    public PacketWriter(SocketConnection socketConnection) {

    }

    public void setWriter(BufferedSink writer) {
        this.writer = writer;
    }

    public void startUp() {

    }

    public void clearQueue() {

    }

    public void shutdown() {

    }

    public void sendPacket(Packet packet) {

    }
}
