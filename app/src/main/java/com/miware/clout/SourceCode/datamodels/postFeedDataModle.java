package com.miware.clout.SourceCode.datamodels;

public class postFeedDataModle {

    private static String postString;

    public postFeedDataModle(){
    }

    private postFeedDataModle(String stringPost){
        postString = stringPost;
    }

    public static String getPostString(){return postString;}
    public void setPostString(String postedString){this.postString = postedString;}
}
