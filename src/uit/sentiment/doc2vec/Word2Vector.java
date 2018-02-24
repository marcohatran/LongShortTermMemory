/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.sentiment.doc2vec;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.datavec.api.util.ClassPathResource;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;

/**
 *
 * @author Phu
 */
public class Word2Vector {
    private static String path = "resources/data.txt"; 
    private static Word2Vec vec;
    
    public Word2Vector(){
        train();
    }
    
    private void train(){
        SentenceIterator iter = null;
        try {
            String filePath = new ClassPathResource(path).getFile().getAbsolutePath();
            iter = new BasicLineIterator(filePath);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Word2Vector.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Split on white spaces in the line to get words
        TokenizerFactory t = new DefaultTokenizerFactory();

        /*
            CommonPreprocessor will apply the following regex to each token: [\d\.:,"'\(\)\[\]|/?!;]+
            So, effectively all numbers, punctuation symbols and some special symbols are stripped off.
            Additionally it forces lower case for all tokens.
         */
        t.setTokenPreProcessor(new CommonPreprocessor());

        vec = new Word2Vec.Builder()
                .minWordFrequency(5)
                .iterations(1)
                .layerSize(100)
                .seed(42)
                .windowSize(5)
                .iterate(iter)
                .tokenizerFactory(t)
                .build();

        vec.fit();
    }
    
    public static INDArray getWordVector(String word){
        return vec.getWordVectorMatrix(word);
    }

    public static void main(String[] args) throws Exception {
        Word2Vector vec = new Word2Vector();
        System.out.println(vec.getWordVector("băm"));
    }
}
