/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.sentiment.doc2vec;

import java.util.ArrayList;
import java.util.List;
import org.nd4j.linalg.api.ndarray.INDArray;

/**
 *
 * @author Phu
 */
public class Sentence2Vector {
    private static Word2Vector vec = new Word2Vector();
    public static ArrayList<INDArray> sentences2Vector(String sentence){
        ArrayList<INDArray> listArr = new ArrayList<>();
        String []words = sentence.split(" ");
        for(String word : words){
            INDArray arr = vec.getWordVector(word);
            if(arr == null){
                System.out.println(word);
            }
            listArr.add(arr);
        }
        return listArr; 
    }
    public static void main(String[] args) {
       
       List<INDArray> arr = sentences2Vector("đi đúng giờ giảng bài đầy_đủ");
       for(INDArray ar : arr){
           System.out.println(ar);
       }
    }
}
