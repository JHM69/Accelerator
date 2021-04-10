package org.jhm69.battle_of_quiz.notification;

import org.jhm69.battle_of_quiz.models.Notification;

public class NotificationSender {
    public Notification data;
    public String to;

    public NotificationSender(Notification data, String to) {
        this.data = data;
        this.to = to;
    }

    public NotificationSender() {
    }
}
