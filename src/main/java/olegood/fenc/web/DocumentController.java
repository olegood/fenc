package olegood.fenc.web;

import java.io.InputStream;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import olegood.fenc.service.FileStorage;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class DocumentController {

  private final FileStorage fileStorage;

  @PostMapping(
      value = "/documents/{documentId}/attachments",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public UUID upload(@PathVariable UUID documentId, @RequestParam MultipartFile file)
      throws Exception {

    if (!"application/pdf".equals(file.getContentType())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }
    return fileStorage.store(documentId, file);
  }

  @GetMapping("/attachments/{id}")
  public ResponseEntity<StreamingResponseBody> download(@PathVariable UUID id) throws Exception {
    var descriptor = fileStorage.load(id);

    StreamingResponseBody stream = outputStream -> {
      try (var in = descriptor.inputStream()) {
        in.transferTo(outputStream);
      }
    };

    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_PDF)
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + descriptor.fileName() + "\"")
            .header(HttpHeaders.CACHE_CONTROL, "no-store")
        .body(stream);
  }
}
