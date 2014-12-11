package com.android.project.model;

import org.json.JSONException;
import org.json.JSONObject;

public class T3Protocol {

	private String request;
    private JSONObject body;

	public String getRequest() {
		return request;
	}

	public void setRequest(String request) {
		this.request = request;
	}

    public String getBody() {
        return body.toString();
    }

    public void setBody(JSONObject body) {
        this.body = body;
    }

	@Override
	public String toString() {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("Request", request);
			jsonObject.put("Body", body);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return jsonObject.toString();
	}
}
