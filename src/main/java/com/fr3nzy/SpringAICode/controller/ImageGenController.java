//package com.fr3nzy.SpringAICode.controller;
//
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.ai.google.genai.GoogleGenAiChatModel;
//import org.springframework.ai.google.genai.image.GoogleGenAiImageModel;
//import org.springframework.ai.image.ImagePrompt;
//import org.springframework.ai.image.ImageResponse;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//public class ImageGenController {
//    private ChatClient chatClient;
//    private GoogleGenAiImageModel googleGenAiImageModel;
//
//    public ImageGenController(GoogleGenAiImageModel googleGenAiImageModel, GoogleGenAiChatModel chatModel){
//        this.googleGenAiImageModel = googleGenAiImageModel;
//        this.chatClient = ChatClient.create(chatModel);
//    }
//
//    @GetMapping("/api/image/{query}")
//    public String genImage(@PathVariable String query){
//
//        ImagePrompt prompt = new ImagePrompt(query);
//
//        ImageResponse response = googleGenAiImageModel.call(prompt);
//
//        return response.getResult().getOutput().getUrl();
//    }
//}

package com.fr3nzy.SpringAICode.controller;

import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImageOptionsBuilder;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ImageGenController {

    private final ImageModel imageModel;

    // Inject the generic ImageModel interface
    public ImageGenController(ImageModel imageModel) {
        this.imageModel = imageModel;
    }

    // Use @RequestParam to handle complex text prompts with spaces/special characters
    @GetMapping("/api/image")
    public String genImage(@RequestParam(name = "query") String query) {
        ImagePrompt prompt = new ImagePrompt(query, ImageOptionsBuilder.builder()
                .height(1024)
                .width(1024)// Usually required alongside height
                .style("natural")
                .build());
        ImageResponse response = imageModel.call(prompt);

        return response.getResult().getOutput().getUrl();
    }
}
