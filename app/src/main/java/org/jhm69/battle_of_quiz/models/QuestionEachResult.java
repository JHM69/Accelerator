package org.jhm69.battle_of_quiz.models;public class QuestionEachResult {    Boolean leftSide;    Boolean rightSide;    Question question;    public QuestionEachResult(Boolean leftSide, Boolean rightSide, Question question) {        this.leftSide = leftSide;        this.rightSide = rightSide;        this.question = question;    }    public QuestionEachResult(Boolean leftSide, Question question) {        this.leftSide = leftSide;        this.question = question;    }    public Boolean getLeftSide() {        return leftSide;    }    public void setLeftSide(Boolean leftSide) {        this.leftSide = leftSide;    }    public Boolean getRightSide() {        return rightSide;    }    public void setRightSide(Boolean rightSide) {        this.rightSide = rightSide;    }    public Question getQuestion() {        return question;    }    public void setQuestion(Question question) {        this.question = question;    }}