package MYbizsocket.tcp;

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
 * Created by dxjf on 16/10/19.
 */
public abstract class SocketConnection implements Connection {

    public static int DEFAULT_HEART_BEAT_INTERVAL = 3000;

    protected final Collection<ConnectionListener> connectionListeners = new CopyOnWriteArrayList<ConnectionListener>();

    protected final Collection<PacketListener> packetListeners = new CopyOnWriteArrayList<>();

    protected final PacketFactory packetFactory;

    protected final Logger logger = LoggerFactory.getLogger(SocketConnection.class.getSimpleName());


    private Socket socket;

    private String host;

    private int port;

    private BufferedSource reader;

    private BufferedSink writer;

    private PacketWriter packetWriter;

    private PacketReader packetReader;

    private Timer timer;

    private int heartbeat = DEFAULT_HEART_BEAT_INTERVAL; //心跳间隔

    private ReconnectionManager reconnectionManager;


    public SocketConnection() {
        this(null, 0);
    }


    public SocketConnection(String host, int port) {
        this.host = host;
        this.port = port;

        packetFactory = createPacketFactory();

    }

    @Override
    public void connect() throws Exception {
        disconnect();
        socket = createSocket(host, port);

        initConnection();

        onSocketConnected();

        callConnectionListenerConnected();
    }

    @Override
    public void disconnect() {
        if (socket != null) {
            try {
                if (packetReader != null) {
                    packetReader.shutdown();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (packetWriter != null) {
                    packetWriter.shutdown();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            stopHeartBeat();
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                socket.shutdownInput();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                socket.shutdownOutput();
            } catch (IOException e) {
                e.printStackTrace();
            }
            socket = null;

            for (ConnectionListener connectionListener : connectionListeners) {
                connectionListener.connectionClosed();
            }

        }

    }

    @Override
    public boolean isConnected() {
        return !isSocketClosed();
    }

    protected abstract PacketFactory createPacketFactory();

    public PacketFactory getPacketFactory() {
        return packetFactory;
    }

    public boolean isSocketClosed() {
        return socket == null || socket.isClosed() || !socket.isConnected();
    }

    public BufferedSource getReader() {
        return reader;
    }

    public BufferedSink getWriter() {
        return writer;
    }

    public void addConnectionListener(ConnectionListener connectionListener) {
        if (connectionListeners.contains(connectionListener)) {
            return;
        }
        this.connectionListeners.add(connectionListener);
    }

    public void removeConnectionListener(ConnectionListener connectionListener) {
        this.connectionListeners.remove(connectionListener);
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

    public void setHostAddress(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void setHeartbeat(int heartbeat) {
        this.heartbeat = heartbeat;
    }

    public void reconnect() {
        try {
            connect();
        } catch (Exception e) {
            e.printStackTrace();
            notifyConnectException(e);
        }
    }

    public void triggerReconnect() {
        bindReconnectionManager();

        notifyConnectException(new SocketException("触发重连"));
    }

    public void bindReconnectionManager() {
        if (reconnectionManager != null) {
            return;
        }

        reconnectionManager = new ReconnectionManager();
        reconnectionManager.bind(this);
        reconnectionManager.setReconnectHandler(this);
    }

    public void unbindReconnectionManager() {
        if (reconnectionManager != null) {
            reconnectionManager.unbind();
        }
        reconnectionManager = null;
    }

    protected Socket createSocket(String host, int port) throws Exception {
        Socket socket = new Socket(host, port);
        socket.setKeepAlive(true);
        socket.setTcpNoDelay(true);
        return socket;
    }

    private void notifyConnectException(Exception exception) {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.connectionClosedOnError(exception);
        }
    }

    private void initReaderAndWriter() {
        try {
            reader = Okio.buffer(Okio.source(socket.getInputStream()));
            writer = Okio.buffer(Okio.sink(socket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initConnection() {
        if (isSocketClosed()) {
            return;
        }
        initReaderAndWriter();
        if (packetReader == null || packetWriter == null) {
            this.packetReader = new PacketReader(this);
            this.packetWriter = new PacketWriter(this);
        }
        this.packetReader.setReader(reader);
        this.packetWriter.setWriter(writer);
        this.packetReader.startUp();
        this.packetWriter.startUp();
    }

    public void sendPacket(Packet packet) {
        if (isSocketClosed()) {
            packetWriter.sendPacket(packet);
        }
    }

    public void startHeartBeat() {
        stopHeartBeat();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                sendPacket(packetFactory.buildHeatBeatPacket());
            }
        };
        timer.scheduleAtFixedRate(timerTask, 0, heartbeat);
    }

    private void stopHeartBeat() {
        if (timer != null) {
            timer.cancel();
        }

    }

    public void handleReadWriteError(Exception e) {
        if ((e instanceof SocketException) || (e instanceof EOFException)) {
            notifyConnectionError(e);
        }
    }

    private void notifyConnectionError(Exception e) {
        stopHeartBeat();
        packetReader.shutdown();
        packetWriter.shutdown();

        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.connectionClosedOnError(e);
        }
    }


    private void callConnectionListenerConnected() {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.connected(this);
        }
    }


    private void onSocketConnected() {

    }

    /**
     * call this method when packet send successful
     *
     * @param packet
     */
    void notifySendSuccessful(Packet packet) {
        for (PacketListener packetListener : packetListeners) {
            packetListener.onSendSuccessful(packet);
        }
    }

    void handlerReceivedPacket(Packet packet) {
        for (PacketListener packetListener : packetListeners) {
            packetListener.processPacket(packet);
        }
    }


    public void clearWriteQuete() {
        if (packetWriter != null) {
            packetWriter.clearQueue();
        }
    }

    public boolean connectAndStartWatch() {
        try {
            bindReconnectionManager();
            connect();
        } catch (Exception e) {
            notifyConnectException(e);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void doReconnect(SocketConnection connection) {
        reconnect();
    }

}







































































































