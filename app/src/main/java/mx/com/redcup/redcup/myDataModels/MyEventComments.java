package mx.com.redcup.redcup.myDataModels;

import java.util.HashMap;

/**
 * Created by downvec on 7/16/17.
 */

public class MyEventComments {
    String commentContent;
    String authorUID;


    public MyEventComments(){
        //default empty constructor
    }

    public MyEventComments(String eventContent,String authorUID){
        this.commentContent = eventContent;
        this.authorUID = authorUID;
    }

    public String getCommentContent() {return commentContent;}

    public String getAuthorUID() {return authorUID;}



}
