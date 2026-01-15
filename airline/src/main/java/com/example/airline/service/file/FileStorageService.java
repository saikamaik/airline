package com.example.airline.service.file;

import com.example.airline.dto.file.FileUploadResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {
    
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;
    
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final String[] ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/png", "image/gif", "image/webp"};
    private static final String[] ALLOWED_DOCUMENT_TYPES = {"application/pdf", "image/jpeg", "image/png"};
    
    public FileUploadResponse storeFile(MultipartFile file, String subdirectory) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size (10MB)");
        }
        
        // Создаем директорию, если не существует
        Path uploadPath = Paths.get(uploadDir, subdirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Генерируем уникальное имя файла
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = UUID.randomUUID().toString() + extension;
        
        // Сохраняем файл
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Формируем URL для доступа к файлу
        String fileUrl = "/files/" + subdirectory + "/" + filename;
        
        return new FileUploadResponse(
                originalFilename,
                fileUrl,
                file.getContentType(),
                file.getSize()
        );
    }
    
    public FileUploadResponse storeTourImage(MultipartFile file) throws IOException {
        validateImageFile(file);
        return storeFile(file, "tours");
    }
    
    public FileUploadResponse storeRequestDocument(MultipartFile file) throws IOException {
        validateDocumentFile(file);
        return storeFile(file, "requests");
    }
    
    public void deleteFile(String fileUrl) throws IOException {
        // Удаляем префикс /files/ из URL
        String relativePath = fileUrl.replaceFirst("^/files/", "");
        Path filePath = Paths.get(uploadDir, relativePath);
        
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
    }
    
    private void validateImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        boolean allowed = false;
        for (String allowedType : ALLOWED_IMAGE_TYPES) {
            if (allowedType.equals(contentType)) {
                allowed = true;
                break;
            }
        }
        if (!allowed) {
            throw new IllegalArgumentException("File type not allowed. Allowed types: JPEG, PNG, GIF, WEBP");
        }
    }
    
    private void validateDocumentFile(MultipartFile file) {
        String contentType = file.getContentType();
        boolean allowed = false;
        for (String allowedType : ALLOWED_DOCUMENT_TYPES) {
            if (allowedType.equals(contentType)) {
                allowed = true;
                break;
            }
        }
        if (!allowed) {
            throw new IllegalArgumentException("File type not allowed. Allowed types: PDF, JPEG, PNG");
        }
    }
}

