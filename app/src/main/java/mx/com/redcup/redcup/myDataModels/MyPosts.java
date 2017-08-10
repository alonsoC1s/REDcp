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
    public boolean sponsored;

    public MyPosts(){
        //Default empty constructor
    }

    public MyPosts(String content, String userID, String eventID ){

        this.userID = userID;
        this.eventContent = content;
        this.eventID = eventID;

        this.contentType = "Post";

        eventDate = null;
        eventLatitude = null;
        eventLongitude = null;
        this.sponsored = false;
    }

    @Override
    public String getEventMinutes() {return null;}
    @Override
    public String getEventHour() {return null;}
    @Override
    public Double getEventLatitude() {return null;}
    @Override
    public Double getEventLongitude() {return null;}
    @Override
    public String getEventName() {return null;}
    @Override
    public String getContentType() {return this.contentType;}

    @Override
    public String getEventID() {return this.eventID;}
    @Override
    public String getUserID() {return this.userID;}

    @Override
    public String getEventContent() {
        return this.eventContent;
    }

    @Override
    public boolean isSponsored() {return sponsored;}
}
