# REDcp

Redcupa Android application

Using Firebase as database, with Firebase UI

File Architecture and breakdown: 

<strong> Packages: </strong> 
-MyDataModels
	Java classes for event, users, and enum class to handle attendance status.Data models for events and users are used to retrieve firebase data as native objects. 
-MyHolders
	Contains the two custom holders for the recyclerviews. 
-MyNavigationFragments
	Contains the fragment classes for each fragment used by the bottom navigation bar
-redcup
	Main package. Contains classes for the activies on the rest of the app


<strong> Files: </strong> 
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
	Fragment for the map tab. Only draws markers if the event is set to public. Has several responsibilities: Get map from Google Maps api (OnMapReady), listen for clicks on markers and the map itself, displaying a Fab that triggers the process for creating a new event, and drawing the markers from the firebase database. Later on it will also be responsible for detecting the users current location, and monitoring geofences. 
		
--NearbyFragment.java
	Friends fragment clone. Later it will be responsible for querying firebase, and getting the events closest to the user based on a radius selected by the user on the settings tab. 
		
--UserProfileFragment.java
		Displays the user profile. Composed of a coordinator layout and appbarlayout. Displays the user's profile, which will contain the events created by the user, as well as <i> "posts" </i>  which will be triggered when the user befriends another user, or marks attendance status to a certain event. The post functionality is yet to be implemented. <i>Note:</i> There is a memory leak that is causing the app to skip frames when loaded. Networking is off main thread, so reason unknown
		
--UserSettingsFragment.java
		Shows app settings and configurations the user may want to tweak, like distance to show nearby events. At the moment it only contains the log out button 

=============
-redcupa. Main package
--EventDetailsActivity.java
	Activity that shows all the important info of a certain event. Displays author, picture, and time at the top, followed by the description provided by the author, found in database as <b> eventContent</b>. It also displays a Fam (Floating Action Menu) that allows a user to declare his/her attendance status to that specific event. Attendance status defined in enum <b>AttendanceStatus.java</b>. Additionally, if the picture of the author is clicked, the user profile of the author can be accessed. The activity works by receiving the unique <b>eventID</b> from the Intent that triggered it, and querying firebase for the data for that event.
	
--LoginActivity.java
	Activity in charge of carrying out the login process. <ins>LoginActivity</ins> is only triggered when the main activity (NavActivity) detects there is no current Firebase session active i.e. when the user is not logged in. 
	This activity is the first screen the user sees when opening the app for the first time. It contains the facebook login button and in the future the option to sign in via email. 
	The activity is responisble of: Managing the facebook sdk login callback and getting the access token, creating a facebook profile tracker to obtain basic info e.g user name, Create a firebase auth listener and handing it the facebook access token, and finally redirecting the user to the main activity once it detects the user logged in. 
	
<b>--NavActivity.java (Main Activity) </b> 
	This is the main activity. This activity is the parent activity for all the navigation fragments, and handles all the fragment switching logic. 
	This activity is responsible for: Checking if there is an active firebase session (user is logged in), Drawing the bottom navigation bar, and also switching fragments based on clicks on the icons, logging the user out when logout button clicked via <i>terminateAllSessions</i> method, and finally displaying the Google Place Picker on click of the maps Fab and also receiving the place coordinates selected by the user and finally passing them to NewPostActivity. 
	
--NewPostActivity.java
	This activity lets the user complete the rest of the details of a new event, like name, description, and time. This activity is prompted by NavActivity after the Google Place Picker callback is completed. This activity gets the selected latitude and longitude for the new event through an Intent triggered by Place Picker Callback. Once the data collection is completed the activity creates an Event object, and then pushes it to the firebase database. 
	
--ProfileDetailsActivity.java
	This activity displays a user's profile (not the current user), and shows a Fam that lets the user befriend, follow, or invite the user to an event. The invite user to event functionality is still in development
	
.......


<strong> Firebase Database </strong> 
The database has two parent nodes: Users_parent and Events_parent. Data inside these root nodes is keyed by unique push id in the case of events, and firebase user id in the case of users. The database is essentially a JSON object and can be treated as such. 

Structure: 
-Events_parent
	-{push id}
		-eventID: unique push id, same as parent node. 
		-eventPublic: boolean that indicates if an event is public
		-eventName
		-eventContent
		-eventLatitude: Double that represents latitude in latlng coordinate of event location
		-eventLongitude: Double that represents longitude
		-eventDate
			-year
			-month
			-day
			-hours
			-minutes
		-attendance_list
			-[ userID : ATTENDANCE_STATUS ]
			
-Users_parent
	-{userID}
		-displayName: User's first name
		-displaySecondName: User's second name
		-facebookUID: int representing the user's unique facebook id. Used to obtain data from Facebook
		-level: int that represents a user's level. Function to be implemented.
		
		
		
<strong> Tips for debugging </strong> 
...
		
    
    
    
