package tn.iset.investplatformpfe.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.iset.investplatformpfe.Entity.ChatAttachment;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${upload.path.chat-attachments:uploads/chat}")
    private String uploadPath;

    @Value("${app.base-url:http://localhost:8089}")
    private String baseUrl;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif"
    );

    private static final List<String> ALLOWED_DOCUMENT_TYPES = Arrays.asList(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain"
    );

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;

    public ChatAttachment storeFile(MultipartFile file, String messageId) throws IOException {

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("Fichier trop volumineux (max 50MB)");
        }

        String contentType = file.getContentType();
        if (!isAllowedFileType(contentType)) {
            throw new RuntimeException("Type de fichier non autorisé");
        }

        String dateFolder = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        Path uploadDir = Paths.get(uploadPath, dateFolder);

        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String filename = UUID.randomUUID().toString() + extension;
        Path filePath = uploadDir.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        String fileUrl = baseUrl + "/uploads/chat/" + dateFolder.replace('\\', '/') + "/" + filename;

        ChatAttachment attachment = new ChatAttachment();
        attachment.setFileName(originalFilename);
        attachment.setFilePath(filePath.toString());
        attachment.setFileUrl(fileUrl);
        attachment.setFileType(contentType);
        attachment.setFileSize(file.getSize());

        if (contentType != null && contentType.startsWith("image/")) {
            String thumbnailUrl = generateThumbnail(filePath, dateFolder, filename);
            attachment.setThumbnailUrl(thumbnailUrl);
        }

        return attachment;
    }

    private boolean isAllowedFileType(String contentType) {
        return contentType != null &&
                (ALLOWED_IMAGE_TYPES.contains(contentType) ||
                        ALLOWED_DOCUMENT_TYPES.contains(contentType));
    }

    private String generateThumbnail(Path originalPath, String dateFolder, String filename) {
        try {
            BufferedImage originalImage = ImageIO.read(originalPath.toFile());
            if (originalImage == null) return null;

            int thumbSize = 150;
            BufferedImage thumbnail = new BufferedImage(thumbSize, thumbSize, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = thumbnail.createGraphics();

            double ratio = Math.min(
                    (double) thumbSize / originalImage.getWidth(),
                    (double) thumbSize / originalImage.getHeight()
            );

            int newWidth = (int) (originalImage.getWidth() * ratio);
            int newHeight = (int) (originalImage.getHeight() * ratio);

            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(originalImage,
                    (thumbSize - newWidth) / 2, (thumbSize - newHeight) / 2,
                    newWidth, newHeight, null);
            g2d.dispose();

            String thumbFilename = "thumb_" + filename.replaceAll("\\.(png|gif)$", ".jpg");
            Path thumbPath = originalPath.getParent().resolve(thumbFilename);
            ImageIO.write(thumbnail, "jpg", thumbPath.toFile());

            return baseUrl + "/uploads/chat/" + dateFolder.replace('\\', '/') + "/" + thumbFilename;

        } catch (Exception e) {
            return null;
        }
    }

    public boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            return false;
        }
    }
}