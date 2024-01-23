package cpen221.mp3.client;

public class Request {
    private final double timeStamp;
    private final RequestType requestType;
    private final RequestCommand requestCommand;
    private final String requestData;

    /**
     * Rep Invariance
     * timeStamp is a double that's > 0
     * requestType is not null and it either CONFIG, CONTROL, ANALYSIS, PREDICT
     * requestCommand is not null and must be in RequestCommand enum
     * requestData is not null, and specifies which data to request
     *
     * Abstract Function
     * AF(r) = r is a request in the server
     * timestamp = r.timestamp
     * requestType = r.requestType
     * requestCommand = r.requestCommand
     * requestData = r.requestData
     */


    /**
     * Create Request
     *
     * @param requestType type of request. it is not null and one of the following type: CONFIG, CONTROL, ANALYSIS, PREDICT
     * @param requestCommand command of request. it is not null and type can be found in RequestCommand enum
     * @param requestData data of request. it is not null
     */
    public Request(RequestType requestType, RequestCommand requestCommand, String requestData) {
        this.timeStamp = System.currentTimeMillis();
        this.requestType = requestType;
        this.requestCommand = requestCommand;
        this.requestData = requestData;
    }

    /**
     * Returns timestamp of request
     *
     * @return timeStamp
     */
    public double getTimeStamp() {
        return timeStamp;
    }

    /**
     * Returns request type of the instance
     *
     * @return requestType
     */
    public RequestType getRequestType() {
        return requestType;
    }

    /**
     * Returns request command of the instance
     *
     * @return requestCommand
     */
    public RequestCommand getRequestCommand() {
        return requestCommand;
    }

    /**
     * Returns request data of the instance
     *
     * @return requestData
     */
    public String getRequestData() {
        return requestData;
    }

    /**
     * Concatenate information of the request instance and turn that in to String
     *
     * @return String type of information of the request
     */
    @Override
    public String toString() {
        return "Request{" +
                "requestType=" + requestType +
                ",, requestCommand=" + requestCommand +
                ",, requestData=" + requestData +
                '}';
    }
}