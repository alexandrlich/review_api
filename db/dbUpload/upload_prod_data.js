
var superUserId = "111111112222222233333333";
var date = new Date();
var time = date.getTime();


//load 10 000 profiles
//load("generatedProfiles.js")



//=================1.generate super Users
var USERS_COUNT=100; //should be more than popular index (40currently)
//var PROFILES_COUNT=100;


var occupationList = [
"Actor",
"Actress",
"Teacher",
"Manager",
"Professor",
"Administrator",
"Singer",
"Doctor",
"Sport player",
"Musician",
"Writer"
];


var skillsList = [
"Accurate",
"Ambitious",
"Artistic",
"Charismatic",
"Collaborative",
"Communicative",
"Creative",
"Confident",
"Consistent",
"Direct",
"Entertaining",
"Energetic",
"Flexible",
"Innovative",
"Leadership",
"Listening",
"Modest",
"Politeness",
"Productive",
"Reliable",
"Resourceful",
"Responsible",
"Sincere",
"Thorough"
];

//super user to generate profiles
//only run once

db.users.insert(
        {"_id" : ObjectId(superUserId),
        "created" : time,
        "first_name" : "fbTest1First",
        "last_name" : "fbTest1Last",
        "email" : "reviyoutest10@gmail.com",//put your fb email here!
        "is_fake_user" :true, //additional field just for our internal purposes
        "login_provider" : "facebook",
        "token" : "token",
        "gender": "male",
        //image should be in our db or on our web server
        //a.png would work since it's less than 6 characters and nginx will return default image this case
        //"user_profile_image" : "a.png",
        "google_login_data" :
            {
            "expiresIn" : "1000000000",
            "idToken" : "someuserid",
            "refreshToken": "sometoken",
            "tokenType" :"type",
            "googleUserId": "gggguserId",
            "image_url": "http://www.emagazin.info/img/articles/arenda-admina/super-admin.jpg",
            "is_default_image" :false
            }
        }
    );

for (var i = 1; i <= USERS_COUNT; i++) {
    db.users.insert(
        { "created" : time,
        "first_name" : "admin_FirstName"+i,
        "last_name" : "admin_LastName"+i,
        "email" : "admin" + i + "@reviyou.com",
        "is_fake_user" :true, //additional field just for our internal purposes
        "login_provider" : "facebook",
        "token" : "token",
        "gender": "male",
        "fb_login_data" :
            {
            "expiresIn" : "1",//small number so that user does expire
            "fbUserId" : "fbUserIdTestUtils"+i,
            "image_url": "https://api.reviyou.com/api/1.0/image/default.pic",
            "is_default_image" :false
            }
        }
    )

}
//userId's
var userIds = [];
var userIdCursor = db.users.find({},{"is_fake_user": true})//projection by _id
while(userIdCursor.hasNext() && userIds.length<=100) {
	var userId = userIdCursor.next();
	 userIds.push(userId._id.str);
}



//=================2.add skills and professions

//var str ="AaBbCcDdEeFfGgHhIiGgKkLlMmNnOoPpRrSsTtUuVvWwXxYyZz";
for (var i = 0; i < skillsList.length; i++) {
    db.skills.insert({"skill_name" : skillsList[i] })
}
for(var i=0;i<occupationList.length;i++) {
    db.professions.insert({"profession_name" : occupationList[i] })	
}









//load visitors for each profile and 1 vote for general
 var profileIdCursor3 = db.profiles.find({"user_id": superUserId},{"popular_index": 1})//TODO:also add a check to run only profiles created today

	while(profileIdCursor3.hasNext()) {
		var profileId3 = profileIdCursor3.next();
			var popular = profileId3.popular_index;
			//print('popular' + popular);	
			//for(var j=0;j<popular;j++) {
				
				db.profileVisitors.insert({"_id": profileId3._id, "user_ids" : userIds.slice(0, popular)});
	 
			
			//}
			//user2 General
			
			db.votes.insert({
						"created" : time,
					  	"profile_id": profileId3._id.str, 
					  	"user_id" : superUserId,
					  	"vote_time" : time,
					  	"vote_value" : (popular/10)
					  	});	
  	

	}

 //-1000*60*60*24*i;//minus day
var uniqueDate = new Date(time);

db.news.insert({
				  	"title": "This is a first official  release 1.0.0!",
				  	"content": "Dear Users! We are proud to present you this application, please give it a try and let us know what you think and what you want us to add to it. You can start with an intro section to learn more about how to use this app. Hope you'll enjoy it and find it useful. We intend to continuosly work on improving it and adding more features. Love Reviyou? Drop us a rating in the App Store! Truly yours, Reviyou Team.",
				  	"create_time": uniqueDate
				  	})
				  	
print('end of upload script');		  




