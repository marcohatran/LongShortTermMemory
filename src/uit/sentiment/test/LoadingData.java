/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.sentiment.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import uit.sentiment.doc2vec.Document2Vector;
import uit.sentiment.doc2vec.Sentence2Vector;
import static uit.sentiment.doc2vec.Sentence2Vector.sentences2Vector;
import uit.sentiment.lstm.LSTM;

/**
 *
 * @author Phu
 */
public class LoadingData {

    private static Document2Vector doc = new Document2Vector();
    private static int numFolds = 5;
    private static int numClasses = 4;
    private static int numSentences = 10000;
    private static int numTrainSentences = 8000;
    private static String pathSentences = "data.txt";
    private static String pathLabel = "label.txt";

    public static List<INDArray> getVectorList(int start, int end) {
        List<INDArray> listVector = new ArrayList<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(pathSentences));
            int i = 1;
            String line = " ";
            while (line != null) {
                line = br.readLine();
                if (i >= start && i <= end) {
                    listVector.add(doc.getDoc2Vector(line));
                    System.out.println(doc.getDoc2Vector(line));
                }
                if (i == end) {
                    break;
                }
                i++;
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return listVector;
    }

    public static List<double[]> getVectorLabel(int start, int end) {
        List<double[]> listVector = new ArrayList<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("label.txt"));
            int i = 1;
            String line = " ";
            while (line != null) {
                line = br.readLine();
                if (i >= start && i <= end) {
                    double[] out = new double[4];
                    int index = Integer.parseInt(line);
                    out[index] = 1;
                    listVector.add(out);
                }
                if (i == end) {
                    break;
                }
                i++;
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return listVector;
    }

    public static List<INDArray> getSentencesVector(int start, int end) {
        List<INDArray> arr = new ArrayList<>();

        File src = new File("data.xlsx");
        XSSFWorkbook wb = null;
        BufferedWriter data = null;
        BufferedWriter label = null;
        try {
            FileInputStream fis = new FileInputStream(src);
            wb = new XSSFWorkbook(fis);
            XSSFSheet sheet1 = wb.getSheetAt(0);
            int rowCount = sheet1.getLastRowNum();

            for (int i = 0; i < rowCount + 1; i++) {

                String sentence = sheet1.getRow(i).getCell(0).getStringCellValue();
                int topLable = Integer.parseInt(sheet1.getRow(i).getCell(2).getStringCellValue());

                if ((i >= start) && (i <= end) && (sentence.length() != 0)) {
                    INDArray array = doc.getDoc2Vector(sentence);
                    arr.add(array);
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
        System.out.println("Done~~");

        return arr;
    }

    public static List<double[]> getSentencesLabel(int start, int end) {
        List<double[]> arr = new ArrayList<>();

        File src = new File("data.xlsx");
        XSSFWorkbook wb = null;
        BufferedWriter data = null;
        BufferedWriter label = null;
        try {
            FileInputStream fis = new FileInputStream(src);
            wb = new XSSFWorkbook(fis);

            XSSFSheet sheet1 = wb.getSheetAt(0);

            int rowCount = sheet1.getLastRowNum();

            for (int i = 0; i < rowCount + 1; i++) {
                String sentence = sheet1.getRow(i).getCell(0).getStringCellValue();
                int topLable = Integer.parseInt(sheet1.getRow(i).getCell(2).getStringCellValue());

                if ((i >= start) && (i <= end) && (sentence.length() != 0)) {
                    double[] temp = new double[4];
                    temp[topLable] += 1;
                    for (int j = 0; j < 4; j++) {
                        System.out.print(temp[j] + "\t");
                    }
                    System.out.println("");
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
        System.out.println("Done~~");

        return arr;
    }

//    public static ArrayList<Integer> getLabelList(int start, int end) {
//        List<Integer> labelList = new ArrayList<>();
//        BufferedReader br = null;
//        try {
//            br = new BufferedReader(new FileReader(pathLabel));
//            int i = 1;
//            String line = " ";
//            while (line != null) {
//                line = br.readLine();
//                if (i >= start && i <= end) {
//                    labelList.add(Integer.parseInt(line));
//                    System.out.println(Integer.parseInt(line));
//                }
//                if (i == end) {
//                    break;
//                }
//                i++;
//            }
//        } catch (FileNotFoundException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//        return labelList;
//    }
    public static Measures compute(int[][] confMatrix) {
        Measures mea = new Measures();
        int totalRightAnswer = 0;
        int totalAnswer = 0;
        int[] rows = new int[confMatrix.length];
        int[] cols = new int[confMatrix[0].length];
        int tempRows = 0, tempCols = 0;
        for (int i = 0; i < confMatrix.length; i++) {
            for (int j = 0; j < confMatrix[i].length; j++) {
                totalAnswer += confMatrix[i][j];
                tempRows += confMatrix[i][j];
                tempCols += confMatrix[j][i];
            }
            rows[i] = tempRows;
            tempRows = 0;
            cols[i] = tempCols;
            tempCols = 0;

        }

        List<Double> listPre = new ArrayList<>();
        List<Double> listRec = new ArrayList<>();
        List<Double> listF1 = new ArrayList<>();
        for (int i = 0; i < confMatrix.length; i++) {
            totalRightAnswer += confMatrix[i][i];
            double pre = (double) confMatrix[i][i] / (cols[i]);
            double rec = (double) confMatrix[i][i] / (rows[i]);
            double f1 = (2 * (pre * rec)) / (pre + rec);
            listPre.add(pre);
            listRec.add(rec);
            listF1.add(f1);
            System.out.println(f1);
        }
        mea.setAccuracy((double) totalRightAnswer / totalAnswer);
        mea.setPrecision(listPre);
        mea.setRecall(listRec);
        mea.setF1_score(listF1);
        return mea;
    }

    public static ArrayList<ArrayList<INDArray>> loadSentences(int start, int end) {
        ArrayList<ArrayList<INDArray>> arraySentences = new ArrayList<ArrayList<INDArray>>();
        File src = new File("data.xlsx");
        XSSFWorkbook wb = null;
        BufferedWriter data = null;
        BufferedWriter label = null;
        try {
            FileInputStream fis = new FileInputStream(src);
            wb = new XSSFWorkbook(fis);
            XSSFSheet sheet1 = wb.getSheetAt(0);
            int rowCount = sheet1.getLastRowNum();

            ArrayList<INDArray> arr;
            for (int i = start; i < end; i++) {
                String sentence = sheet1.getRow(i).getCell(0).getStringCellValue();
                int topLable = Integer.parseInt(sheet1.getRow(i).getCell(2).getStringCellValue());
                if (sentence.length() != 0) {
                    arr = sentences2Vector(sentence);
                    arraySentences.add(arr);
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
        return arraySentences;
    }

    public static ArrayList<Integer> loadLabels(int start, int end) {
        ArrayList<Integer> arrayLabels = new ArrayList<Integer>();
        File src = new File("data.xlsx");
        XSSFWorkbook wb = null;
        BufferedWriter data = null;
        BufferedWriter label = null;
        try {
            FileInputStream fis = new FileInputStream(src);
            wb = new XSSFWorkbook(fis);
            XSSFSheet sheet1 = wb.getSheetAt(0);
            int rowCount = sheet1.getLastRowNum();

            for (int i = start; i < end; i++) {
                String sentence = sheet1.getRow(i).getCell(0).getStringCellValue();
                int topLable = Integer.parseInt(sheet1.getRow(i).getCell(2).getStringCellValue());

                String[] temp = sentence.split(" ");

                if ((sentence.length() != 0)) {
                    arrayLabels.add(topLable);
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
        return arrayLabels;
    }

    public static void statictics(int start, int end) {
        ArrayList<Integer> b10 = new ArrayList<Integer>();
        ArrayList<Integer> f10t20 = new ArrayList<Integer>();
        ArrayList<Integer> f20t30 = new ArrayList<Integer>();
        ArrayList<Integer> b30 = new ArrayList<Integer>();

        //int[] lengthArr = new int[4];
        File src = new File("data.xlsx");
        XSSFWorkbook wb = null;
        BufferedWriter data = null;
        BufferedWriter label = null;
        try {
            FileInputStream fis = new FileInputStream(src);
            wb = new XSSFWorkbook(fis);
            XSSFSheet sheet1 = wb.getSheetAt(0);
            int rowCount = sheet1.getLastRowNum();

            for (int i = start; i < end; i++) {
                String sentence = sheet1.getRow(i).getCell(0).getStringCellValue();
                int topLable = Integer.parseInt(sheet1.getRow(i).getCell(2).getStringCellValue());

                int lenght = sentence.split(" ").length;

                if ((sentence.length() != 0)) {
                    if (lenght <= 10) {
                       b10.add(lenght);
                    }
                    if(lenght >10 && lenght < 20)
                        f10t20.add(lenght);
                    if(lenght >=20 && lenght < 30)
                        f20t30.add(lenght);
                    if(lenght > 30)
                        b30.add(lenght);
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
        System.out.println(b10.size());
        System.out.println(f10t20.size());
        System.out.println(f20t30.size());
        System.out.println(b30.size());
    }

    public static void main(String[] args) {
        statictics(0,16150);
    }

}
