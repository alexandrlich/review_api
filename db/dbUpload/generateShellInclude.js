#!/usr/bin/env node
var SIZE = 10;
var lineIndex = 0;
var superUserId = "111111112222222233333333";//must exist prior to execution
var feedInFileName = 'dataFeed10000.csv';
var generatedShellOutFileName = 'generatedProfiles.js';
var date = new Date();
var time = date.getTime();

var outputBuffer = '';
var fs = require('fs');
var inputLines = new Array(SIZE);
		
	actions = [
			readFromFile,
			generateProfiles,
			writeToFile

			
		];callback();	




function readFromFile() {
	var input = fs.createReadStream(feedInFileName);
	readLines(input, parseLine);
}

function generateProfiles() {
		var profileTemplate = 'db.profiles.insert({"first_name":"%FIRST%","last_name":"%LAST%","email":"","user_id":"'+superUserId+'","theme_name":"%THEME%","views_count": %POPULAR%,"is_deleted": false,"votes_count" : 1,"jobs":[{"company":"%WORK%","occupation":"%JOB%","start_date":"","end_date":""}],"popular_index":%POPULAR2%,"general_average_rank":%POPULAR3%,"created" : %DATE%});\n';
		
		
		outputBuffer= "/*profiles createion*/ \n";
	 
	for(var i=0;i<lineIndex;i++) {
		//outputBuffer += "data:" + inputLines[i][0] + '\n';
		var profile =profileTemplate
			.replace("%FIRST%",inputLines[i][0])
			.replace("%LAST%",inputLines[i][1])
			.replace("%THEME%",1+(i%6))
			.replace("%POPULAR%",inputLines[i][4]).replace("%POPULAR2%",inputLines[i][4]).replace("%POPULAR3%",inputLines[i][4])//in 2 places
			.replace("%JOB%",inputLines[i][2])
			.replace("%WORK%",inputLines[i][3])
			.replace("%DATE%",time);//comany\movie name
		
		outputBuffer+= profile;
	}
	
	callback();
}


function writeToFile() {
	console.log('writeToFile');
	console.log('data retrieved from cvs'+ inputLines[0][0]);
		
	fs.writeFile(generatedShellOutFileName, outputBuffer, function (err) {
	  if (err) return console.log(err);
	  console.log('data written to output file');
	  callback();
	});

}

function readLines(input, parseLine) {
  var remaining = '';

  input.on('data', function(data) {
    remaining += data;
    var index = remaining.indexOf('\n');
    var last  = 0;
    while (index > -1) {
      var line = remaining.substring(last, index);
      last = index + 1;
      parseLine(line);
      index = remaining.indexOf('\n', last);
    }

    remaining = remaining.substring(last);
  });

  input.on('end', function() {
    if (remaining.length > 0) {
    
      parseLine(remaining);
    }
    callback();
  });
}


function parseLine(data) {

  inputLines[lineIndex] = new Array(6);//first,last,occupation,movie,popular,vote
  var array = data.split(",");
  if(array.length != 7) {
	  console.error('row ' + lineIndex + 'is invalid');
  }
  for(var i=1;i<array.length;i++) {
    
    inputLines[lineIndex][i-1] = array[i];
    //console.log('el: ' + inputLines[lineIndex][i-1] );
  }
  lineIndex++;
}






//executes next command(for async list of commands)
function callback() {
    if (actions.length) {
        actions.shift()();
    } else {
        console.log("script is completed");
    }
}