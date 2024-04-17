package com.todo.fileupload;

import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {
    String save(MultipartFile file, String fileRootPath);
}
