package org.example;

public class MsgStructure {
    String cOrigin;
    String cDestination;
    double amount;
    Long ttl;

    public MsgStructure() {
    }

    public MsgStructure(String cOrigin, String cDestination, double amount, Long ttl) {
        this.cOrigin = cOrigin;
        this.cDestination = cDestination;
        this.amount = amount;
        this.ttl = ttl;
    }

    public String getCOrigin() {
        return this.cOrigin;
    }

    public void setCOrigin(String cOrigin) {
        this.cOrigin = cOrigin;
    }

    public String getCDestination() {
        return this.cDestination;
    }

    public void setCDestination(String cDestination) {
        this.cDestination = cDestination;
    }

    public double getAmount() {
        return this.amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Long getTtl() {
        return this.ttl;
    }

    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }

    public MsgStructure cOrigin(String cOrigin) {
        this.cOrigin = cOrigin;
        return this;
    }

    public MsgStructure cDestination(String cDestination) {
        this.cDestination = cDestination;
        return this;
    }

    public MsgStructure amount(double amount) {
        this.amount = amount;
        return this;
    }

    public MsgStructure ttl(Long ttl) {
        this.ttl = ttl;
        return this;
    }

  

    @Override
    public String toString() {
        return "{" +
            " cOrigin='" + getCOrigin() + "'" +
            ", cDestination='" + getCDestination() + "'" +
            ", amount='" + getAmount() + "'" +
            ", ttl='" + getTtl() + "'" +
            "}";
    }

}