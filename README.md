**Getting start**

Application will use port 8090 config in file application.yaml. If this port unavaible then you change to use yout port in application.yaml

When application ws started then you can test by using Swagger-ui through this link:

http://localhost:8090/swagger-ui.html

or

http://localhost:<your-port>/swagger-ui.html

**Maven credentials:**

For use amazonS3 sdk:

<dependency>
   <groupId>com.amazonaws</groupId>
   <artifactId>aws-java-sdk</artifactId>
   <version>1.11.133</version>
</dependency>  

Technical:

AmazonS3 

Need have config for init amazon sdk:

aws:
  s3:
    accessKey: <access-key-account-amazon>
    secretKey: <secret-key-account-amazon>
    region: <region-account-amazon>
    bucket: <bucket-storage> 