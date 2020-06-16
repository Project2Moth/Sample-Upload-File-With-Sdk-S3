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

**Technical**

AmazonS3 

Need have config for init amazon sdk:

aws:
  s3:
    accessKey: <access-key-account-amazon>
    secretKey: <secret-key-account-amazon>
    region: <region-account-amazon>
    bucket: <bucket-storage>

**How to config use amazon cli for mac** 

Turn on Terminal 
install: sudo ./aws/install
check version: aws2 --version (in current time create project version aws is version 2)
create file config aws cli: aws2 configure
-> enter access key
-> enter secret key
-> enter region
-> enter output format (json)

Example command:

Copy all files in folder to another folder
aws2 s3 mv s3://unity-test-1/release s3://unity-test-1/next_version —recursive

//NOTICE : Không được phép đẩy thông tin của tài khoản amazon(accsessKey, secretKey) lên github public.

    
    