package olegood.fenc.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface FileStorage {

    UUID store(UUID documentId, MultipartFile file) throws Exception;

    AttachmentDownload load(UUID attachmentId) throws Exception;

}
