package org.example;

public class ExchangeMsg {
    String header;
    String body;

    public ExchangeMsg(String header, String body) {
        this.header = header;
        this.body = body;
    }

    public ExchangeMsg() {
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
