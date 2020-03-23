package com.example.quiz;

import java.util.List;

public class QuestionModel {
    private String ques,optionA,optionB,optionC,optionD,correct;
    private List<String> options;

    public QuestionModel() {
        //For Firebase
    }

    public QuestionModel(String ques, String correct, List<String> options) {
        this.ques = ques;
        this.correct = correct;
        this.options = options;
        setOption(this.options);
    }

    public void setOption(List<String> options){
        for(int i = 0; i < options.size(); i++){
            if(i == 0){
                setOptionA(options.get(i));
            }else if(i == 1){
                setOptionB(options.get(i));
            }else if(i == 2){
                setOptionC(options.get(i));
            }else if(i == 3){
                setOptionD(options.get(i));
            }
        }
    }
//    public QuestionModel(String ques, String optionA, String optionB, String optionC, String optionD, String correctANS) {
//        this.ques = ques;
//        this.optionA = optionA;
//        this.optionB = optionB;
//        this.optionC = optionC;
//        this.optionD = optionD;
//        this.correctANS = correctANS;
//    }

    public String getQuestion() {
        return ques;
    }

    public void setQuestion(String question) {
        this.ques = question;
    }

    public String getOptionA() {
        return optionA;
    }

    public void setOptionA(String optionA) {
        this.optionA = optionA;
    }

    public String getOptionB() {
        return optionB;
    }

    public void setOptionB(String optionB) {
        this.optionB = optionB;
    }

    public String getOptionC() {
        return optionC;
    }

    public void setOptionC(String optionC) {
        this.optionC = optionC;
    }

    public String getOptionD() {
        return optionD;
    }

    public void setOptionD(String optionD) {
        this.optionD = optionD;
    }

    public String getcorrect() {
        return correct;
    }

    public void setcorrect(String correct) {
        this.correct = correct;
    }

    public int getNumberOfOptions(){
        int size;
        size = options.size();
        return size;
    }
}
