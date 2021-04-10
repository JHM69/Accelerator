package org.jhm69.battle_of_quiz.models;

import androidx.annotation.NonNull;

public class DocumentID {
    public String documentId;

    @SuppressWarnings("unchecked")
    public <T extends DocumentID> T withId(@NonNull final String id) {
        this.documentId = id;
        return (T) this;
    }


}
