# REDcp

Redcupa Android application

Using Firebase as database, with Firebase UI

File Architecture and breakdown: 

Packages: 
-MyDataModels
  Java classes for event, users, and enum class to handle attendance status.
  Data models for events and users are used to retrieve firebase data as native objects. 
-MyHolders
  Contains the two custom holders for the recyclerviews. 
-MyNavigationFragments
  Contains the fragment classes for each fragment used by the bottom navigation bar
-redcup
  Main package. Contains classes for the activies on the rest of the app


Files: 
-MyDataModels
--AttendanceStatus.java
    Enum class that handles the attendance status the user might choose for a certain event. There are 3 options: ATTENDANCE_CONFIRMED, ATTENDANCE_DECLINED, ATTENDANCE_UNCERTAIN. The enum was created to avoid the use of strings when calling the function to update the attendance_list of an event
    
--MyDate.java
    Custom class to handle the eventDate of an event. Created to avoid the native Java Data class and guarantee compatibility with Swift
    
--MyUsers.java
    Class for all the user objects. Users have the following properties: userID, facebookUID, displayName, displaySecondName, email, level, userFriends. userFriends is a dictionary [UID: RelationDetails]. The class includes getters and setters for all the properties that need to be accessed from the objects, like name and UID. 

--RelationDetails.java
    Simple enum, similar to AttendanceStatus in form and function. Specifies the relationship between two users. The relationship can either be USER_FRIEND, or USER_FOLLOWER
    
========
-MyHolders
--EventsRecyclerHolder.java
    Subclass of RecyclerView.ViewHolder. Viewholder that manages the source data for the post lists displayed around the app. The actual recyclerviews are managed by FirebaseUI
    
--UserProfileEventsHolder.java
    Clone of EventsRecyclerHolder without the onClick functionality for the cards
    
=========
-MyNavigationFragments
--FriendsFragments.java
    Class for the Friends tab. Only contains one reyclerview that displays all events on the database. At the moment of beta testing there is no function that filters only the posts created by the user's friends 
    
    
    
    
