package org.example;

public class MensajeIntercambio {
    String header;
    // /getFile ; /subirTarea -- > ?tarea=algo
    String body;

    public MensajeIntercambio(String header, String body) {
        this.header = header;
        this.body = body;
    }

    public MensajeIntercambio() {

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
