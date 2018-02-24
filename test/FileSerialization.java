
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import uit.sentiment.doc2vec.Document2Vector;

public class FileSerialization {

    public static void main(String[] args) {

        try {
            Document2Vector mb = new Document2Vector();
            mb.trainModel();

            // write object to file
            

            // read object from file
            FileInputStream fis = new FileInputStream("mybean.ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            Document2Vector result = (Document2Vector) ois.readObject();
            ois.close();

            System.out.println("One:" + result.getDoc2Vector("cố giáo"));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
