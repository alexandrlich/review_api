// mongo --host <host> -u<admin> -p<password> --port <port> admin ~/reviyou/conf/db/scripts/indexes.js


function log(m) {
    var date = new Date(),
        timestamp = date.toLocaleDateString() + " " + date.toLocaleTimeString();

    print(timestamp + ": " + m);
}

var indexes = [
{
    collection: "profiles",
    indexes: [
    {index:{"email":"text", "n":"text", "os.o":"text", "os.os.at":"text", "state": 1, "tags": 1},
    options:{"weights":{"email":200, "n":100, "os.o":25, "os.os.at":10},
            "name":"ft_profile_search_v2", "background":true}},
    {index: {"tags":1, "pi":-1},options:{"background":true, "name":"i_popular_index_tags"}}
    ]
},

{
    collection: "bookmarks",
    indexes: [
    {index:{"profile_id":1}, options:{"background":true, "name":"i_profile_id"}},
    {index:{"user_id":1}, options:{"background":true, "name":"i_user_id"}}
    ]
},
{
    collection: "comments",
    indexes:[
    {index:{"profile_id":1}, options:{"background":true, "name":"i_profile_id"}},
    ]
},
{
    collection: "contact_us",
    indexes:[
    {index:{"create_time":-1}, options:{"background":true, "name":"i_create_time"}},
    ]
},
{
    collection: "feedbacks",
    indexes:[
    {index:{"user_id":1}, options:{"background":true, "name":"i_user_id"}},
    ]
},
{
    collection: "news",
    indexes:[
    {index:{"create_time":-1}, options:{"background":true, "name":"i_create_time"}},
    ]
},
{
    collection: "professions",
    indexes:[
    {index:{"profession_name":1}, options:{"background":true, "name":"i_profession_name"}},
    ]
},
{
    collection: "skills",
    indexes:[
    {index:{"skill_name":1}, options:{"background":true, "name":"i_skill_name"}},
    ]
},
{
    collection: "users",
    indexes:[
    {index:{"email":1}, options:{"background":true, "name":"i_email"}},
    ]
},
{
    collection: "votes",
    indexes:[
    {index:{"profile_id":1}, options:{"background":true, "name":"i_profile_id"}},
    ]
},
{
    collection: "user_credencials",
    indexes:[
     {index:{"email":1}, options:{"background":true, "name":"i_email"}},
    {index:{"conf_uid":1}, options:{"background":true, "name":"i_conf_uid"}}
   
    ]
}
]

db = db.getSiblingDB("db01");

//db.getCollection("profiles").dropIndex("ft_profile_search");
db.getCollection("profiles").dropIndex("i_popular_index_tags");
//db.getCollection("profiles").dropIndex("ft_profile_search_v2");

for (var i = 0; i < indexes.length; i++) {
    var c = db.getCollection(indexes[i].collection);
    for(var ii = 0; ii < indexes[i].indexes.length;ii++){
        // c.dropIndex(indexes[i].indexes[ii].options.name);
        var res = c.createIndex(indexes[i].indexes[ii].index, indexes[i].indexes[ii].options);

        if (res && res["ok"] == 1) {
            log("Created index 'db01."+ indexes[i].collection + "."+ indexes[i].indexes[ii].options.name +"'");
        } else {
            log("Failed to create index 'db01." + indexes[i].collection + "."+ indexes[i].indexes[ii].options.name +"'");
        }
    }
}
