package com.fr3nzy.SpringAICode;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.embedding.EmbeddingModel;
//import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class AIController {

    private ChatClient chatClient;

    @Autowired
//    @Qualifier("openAiEmbeddingModel")
    private EmbeddingModel embeddingModel;

//    public AIController(OpenAiChatModel chatModel){
//        this.chatClient = ChatClient.create(chatModel);
//    }

    // Only use when dealing with only one model
//    public AIController(ChatClient.Builder builder){
//        this.chatClient = builder.build();
//    }

    ChatMemory chatMemory = MessageWindowChatMemory
            .builder()
            .build();

    public AIController(ChatClient.Builder builder, EmbeddingModel embeddingModel){
        this.chatClient = builder
                .defaultAdvisors(MessageChatMemoryAdvisor
                        .builder(chatMemory)
                        .build())
                .build();
        this.embeddingModel = embeddingModel;
    }

    @GetMapping("/api/{message}")
    public ResponseEntity<String> getAnswer(@PathVariable String message){

        ChatResponse chatResponse = chatClient
                .prompt(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, "amit-default-session"))
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
    public String recommend(@RequestParam String type, @RequestParam String year, @RequestParam String lang){

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
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, "amit-default-session"))
                .call()
                .content();
        return response;
    }

    @PostMapping("/api/embedding")
    public float[] embeddings(@RequestParam String text){
        return embeddingModel.embed(text);
    }
}
