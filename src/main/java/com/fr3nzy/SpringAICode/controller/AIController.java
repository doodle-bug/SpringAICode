package com.fr3nzy.SpringAICode.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.google.genai.text.GoogleGenAiTextEmbeddingOptions;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImageOptionsBuilder;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.content.Media;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

//import org.springframework.ai.openai.OpenAiChatModel;
//import org.springframework.ai.google.genai.GoogleGenAiChatModel;
//import org.springframework.ai.google.genai.image.GoogleGenAiImageModel;

import java.util.List;
import java.util.Map;

@RestController
public class AIController {

    private final ChatClient chatClient;
    private final ImageModel imageModel;

    @Autowired
    private VectorStore vectorStore;

    @Autowired
//    @Qualifier("openAiEmbeddingModel")
    private EmbeddingModel embeddingModel;

    ChatMemory chatMemory = MessageWindowChatMemory
            .builder()
            .build();

//    public AIController(OpenAiChatModel chatModel){
//        this.chatClient = ChatClient.create(chatModel);
//    }

    // Only use when dealing with only one model
//    public AIController(ChatClient.Builder builder){
//        this.chatClient = builder.build();
//    }

//    public ImageGenController(GoogleGenAiImageModel googleGenAiImageModel, GoogleGenAiChatModel chatModel){
//        this.googleGenAiImageModel = googleGenAiImageModel;
//        this.chatClient = ChatClient.create(chatModel);
//    }

    public AIController(ChatClient.Builder builder, EmbeddingModel embeddingModel, ImageModel imageModel) {
//        this.chatClient = builder
//                .defaultAdvisors(MessageChatMemoryAdvisor
//                        .builder(chatMemory)
//                        .build())
//                .build();

        this.chatClient = builder
                .defaultAdvisors(MessageChatMemoryAdvisor
                        .builder(chatMemory)
                        .build())
                .defaultAdvisors(a -> a.param(ChatMemory.CONVERSATION_ID, "amit-default-session"))
                .build();
        this.embeddingModel = embeddingModel;
        this.imageModel = imageModel;
    }

    // ==========================================
    // Chat & Completion Endpoints
    // ==========================================

    @GetMapping("/api/{message}")
    public ResponseEntity<String> getAnswer(@PathVariable String message) {
        ChatResponse chatResponse = chatClient
                .prompt(message)
//                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, "amit-default-session"))
                .call()
                .chatResponse();

        System.out.println(chatResponse.getMetadata().getModel());

        String response = chatResponse
                .getResult()
                .getOutput()
                .getText();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/recommend")
    public String recommend(@RequestParam String type, @RequestParam String year, @RequestParam String lang) {
        String tempt = """
                    I want to watch a {type} movie tonight with good rating,
                    looking for movies around this year {year}.
                    The language I'm looking for is {lang}.
                    Suggest one specific movie and tell me the cast and length of the movie.
                    
                    response format should be :
                    1. Movie name
                    2. basic plot
                    3. cast
                    4. length
                    5. IMDB rating
                """;

        PromptTemplate promptTemplate = new PromptTemplate(tempt);
        Prompt prompt = promptTemplate.create(Map.of("type", type, "year", year, "lang", lang));

        String response = chatClient
                .prompt(prompt)
//                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, "amit-default-session"))
                .call()
                .content();
        return response;
    }

    // ==========================================
    // Embedding & Vector Search Endpoints
    // ==========================================

    @PostMapping("/api/embedding")
    public float[] embeddings(@RequestParam String text) {
//        return embeddingModel.embed(text);
        EmbeddingRequest request = new EmbeddingRequest(
                List.of(text),
                GoogleGenAiTextEmbeddingOptions.builder()
                        .dimensions(2)
                        .build()
        );

        EmbeddingResponse response = embeddingModel.call(request);

        return response.getResult().getOutput();
    }

    @PostMapping("/api/similarity")
    public double getSimilarity(@RequestParam String text1, @RequestParam String text2) {
        float[] embedding1 = embeddingModel.embed(text1);
        float[] embedding2 = embeddingModel.embed(text2);

        double dotProduct = 0;
        double norm1 = 0;
        double norm2 = 0;

        for (int i = 0; i < embedding1.length; i++) {
            dotProduct += embedding1[i] * embedding2[i];
            norm1 += Math.pow(embedding1[i], 2);
            norm2 += Math.pow(embedding2[i], 2);
        }

        return dotProduct * 100 / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    @PostMapping("/api/product")
    public List<Document> getProducts(@RequestParam String text) {
//        return vectorStore.similaritySearch(text);
        return vectorStore.similaritySearch(SearchRequest.builder().query(text).topK(2).build());
    }

    @PostMapping("/api/ask")
    public String getAnswerUsingRag(@RequestParam String query) {
        return chatClient
                .prompt(query)
//                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, "amit-default-session"))
                .advisors(QuestionAnswerAdvisor.builder(vectorStore).build())
                .call()
                .content();
    }

    // ==========================================
    // Image Generation & Multimodal Vision
    // ==========================================

//    @GetMapping("/api/image/{query}")
//    public String genImage(@PathVariable String query){
//        ImagePrompt prompt = new ImagePrompt(query);
//        ImageResponse response = googleGenAiImageModel.call(prompt);
//        return response.getResult().getOutput().getUrl();
//    }

    @GetMapping("/api/imagegen")
    public String genImage(@RequestParam(name = "query") String query) {
        ImagePrompt prompt = new ImagePrompt(query, ImageOptionsBuilder.builder()
                .height(1024)
                .width(1024)
                .style("natural")
                .build());
        ImageResponse response = imageModel.call(prompt);

        return response.getResult().getOutput().getUrl();
    }

    @PostMapping("/api/imagedes")
    public String descimage(@RequestParam String query, @RequestParam MultipartFile file) {
        // 1. Resolve MIME type from file or fallback
        MimeType mimeType = MediaTypeFactory.getMediaType(file.getOriginalFilename())
                .map(mediaType -> (MimeType) mediaType)
                .orElseGet(() -> {
                    String ct = file.getContentType();
                    return (ct != null && !ct.equals(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                            ? MimeTypeUtils.parseMimeType(ct)
                            : MimeTypeUtils.IMAGE_JPEG;
                });

        // 2. Pass to Gemini WITHOUT the chat memory advisor
        return chatClient.prompt()
                .user(u -> u.text(query)
                        .media(new Media(mimeType, file.getResource())))
                .call()
                .content();
    }
}