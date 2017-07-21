package mx.com.redcup.redcup.myDataModels;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by downvec on 7/20/17.
 */

public class MyPosts extends MyEvents {
    public String userID;
    public String eventContent;
    public String eventID;
    public Map<String ,String> likes = new HashMap<>();
    public MyEventComments event_comments;
    public String contentType;

    public MyPosts(){
        //Default empty constructor
    }

    public MyPosts(String content, String userID, String eventID ){

        this.userID = userID;
        this.eventContent = content;
        this.eventID = eventID;

        this.contentType = "Post";

    }

}
