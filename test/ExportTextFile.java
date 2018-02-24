/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author Phu
 */
public class ExportTextFile {

    /**
     * @param args the command line arguments
     */
    
    public static void exportFile(String path){
        File src = new File(path);
        XSSFWorkbook wb = null;
        BufferedWriter data = null;
        try {
            FileInputStream fis = new FileInputStream(src);
            wb = new XSSFWorkbook(fis);

           data  = new BufferedWriter(new FileWriter("label.txt"));
           
            XSSFSheet sheet1 = wb.getSheetAt(0);

            int rowCount = sheet1.getLastRowNum();
            int label = -1;
            for (int i = 0; i < rowCount; i++) {
                
                try{
                label = (int)sheet1.getRow(i).getCell(5).getNumericCellValue();
                } catch (IllegalStateException e ){
                label = Integer.parseInt(sheet1.getRow(i).getCell(5).getStringCellValue());
                }
                data.write(label + "\n");
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
            try {
                data.flush();
                data.close();
                
            } catch (IOException ex) {
                Logger.getLogger(ExportTextFile.class.getName()).log(Level.SEVERE, null, ex);
            }
           
        }
    }
    public static void main(String[] args) {
        exportFile("corpus_full.xlsx");
    }
}
