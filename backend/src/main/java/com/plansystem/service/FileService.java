package com.plansystem.service;

import com.plansystem.entity.Attachment;
import com.plansystem.exception.BusinessException;
import com.plansystem.exception.ErrorCode;
import com.plansystem.mapper.AttachmentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {
    private final AttachmentMapper attachmentMapper;
    @Value("${file.upload-dir:./uploads}") private String uploadDir;
    @Value("${file.max-size:10MB}") private String maxSize;

    private static final List<String> ALLOWED_EXTENSIONS = List.of(
            "jpg","jpeg","png","gif","pdf","doc","docx","xls","xlsx","ppt","pptx","txt","zip");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    @Transactional
    public Attachment upload(MultipartFile file) throws IOException {
        if (file.getSize() > MAX_FILE_SIZE) throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED);
        String ext = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(ext.toLowerCase())) throw new BusinessException(ErrorCode.FILE_TYPE_NOT_SUPPORTED);

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

        String storedName = UUID.randomUUID().toString() + "." + ext;
        Path filePath = uploadPath.resolve(storedName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        Attachment attachment = new Attachment();
        attachment.setFileName(file.getOriginalFilename());
        attachment.setFilePath(filePath.toString());
        attachment.setFileSize(file.getSize());
        attachment.setFileType(file.getContentType());
        attachment.setUploadedAt(LocalDateTime.now());
        attachmentMapper.insert(attachment);
        return attachment;
    }

    public Attachment getById(Long id) {
        Attachment a = attachmentMapper.selectById(id);
        if (a == null) throw new BusinessException(ErrorCode.NOT_FOUND, "文件不存在");
        return a;
    }

    @Transactional
    public void delete(Long id) {
        Attachment a = getById(id);
        try { Files.deleteIfExists(Paths.get(a.getFilePath())); } catch (IOException ignored) {}
        attachmentMapper.deleteById(id);
    }

    private String getExtension(String filename) {
        int i = filename.lastIndexOf('.');
        return i > 0 ? filename.substring(i + 1) : "";
    }
}
