package org.example;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel; // Нужно для работы с каналами
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent; // Событие входа
import net.dv8tion.jda.api.entities.Activity;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role; // Чтобы Java знала, что такое "Роль"

public class Main extends ListenerAdapter {
    // Это наш общий рубильник. false = выключено, true = работает
    public static boolean isSpamming = false;

    public static void main(String[] args) {
        String token = System.getenv("TOKEN");
        // Создаем бота и подключаем его
        JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS) // Разрешаем читать сообщения (ВАЖНО!)
                .addEventListeners(new Main()) // Подключаем "слушателя" (этот же класс)
                .setActivity(Activity.customStatus("Занятой"))
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .build();
    }

    // Этот метод срабатывает, когда бот видит сообщение
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (!event.isFromGuild()) return;

        // 3. НАСТРОЙКА: ID роли, которой разрешен доступ
        String requiredRoleId = "1445492460615761991"; // Вставь сюда скопированный ID

        // 4. Логика проверки
        // По умолчанию считаем, что доступа нет (false)
        boolean hasAccess = false;


        // Получаем все роли участника и перебираем их одну за другой
        for (Role role : event.getMember().getRoles()) {
            // Если ID текущей роли совпадает с нужным...
            if (role.getId().equals(requiredRoleId)) {
                hasAccess = true; // ...даем доступ
                break; // ...и останавливаем перебор (мы уже нашли что искали)
            }
        }

        String message = event.getMessage().getContentRaw();
        if (!message.startsWith("!") && !message.equalsIgnoreCase("прокоментуй") && !hasAccess) {
            return;
        }

        // 5. Если после проверки доступ так и не дали — выгоняем
        if (!hasAccess) {
            event.getMessage().reply("⛔ Пошёл нахуй еблан, у тебя нет прав командовать мной!").queue();
            return;
        }

        // --- Дальше твой код команд ---
        if (event.getAuthor().isBot()) return;
        String RusaID = "799277173369339934";
        // 2. Получаем текст

        if (message.startsWith("!мм ")) { // Обрати внимание на пробел после !ai
            // 1. Берем всё, что написано после команды
            String question = message.substring(4);

            // 2. Пишем "Печатает...", чтобы пользователь видел, что мы думаем
            event.getChannel().sendTyping().queue();

            // 3. Запускаем AI (Важно! Делаем это в отдельном потоке, чтобы бот не завис)
            // CompletableFuture позволяет делать дела в фоне
            java.util.concurrent.CompletableFuture.runAsync(() -> {

                // Спрашиваем у нашего нового класса
                String answer = AI.askGemini(question);

                // Ограничение Дискорда: сообщение не может быть длиннее 2000 символов
                if (answer.length() > 2000) {
                    answer = answer.substring(0, 1990) + "... (текст обрізано)";
                }

                // Отвечаем
                event.getMessage().reply(answer).queue();
            });
        }

        if (message.equals("Шо ти?")) {
            event.getMessage().reply("А тебя это ебать не должно. Пошёл нахуй <@" + RusaID +">").queue();
        }
        if (message.equals("Хто йде нахуй?")) {
            List<Member> members = event.getChannel().asTextChannel().getMembers();
            List<Member> realPeople = new ArrayList<>();
            for (Member member : members) {
                // Если это не бот — добавляем в наш список кандидатов
                if (!member.getUser().isBot()) {
                    realPeople.add(member);
                }
            }
            if (!realPeople.isEmpty()) {
                // 3. Выбираем случайного
                Random random = new Random();
                int index = random.nextInt(realPeople.size()); // Число от 0 до количества людей
                Member luckyMember = realPeople.get(index); // Получаем участника по этому номеру
                event.getMessage().reply("Нахуй идёт " + luckyMember.getAsMention()).queue();
            }
        }
        if (message.startsWith("!Виклик")) {
            if (!event.getMessage().getMentions().getUsers().isEmpty()) {
                User target = event.getMessage().getMentions().getUsers().get(0);
                User author = event.getAuthor();
                String ID = "621295677875552257";
                if (target.getId().equals(ID)) {
                    event.getMessage().reply("А йому спамити не можна, іді нахуй").queue();
                    target.openPrivateChannel().queue(dm -> {
                        dm.sendMessage("Оця хуйня на тебе спам кидала: " + author.getAsMention()).queue();
                    });
                    return;
                }
                // Включаем рубильник
                isSpamming = true;
                event.getMessage().reply("Почав виклик...").queue();

                target.openPrivateChannel().queue(dm -> {
                    // Запускаем функцию, которая будет слать по 1 сообщению
                    spamLoop(dm, target, author, 20);
                });
            }
        }
        if (message.equals("!Команди")) {
            event.getMessage().reply( "Команди:" +
                    "\n" + "Шо ти? - Посилає Русю нахуй, та дає тобі відповідь" +
                    "\n" + "!Виклик @юзер пише в лс обраному юзеру що ви його чекаєте" +
                    "\n" + "!Стоп зупиняє усі виклики для усіх користувачів" +
                    "\n" + "Хто йде нахуй? Рандомно обирає юзера який піде нахуй (Юзери тільки в рамках каналу)." +
                    "\n" + "!Прокоментуй + повідомлення на яке треба відповідь. Видає рандомну відповідь на повідомлення" +
                    "\n" + "!Гей @юзер Показує наскільки юзер є геєм" +
                    "\n" + "!Нацик @юзер те саме шо і з геєм" +
                    "\n" + "Пассивна робота: відправляю інструкції усім новачкам." +
                    "\n" + "Хто може мною скористатись? Усі хто має роль: Може юзати бота").queue();
        }
        // Перевіряємо, чи написав користувач "прокоментуй" (незалежно від регістру)
        if (message.equalsIgnoreCase("!прокоментуй")) {

            // 1. ПЕРЕВІРКА: Чи є це відповіддю на інше повідомлення?
            if (event.getMessage().getMessageReference() == null) {
                event.getMessage().reply("Не зроз шо коментить").queue();
                return;
            }

            // 2. Отримуємо повідомлення, на яке відповіли
            Message referencedMessage = event.getMessage().getReferencedMessage();

            // Іноді повідомлення може бути null (якщо воно занадто старе або видалене), перевіримо це
            if (referencedMessage == null) {
                event.getMessage().reply("Не зроз шо коментить").queue();
                return;
            }

            // 3. (Опціонально) Можемо отримати текст того повідомлення
            // String textToComment = referencedMessage.getContentRaw();
            // Це знадобиться, якщо ти захочеш аналізувати саме слова, але поки зробимо рандом.

            // 4. Список варіантів відповідей бота
            List<String> reactions = Arrays.asList(
                    "Єбать дрисня брат",
                    "Ммм.... хуйня",
                    "Норм",
                    "Такє собі",
                    "Не хочу",
                    "Нет, іді нахуй",
                    "🤡🤡🤡🤡",
                    "База",
                    "<@" + RusaID +"> йде нахуй"
            );

            // 5. Вибираємо випадкову відповідь
            Random random = new Random();
            String randomReaction = reactions.get(random.nextInt(reactions.size()));

            // 6. Відповідаємо САМЕ НА ТЕ повідомлення, яке виділив користувач
            referencedMessage.reply(randomReaction).queue();
        }
        if (message.startsWith("!Гей")) {
            double randN1 = (Math.random() * 100);
            long randN = Math.round(randN1);
            // Проверяем, упомянул ли кто-то кого-то
            if (!event.getMessage().getMentions().getUsers().isEmpty()) {
                // Берем первого упомянутого человека
                User target = event.getMessage().getMentions().getUsers().get(0);
                    event.getMessage().reply(target.getAsMention() + " гей на " + randN +"%").queue();
            }
        }
        if (message.startsWith("!Нацик")) {
            double randN1 = (Math.random() * 100);
            long randN = Math.round(randN1);
            // Проверяем, упомянул ли кто-то кого-то
            if (!event.getMessage().getMentions().getUsers().isEmpty()) {
                // Берем первого упомянутого человека
                User target = event.getMessage().getMentions().getUsers().get(0);
                event.getMessage().reply(target.getAsMention() + " Нацик на " + randN +"%").queue();
            }
        }
        if (message.equals("!Стоп")) {
            isSpamming = false; // Опускаем рубильник вниз
            event.getMessage().reply("Зупиняюсь! 🛑").queue();
        }
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        String ChannelId = "1409600926645489818";
        // 1. Ищем системный канал (тот, куда Дискорд сам пишет "User joined")
        TextChannel channel = event.getGuild().getTextChannelById(ChannelId);
        // Если такой канал настроен на сервере, пишем туда
        if (channel != null) {
            // getAsMention() превращает имя в синюю ссылку @User
            String AdminId = "621295677875552257";

            String welcomeMessage = "О, а ты кто такой, " +event.getMember().getAsMention() + "? Следуя правилам сервера отправьте свои паспортные данные сюда :point_right:<@" + AdminId +">";

            channel.sendMessage(welcomeMessage).queueAfter(1, TimeUnit.SECONDS);
        } else {
            // Если системного канала нет, можно выводить в консоль, что мы не нашли куда писать
            System.out.println("Не могу найти канал для приветствия!");
        }
    }

    public void spamLoop(PrivateChannel dm, User target, User author, int count) {
        // 1. Если счетчик кончился ИЛИ рубильник выключили (!isSpamming)
        if (count <= 0 || !isSpamming) {
            return; // Просто выходим, ничего не отправляя
        }

        // 2. Отправляем сообщение
        dm.sendMessage("Вас викликає " + author.getAsMention())
                .queueAfter(2, TimeUnit.SECONDS, success -> {
                    // 3. Через 2 секунды запускаем эту же функцию снова, но count - 1
                    spamLoop(dm, target, author, count - 1);
                });
    }
}