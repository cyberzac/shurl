#Shurl
## About
An Akka-Scala implementation of an URL shortener.
This is a bare minimal implementation, it does not support persistence nor deletion of created short urls. Also the produced url's are the hex value of the hash code of the long url. This should be improved by using a secure hash algorithm, e.g. SHA-256, and encoding the result in a more compact human friendly way, e.g. Base-58.

Shurl do support two operations: Create and Lookup.
## Usage
To start use sbt:

    sbt run
### Create
To create a short url send a http post on /create.

    curl -H 'content-type:application/json' \
    -d '{ "url": "https://www.google.com"}' \
    http://localhost:8099/create
Response will be:

    {"url":"http://localhost:8099/9eb2d592"} 
    
or 400 Bad Request if the supplied url is invalid.
   
   
### Lookup
Use the returned short url, e.g. http://localhost:8099/9eb2d592
    
    curl -L http://localhost:8099/9eb2d592    
   
The response will be a 308 permanent redirect if the short url exists or a 404 not found if it does not.

## Scaling
The code is using the akka actor library. To scale use an Akka cluster with cluster sharding. The short url id is a good choice as the sharding key.

    