package com.example.filedemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class UploadController {
  
  Logger logger = LoggerFactory.getLogger(UploadController.class);
  
  @PostMapping("/upload")
  Map<String, Object> uploadFile(MultipartFile file) throws Exception {
    Map<String, Object> fileInfo = new HashMap<>();
    fileInfo.put("name", file.getName());
    fileInfo.put("originalFilename", file.getOriginalFilename());
    fileInfo.put("contentType", file.getContentType());
    fileInfo.put("size", file.getSize());
    
    // Make a folder to save files to.
    File filesDirectory = new File("/tmp/file-demo");
    if (!filesDirectory.exists()) {
      if (!filesDirectory.mkdirs()) {
        throw new IOException("Could not create directory " + filesDirectory);
      }
    }
    
    String newLocation = filesDirectory.getAbsolutePath() + File.separator + file.getOriginalFilename();
    logger.info("File will be saved to {}", newLocation);
    
    file.transferTo(new File(newLocation));
    return fileInfo;
  }
  
}
