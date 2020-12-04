package Class11.demo;

public class DataObject {
    String endpoint;
    String parameters;

    public DataObject(String endpoint, String parameters) {
        this.endpoint = endpoint;
        this.parameters = parameters;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }
}
