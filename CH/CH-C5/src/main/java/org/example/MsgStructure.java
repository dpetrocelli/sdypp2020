package org.example;

public class MsgStructure {
    String cOrigin;
    String cDestination;
    double amount;
    String operationType;
    Long ttl;

    public MsgStructure() {
    }

    public MsgStructure(String cOrigin, String cDestination, double amount, String operationType, Long ttl) {
        this.cOrigin = cOrigin;
        this.cDestination = cDestination;
        this.amount = amount;
        this.operationType = operationType;
        this.ttl = ttl;
    }

    public String getcOrigin() {
        return cOrigin;
    }

    public void setcOrigin(String cOrigin) {
        this.cOrigin = cOrigin;
    }

    public String getcDestination() {
        return cDestination;
    }

    public void setcDestination(String cDestination) {
        this.cDestination = cDestination;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public Long getTtl() {
        return ttl;
    }

    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }
}