package edu.uea.acadmanage.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.uea.acadmanage.config.FileStorageProperties;

@RestController
@RequestMapping("/api/files")
public class FileController {
    private final Path fileStorageLocation;

    public FileController(
        FileStorageProperties fileStorageProperties) throws IOException {
            this.fileStorageLocation = Paths.get(fileStorageProperties.getStorageLocation()).toAbsolutePath().normalize(); 
    }

    @GetMapping("/{filename}")
    @PreAuthorize("isAuthenticated()") // Garante que apenas usuários autenticados possam acessar
    public ResponseEntity<Resource> getFile(@PathVariable String filename) throws MalformedURLException {
        Path filePath = this.fileStorageLocation.resolve(filename).normalize();
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new RuntimeException("Arquivo não encontrado ou não é legível.");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}