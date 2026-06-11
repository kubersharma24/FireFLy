package com.emailagent.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

@Slf4j
@Service
public class FileStorageService {

    @Value("${app.storage.cvcl-dir}")
    private String cvclDirPath;

    private Path cvclDir;

    /**
     * On every app startup:
     * 1. If CVCL exists → delete entire directory
     * 2. Create fresh empty CVCL directory
     */
    @PostConstruct
    public void initDirectory() throws IOException {
        cvclDir = Paths.get(cvclDirPath);

        log.info("[FileStorage] CVCL path: {}", cvclDir.toAbsolutePath());

        if (Files.exists(cvclDir)) {
            log.info("[FileStorage] Existing CVCL found at {} — deleting...",
                    cvclDir.toAbsolutePath());
            deleteDirectory(cvclDir);
            log.info("[FileStorage] ✅ Old CVCL deleted");
        }

        Files.createDirectories(cvclDir);
        log.info("[FileStorage] ✅ Fresh CVCL created at: {}", cvclDir.toAbsolutePath());
    }

    /**
     * Saves uploaded file into CVCL with its original filename.
     * Overwrites if same name exists.
     *
     * @return absolute Windows path e.g. D:\workspace\workspace-3\email-agent\CVCL\MyResume.pdf
     */
    public String save(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            log.warn("[FileStorage] Skipping — file is null or empty");
            return null;
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            throw new IOException("Uploaded file has no filename");
        }

        Path targetPath = cvclDir.resolve(originalName);

        if (Files.exists(targetPath)) {
            log.warn("[FileStorage] {} already exists — overwriting", originalName);
        }

        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        String absolutePath = targetPath.toAbsolutePath().toString();
        log.info("[FileStorage] ✅ Saved: {} → {} | Size: {} bytes",
                originalName, absolutePath, Files.size(targetPath));

        return absolutePath;
    }

    /**
     * Recursively deletes directory and all contents.
     */
    private void deleteDirectory(Path path) throws IOException {
        Files.walk(path)
                .sorted((a, b) -> b.compareTo(a))   // children before parent
                .forEach(p -> {
                    try {
                        Files.delete(p);
                        log.debug("[FileStorage] Deleted: {}", p);
                    } catch (IOException e) {
                        log.error("[FileStorage] Could not delete: {} — {}", p, e.getMessage());
                    }
                });
    }
}