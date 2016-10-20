package MYbizsocket.tcp;

import bizsocket.logger.Logger;
import bizsocket.logger.LoggerFactory;

/**
 * Created by dxjf on 16/10/19.
 */
public abstract class Packet {
    private static final Logger LOGGER = LoggerFactory.getLogger(Packet.class.getSimpleName());

    private long longPacketId;

    private String description;

    public long getLongPacketId() {
        return longPacketId;
    }

    public void setLongPacketId(long longPacketId) {
        this.longPacketId = longPacketId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public abstract byte[] toBytes();

    public abstract String getContent();

    public abstract String getPacketID();

    public abstract int getCommand();

    public abstract void setPacketId(String packetID);

    public abstract void setCommand(int command);

    public String nextPacketID() {
        if (longPacketId == Long.MAX_VALUE) {
            longPacketId = Long.MAX_VALUE;
        }

        return String.valueOf(Long.valueOf(longPacketId));
    }

    public void onSendSuccessful() {
        LOGGER.debug("-------------------send packet: " + getCommand() + " ,desc: " + getDescription() + ", id: " + getPacketID());
        LOGGER.debug("-------------------send content: " + getContent());
    }

    public void onReceiveFromServer() {

    }

    public void onDispatch() {
        LOGGER.debug("-------------------send packet: " + getCommand() + " ,desc: " + getDescription() + ", id: " + getPacketID());
        LOGGER.debug("-------------------send content: " + getContent());
    }


}



































