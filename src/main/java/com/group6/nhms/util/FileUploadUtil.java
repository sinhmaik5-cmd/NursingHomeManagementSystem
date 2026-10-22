package com.group6.nhms.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class FileUploadUtil {

    private FileUploadUtil() {
    }

    /**
     * Lưu file upload vào đường dẫn chỉ định
     * @return Tên file mới đã được mã hóa UUID
     */
    public static String saveFile(String uploadDir, MultipartFile multipartFile) throws IOException {
        if (multipartFile == null || multipartFile.isEmpty()) {
            return null;
        }

        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = multipartFile.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // Tạo tên file ngẫu nhiên tránh trùng lặp
        String newFilename = UUID.randomUUID().toString() + fileExtension;

        try (InputStream inputStream = multipartFile.getInputStream()) {
            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ioe) {
            throw new IOException("Không thể lưu file: " + originalFilename, ioe);
        }

        return newFilename;
    }

    /**
     * Xóa file khỏi hệ thống khi người dùng xóa/cập nhật ảnh
     */
    public static boolean deleteFile(String filePathStr) {
        if (filePathStr == null || filePathStr.trim().isEmpty()) return false;
        try {
            Path path = Paths.get(filePathStr);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            return false;
        }
    }
}