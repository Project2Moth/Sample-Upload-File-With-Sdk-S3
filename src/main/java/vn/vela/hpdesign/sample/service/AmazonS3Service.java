package vn.vela.hpdesign.sample.service;

import com.amazonaws.services.s3.AmazonS3;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface AmazonS3Service {

  String uploadFile(MultipartFile multipartFile, String sourcePath);

  String uploadMultiFile(MultipartFile[] multipartFiles, String sourcePath);

  String copyFile(String pathFile, String sourcePath, String destinationPath);

  String copyMultiFile(String sourcePath, String destinationPath);

  String removeFilesInFolder(String sourceFolder);

  String delete(String... filePaths);

  List<String> getAll(String folder);

  AmazonS3 buildS3Client();
}
