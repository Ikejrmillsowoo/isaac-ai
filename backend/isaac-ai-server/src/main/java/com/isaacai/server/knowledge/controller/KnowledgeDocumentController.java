package com.isaacai.server.knowledge.controller;

import com.isaacai.server.knowledge.dto.CreateKnowledgeDocumentRequest;
import com.isaacai.server.knowledge.dto.KnowledgeDocumentResponse;
import com.isaacai.server.knowledge.model.KnowledgeDocument;
import com.isaacai.server.knowledge.service.KnowledgeDocumentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(
        "/api/workspaces/{workspaceId}/knowledge"
)
public class KnowledgeDocumentController {

    private final KnowledgeDocumentService service;

    public KnowledgeDocumentController(
            KnowledgeDocumentService service
    ) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public KnowledgeDocumentResponse create(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody CreateKnowledgeDocumentRequest request
    ) {
        KnowledgeDocument document =
                service.create(
                        workspaceId,
                        request.name(),
                        request.originalFilename(),
                        request.contentType()
                );

        return KnowledgeDocumentResponse.from(document);
    }

    @GetMapping
    public List<KnowledgeDocumentResponse> findAll(
            @PathVariable UUID workspaceId
    ) {
        return service.findAll(workspaceId)
                .stream()
                .map(KnowledgeDocumentResponse::from)
                .toList();
    }

    @PostMapping(
        value = "/upload",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
)
@ResponseStatus(HttpStatus.CREATED)
public KnowledgeDocumentResponse upload(
        @PathVariable UUID workspaceId,
        @RequestParam("file") MultipartFile file
) {
    KnowledgeDocument document =
            service.upload(
                    workspaceId,
                    file
            );

    return KnowledgeDocumentResponse.from(document);
}
}