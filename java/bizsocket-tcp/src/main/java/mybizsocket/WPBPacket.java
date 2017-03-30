package mybizsocket;

import okio.BufferedSink;
import okio.BufferedSource;
import okio.ByteString;
import okio.Okio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Created by nw on 17/3/22.
 */
public class WPBPacket extends Packet {

    public int length;

    private WPBPacket(Builder builder) {
        setCmd(builder.cmd);
        setSqe(builder.sqe);
        setContent(builder.content);
        setDescribe(builder.describe);
    }

    public WPBPacket() {

    }

    @Override
    public int getSqe() {
        sqe++;
        if (sqe == Integer.MAX_VALUE) {
            sqe = 0;
        }
        return sqe;
    }

    public WPBPacket(int cmd, String content) {
        this.cmd = cmd;
        this.content = content;
        this.sqe = getSqe();
    }

    public WPBPacket(int cmd, int sqe, String content) {
        this.cmd = cmd;
        this.sqe = sqe;
        this.content = content;
    }

    @Override
    public byte[] toBytes() {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        BufferedSink buffer = Okio.buffer(Okio.sink(byteStream));
        try {
            ByteString byteString = ByteString.encodeUtf8(content);
            buffer.writeInt(byteString.size() + 12);
            buffer.writeInt(cmd);
            buffer.writeInt(getSqe());
            buffer.write(byteString);
            buffer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return byteStream.toByteArray();
    }


    public static final class Builder {
        private int cmd;
        private int sqe;
        private String content;
        private String describe;

        public Builder() {
        }

        public Builder cmd(int val) {
            cmd = val;
            return this;
        }

        public Builder sqe(int val) {
            sqe = val;
            return this;
        }

        public Builder content(String val) {
            content = val;
            return this;
        }

        public Builder describe(String val) {
            describe = val;
            return this;
        }

        public WPBPacket build() {
            return new WPBPacket(this);
        }
    }

    public static WPBPacket build(BufferedSource source) throws Exception {
        WPBPacket wpbPacket = new WPBPacket();
        if (source != null) {
            int length = source.readInt();
            wpbPacket.cmd = source.readInt();
            wpbPacket.sqe = source.readInt();
            wpbPacket.content = source.readString(length - 12, Charset.forName("utf-8"));
        }
        return wpbPacket;
    }
}
