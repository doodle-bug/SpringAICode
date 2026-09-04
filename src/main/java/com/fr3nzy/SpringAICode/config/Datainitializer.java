package com.fr3nzy.SpringAICode.config;


import jakarta.annotation.PostConstruct;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Datainitializer {

    @Autowired
    private VectorStore vectorStore;

    @PostConstruct
    public void initData(){
        TextReader textReader = new TextReader(new ClassPathResource("product_details.txt"));
        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(100)
                .withMinChunkSizeChars(30)
                .withMinChunkLengthToEmbed(5)
                .withMaxNumChunks(500)
                .withKeepSeparator(false)
                .build();
        List<Document> documents = splitter.split(textReader.get());
        vectorStore.add(documents);
    }
}
