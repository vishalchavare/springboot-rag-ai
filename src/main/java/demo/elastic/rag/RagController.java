package demo.elastic.rag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rag")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/ingest")
    public ResponseEntity<String> ingestPDF(@RequestBody String path) {
        try {
            ragService.ingestPDF(path);
            return ResponseEntity.ok("PDF indexed successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/ask")
    public ResponseEntity<String> ask(@RequestBody String question) {
        try {
            return ResponseEntity.ok(ragService.queryLLM(question));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}