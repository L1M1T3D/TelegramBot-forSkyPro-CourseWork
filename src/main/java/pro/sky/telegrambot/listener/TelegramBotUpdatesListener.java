package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener, InitializingBean {

    private static final Pattern NOTIFICATION_PATTERN =
            Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})(\\s+)(.+)");

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final TelegramBot telegramBot;
    private final NotificationTaskRepository notificationTaskRepository;

    public TelegramBotUpdatesListener(TelegramBot telegramBot,
                                      NotificationTaskRepository notificationTaskRepository) {
        this.telegramBot = telegramBot;
        this.notificationTaskRepository = notificationTaskRepository;
    }

    @Override
    public void afterPropertiesSet() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        for (Update update : updates) {
            if (update.message() == null || update.message().text() == null) {
                continue;
            }

            String text = update.message().text();
            Long chatId = update.message().chat().id();

            if ("/start".equals(text)) {
                telegramBot.execute(new SendMessage(
                        chatId,
                        "Привет! Я бот-напоминатель. Отправь сообщение в формате: 01.01.2027 20:00 Сделать домашнюю работу"
                ));
                continue;
            }

            Matcher matcher = NOTIFICATION_PATTERN.matcher(text);

            if (matcher.matches()) {
                try {
                    LocalDateTime dateTime = LocalDateTime.parse(
                            matcher.group(1),
                            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                    );
                    String message = matcher.group(3);

                    NotificationTask notificationTask = new NotificationTask();
                    notificationTask.setChatId(chatId);
                    notificationTask.setMessage(message);
                    notificationTask.setDateTime(dateTime);
                    notificationTaskRepository.save(notificationTask);

                    telegramBot.execute(new SendMessage(
                            chatId,
                            "Напоминание сохранено на " + dateTime.format(DATE_TIME_FORMATTER)
                    ));
                } catch (DateTimeParseException exception) {
                    telegramBot.execute(new SendMessage(
                            chatId,
                            "Не удалось распознать дату. Используй формат: 01.01.2027 20:00 Текст напоминания"
                    ));
                }
            } else {
                telegramBot.execute(new SendMessage(
                        chatId,
                        "Используй формат: 01.01.2027 20:00 Текст напоминания"
                ));
            }
        }

        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
}
