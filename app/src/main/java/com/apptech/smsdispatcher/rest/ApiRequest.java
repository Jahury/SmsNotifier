package com.apptech.smsdispatcher.rest;

/**
 * Created by Jahury on 5/6/2017.
 */

public class ApiRequest {

    public String Url;
    public int RequestMethod;
    public String JsonBody;
    public String Tag;

    
    public String getUrl () {
        return Url;
    }

    public void setUrl (String url) {
        Url = url;
    }

    public int getRequestMethod () {
        return RequestMethod;
    }

    public void setRequestMethod (int requestMethod) {
        RequestMethod = requestMethod;
    }

    public String getJsonBody () {
        return JsonBody;
    }

    public void setJsonBody (String jsonBody) {
        JsonBody = jsonBody;
    }

    public String getTag () {
        return Tag;
    }

    public void setTag (String tag) {
        Tag = tag;
    }
}
