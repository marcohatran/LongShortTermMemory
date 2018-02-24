/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.sentiment.doc2vec;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Phu
 */
public class SaveAndLoadDoc2Vec {

    private static Document2Vector doc = new Document2Vector();

    public static void saveModel() {
        FileOutputStream fos = null;
        try {
            doc.trainModel();
            fos = new FileOutputStream("C:\\Users\\Phu\\Documents\\BookStoreManagement\\LongShortTermMemory\\src\\resources\\doc2vec.model");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(doc);
            oos.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SaveAndLoadDoc2Vec.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SaveAndLoadDoc2Vec.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fos.close();
            } catch (IOException ex) {
                Logger.getLogger(SaveAndLoadDoc2Vec.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static Document2Vector loadModel() {
        Document2Vector doc = new Document2Vector();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream("C:\\Users\\Phu\\Documents\\BookStoreManagement\\LongShortTermMemory\\src\\resources\\doc2vec.model");
            ObjectInputStream ois = new ObjectInputStream(fis);
            doc = (Document2Vector) ois.readObject();
            ois.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SaveAndLoadDoc2Vec.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SaveAndLoadDoc2Vec.class.getName()).log(Level.SEVERE, null, ex);
        }  catch (ClassNotFoundException ex) {
            Logger.getLogger(SaveAndLoadDoc2Vec.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fis.close();
            } catch (IOException ex) {
                Logger.getLogger(SaveAndLoadDoc2Vec.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return doc;
    }

    public static void main(String[] args) {
        Document2Vector doc = new Document2Vector();
        
        doc = loadModel();
        System.out.println(doc.getDoc2Vector("cô giáo"));
        
        
    }

}
