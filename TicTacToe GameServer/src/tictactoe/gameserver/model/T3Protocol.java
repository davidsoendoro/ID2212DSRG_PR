package tictactoe.gameserver.model;

import com.google.gson.JsonObject;

public class T3Protocol {

    private String request;
    private String body;

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
/**
 * Creates JSON object from request and body and converts to string
 * @return String
 */
    @Override
    public String toString() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("Request", request);
        jsonObject.addProperty("Body", body);

        return jsonObject.toString();
    }
}
