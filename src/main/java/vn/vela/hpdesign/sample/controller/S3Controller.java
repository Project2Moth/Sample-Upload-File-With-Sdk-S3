package vn.vela.hpdesign.sample.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vn.vela.hpdesign.sample.service.AmazonS3Service;

@RestController
@RequestMapping("api/v1/s3/storage")
public class S3Controller {

  @Autowired
  private AmazonS3Service amazonS3Service;

  @PostMapping("upload")
  public String uploadFile(@RequestPart MultipartFile multipartFile,
      @RequestParam String sourcePath) {
    return amazonS3Service.uploadFile(multipartFile, sourcePath);
  }

  @PostMapping("multi-upload")
  public String multiUpload(@ModelAttribute MultipartFile[] multipartFiles,
      @RequestParam String sourcePath) {
    return amazonS3Service.uploadMultiFile(multipartFiles, sourcePath);
  }

  @PostMapping("copy")
  public String copyFile(
      @RequestParam String sourcePath,
      @RequestParam String destinationPath,
      @RequestParam String filePath) {
    return amazonS3Service.copyFile(filePath, sourcePath, destinationPath);
  }


  @DeleteMapping
  public String delete(@RequestParam String... filePaths) {
    return amazonS3Service.delete(filePaths);
  }

  @GetMapping()
  public ResponseEntity<List<String>> getAll(@RequestParam String folder) {
    return ResponseEntity.ok(amazonS3Service.getAll(folder));
  }

  // Use command amazon cli
  @PostMapping("multi/copy")
  public ResponseEntity<String> copyMultiFile(@RequestParam String sourcePath,
      @RequestParam String desPath) {
    return ResponseEntity.ok(amazonS3Service.copyMultiFile(sourcePath, desPath));
  }

  @DeleteMapping("file-folder")
  public ResponseEntity<String> removeAllFilesInFolder(@RequestParam String sourcePath) {
    return ResponseEntity.ok(amazonS3Service.removeFilesInFolder(sourcePath));
  }

}
