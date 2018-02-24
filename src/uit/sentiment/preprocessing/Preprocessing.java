/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.sentiment.preprocessing;

/**
 *
 * @author Phu
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import vn.hus.nlp.tokenizer.VietTokenizer;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Phu
 */
public class Preprocessing {

    private static List<String> listStopwords = loadStopwords();
    private static VietTokenizer tokenizer = new VietTokenizer();

    public static boolean checkAccent(String str) {
        str = str.toLowerCase();

        char[] chr = {'à', 'á', 'ả', 'ã', 'ạ', 'è', 'é', 'ẻ', 'ẽ', 'ẹ', 'â' , 'ă', 'ấ', 'ầ', 'ẩ', 'ẫ', 'ậ', 'ằ', 'ắ', 'ẳ', 'ẵ', 'ậ', 'ê', 'ề', 'ế', 'ể', 'ễ', 'ệ', 'ò', 'ó', 'ỏ', 'õ', 'ọ', 'ồ', 'ô', 'ố', 'ổ', 'ỗ', 'ộ', 'ơ', 'ờ', 'ớ', 'ở', 'ỡ', 'ợ', 'ù', 'ú', 'ủ', 'ũ', 'ụ', 'ư', 'ừ', 'ứ', 'ử', 'ữ', 'ự', 'ỳ', 'ý', 'ỷ', 'ỹ', 'ỵ', 'đ', 'í', 'ì', 'ỉ', 'ĩ', 'ị'};

        for (int i = 0; i < str.length(); i++) {
            for (int j = 0; j < chr.length; j++) {
                if (str.charAt(i) == chr[j]) {
                    return true;
                }
            }
        }
        System.out.println(str);
        return false;
    }

    public static String preProcess(String str, String regEx) {
        //str.toLowerCase().replaceAll("\\.|,|\\?|!|\"|'|;|:|\\(|\\)|\\[|\\]|-|_|\\d|\\s+", " ");
        return str.toLowerCase().replaceAll(regEx, " ").replaceAll("\\s+", " ").trim();
    }

    public static List<String> loadStopwords() {
        String fileIn = "stopwords.txt";
        List<String> stopwords = new ArrayList<String>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(fileIn), "UTF-8"));
            String line = "";
            while ((line = br.readLine()) != null) {
                stopwords.add(line);

            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Preprocessing.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Preprocessing.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(Preprocessing.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return stopwords;
    }

    public static String removeStopWord(String str) {
        StringBuilder builder = new StringBuilder();
        String[] arr = (str.toLowerCase().split("\\s"));
        List<String> listWord = new ArrayList<String>();
        for (String s : arr) {
            listWord.add(s);
        }
        listWord.removeAll(listStopwords);
        for (String build : listWord) {
            builder.append(build + " ");
        }
        return builder.toString().trim();
    }

    public static List<List<String>> createData(String path) {
        List<List<String>> data = new ArrayList<List<String>>();
        File src = new File(path);
        XSSFWorkbook wb = null;
        try {
            FileInputStream fis = new FileInputStream(src);
            wb = new XSSFWorkbook(fis);

            XSSFSheet sheet1 = wb.getSheetAt(0);

            int rowCount = sheet1.getLastRowNum();
            for (int i = 0; i < rowCount; i++) {
                List<String> list = new ArrayList<String>();
                String sentence = sheet1.getRow(i).getCell(0).getStringCellValue();
                String sentLabel, topicLabel;
                try {
                    sentLabel = (int) sheet1.getRow(i).getCell(4).getNumericCellValue() + "";
                    topicLabel = (int) sheet1.getRow(i).getCell(5).getNumericCellValue() + "";
                } catch (java.lang.IllegalStateException ex) {
                    sentLabel = (int) Double.parseDouble(sheet1.getRow(i).getCell(4).getStringCellValue()) +"";
                    topicLabel = (int) Double.parseDouble(sheet1.getRow(i).getCell(5).getStringCellValue()) + "";

                }

                // preprocess --> tokenize --> preprocess
                if (checkAccent(sentence)) {

                    sentence = preProcess(sentence, "\\d|\\s+");
                    //tokenize
                    sentence = tokenizer.tokenize(sentence)[0];
                    // remove stopword
                    sentence = removeStopWord(sentence);
                    sentence = preProcess(sentence, "\\.|,|\\?|!|\"|'|;|:|\\(|\\)|\\[|\\]|-|wzjwz|%|franction|:v|:3|:p");
                    list.add(sentence);
                    list.add(sentLabel);
                    list.add(topicLabel);
                    data.add(list);
                }

            }

        } catch (FileNotFoundException ex) {
            System.out.println("Không tìm thấy file");
        } catch (IOException ex) {
            System.out.println("Lỗi");
        } finally {
            try {
                wb.close();
            } catch (IOException ex) {
                System.out.println("Lỗi");
            }
        }
        return data;

    }

    public static void exportData() {
        String FILE_NAME = "data.xlsx";
        List<List<String>> data = createData("corpus_full.xlsx");
        String[][] array = new String[data.size()][];
        for (int i = 0; i < data.size(); i++) {
            List<String> row = data.get(i);
            array[i] = row.toArray(new String[row.size()]);
        }
        System.out.println(array.length);
        System.out.println(array[2].length);
        
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Datatypes in Java");
        
        int rowNum = 0;
        System.out.println("Creating excel");

        for (Object[] datatype : array) {
            XSSFRow row = sheet.createRow(rowNum++);
            int colNum = 0;
            for (Object field : datatype) {
                XSSFCell cell = row.createCell(colNum++);
                if (field instanceof String) {
                    cell.setCellValue((String) field);
                } else if (field instanceof Integer) {
                    cell.setCellValue((Integer) field);
                }
            }
        }

        try {
            FileOutputStream outputStream = new FileOutputStream(FILE_NAME);
            workbook.write(outputStream);
            workbook.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("done");
        
    }

    public static void main(String[] args) {
        //createData("corpus_full.xlsx");
        exportData();
    }

}
