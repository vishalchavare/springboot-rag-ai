package demo.elastic.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagService {

    private final ElasticsearchVectorStore vectorStore;
    private final ChatClient chatClient;

    public RagService(ElasticsearchVectorStore vectorStore, ChatClient.Builder builder) {
        this.vectorStore = vectorStore;
        this.chatClient = builder.build();
    }

    public void ingestPDF(String path) {

        PagePdfDocumentReader reader = new PagePdfDocumentReader(path);
        List<Document> documents = reader.read();

        documents = new TokenTextSplitter().apply(documents);

        vectorStore.doAdd(documents);
    }

    public String queryLLM(String question) {

        List<Document> results =
                vectorStore.doSimilaritySearch(
                        SearchRequest.builder()
                                .query(question)
                                .topK(5)
                                .similarityThreshold(0.6)
                                .build());

        if (results.isEmpty()) {
            return "No relevant information found.";
        }

        String context = results.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n"));

        String prompt = "You are an AI assistant. Use the context to answer the question. "
                + "If the answer is not present, say you don't know.\n\nCONTEXT:\n"
                + context + "\n\nQUESTION:\n" + question;

        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        Object page = results.get(0)
                .getMetadata()
                .get(PagePdfDocumentReader.METADATA_START_PAGE_NUMBER);

        return response + "\n\nSource page: " + page;
    }
}