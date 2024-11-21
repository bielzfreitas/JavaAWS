package com.rocketseat.redirectUrlShortner;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main implements RequestHandler<Map<String, Object>, Map<String, Object>> {
    private final S3Client s3Client = S3Client.builder().build(); //criando o S3
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        String pathParameters = (String) input.get("rawPath"); //path cru, para pegar somente o UUID da url
        String shortUrlCode = pathParameters.replace("/", ""); //substituindo o barra pelo vazio

        //se não estiver na requisição
        if(shortUrlCode == null || shortUrlCode.isEmpty()) {
            throw new IllegalArgumentException("Invalid input: 'shortUrlCod' is required ");
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket("url-shortener-storage-skyline") //usar o seu bucket criado na AWS
                .key(shortUrlCode + ".json")
                .build();

        //criando variavel para fazer o strem (pegar pacotinhos, ir juntando para formar o objeto final e transformar em um arquivo)
        InputStream s3ObjectStream;

        try {
            s3ObjectStream = s3Client.getObject(getObjectRequest);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching URL data from S3: " + e.getMessage(), e);
        }

        //transformar o stream (json) em um tipo UrlData
        UrlData urlData;
        try {
            urlData = objectMapper.readValue(s3ObjectStream, UrlData.class);
        } catch (Exception e) {
            throw new RuntimeException("Error deserializing URL data: " + e.getMessage(), e);
        }

        //verificar se a url ainda é valida - se atingiu o limite tempo ou não
        long currentTimeInSeconds = System.currentTimeMillis() / 1000; //tem que transformar em segundos para comparar corretamente

        Map<String, Object> response = new HashMap<>();
        //se for vállida - cenario onde a URL expirou
        if(urlData.getExpirationTime() < currentTimeInSeconds) { //caso não for valida
            response.put("statusCode", 410); //code error
            response.put("body", "This URL has expired.");
            return response;
        }

        //cenario onde a URL ainda é valida
        response.put("statusCode", 302); //code de found
        Map<String, String> headers = new HashMap<>(); //obj do tipo header
        headers.put("Location", urlData.getOrignalUrl());
        response.put("headers", headers);

        return response;
    }
}
