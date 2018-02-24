/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.sentiment.test;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Phu
 */
public class Measures {
    private double accuracy;
    private List<Double> precision;
    private List<Double> recall;
    private List<Double> f1_score;

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public void display(){
        System.out.format("Accuracy : %.3f " , this.getAccuracy());
        System.out.println("");
        System.out.print(" Precision : ");
        for(Double x: precision){
            System.out.format("\t  %.3f \t", x);
        }
        System.out.println("");
        System.out.print(" Recall : ");
        for(Double x: recall){
            System.out.format("\t  %.3f \t", x);
        }
        System.out.println("");
        System.out.print(" F1 Score : ");
        for(Double x: f1_score){
            System.out.format("\t  %.3f \t", x);
        }
        System.out.println("");
    }
    public List<Double> avg(List<Double> l1, List<Double> l2, List<Double> l3, List<Double> l4, List<Double> l5){
        List<Double> list = new ArrayList<>();
        
        for(int i= 0; i < l1.size();i++){
            list.set(i, (l1.get(i) + l2.get(i) + l3.get(i) + l4.get(i) + l5.get(i)) /5);
        }
        return list;
    }
    public Measures computeAverage(Measures m1, Measures m2, Measures m3, Measures m4, Measures m5){
        Measures avg = new Measures();
        avg.setAccuracy(((m1.getAccuracy()) + (m2.getAccuracy()) + (m3.getAccuracy()) + (m4.getAccuracy()) + (m5.getAccuracy())) / 5);
        for(int i=0 ; i < 5; i++){
          avg.setPrecision(avg(m1.getPrecision(),m2.getPrecision(),m3.getPrecision(),m4.getPrecision(),m5.getPrecision()));
          avg.setRecall(avg(m1.getRecall(),m2.getRecall(),m3.getRecall(),m4.getRecall(),m5.getRecall()));
          avg.setF1_score(avg(m1.getF1_score(),m2.getF1_score(),m3.getF1_score(),m4.getF1_score(),m5.getF1_score()));     
        }
        return avg;
    }
    
    
    public void setPrecision(List<Double> precision) {
        this.precision = precision;
    }

    public void setRecall(List<Double> recall) {
        this.recall = recall;
    }

    public void setF1_score(List<Double> f1_score) {
        this.f1_score = f1_score;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public List<Double> getPrecision() {
        return precision;
    }

    public List<Double> getRecall() {
        return recall;
    }

    public List<Double> getF1_score() {
        return f1_score;
    }
    
}
