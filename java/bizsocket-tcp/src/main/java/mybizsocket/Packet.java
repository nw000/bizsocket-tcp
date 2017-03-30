package mybizsocket;

/**
 * Created by nw on 17/3/22.
 */
public abstract class Packet {



    public int cmd;

    public int sqe;

    public String content;

    public String describe;


    public int getCmd() {
        return cmd;
    }

    public void setCmd(int cmd) {
        this.cmd = cmd;
    }

    public abstract int getSqe();

    public void setSqe(int sqe) {
        this.sqe = sqe;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public abstract byte[] toBytes();



}
