package com.fr3nzy.SpringAICode.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.content.Media;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImageOptionsBuilder;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ImageGenController {

    private final ChatClient chatClient;
    private final ImageModel imageModel;

    public ImageGenController(ChatClient.Builder builder, ImageModel imageModel) {
        this.chatClient = builder.build();
        this.imageModel = imageModel;;
    }

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
