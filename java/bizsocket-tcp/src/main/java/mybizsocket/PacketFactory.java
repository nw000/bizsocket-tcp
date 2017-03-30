package mybizsocket;

import okio.BufferedSource;
import okio.ByteString;

import java.util.Map;

/**
 * Created by nw on 17/3/29.
 */
public interface PacketFactory {

    Packet buildRequestPacket(int command, ByteString requestBody, Map<String, String> attach);


    Packet buildPacket(BufferedSource source);

    boolean supportHeatPacket();

    Packet buildHeartPacket();

}
