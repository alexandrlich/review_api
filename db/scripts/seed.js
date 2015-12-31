// mongo --host <host> -u<admin> -p<password> --port <port> admin ~/reviyou/conf/db/scripts/seed.js


function log(m) {
    var date = new Date(),
        timestamp = date.toLocaleDateString() + " " + date.toLocaleTimeString();

    print(timestamp + ": " + m);
}


var occupationSeeds = [
"Actor",
"Actress",
"Teacher",
"Manager",
"Professor",
"Administrator",
"Singer",
"Doctor",
"Sports Player",
"Musician",
"Writer",
"Producer",
"Director",
"Composer",
"Cinematographer"
];

var interestsList = [
"Music",
"France",
"Games",
"Theatre",
"Race",
"New Year",
"Comics",
"Politics",
"Science",
"Holidays",
"Music Albums",
"Hollywood",
"Fortune 500",
"Test1",
"Test2",
"Test3"
];

db = db.getSiblingDB("db01");

var collection = db.getCollection("occupations");

for (var i = 0; i < occupationSeeds.length;i++) {
   collection.insert({"_id": occupationSeeds[i]});
}

var collection = db.getCollection("tags");
for (var i = 0; i < interestsList.length;i++) {
   collection.insert({"_id": interestsList[i]});
}