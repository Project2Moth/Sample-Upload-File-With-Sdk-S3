package vn.vela.hpdesign.sample.service.impl;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import vn.vela.hpdesign.sample.AmazonS3Exception;
import vn.vela.hpdesign.sample.service.AmazonS3Service;

@Service
@Transactional
@Log4j2
public class AmazonS3ServiceImpl implements AmazonS3Service {

  @Value("${aws.s3.bucket}")
  private String bucketName;
  @Value("${aws.s3.accessKey}")
  private String accessKey;
  @Value("${aws.s3.secretKey}")
  private String secretKey;
  @Value("${aws.s3.region}")
  private String region;

  @Override
  public String uploadFile(MultipartFile file, String sourcePath) {
    try {
      AmazonS3 s3Client = buildS3Client();
      log.info("Upload file contentType : " + file.getContentType(),
          "originalFilename: " + file.getOriginalFilename());
      if (!file.isEmpty()) {
        String fileName = file.getOriginalFilename() + "_" + LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyyMMdd_hhmmss"));
        log.debug(fileName);
        s3Client
            .putObject(bucketName, sourcePath + File.separator + fileName,
                convertMultiPartFileToFile(file));
      }
      return "upload successfully";
    } catch (Exception e) {
      log.error("Upload have fail with exception: " + Arrays.toString(e.getStackTrace()));
      throw new AmazonS3Exception();
    }
  }

  @Override
  public String uploadMultiFile(MultipartFile[] multipartFiles, String sourcePath) {
    try {
      List<File> files = new ArrayList<>();
      for (MultipartFile multipartFile : multipartFiles) {
        files.add(convertMultiPartFileToFile(multipartFile));
      }
      TransferManager tm = TransferManagerBuilder.standard().withS3Client(buildS3Client()).build();
      MultipleFileUpload xfer = tm
          .uploadFileList(bucketName, sourcePath, new File("."),
              files);

      // Sai ten file sau khi transfer qua new File(".")
      return "Upload successfully";
    } catch (AmazonServiceException e) {
      log.error("Upload multi files fail with exception: " + Arrays.toString(e.getStackTrace()));
      throw new AmazonS3Exception();
    }
  }

  @Override
  public String copyFile(String filePath, String sourcePath, String destinationPath) {
    AmazonS3 s3Client = buildS3Client();
    CopyObjectRequest copyObjectRequest = new CopyObjectRequest(
        bucketName + File.separator + sourcePath, filePath,
        bucketName, destinationPath + File.separator + filePath);
    s3Client.copyObject(copyObjectRequest);
    return "Copy file successfully";
  }

  @Override

  public String delete(String... filePaths) {
    DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucketName)
        .withKeys(filePaths);
    buildS3Client().deleteObjects(deleteObjectsRequest);
    return "Delete list file successfully";
  }

  public String copyMultiFile(String sourcePath, String destinationPath, String filePath) {
    return null;
  }

  @Override
  public List<String> getAll(String folder) {
    AmazonS3 s3Client = buildS3Client();
    ObjectListing objectListing = s3Client.listObjects(
        new ListObjectsRequest().withBucketName(bucketName)
            .withPrefix(folder + File.separator));
    return objectListing.getObjectSummaries().stream().map(
        (s3ObjectSummary -> s3ObjectSummary.getKey().replaceAll(folder + File.separator, "")))
        .collect(
            Collectors.toList());

  }

  @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(
      value = {"NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE",
          "OBL_UNSATISFIED_OBLIGATION_EXCEPTION_EDGE"},
      justification = "I know what I'm doing")
  private File convertMultiPartFileToFile(MultipartFile multipartFile) {
    try {
      File conFile = new File("");
      if (!multipartFile.isEmpty()) {
        if (!StringUtils.isEmpty(multipartFile.getOriginalFilename())) {
          conFile = new File("./" +
              multipartFile.getOriginalFilename());
          FileOutputStream fos = new FileOutputStream(conFile);
          fos.write(multipartFile.getBytes());
          fos.close();
        }
      }
      return conFile;
    } catch (Exception ex) {
      log.error("Can't parse multipart file to file : " + Arrays.toString(ex.getStackTrace()));
      throw new AmazonS3Exception();
    }
  }


  public AmazonS3 buildS3Client() {
    try {
      AmazonS3 s3Client;
      AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
      s3Client = AmazonS3ClientBuilder.standard()
          .withCredentials(new AWSStaticCredentialsProvider(credentials))
          .withRegion(region)
          .build();
      return s3Client;
    } catch (Exception e) {
      log.error("Build connection to s3 client fail : " + Arrays.toString(e.getStackTrace()));
      throw new AmazonS3Exception();
    }
  }
}
