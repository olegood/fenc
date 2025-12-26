package olegood.fenc.service;

import java.io.InputStream;

public record AttachmentDownload(InputStream inputStream, String fileName, long contentLength) {}
