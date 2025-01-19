package tmanager.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import tmanager.entity.Attachment;
import tmanager.entity.AttachmentContent;
import tmanager.repository.AttachmentContentRepository;
import tmanager.repository.AttachmentRepository;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("file")
@RequiredArgsConstructor
public class FileController {

    private final AttachmentRepository attachmentRepository;
    private final AttachmentContentRepository attachmentContentRepository;

    @GetMapping("/{attachmentId}")
    public void inputPic(@PathVariable UUID attachmentId, HttpServletResponse response) throws IOException {
        Attachment attachment = attachmentRepository.findById(attachmentId).orElseThrow();
        AttachmentContent attachmentContent = attachmentContentRepository.findByAttachmentId(attachmentId);
        response.setContentType("image/jpeg");
        response.setHeader("Content-Disposition", "inline; filename=\"" + attachment.getFilename() + "\"");
        response.getOutputStream().write(attachmentContent.getFileData());

    }
}