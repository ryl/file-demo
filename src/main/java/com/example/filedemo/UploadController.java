package com.example.filedemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
public class UploadController {
  
  private static final Logger logger = LoggerFactory.getLogger(UploadController.class);

  @Value("${uploads.location}")
  private String uploadLocation;

  private FileUploadRepository fileUploadRepository;

  public UploadController(FileUploadRepository fileUploadRepository) {
    this.fileUploadRepository = fileUploadRepository;
  }

  @PostMapping("/upload")
  FileUpload uploadFile(@Validated @RequestBody MultipartFile file) throws Exception {
    Map<String, Object> fileInfo = new HashMap<>();
    fileInfo.put("name", file.getName());
    fileInfo.put("originalFilename", file.getOriginalFilename());
    fileInfo.put("contentType", file.getContentType());
    fileInfo.put("size", file.getSize());

    // Make a folder to save files to.
    File filesDirectory = new File(uploadLocation);
    if (!filesDirectory.exists()) {
      if (!filesDirectory.mkdirs()) {
        throw new IOException("Could not create directory " + filesDirectory);
      }
    }
    
    String newLocation = filesDirectory.getAbsolutePath() + File.separator + UUID.randomUUID().toString();
    logger.info("File will be saved to {}", newLocation);
    
    file.transferTo(new File(newLocation));
    FileUpload fileUpload = new FileUpload(file, newLocation);

    return fileUploadRepository.save(fileUpload);
  }

  @GetMapping("/fileUploads/{id}/download")
  public void download(@PathVariable Long id, HttpServletResponse response) {
    Optional<FileUpload> optionalFileUpload = fileUploadRepository.findById(id);

    if (!optionalFileUpload.isPresent()) {
     response.setStatus(404);
     return;
    }

    FileUpload fileUpload = optionalFileUpload.get();
    File file = new File(fileUpload.getLocation());

    try {
      InputStream in = new FileInputStream(file);
      byte[] buffer = new byte[1024];
      int read = 0;

      response.setContentType(fileUpload.getContentType());
      response.setHeader("Content-Disposition", String.format(
              "attachment;filename=\"%s\"", fileUpload.getOriginalFilename()));

      while ((read = in.read(buffer)) != -1) {
        System.out.println(read);
        response.getOutputStream().write(buffer, 0, read);
      }
    } catch (FileNotFoundException e) {
      response.setStatus(404);
    } catch (IOException e) {
      response.setStatus(500);
    }

  }
  
}
