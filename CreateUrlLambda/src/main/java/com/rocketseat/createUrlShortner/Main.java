package com.rocketseat.createUrlShortner;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class Main implements RequestHandler<Map<String, Object>, Map<String, String>> {
    //add dependencia do ObjectMapper
    private final ObjectMapper objectMapper = new ObjectMapper();

    //add dependica do S3
    private final S3Client s3Client = S3Client.builder().build();

    @Override
    public Map<String, String> handleRequest(Map<String, Object> input, Context context) {
        //extraindo o body imbutido no input -> usando casthing (transformando em string)
        String body = input.get("body").toString();

        //mapeando para obter os valores
        //tipo chave valor
        Map<String, String> bodyMap;
        try{
            bodyMap = objectMapper.readValue(body, Map.class);
        } catch (JsonProcessingException exception) {
            //transforma a resposta do lambda num erro 500
            throw new RuntimeException("Error parsing JSON body: " + exception.getMessage(), exception);
        }

        //extraindo campos do body
        String originalUrl = bodyMap.get("originalUrl");
        String expirationTime = bodyMap.get("expirationTime");
        long expirationTimeInSeconds = Long.parseLong(expirationTime); //salvar como segundos (timestamp)

        //criando um UUID
        String shortUrlCode = UUID.randomUUID().toString().substring(0, 8);

        UrlData urlData = new UrlData(originalUrl, expirationTimeInSeconds);

        //criando conexão com o s3 e tentando salvar o obj JSON lá dentro
        try {
            String urlDataJson = objectMapper.writeValueAsString(urlData); //transformando em json
            //criando os objetos
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket("") //usar o seu bucket criado na AWS
                    .key(shortUrlCode + ".json")
                    .build();
            //usando o S3
            s3Client.putObject(request, RequestBody.fromString(urlDataJson));
        } catch (Exception exception) {
            throw new RuntimeException("Error saving data to S3: " + exception.getMessage(), exception); //indicando onde ocorreu o erro
        }

        Map<String, String> response = new HashMap<>();
        response.put("code", shortUrlCode);

        return response;
    }
}
