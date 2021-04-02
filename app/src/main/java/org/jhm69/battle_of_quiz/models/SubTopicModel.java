package org.jhm69.battle_of_quiz.models;

import java.io.Serializable;

public class SubTopicModel implements Serializable {
    public String name;
    public String id;

    public SubTopicModel(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public SubTopicModel() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
