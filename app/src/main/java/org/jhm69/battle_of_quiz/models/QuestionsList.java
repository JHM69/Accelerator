package org.jhm69.battle_of_quiz.models;import java.util.List;public class QuestionsList {    List<Question> questionList;    public QuestionsList(List<Question> questionList) {        this.questionList = questionList;    }    public List<Question> getQuestionList() {        return questionList;    }    public void setQuestionList(List<Question> questionList) {        this.questionList = questionList;    }}