package org.example;

public class Operation {
    String cOrigin;
    String cDestination;
    int operationType;
    double amount;
    long ttl;

    public Operation(String cOrigin, String cDestination, int operationType, double amount, long ttl) {
        this.cOrigin = cOrigin;
        this.cDestination = cDestination;
        this.operationType = operationType;
        this.amount = amount;
        this.ttl = ttl;
    }

    public Operation() {
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

    public int getOperationType() {
        return operationType;
    }

    public void setOperationType(int operationType) {
        this.operationType = operationType;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public long getTtl() {
        return ttl;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }
}
