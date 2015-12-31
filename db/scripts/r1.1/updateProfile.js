// just run from any client for db01


//load visitors for each profile and 1 vote for general
 var profileIdCursor3 = db.profiles.find({},{})
	while(profileIdCursor3.hasNext()) {
		var profileId3 = profileIdCursor3.next();
			var popular = profileId3.popular_index;				
				db.profiles.update({"_id": profileId3._id},{ $set: { "state": 2 } });
				db.profiles.update({"_id": profileId3._id},{ $unset: { "is_deleted": 1 } });
	}

