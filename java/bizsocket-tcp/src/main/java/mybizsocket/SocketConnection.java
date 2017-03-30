package mybizsocket;

import bizsocket.logger.Logger;
import bizsocket.logger.LoggerFactory;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by nw on 17/3/23.
 */
public abstract class SocketConnection implements Connection {

    public static final int DEFAULT_HEART_BEAT_INTERVAL = 30000;

    private static final Logger LOGGER = LoggerFactory.getLogger(SocketConnection.class.getSimpleName());
    private final Collection<ConnectionListener> connectionListeners = new CopyOnWriteArrayList<>();
    private final Collection<PacketListener> packetListeners = new CopyOnWriteArrayList<>();
    private final PacketFactory packetFactory;


    private Socket socket;
    private String host;
    private int port;
    private BufferedSource reader;
    private BufferedSink writer;

    private PacketWriter packetWriter;
    private PacketReader packetReader;
    private Timer timer;
    private int heartBeat = DEFAULT_HEART_BEAT_INTERVAL;
    private ReconnectionManager reconnectionManager;


    public SocketConnection() {
        this(null, 0);
    }


    public SocketConnection(String host, int port) {
        this.host = host;
        this.port = port;
        packetFactory = createPacketFactory();
    }

    public boolean connectAndStartWatch() {
        LOGGER.debug("connectAndStartWatch host : " + host + "port :" + port);
        try {
            connection();
        } catch (Exception e) {
            e.printStackTrace();
            notifyConnectException(e);
            return false;
        }
        return true;
    }

    private void notifyConnectException(Exception e) {
        packetWriter.shutDown();
        packetReader.shutDown();
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.connectionClosedOnError(e);
        }
    }

    public void startHeartBeat() {
        stopHeartBeat();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                sendPacket(packetFactory.buildHeartPacket());
            }
        };
        timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, 0, heartBeat);
    }

    private void stopHeartBeat() {
        if (timer != null) {
            timer.cancel();
        }
    }

    public void setHeartBeat(int heartBeat) {
        this.heartBeat = heartBeat;
    }

    @Override
    public void connection() throws Exception {
        disConnection();
        LOGGER.debug("connect host :" + host + " port : " + port);
        socket = createSocket();

        initConnection();

        onSocketConnected();
        notifySocketConnected();
    }

    @Override
    public boolean isConnected() {
        return !isSocketClosed();
    }

    public PacketFactory getPacketFactory() {
        return packetFactory;
    }

    private void notifySocketConnected() {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.connected(this);
        }
    }

    public void onSocketConnected() {

    }

    private void initConnection() {
        if (isSocketClosed()) {
            return;
        }

        boolean isFirstInitialIzation = packetReader == null || packetWriter == null;
        initReaderAndWriter();
        if (isFirstInitialIzation) {
            packetWriter = new PacketWriter(this);
            packetReader = new PacketReader(this);
        }
        this.packetReader.startUp();
        this.packetWriter.startUp();

    }

    private void initReaderAndWriter() {
        try {
            reader = Okio.buffer(Okio.source(socket.getInputStream()));
            writer = Okio.buffer(Okio.sink(socket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isSocketClosed() {
        return socket == null || socket.isClosed() || !socket.isConnected();
    }

    private Socket createSocket() throws Exception {
        Socket socket = null;
        if (socket == null || socket.isClosed()) {
            socket = new Socket(host, port);
            socket.setKeepAlive(true);
            socket.setTcpNoDelay(true);
        }
        return socket;
    }

    @Override
    public void disConnection() {
        if (socket != null) {
            LOGGER.debug("disconnect");
            try {
                if (packetReader != null) {
                    packetReader.shutDown();
                }
            } catch (Throwable e) {

            }
            try {
                if (packetWriter != null) {
                    packetWriter.shutDown();
                }
            } catch (Throwable e) {

            }

            try {
                socket.close();
            } catch (Exception e) {

            }
            try {
                socket.shutdownInput();
            } catch (Exception e) {

            }
            socket = null;
            notifyConnectionClosed();

        }
    }

    private void notifyConnectionClosed() {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.connectionClosed();
        }
    }

    protected abstract PacketFactory createPacketFactory();


    public void addConnectionListener(ConnectionListener connectionListener) {
        if (connectionListeners.contains(connectionListener)) {
            return;
        }
        connectionListeners.add(connectionListener);
    }


    public void addPacketListener(PacketListener packetListener) {
        if (packetListeners.contains(packetListener)) {
            return;
        }
        packetListeners.add(packetListener);
    }


    public void removePacketListener(PacketListener packetListener) {
        packetListeners.remove(packetListener);
    }

    void handlerReceivedPacket(Packet packet) {
        for (PacketListener packetListener : packetListeners) {
            packetListener.processPacket(packet);
        }
    }

    void notifySendSuccessful(Packet packet) {
        for (PacketListener packetListener : packetListeners) {
            packetListener.onSendSuccessful(packet);
        }
    }


    public void removeConnectionListener(ConnectionListener connectionListener) {
        connectionListeners.remove(connectionListener);
    }


    public BufferedSource getInputStream() {
        return reader;
    }

    public BufferedSink getOutputStream() {
        return writer;
    }

    public void handleReadWriterError(Exception e) {
        if ((e instanceof SocketException) || (e instanceof EOFException)) {
            notifyConnectException(e);
        }
    }

    public void sendPacket(Packet packet) {
        if (isSocketClosed()) {
            return;
        }
        packetWriter.sendPacket(packet);
    }

    public void setHostAddress(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void clearWriteQuete() {
        if (packetWriter != null) {
            packetWriter.clearQueue();
        }
    }


}
