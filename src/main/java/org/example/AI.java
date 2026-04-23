package org.example;

import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class AI {
    static String[] Frazochki = {"Нет, іді нахуй", "Хуесос ебаний", "Я твою маму вертел", "Пішов нахуй", "Я твій рот єбав"};
    private static final String API_KEY = System.getenv("TOKEN");
    private static final String URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + API_KEY;
    public static String askGemini(String prompt) {
        try {
            // --- ПОЧАТОК НОВОГО КОДУ ДЛЯ СТИЛЮ ---
            // 1. Придумай, як бот має поводитись
            String systemStyle = "Ти знаходишся на сервері, відповідай так само як до тебе. Якщо в повідомленні є мат," +
                    " можеш також використати, АЛЕ МАТ НЕ Є ОБОВ'ЯЗКОВИМ. Якщо мату в повідомленні немає мату можеш використати мат з шансом 1/5." +
                    " Відповідай виключно українською." + Arrays.toString(Frazochki) +
                    "Можеш використовувати ці фрази для генерації відповідей, якщо в повідомленні був мат";

            // 2. Запаковуємо цей стиль у JSON (так само, як і повідомлення)
            JSONObject styleText = new JSONObject();
            styleText.put("text", systemStyle);

            JSONObject styleParts = new JSONObject();
            styleParts.put("parts", new JSONArray().put(styleText));
            // --- КІНЕЦЬ НОВОГО КОДУ ---


            // 3. Формуємо повідомлення користувача (твій старий код)
            JSONObject textPart = new JSONObject();
            textPart.put("text", prompt);

            JSONObject userMessage = new JSONObject();
            userMessage.put("parts", new JSONArray().put(textPart));


            // 4. Збираємо все разом у головний jsonBody
            JSONObject jsonBody = new JSONObject();

            // ВАЖЛИВО: Додаємо інструкцію стилю
            jsonBody.put("system_instruction", styleParts);

            // Додаємо повідомлення користувача
            jsonBody.put("contents", new JSONArray().put(userMessage));


            // 5. Відправляємо запит (далі все як було)
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody.toString(), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // ... (твоя перевірка кодів і помилок залишається без змін) ...
            System.out.println("Response Code: " + response.statusCode());

            if (response.statusCode() != 200) {
                return "Помилка API! Код: " + response.statusCode();
            }

            JSONObject responseJson = new JSONObject(response.body());
            return responseJson.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text");

        } catch (Exception e) {
            e.printStackTrace();
            return "Ой, щось зламалося... (Помилка в консолі)";
        }
    }
}

