package mx.com.redcup.redcup.myDataModels;

import java.util.HashMap;

/**
 * Created by downvec on 7/16/17.
 */

public class MyEventComments {
    String commentContent;
    String authorUID;
    String parentEventID;


    public MyEventComments(){
        //default empty constructor
    }

    public MyEventComments(String eventContent,String authorUID, String parentEventID){
        this.commentContent = eventContent;
        this.authorUID = authorUID;
        this.parentEventID = parentEventID;
    }

    public String getCommentContent() {return this.commentContent;}

    public String getAuthorUID() {return this.authorUID;}

    public String getParentEventID() {return parentEventID;}


}
