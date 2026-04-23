package org.example;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GeminiTest {
    public static void main(String[] args) {
        String API_KEY = "AIzaSyDGIS8l9n84kpdVD5rmRhG8HY7D65RPW7Q"; // Встав свій ключ
        // Це інший URL - він покаже список усіх моделей
        String url = "https://generativelanguage.googleapis.com/v1beta/models?key=" + API_KEY;

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET() // Тут важливо GET
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Status: " + response.statusCode());
            System.out.println("Available Models:\n" + response.body());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
