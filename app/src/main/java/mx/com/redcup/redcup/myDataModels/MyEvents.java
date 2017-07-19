package mx.com.redcup.redcup.myDataModels;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static mx.com.redcup.redcup.myDataModels.AttendanceStatus.ATTENDANCE_CONFIRMED;


@IgnoreExtraProperties
public class MyEvents {
    public Double eventLatitude;
    public Double eventLongitude;
    public String eventName;
    public String userID;
    public String eventContent;
    public String eventID;
    public Map<String, AttendanceStatus> attendance_list = new HashMap<>();
    public Map<String, InviteStatus> invitee_list = new HashMap<>();
    public Map<String,String> user_posts = new HashMap<>();
    public Map<String ,String> likes = new HashMap<>();
    public MyEventComments event_comments;
    public boolean eventPublic;
    public MyDate eventDate;


    public MyEvents(){
        //Default empty constructor
    }

    public MyEvents(String name,String content, String userID, Double lat, Double lng, boolean eventPublic,
                    String eventID, int year, int month, int day,
                    int hour,int minute ){

        this.userID = userID;
        this.eventName = name;
        this.eventContent = content;
        this.eventLatitude = lat;
        this.eventLongitude = lng;
        this.eventPublic = eventPublic;
        this.eventID = eventID;

        eventDate = new MyDate(year,month,day,hour,minute);

    }

    public String getEventName() {return eventName;}
    ////
    public String getUserID(){return userID;}
    /////
    public Double getEventLatitude() {return eventLatitude;}
    public Double getEventLongitude() {return eventLongitude;}
    /////
    public String getEventContent() {return eventContent;}

    public boolean eventIsPublic(){return eventPublic;}

    public String getEventID(){return eventID;}

    public String getEventHour(){ return ( String.valueOf(eventDate.getHour() % 12)); }
    public String getEventMinutes(){ return String.valueOf(eventDate.getMinutes());}



    //TODO: Create a method that adds UIDs of the users invited. Only if private; Add Timestamp to markers

    @Exclude
    public Map<String,AttendanceStatus> toMap(){
        HashMap<String,AttendanceStatus> result = new HashMap<>();
        result.put(userID,ATTENDANCE_CONFIRMED);
        return result;
    }


}