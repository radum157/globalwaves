package fileio.commands.utils.formats;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public final class NotificationFormat {
    private final String name;
    private final String description;

    public NotificationFormat(final String notification) {
        /* First twp words set the name */
        String[] words = notification.split("\\s+");
        name = words[0] + " " + words[1];
        description = notification;
    }

    /**
     * Formats a given list
     *
     * @param notifications the string list
     * @return The formatted list
     */
    public static List<NotificationFormat> formatList(final List<String> notifications) {
        List<NotificationFormat> formattedList = new ArrayList<>();
        notifications.forEach(notification ->
                formattedList.add(new NotificationFormat(notification))
        );

        return formattedList;
    }
}
