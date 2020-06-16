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
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CopyUploadBigFileSample {

  static String bucketName1 = "bucketName1";
  static String bucketName = "bucketName";
  static String copySource = "assets/panorama";
  static String desSource = "assets/panorama";
  static String fileName = "tasa-cu-129";
  static String keyNameToEncrypt = "Test12345/tocnu13.jpeg";
  static String keyNameToCopyAndEncrypt = "Test12345/tocnu13.jpeg";
  static String copiedObjectKeyName = "next_version" + File.separator + "Test12345/tocnu13.jpeg";


  public static void main(String[] args) {

    // parse millisecond to minutes and second
    long time = 121767;
    String a = String.format("%d min, %d sec ",
        TimeUnit.MILLISECONDS.toMinutes(time),
        TimeUnit.MILLISECONDS.toSeconds(time) -
            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time))
    );
    System.out.println(a);
    // run command for copy folder to folder on s3
    runCommandCopyFolderInS3BucketToAnotherBucket();

    // upload big file use filepath
    uploadBigFileWithPathFile();

    //TODO: sdk s3 không hỗ trợ copy cả thư mục. Chỉ hỗ trợ copy từng file.
    copyFileBucketToBucket();

  }

  private static void uploadObjectWithSSEEncryption(AmazonS3 s3Client, String bucketName,
      String keyName) {
    String objectContent = "Test object encrypted with SSE";
    byte[] objectBytes = objectContent.getBytes();

    // Specify server-side encryption.
    ObjectMetadata objectMetadata = new ObjectMetadata();
    objectMetadata.setContentLength(objectBytes.length);
    objectMetadata.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);
    PutObjectRequest putRequest = new PutObjectRequest(bucketName,
        keyName,
        new ByteArrayInputStream(objectBytes),
        objectMetadata);

    // Upload the object and check its encryption status.
    PutObjectResult putResult = s3Client.putObject(putRequest);
    System.out.println("Object \"" + keyName + "\" uploaded with SSE.");
    printEncryptionStatus(putResult);
  }

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

    CopyObjectRequest copyObjectRequest = new CopyObjectRequest(
        bucketName + File.separator, copySource + fileName,
        "unity-test-1", desSource + File.separator + fileName);
    ObjectMetadata objectMetadata = new ObjectMetadata();
    objectMetadata.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);
    copyObjectRequest.setNewObjectMetadata(objectMetadata);

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


  /**
   * Sử dụng command để copy thư mục từ bucket sang bucket khác.
   */
  private static void runCommandCopyFolderInS3BucketToAnotherBucket() {

    String commandCopy =
        "aws s3 cp s3://" + bucketName + "/" + copySource + "/" + fileName + " s3://"
            + bucketName1
            + "/" + desSource + "/" + fileName + " --recursive";
    try {
      Process proc = Runtime.getRuntime().exec(commandCopy);

      // Read the output
      InputStreamReader inputStreamReader = new InputStreamReader(proc.getInputStream(),
          Charset.forName("UTF-8"));
      BufferedReader reader = new BufferedReader(inputStreamReader);

      String line;
      while ((line = reader.readLine()) != null) {
        System.out.println(line + "\n");
      }
      proc.waitFor();
      reader.close();

    } catch (Exception ex) {
      System.out.println("dfsfsdfdsfdsfds");
//        log.error(
//            "Executed command has been fail with exception: " + Arrays.toString(ex.getStackTrace()));
//        throw new BizException(Collections.singletonList("err.executed-command-fail"));

    }
  }

  // Sử dụng HighLevelMultipartUpload của amazon để upload file lớn.
  private static void uploadBigFileWithPathFile() {
    System.out.println("Start time: " + LocalDateTime.now());
    Long start = System.currentTimeMillis();
    Regions clientRegion = Regions.AP_SOUTHEAST_1;
    String bucketName = "bucketName"; // Khai báo nơi lưu trữ file (lưu trực tiếp trên bucket hoặc object khác.)
    String keyName = "keyName"; // Khai báo tên file lưu trữ
    String filePath = "filePath"; // Khai báo đường dẫn của file muốn upload lên
    String accessKey = "accessKey"; // Khai báo accessKey của bucket amazon.
    String secretKey = "secretKey"; // Khai báo secretKey của bucket amazon.

    try {

      // mặc định TransferManager sẽ sinh ra thread để upload file
      // tuy nhiên ta có thể control với số lượng thread khác tuỳ vào dung lượng file upload lên.
      int maxUploadThreads = 5;
      AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
      AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
          .withRegion(clientRegion)
          .withCredentials(new AWSStaticCredentialsProvider(credentials))
          .build();
      TransferManager tm = TransferManagerBuilder.standard()
          .withS3Client(s3Client)
          .withMultipartUploadThreshold(
              (long) (5 * 1024 * 1025)) // Ngưỡng của phần chặt nhỏ file khi truyền đơn lẻ.
          .withExecutorFactory(() -> Executors.newFixedThreadPool(maxUploadThreads))
          .build();

      // TransferManager processes all transfers asynchronously,
      // so this call returns immediately.
      Upload upload = tm.upload(bucketName, keyName, new File(filePath));
      System.out.println("Object upload started");

      // Optionally, wait for the upload to finish before continuing.
      upload.waitForCompletion();
      System.out.println("Object upload complete");
    } catch (AmazonServiceException e) {
      // The call was transmitted successfully, but Amazon S3 couldn't process
      // it, so it returned an error response.
      e.printStackTrace();
    } catch (SdkClientException e) {
      // Amazon S3 couldn't be contacted for a response, or the client
      // couldn't parse the response from Amazon S3.
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    System.out.println("End time: " + LocalDateTime.now());
    Long end = System.currentTimeMillis();
    System.out.println("Eslaptime : " + (end - start));
  }

  private static void copyFileBucketToBucket() {
    try {
      Regions clientRegion = Regions.AP_SOUTHEAST_1;

      String accessKey = "accessKey";// accessKey of bucket unity-test-1
      String secretKey = "secretKey";// secretKey of bucket unity-test-1

      AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
      AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
          .withCredentials(new AWSStaticCredentialsProvider(credentials))
          .withRegion(clientRegion)
          .build();
      CopyObjectRequest copyObjectRequest = new CopyObjectRequest(
          bucketName, copySource + "/" + fileName,
          "unity-test-1", desSource + "/" + fileName);
      CopyObjectResult response = s3Client.copyObject(copyObjectRequest);

//       Upload an object and encrypt it with SSE.
      uploadObjectWithSSEEncryption(s3Client, bucketName, keyNameToEncrypt);

//       Upload a new unencrypted object, then change its encryption state
//       to encrypted by making a copy.
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
}