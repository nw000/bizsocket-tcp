package MYbizsocket.tcp;

import okio.BufferedSource;
import okio.ByteString;

import java.io.IOException;
import java.util.Map;

/**
 * Created by dxjf on 16/10/20.
 */
public interface PacketFactory {
    Packet buildRequestPacket(int command, ByteString requestBodym, Map<String, String> aattach);


    Packet buildPacket(BufferedSource source) throws IOException;

    boolean supportHearBeat();

    Packet buildHeatBeatPacket();


}
