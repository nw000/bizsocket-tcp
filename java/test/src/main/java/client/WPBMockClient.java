package client;

import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import server.Packet;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by tong on 16/10/2.
 */
public class WPBMockClient {
    Socket socket;
    BufferedSource reader;
    BufferedSink writer;
    boolean isReading;

    public static void main(String[] args) throws IOException {
        WPBMockClient client = new WPBMockClient();
        client.connect();
    }

    public void connect() throws IOException {
        socket = new Socket("127.0.0.1", 9103);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        writer = Okio.buffer(Okio.sink(socket.getOutputStream()));
        String json = "{\"productId\" : \"1\",\"isJuan\" : \"0\",\"type\" : \"2\",\"sl\" : \"1\"}";
        writePacket(new Packet(Packet.CMD_CREATE_ORDER, 1, json));


        reader = Okio.buffer(Okio.source(socket.getInputStream()));
        isReading = true;
        while (isReading) {
            try {
//                byte[] buffer = new byte[256];
//                int len = -1;
//                while ((len = reader.read(buffer)) != -1) {
//                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
//                    bos.write(buffer,0,len);
//                    System.out.println(new String(bos.toByteArray()));
//                }
                Packet packet = Packet.build(reader);
                System.out.println("rec: " + packet.toString());

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void writePacket(Packet packet) throws IOException {
        writer.write(packet.toBytes());
        writer.flush();
    }
}
