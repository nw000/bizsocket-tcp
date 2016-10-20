package MYbizsocket.tcp;

import bizsocket.logger.Logger;
import bizsocket.logger.LoggerFactory;
import okio.BufferedSource;

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
        //this.init();
    }

    public void setReader(BufferedSource reader) {
        this.reader = reader;
    }

    public void startUp() {

    }

    public void shutdown() {

    }

    //private void init() {
    //    this.reader = connection.
    //}
    //
    //protected void


}
