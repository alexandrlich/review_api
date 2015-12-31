0) import imdb data to cvs format:
id,first_name,last_name,occupation,Worked at,Popular Index,initial vote
	

1)drop old schema by running drop.js

2) run generateShellInclude as a nodejs script to generate shell script for profiles. It will produce a new generatedProfiles.js
node generateShellInclude.js

3)add generatedProfiles.js to the database

4) run upload_prod_data.js against the datababase. 

	
