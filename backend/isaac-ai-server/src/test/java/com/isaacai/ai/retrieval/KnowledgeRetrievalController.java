package com.isaacai.ai.retrieval;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/knowledge/search")
public class KnowledgeRetrievalController {

    private final KnowledgeRetriever knowledgeRetriever;

    public KnowledgeRetrievalController(
            KnowledgeRetriever knowledgeRetriever
    ) {
        this.knowledgeRetriever = knowledgeRetriever;
    }

    @GetMapping
    public List<RetrievedKnowledge> search(
            @PathVariable UUID workspaceId,
            @RequestParam String query
    ) {
        return knowledgeRetriever.retrieve(
                workspaceId,
                query
        );
    }
}