package vn.vela.hpdesign.sample.init;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import javax.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Component
public class AmazonClient {

  private AmazonS3 s3Client;

  @Value("${aws.s3.bucket}")
  private String bucketName;
  @Value("${aws.s3.accessKey}")
  private String accessKey;
  @Value("${aws.s3.secretKey}")
  private String secretKey;
  @Value("${aws.s3.region}")
  private String region;

  @PostConstruct
  private void initializeAmazon() {
    AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
    this.s3Client = AmazonS3ClientBuilder.standard()
        .withCredentials(new AWSStaticCredentialsProvider(credentials))
        .withRegion(this.region).build();
  }

}
