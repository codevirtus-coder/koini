package com.koini.api.service.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

  private final Path uploadRoot;

  public FileStorageService(@Value("${koini.upload.dir:uploads}") String uploadDir) {
    this.uploadRoot = Path.of(uploadDir).toAbsolutePath().normalize();
  }

  public String storeMerchantOnboardingFile(UUID userId, String fieldName, MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException(fieldName + " file is required");
    }
    String extension = fileExtension(file.getOriginalFilename());
    String filename = fieldName + "-" + UUID.randomUUID() + (extension.isBlank() ? "" : "." + extension);
    Path targetDir = uploadRoot.resolve("merchant-onboarding").resolve(userId.toString());
    Path targetFile = targetDir.resolve(filename).normalize();
    if (!targetFile.startsWith(targetDir.normalize())) {
      throw new IllegalStateException("Invalid file path");
    }
    try {
      Files.createDirectories(targetDir);
      try (InputStream in = file.getInputStream()) {
        Files.copy(in, targetFile, StandardCopyOption.REPLACE_EXISTING);
      }
    } catch (IOException e) {
      throw new IllegalStateException("Failed to store file", e);
    }
    return targetFile.toString();
  }

  private static String fileExtension(String originalFilename) {
    if (originalFilename == null) {
      return "";
    }
    int dot = originalFilename.lastIndexOf('.');
    if (dot < 0 || dot == originalFilename.length() - 1) {
      return "";
    }
    return originalFilename.substring(dot + 1).toLowerCase(Locale.ROOT);
  }
}

