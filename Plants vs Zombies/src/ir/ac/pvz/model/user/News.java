package ir.ac.pvz.model.user;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class News {
    private LocalDateTime time;
    private String message;
    private NewsType type;
    private boolean isRead;

    public News(String message, NewsType type) {
        this.message = message;
        this.type = type;
        this.isRead = false;
        this.time = LocalDateTime.now();
    }

    //Getters

    public String getMessage() {
        return this.message;
    }

    public boolean isRead() {
        return isRead;
    }

    public NewsType getType() {
        return type;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public String getFormattedDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss yyyy");
        return time.format(formatter);
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
