package com.fr3nzy.SpringAICode.controller;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.content.Media;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class AudioGenController {

    private final ChatClient chatClient;

    // Building a fresh ChatClient here keeps this controller stateless
    // and prevents the chat memory corruption issues we saw yesterday!
    public AudioGenController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @PostMapping("/api/stt")
    public String speechToText(@RequestParam MultipartFile file) {
        // 1. Resolve MIME type from the audio file (e.g., .mp3, .wav)
        MimeType mimeType = MediaTypeFactory.getMediaType(file.getOriginalFilename())
                .map(mediaType -> (MimeType) mediaType)
                .orElseGet(() -> {
                    String ct = file.getContentType();
                    return (ct != null && !ct.equals(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                            ? MimeTypeUtils.parseMimeType(ct)
                            : MimeTypeUtils.parseMimeType("audio/mpeg"); // default fallback
                });

        // 2. Pass the audio to Gemini for transcription
        return chatClient.prompt()
                .user(u -> u.text("Please transcribe this audio exactly as it is spoken. Do not add any extra commentary or conversational filler.")
                        .media(new Media(mimeType, file.getResource())))
                .call()
                .content();
    }
}
