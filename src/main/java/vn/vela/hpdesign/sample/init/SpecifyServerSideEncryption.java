package vn.vela.hpdesign.sample.init;


import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.internal.SSEResultBase;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.CopyObjectResult;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import java.io.ByteArrayInputStream;
import java.io.File;

public class SpecifyServerSideEncryption {

  String bucketNameSource = "hp-qrcode-dev";
  String copySource = "assets/panorama";
  String desSource = "assets/panorama";
  public static void main(String[] args) {
    Regions clientRegion = Regions.AP_SOUTHEAST_1;
    String bucketName = "unity-test-1";
    String keyNameToEncrypt = "Test12345/tocnu13.jpeg";
    String keyNameToCopyAndEncrypt = "Test12345/tocnu13.jpeg";
    String copiedObjectKeyName = "next_version" + File.separator + "Test12345/tocnu13.jpeg";
    String accessKey = "AKIAWWFSTK4E67RZA3NC";
    String secretKey = "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";


    try {
//      AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
//          .withRegion(clientRegion)
//          .withCredentials(new ProfileCredentialsProvider())
//          .build();

      AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
      AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
          .withCredentials(new AWSStaticCredentialsProvider(credentials))
          .withRegion(clientRegion)
          .build();

      // Upload an object and encrypt it with SSE.
//      uploadObjectWithSSEEncryption(s3Client, bucketName, keyNameToEncrypt);

      // Upload a new unencrypted object, then change its encryption state
      // to encrypted by making a copy.
      changeSSEEncryptionStatusByCopying(s3Client,
          bucketName,
          keyNameToCopyAndEncrypt,
          copiedObjectKeyName);
    } catch (AmazonServiceException e) {
      // The call was transmitted successfully, but Amazon S3 couldn't process
      // it, so it returned an error response.
      e.printStackTrace();
    } catch (SdkClientException e) {
      // Amazon S3 couldn't be contacted for a response, or the client
      // couldn't parse the response from Amazon S3.
      e.printStackTrace();
    }
  }

//  private static void uploadObjectWithSSEEncryption(AmazonS3 s3Client, String bucketName,
//      String keyName) {
//    String objectContent = "Test object encrypted with SSE";
//    byte[] objectBytes = objectContent.getBytes();
//
//    // Specify server-side encryption.
//    ObjectMetadata objectMetadata = new ObjectMetadata();
//    objectMetadata.setContentLength(objectBytes.length);
//    objectMetadata.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);
//    PutObjectRequest putRequest = new PutObjectRequest(bucketName,
//        keyName,
//        new ByteArrayInputStream(objectBytes),
//        objectMetadata);
//
//    // Upload the object and check its encryption status.
//    PutObjectResult putResult = s3Client.putObject(putRequest);
//    System.out.println("Object \"" + keyName + "\" uploaded with SSE.");
//    printEncryptionStatus(putResult);
//  }

  private static void changeSSEEncryptionStatusByCopying(AmazonS3 s3Client,
      String bucketName,
      String sourceKey,
      String destKey) {
    // Upload a new, unencrypted object.
    PutObjectResult putResult = s3Client
        .putObject(bucketName, sourceKey, "Object example to encrypt by copying");
    System.out.println("Unencrypted object \"" + sourceKey + "\" uploaded.");
    printEncryptionStatus(putResult);

    // Make a copy of the object and use server-side encryption when storing the copy.
    CopyObjectRequest request = new CopyObjectRequest(bucketName,
        sourceKey,
        bucketName,
        destKey);
    CopyObjectRequest copyObjectRequest = new CopyObjectRequest(
        bucketName + File.separator + "release", sourceKey,
        bucketName, "next_version" + File.separator + "Test12345/tocnu13.jpeg");
    ObjectMetadata objectMetadata = new ObjectMetadata();
    objectMetadata.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);
    request.setNewObjectMetadata(objectMetadata);

    // Perform the copy operation and display the copy's encryption status.
    CopyObjectResult response = s3Client.copyObject(copyObjectRequest);
    System.out.println("Object \"" + destKey + "\" uploaded with SSE.");
    printEncryptionStatus(response);

    // Delete the original, unencrypted object, leaving only the encrypted copy in Amazon S3.
    s3Client.deleteObject(bucketName, sourceKey);
    System.out.println("Unencrypted object \"" + sourceKey + "\" deleted.");
  }

  private static void printEncryptionStatus(SSEResultBase response) {
    String encryptionStatus = response.getSSEAlgorithm();
    if (encryptionStatus == null) {
      encryptionStatus = "Not encrypted with SSE";
    }
    System.out.println("Object encryption status is: " + encryptionStatus);
  }
}