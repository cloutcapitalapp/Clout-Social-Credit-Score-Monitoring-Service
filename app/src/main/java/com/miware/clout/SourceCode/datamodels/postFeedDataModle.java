package com.miware.clout.SourceCode.datamodels;

import java.io.Serializable;

public class postFeedDataModle implements Serializable {

    public postFeedDataModle(){
    }

    public String postString;

    public String userKey;

    public postFeedDataModle(String postString){
        this.postString = postString;
    }

    public String getPostString(){return postString;}
    public void setPostString(String postedString){
        postString = postedString;}

    public String getUserKey() {
        return userKey;
    }
    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }
}
