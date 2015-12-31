//first - drop all existing collections;
db.profileImage.chunks.drop();
db.profileImage.files.drop();
db.logging_history.drop();
db.feedbacks.drop();
db.professions.drop();
db.votes.drop();
db.bookmarks.drop();
db.news.drop();
db.tags.drop();
db.comments.drop();
db.profileVisitors.drop();
db.profiles.drop();
db.skills.drop();
db.professions.drop();
db.users_history.drop();
db.users.drop();
db.contact_us.drop();



//=================1.generateUsers
var USERS_COUNT=100; //should be more than popular index (40currently)
var PROFILES_COUNT=100;
var COMMENTS_COUNT=25;//at average


var skillsList = [
"Accurate",
"Ambitious",
"Being Artistic",
"Being Thorough",
"Collaboration",
"Communication",
"Creativity",
"Confident",
"Consistent",
"Entertainment",
"Energetic",
"Flexible",
"Humor",
"Honesty",
"Innovation",
"Leadership",
"Listening",
"Politeness",
"Productive",
"Reliable",
"Resourceful",
"Responsible"
];

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
"Writer",
"IT Engineer"
];


var interestsList = [
"tv",
"movies",
"Test1",
"Test2",
"Test3",
"Test4",
"Test5",
"Test6",
"Test7",
"Test8",
"Test9",
"Test10",
"Test11",
"Test12",
"Test13",
"Test14",
"Test15",
"Test16",
"Test17",
"Test18",
"Test19",
"Test20",
"Test21",
"Test22",
"Test23",
"Test24",
"Test25",
"Test26",
"Test27",
"Test28",
"Test29"
];

for (var i = 1; i <= USERS_COUNT; i++) {
    db.users.insert(
        { "created" : 1412506610672,
        "first_name" : "admin_FirstName"+i,
        "last_name" : "admin_LastName"+i,
        "email" : "admin" + i + "@reviyou.com",
        "is_fake_user" :true, //additional field just for our internal purposes
        "login_provider" : "facebook",
        "token" : "token",
        "gender": "male",
        //image should be in our db or on our web server
        //a.png would work since it's less than 6 characters and nginx will return default image this case
        //"user_profile_image" : "a.png",
            //TODO: bookmarks. We probably should 1. create users without bookmarks 2. add profiles 2. add bookmarks
            //"bookmarks":{"1","2","3"}
        "fb_login_data" :
            {
            "expiresIn" : "1",//small number so that user does expire
            "fbUserId" : "fbUserIdTestUtils"+i,
            "image_url": "http://www.emagazin.info/img/articles/arenda-admina/super-admin.jpg",
            "is_default_image" :false
            }
        }
    )

}


//######## add real users we can test with:


db.users.insert(
        { "created" : 1412506610672,
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
 
//=================2.add skills and professions and interests\tags

//var str ="AaBbCcDdEeFfGgHhIiGgKkLlMmNnOoPpRrSsTtUuVvWwXxYyZz";
for (var i = 0; i < skillsList.length; i++) {
    db.skills.insert({"skill_name" : skillsList[i] })
}
for(var i=0;i<occupationList.length;i++) {
    db.professions.insert({"profession_name" : occupationList[i] })	
}
for(var i=0;i<interestsList.length;i++) {
    db.tags.insert({"name" : interestsList[i] })	
}




//=================3.profiles
for (var i = 1; i <= PROFILES_COUNT; i++) {
	var themeName = (Math.floor(Math.random()*7) +1);//b\w 1 and 6
	var popularIndex =0;
	var url;
	if(i%2==0) {
		popularIndex = i;
		url = "http://ia.media-imdb.com/images/M/MV5BMTQwMjAwNzI0M15BMl5BanBnXkFtZTcwOTY1MTMyOQ@@._V1_SY317_CR22,0,214,317_AL_.jpg";
	}
	var uniqueVisits = 	35+i%10; //from 35 till 45
	var occupationName = occupationList[i%occupationList.length];
	
	
	
	db.profiles.insert(
		
		{
		"n":"Jim"+i + " Kerry"+(i%10),
		"email":"jkerry@mail.com"+i,
		"user_id":"5378de960b00000b00124b00",
		"tn":""+themeName,
		//"views_count": popularIndex,//just some random fake count
		"vwc": 1000+uniqueVisits,//just some random fake count
		"state": 2,//1-Pending, 2-Waived, 3-approved, 4-Rejected. -1-deleted
		//"visible":true,
		"vtc" : 0,
		"i": url,
		"os":[
			{
				"os":[{
					"name":"XX Fox"+i,
					"start_date":1412457628427,
					"end_date":1412457628427
					}],
				"o":occupationName,
				"start_date":1412457628427,
				"end_date":1412457628427
			}
				],
		"tags":["tv","movies"],
		"pi":uniqueVisits
		}
	);
}

//=================4.profile, visitors, comments section


//userId's
var userIds = [];
var userIdCursor = db.users.find({},{"_id": 1})//projection by _id
while(userIdCursor.hasNext()) {
	var userId = userIdCursor.next();
	 //print (tojson(userId._id.str));
	 userIds.push(userId._id.str);
//	  db.profileVisitors.insert({"_id": profileId._id, "user_ids" : userIds})
}




var profileIdCursor = db.profiles.find({},{"_id": 1, "pi":1})//projection by _id

while(profileIdCursor.hasNext() ) {
	var profileId = profileIdCursor.next();
	 //print (tojson(profileId._id));
	 	//based on popular_index amount of unique visitors should be the same
	  print('popular index:' +profileId.pi);
	  db.profileVisitors.insert({"_id": profileId._id, "user_ids" : userIds.slice(0, profileId.pi)})
	 
	  //=================5.add comments:	
	  var com_count = Math.floor(Math.random() * COMMENTS_COUNT);
	  for(var i=0; i<com_count;i++) {
		  	var text =Array(Math.floor(Math.random()*140 )).join(i) ;
		  	 var date = new Date();
		  	 var time = date.getTime()-1000*60*60*24*i;
	  
		  db.comments.insert({
		  	"profile_id": profileId._id.str, 
		  	"user_id" : userIds[i%USERS_COUNT],
		  	"user_first_name":"alex"+i,
		  	"user_last_name":"Smith"+i,
		  	"text": "comment"+i + text+"comment"+i,
		  	"create_time":time,
		  	"group_warm":userIds.slice(0, 1),
		  	"group_cold":userIds.slice(0, 1),
		  	"group_troll":userIds.slice(0, 1),
		  	"group_report":userIds.slice(0, 2)
		  	
		  	
		  	}
		  );
		  
	  }
	  //update profile comments count
	  
	  //=================6. add a few skills to each profile(from 1 till 5)
	  var skillIdCursor = db.skills.find({},{"_id": 1, "skill_name": 1})//projection by _id
	  var j = (Math.floor(Math.random()*5));//b\w 0 and 5 skills
	  var skillsInd =  0;
	  var profileSkills = [];
	  while(skillIdCursor.hasNext() && j>0) {
	  		j--;
	  		skillsInd++;
		  var skill = skillIdCursor.next();
		  
		  
		  //with average rank
		  if(j==1) {
			  profileSkills.push({"skill_id":skill._id.str,
			  	"is_custom":false,
				"skill_name" :skill.skill_name,
				"votes_count" : 0,
				"skill_average_rank" :15//see #9 - we add vote from user 3 and 4(values 1&2)			
				});
		  } else {
		  	 //without average rank
		     profileSkills.push({"skill_id":skill._id.str,
			  	"is_custom":false,
				"skill_name" :skill.skill_name,				
				"votes_count" :0				
				});
		  }
			
		 
	
	  }
	  
	  //TODO: add a few professions for some profiles
	  
	  	db.profiles.update(
		   { _id: profileId._id },
		   { $set: { "skills": profileSkills } }
		)

}


//=================7.bookmarks for our test user:
var profileIdCursor2 = db.profiles.find({},{"_id": 1})//projection by _id
var testUserIdCursor = db.users.find({"email": "reviyoutest10@gmail.com"},{"_id":1})//projection by _id
var ind =0;
if(testUserIdCursor.hasNext() && profileIdCursor2.hasNext() ) {
	var testUserId=testUserIdCursor.next()._id.str;
	while(profileIdCursor2.hasNext() && ind<20) {//add 20 bookmarks only for now
		var profileId = profileIdCursor2.next();
		ind++;
		
		db.bookmarks.insert({
				  	"profile_id": profileId._id.str, 
				  	"user_id" : testUserId
				  	})
	}
} else {
	print('cant find test user or any profile');
}	


//=================8. add some news

for(var i=1; i<50; i++) {
var date = new Date();
		  	 var time = date.getTime()-1000*60*60*24*i;//minus day
		  	 var uniqueDate = new Date(time);
		  	 
		db.news.insert({
				  	"title": "this is some title of the news"+i,
				  	"content": "this is some content.this is some content.this is some content.this is some content.this is some content.this is some content.this is some content.this is some content.this is some content.this is some content.this is some content.this is some content.this is some content.this is some content.this is some content.this is some content.this is some content.this is some content.this is some content.this is some content.this is some content.this is some content.this is some content.this is some content.this is some content.this is some content.this is some content.this is some content.this is some content.this is some content.this is some content.",
				  	"create_time": uniqueDate
				  	})
}



//=================9. add votes to each profile for general from 1-2 diff users

var profileIdCursor3 = db.profiles.find({},{"skills": 1})//projection by _id
var ind2 =0;
	while(profileIdCursor3.hasNext()) {//add General votes to odd profiles only
		var profileId3 = profileIdCursor3.next();
		ind2++;
		
		//user1 General
		if(ind2%2==0) {
			db.votes.insert({
					  	"profile_id": profileId3._id.str, 
					  	"user_id" : "11111111111111",
					  	"vote_time" : 1412457628427,
					  	"vote_value" : 8
					  	});
			//user2 General
			db.votes.insert({
					  	"profile_id": profileId3._id.str, 
					  	"user_id" : "11111111111112",
					  	"vote_time" : 1412457628427,
					  	"vote_value" : 6
					  	});	
			db.profiles.update(
				{ _id: profileId3._id },
				{ $set: { "votes_count": 2 } }
		    );	  	
		}
		
		//check if profile has any skills
		var profileSkillsArr = profileId3.skills;
		if(profileSkillsArr.length !=0) {
			//add to skill A 2 votes to make rank=15(see #6)
			//user3  
			db.votes.insert({
					  	"profile_id": profileId3._id.str, 
					  	"user_id" : "11111111111113",
					  	"vote_time" : 1412457628427,
					  	"vote_value" : 1,
					  	"skill_id":profileSkillsArr[0].skill_id
					  	});	
			//user4  
			db.votes.insert({
					  	"profile_id": profileId3._id.str, 
					  	"user_id" : "11111111111114",
					  	"vote_time" : 1412457628427,
					  	"vote_value" : 2,
					  	"skill_id":profileSkillsArr[0].skill_id
					  	});
					  	
			db.profiles.update(
				{ _id: profileId3._id },
				{ $set: { "skills.0.votes_count": 2 } }
		    );		  							  	
		}

	}


print('end of upload script');		  




