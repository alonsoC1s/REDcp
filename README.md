# REDcp

Redcupa Android application

Using Firebase as database, with Firebase UI

File Architecture and breakdown: 

<strong> Packages: </strong> 
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
    Class for the Friends tab. Only contains one reyclerview that displays all events on the database. At the moment of beta testing there is no function that filters only the posts created by the user's friends. Clicking the cards displayed by the recyclerview opens EventDetailsActivity and passes the eventID to it so more detailed info about the event can be accessed. 

--MapsFragment.java
		Fragment for the map tab. Has several responsibilities: Get map from Google Maps api (OnMapReady), listen for clicks on markers and the map itself, displaying a Fab that triggers the process for creating a new event, and drawing the markers from the firebase database. Later on it will also be responsible for detecting the users current location, and monitoring geofences. 
		
--NearbyFragment.java
		Friends fragment clone. Later it will be responsible for querying firebase, and getting the events closest to the user based on a radius selected by the user on the settings tab. 
		
--UserProfileFragment.java
		Displays the user profile. Composed of a coordinator layout and appbarlayout. Displays the user's profile, which will contain the events created by the user, as well as "posts" which will be triggered when the user befriends another user, or marks attendance status to a certain event. The post functionality is yet to be implemented
		
--UserSettingsFragment.java
		Shows app settings and configurations the user may want to tweak, like distance to show nearby events. At the moment it only contains the log out button 
		

    
    
    
