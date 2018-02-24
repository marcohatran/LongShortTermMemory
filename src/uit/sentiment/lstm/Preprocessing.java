/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.sentiment.lstm;

/**
 *
 * @author Phu
 */
public class Preprocessing {
    public static boolean checkAccent(String str){
        str = str.toLowerCase();    
        
        char []chr = {'à' , 'á' , 'ả' , 'ã' , 'ạ' , 'è' , 'é' , 'ẻ', 'ẽ' , 'ẹ' , 'ấ' , 'ầ' , 'ẩ' , 'ẫ' , 'ậ' , 'ằ' , 'ắ', 'ẳ' , 'ẵ' , 'ậ', 'ê' ,'ề' , 'ế' , 'ể', 'ễ' , 'ệ' , 'ò' , 'ó' , 'ỏ' , 'õ' , 'ọ', 'ồ' , 'ô' , 'ố' , 'ổ' , 'ỗ' , 'ộ' , 'ơ' , 'ờ' , 'ớ', 'ở', 'ỡ','ợ', 'ù', 'ú' , 'ủ' , 'ũ' , 'ụ', 'ư' , 'ừ', 'ứ', 'ử', 'ữ' , 'ự' , 'ỳ' , 'ý' , 'ỷ', 'ỹ', 'ỵ' , 'đ', 'í' , 'ì', 'ỉ', 'ĩ', 'ị'};
        
        for(int i =0; i < str.length(); i++){
            for(int  j = 0; j < chr.length; j++){
                if(str.charAt(i) == chr[j]){
                    return true;
                }
            }            
        }
        System.out.println(str);
        return false;
    }
    
    public static String preProcess(String str, String regex){
        if(checkAccent(str)){
            //str.toLowerCase().replaceAll("\\.|,|\\?|!|\"|'|;|:|\\(|\\)|\\[|\\]|-|_|\\d|\\s+", " ");
            //\\.|,|\\?|!|\"|'|;|:|\\(|\\)|\\[|\\]|-|_|
            // "\\d|\\s+"
            return str.toLowerCase().replaceAll(regex, " ").replaceAll("\\s+", " ").trim();
        }
        return null;
    }

    //Load dữ liệu --> tokenize --> ghi lại
    
    public static void exportData(){
        
    }
    
    
    public static void main(String[] args) {
        
    }
    
}
