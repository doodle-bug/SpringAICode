package com.fr3nzy.SpringAICode.controller;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.content.Media;
import org.springframework.http.*;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
                .user(u -> u.text("Transcribe the attached audio and translate the final output into \" + french + \". Provide only the translated text without any extra commentary.")
                        .media(new Media(mimeType, file.getResource())))
                .call()
                .content();
    }

    @GetMapping("/api/tts")
    public ResponseEntity<byte[]> textToSpeech(@RequestParam String text) {
        try {
            // 1. Encode the text for the URL
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);

            // 2. Use a free TTS endpoint (Google's open TTS API)
            String url = "https://translate.google.com/translate_tts?ie=UTF-8&client=tw-ob&tl=en&q=" + encodedText;

            // 3. Fetch the audio file as a byte array using standard Spring RestTemplate
            RestTemplate restTemplate = new RestTemplate();
            byte[] audioBytes = restTemplate.getForObject(url, byte[].class);

            // 4. Return the bytes as a downloadable MP3 file
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("audio/mpeg"));
            headers.setContentDispositionFormData("attachment", "speech.mp3");

            return new ResponseEntity<>(audioBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
