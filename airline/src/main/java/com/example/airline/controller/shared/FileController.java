package com.example.airline.controller.shared;

import com.example.airline.dto.file.FileUploadResponse;
import com.example.airline.service.file.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/files")
public class FileController {
    
    private final FileStorageService fileStorageService;
    
    @org.springframework.beans.factory.annotation.Value("${app.upload.dir:uploads}")
    private String uploadDir;
    
    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }
    
    @PostMapping("/tours/{tourId}/images")
    public ResponseEntity<FileUploadResponse> uploadTourImage(
            @PathVariable Long tourId,
            @RequestParam("file") MultipartFile file) {
        try {
            FileUploadResponse response = fileStorageService.storeTourImage(file);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/requests/{requestId}/documents")
    public ResponseEntity<FileUploadResponse> uploadRequestDocument(
            @PathVariable Long requestId,
            @RequestParam("file") MultipartFile file) {
        try {
            FileUploadResponse response = fileStorageService.storeRequestDocument(file);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/{subdirectory}/{filename:.+}")
    public ResponseEntity<Resource> getFile(
            @PathVariable String subdirectory,
            @PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir, subdirectory, filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                String contentType = "application/octet-stream";
                if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                } else if (filename.endsWith(".png")) {
                    contentType = "image/png";
                } else if (filename.endsWith(".pdf")) {
                    contentType = "application/pdf";
                }
                
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @DeleteMapping("/{subdirectory}/{filename:.+}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable String subdirectory,
            @PathVariable String filename) {
        try {
            String fileUrl = "/files/" + subdirectory + "/" + filename;
            fileStorageService.deleteFile(fileUrl);
            return ResponseEntity.noContent().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}

