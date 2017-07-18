package mx.com.redcup.redcup.myDataModels;


import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static mx.com.redcup.redcup.myDataModels.AttendanceStatus.ATTENDANCE_CONFIRMED;

public class MyUsers {
    public String userID;
    public String facebookUID;
    public String displayName;
    public String displaySecondName;
    public String email;
    public int level;
    public Double userRating;
    public Map<String, String> userFriends = new HashMap<>();
    public List<String> userPosts; //String list to store pushID of the posts created by user.

    public MyUsers(){
        //Default empty constructor
    }

    public MyUsers(String userID,String firstName, String secondName, String facebookUID){
        this.userID = userID;
        this.displayName = firstName;
        this.displaySecondName = secondName;
        this.facebookUID = facebookUID;
        this.level = 0;
    }

    public MyUsers(String userID,String firstName, String secondName){
        this.userID = userID;
        this.displayName = firstName;
        this.displaySecondName = secondName;
        this.level = 0;
    }


    public String getDisplayName() {return (displayName + " " + displaySecondName);}
    ////
    public String getFirebaseUID(){ return (this.userID);}

    public String getFacebookUID(){return facebookUID;}

    @Exclude
    public Map<String,String> toMap(){
        HashMap<String,String> result = new HashMap<>();

        return result;
    }
}
