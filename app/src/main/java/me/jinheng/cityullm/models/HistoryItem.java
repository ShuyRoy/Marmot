package me.jinheng.cityullm.models;

public class HistoryItem {
    private Long id;
    private String send;
    private String receive;

    public HistoryItem() {
    }

    public HistoryItem(Long id, String send, String receive) {
        this.id = id;
        this.send = send;
        this.receive = receive;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSend() {
        return send;
    }

    public void setSend(String send) {
        this.send = send;
    }

    public String getReceive() {
        return receive;
    }

    public void setReceive(String receive) {
        this.receive = receive;
    }
}