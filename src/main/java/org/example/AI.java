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

    public static String askGemini(String prompt) {
        try {
            // 1. Беремо ключ прямо з налаштувань Railway
            String apiKey = System.getenv("GEMINI_KEY");

            // Захист від забудькуватості: якщо ключа немає в Railway, бот скаже про це в Дискорді!
            if (apiKey == null || apiKey.isEmpty()) {
                return "Помилка: Ключ GEMINI_KEY не знайдено в налаштуваннях Railway! Додай його у вкладці Variables.";
            }

            // Точне і правильне посилання на найновішу швидку модель (без помилки 404)
            String url = "String url = \"https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=\" + apiKey;" + apiKey;

            // 2. Формуємо стиль
            String systemStyle = "Ти знаходишся на сервері, відповідай так само як до тебе. Якщо в повідомленні є мат," +
                    " можеш також використати, АЛЕ МАТ НЕ Є ОБОВ'ЯЗКОВИМ. Якщо мату в повідомленні немає мату можеш використати мат з шансом 1/5." +
                    " Відповідай виключно українською." + Arrays.toString(Frazochki) +
                    "Можеш використовувати ці фрази для генерації відповідей, якщо в повідомленні був мат";

            JSONObject styleText = new JSONObject();
            styleText.put("text", systemStyle);

            JSONObject styleParts = new JSONObject();
            styleParts.put("parts", new JSONArray().put(styleText));

            // 3. Формуємо повідомлення користувача
            JSONObject textPart = new JSONObject();
            textPart.put("text", prompt);

            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user"); // ДОДАНО: це лікує помилку 400 у багатьох випадках
            userMessage.put("parts", new JSONArray().put(textPart));

            // 4. Збираємо весь запит
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("system_instruction", styleParts);
            jsonBody.put("contents", new JSONArray().put(userMessage));

            // 5. Відправляємо
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody.toString(), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Якщо вилізе помилка - ми побачимо її текст у логах Railway!
            if (response.statusCode() != 200) {
                System.out.println("Gemini Помилка " + response.statusCode() + ": " + response.body());
                return "Помилка API! Код: " + response.statusCode() + ". Глянь логи Railway!";
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