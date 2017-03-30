package mybizsocket;

/**
 * Created by nw on 17/3/28.
 */
public interface PacketListener {

    void onSendSuccessful(Packet packet);

    void processPacket(Packet packet);

}
