# sct-id-service-API   
A REST Server for managing the generation and assignment of Terminology Component Identifiers. Supports SNOMED CT Identifiers and other identifier Schemes. Pre-bundled implementations for optional generation of legacy identifiers in SNOMED CT (SNOMEDIDs and CTV3IDs)
Installation
This application requires a MySQL database running on localhost. A new Database with your Specified name needs to be available before running the application.
Clone this project with:
Clone this Project in IDE: https://github.com/IHTSDO/sct-id-service

Execute the MySQL Schema generator script to create all the necessary tables and indexes:
config/db_script.sql
Configure Java 11 in your IDE.
Right Click on pom.XML , then select Add to Maven project. This will add all essential dependencies to project.
Create a new file named "application.properties" under Resources Folder.

Copy the contents Present in application.stage.properties and paste in application.properties.

Replace Ip,Port,DB Name in spring.datasource.url

Enter your DB username in spring.datasource.username

Enter your DB Password in spring.datasource.password

Enter dev-IMSURL in snomed.devIms.url

To Execute login API from Security Controller to connect devIMS authenticate API,
Please fill All the Details of snomed.user in appication.properties file

Now you can run your local and check API:

This API will Authenticate you : http://localhost:port/login

API to logout : http://localhost:port/logout

# Component Identifiers
The application supports to basic types of component identifiers, SCTIDS and generic Identifier Schemes. SCTIDs are assigned based on namespaces, Namcespaces can be created and managed with the Api. Identifier Schemes represent additional identifiers, like SNOMEDIDs (legacy ids from previous SNOMED Versions) and CTV3IDs (legacy Ids from the UK NHS Read Codes). Other identifiers can be added using code extension points without the need for alterations in the data structure or the Api.

# The Identifier Record

The application stores related metadata for each idenfier generated or registered in the satabase, the model for a SCTID Identifer Record is:

"SCTIDRecord" : {
            "properties": {
                "sctid": {
                    "type": "string"
                },
                "sequence": {
                    "type": "integer"
                },
                "namespace": {
                    "type": "integer"
                },
                "partitionId": {
                    "type": "string"
                },
                "checkDigit": {
                    "type": "integer"
                },
                "systemId": {
                    "type": "string"
                },
                "status": {
                    "type": "string"
                },
                "author": {
                    "type": "string"
                },
                "software": {
                    "type": "string"
                },
                "expirationDate": {
                    "type": "string"
                },
                "comment": {
                    "type": "string"
                },
                "additionalIds": {
                  "type": "array",
                  "items": {
                    "$ref": "#/definitions/SchemeIdRecord"
                  }
                }
            }
        }
        
A similar model is used for Scheme Identifiers.

# The identifiers lifecycle

Identifiers are generated or registered using the Api, and they will change status to represent publications, reservations and other events that may make the identifier to be available again or to be decisively linked with a terminology component.

The set of valid statuses and actions are represented in the State Machine diagram included in this git project as a pdf file.

# Authentication

Currently Authentication is done by IMS Integration to bypass Crowd. Tokens are Read from Cookies.

# Example REST Api calls

Integrating this service into an application will require to perform http calls to the Api, 

For example:

Retrieving the Sctid from table

GET http://localhost:YourPort/getSctByIds?token=hdaskjdhakjdgy7,ids=123456789,456789234


### Build

The application has 3 build profiles configured

Command to build jar and deb files

`mvn clean package`


Output:
1) jar file in target folder (Eg. target/cis.jar)
2) deb file in target folder (Eg. target/cis-0.0.1-SNAPSHOT-all.deb)


###Deployment

Deployment requires JRE11 installed on the target machine

Command to deploy the jar file

`java -jar target/cis.jar --spring.config.location=<<path to application.yml file>>`

Input parameters:
1) spring.config.location
    1) The path to application.yml file  in target machine which will be used for deployment 

Output:
1) jar file deployed in target machine