package uit.sentiment.doc2vec;

import org.datavec.api.util.ClassPathResource;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.wordstore.inmemory.AbstractCache;
import org.deeplearning4j.text.documentiterator.LabelsSource;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

/**
 * This is example code for dl4j ParagraphVectors implementation. In this
 * example we build distributed representation of all sentences present in
 * training corpus. However, you still use it for training on labelled
 * documents, using sets of LabelledDocument and LabelAwareIterator
 * implementation.
 *
 * *************************************************************************************************
 * PLEASE NOTE: THIS EXAMPLE REQUIRES DL4J/ND4J VERSIONS >= rc3.8 TO COMPILE
 * SUCCESSFULLY
 * *************************************************************************************************
 *
 * @author raver119@gmail.com
 */
public class Document2Vector implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(Document2Vector.class);
    private static ParagraphVectors vec;
    
    public Document2Vector(){
        trainModel();
    }

    public static void trainModel() {
        ClassPathResource resource = new ClassPathResource("/resources/data.txt");
        SentenceIterator iter = null;
        try {
            File file = resource.getFile();
            iter = new BasicLineIterator(file);
        } catch (Exception e) {
            log.info("Error getting file");
        }
        AbstractCache<VocabWord> cache = new AbstractCache<>();
        TokenizerFactory t = new DefaultTokenizerFactory();
        t.setTokenPreProcessor(new CommonPreprocessor());
        LabelsSource source = new LabelsSource("DOC_");
        vec = new ParagraphVectors.Builder()
                .minWordFrequency(1)
                .iterations(5)
                .epochs(1)
                .layerSize(100)
                .learningRate(0.025)
                .labelsSource(source)
                .windowSize(5)
                .iterate(iter)
                .trainWordVectors(false)
                .vocabCache(cache)
                .tokenizerFactory(t)
                .sampling(0)
                .build();

        vec.fit();
    }

    public INDArray getDoc2Vector(String str) {
        return vec.inferVector(str);
    }

    public static void serializeDoc2Vec(Document2Vector doc) {
        doc.trainModel();
        FileOutputStream fout = null;
        ObjectOutputStream oos = null;
        File outputModel = new File("C:\\Users\\Phu\\Documents\\BookStoreManagement\\LongShortTermMemory\\src\\resources\\doc2vec.model");

        try {
            outputModel.createNewFile();
            fout = new FileOutputStream(outputModel, false);
            oos = new ObjectOutputStream(fout);
            oos.writeObject(doc);

            System.out.println("Done");

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException e) {
                    log.error("File not found!");
                }
            }
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public static Document2Vector deserilizeDoc2Vec(String fileName) {
        Document2Vector doc = null;

        FileInputStream fin = null;
        ObjectInputStream ois = null;

        try {

            fin = new FileInputStream(fileName);
            ois = new ObjectInputStream(fin);
            doc = (Document2Vector) ois.readObject();

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {

            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return doc;
    }
    

    public static void main(String[] args) throws Exception {
        //Document2Vector doc2Vec = deserilizeDoc2Vec("C:\\Users\\Phu\\Documents\\BookStoreManagement\\LongShortTermMemory\\src\\resources\\doc2vec.model");

        Document2Vector doc2Vec = new Document2Vector();
        doc2Vec.trainModel();
        
        System.out.println(doc2Vec.getDoc2Vector("tôi không ý_kiến"));
        System.out.println(doc2Vec.getDoc2Vector("cám_ơn thầy"));
        System.out.println(doc2Vec.getDoc2Vector("kiểm_tra bằng web"));
        System.out.println(doc2Vec.getDoc2Vector("kiến_thức ngữ_pháp viết đạt"));
        System.out.println(doc2Vec.getDoc2Vector("không hiểu bài lắm"));
        System.out.println(doc2Vec.getDoc2Vector("mọi thứ tốt"));
        System.out.println(doc2Vec.getDoc2Vector("hài_lòng"));
        System.out.println(doc2Vec.getDoc2Vector("học khó hiểu"));
        System.out.println(doc2Vec.getDoc2Vector("lớp không hướng_dẫn thực_hành"));
        System.out.println(doc2Vec.getDoc2Vector("giọng nói"));
       
       
        
        
        
        
        
        //serializeDoc2Vec(doc2Vec);

    }
}
