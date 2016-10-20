package MYbizsocket.tcp;

/**
 * Created by dxjf on 16/10/20.
 */
public interface PacketListener {

    void onSendSuccessful(Packet packet);

    void processPacket(Packet packet);
}
