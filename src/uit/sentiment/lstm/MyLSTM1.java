/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.sentiment.lstm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.GravesLSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import uit.sentiment.doc2vec.Sentence2Vector;
import static uit.sentiment.doc2vec.Sentence2Vector.sentences2Vector;
import static uit.sentiment.test.LoadingData.compute;
import static uit.sentiment.test.LoadingData.getSentencesLabel;
import static uit.sentiment.test.LoadingData.getSentencesVector;
import static uit.sentiment.test.LoadingData.getVectorLabel;
import static uit.sentiment.test.LoadingData.getVectorList;
import static uit.sentiment.test.LoadingData.loadLabels;
import static uit.sentiment.test.LoadingData.loadSentences;
import uit.sentiment.test.Measures;

/**
 *
 * @author Phu
 */
public class MyLSTM1 {

    private static int nIn = 100;
    private static int nOut = 4;
    private static int numExample = 2000;
    private static int timeSeries = 10;
    private static INDArray inputArray;
    private static INDArray inputLabels;
    
    private static final double learningRate = 0.05;
    private static final int iterations = 1;
    private static final int seed = 12345;

    private static final int lstmLayer1Size = 256;
    private static final int lstmLayer2Size = 256;
    private static final int denseLayerSize = 32;
    private static final double dropoutRatio = 0.2;
    private static final int truncatedBPTTLength = 22;
    
    private static ArrayList<INDArray> testset = new ArrayList<>();

    private static Sentence2Vector convert2Vector = new Sentence2Vector();

    private static void createDataset(ArrayList<ArrayList<INDArray>> arr, ArrayList<Integer> listLabels) {
        for (int i = 0; i < arr.size(); i++) {
            int length = arr.get(i).size();
            for (int j = 0; j < length; j++) {
                if (arr.get(i).get(j) != null) {
                    for (int k = 0; k < 100; k++) {
                        inputArray.putScalar(new int[]{i, k, j}, arr.get(i).get(j).getDouble(k));
                        inputLabels.putScalar(new int[]{i, listLabels.get(i), 0}, 1);
                    }
                }
            }
        }
    }

    private static int getMax(INDArray arr) {
        double gvVal = arr.getDouble(0, 0, 0);
        double dtVal = arr.getDouble(0, 1, 0);
        double vcVal = arr.getDouble(0, 2, 0);
        double otVal = arr.getDouble(0, 3, 0);

        List<Double> list = new ArrayList<>();
        list.add(gvVal);
        list.add(dtVal);
        list.add(vcVal);
        list.add(otVal);

        double temp1 = gvVal > dtVal ? gvVal : dtVal;
        double temp2 = temp1 > vcVal ? temp1 : vcVal;
        double temp3 = temp2 > otVal ? temp2 : otVal;

        return (int) list.indexOf(temp3);
    }

    public static ArrayList<INDArray> loadTestData(int start, int end) {
        ArrayList<INDArray> arraySentences = new ArrayList<INDArray>();
        File src = new File("data.xlsx");
        XSSFWorkbook wb = null;
        BufferedWriter data = null;
        BufferedWriter label = null;
        try {
            FileInputStream fis = new FileInputStream(src);
            wb = new XSSFWorkbook(fis);
            XSSFSheet sheet1 = wb.getSheetAt(0);
            List<INDArray> arr;
            for (int i = start; i < end; i++) {
                String sentence = sheet1.getRow(i).getCell(0).getStringCellValue();
                int topLable = Integer.parseInt(sheet1.getRow(i).getCell(2).getStringCellValue());
                System.out.println(sentence);
                if (sentence.length() != 0) {
                    INDArray temp = Nd4j.zeros(1, nIn, timeSeries);

                    arr = convert2Vector.sentences2Vector(sentence);

                    for (int j = 0; j < arr.size(); j++) {
                        if (arr.get(j) != null) {
                            for (int k = 0; k < nIn; k++) {
                                temp.putScalar(new int[]{0, k, j}, arr.get(j).getDouble(k));

                            }
                        }
                    }
                    arraySentences.add(temp);
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

    private static void train() {

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .iterations(iterations)
                .learningRate(learningRate)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .weightInit(WeightInit.XAVIER)
                .updater(Updater.RMSPROP)
                .regularization(true)
                .l2(1e-4)
                .list()
                .layer(0, new GravesLSTM.Builder()
                        .nIn(nIn)
                        .nOut(lstmLayer1Size)
                        .activation(Activation.TANH)
                        .gateActivationFunction(Activation.HARDSIGMOID)
                        .dropOut(dropoutRatio)
                        .build())
                .layer(1, new GravesLSTM.Builder()
                        .nIn(lstmLayer1Size)
                        .nOut(lstmLayer2Size)
                        .activation(Activation.TANH)
                        .gateActivationFunction(Activation.HARDSIGMOID)
                        .dropOut(dropoutRatio)
                        .build())
                .layer(2, new DenseLayer.Builder()
                        .nIn(lstmLayer2Size)
                        .nOut(denseLayerSize)
                        .activation(Activation.RELU)
                        .build())
                .layer(3, new RnnOutputLayer.Builder()
                        .nIn(denseLayerSize)
                        .nOut(nOut)
                        .activation(Activation.IDENTITY)
                        .lossFunction(LossFunctions.LossFunction.MSE)
                        .build())
                .backpropType(BackpropType.TruncatedBPTT)
                .tBPTTForwardLength(truncatedBPTTLength)
                .tBPTTBackwardLength(truncatedBPTTLength)
                .pretrain(false)
                .backprop(true)
                .build();

        MultiLayerNetwork network = new MultiLayerNetwork(conf);
        network.init();
        network.setListeners(new ScoreIterationListener(100));

        // Định dạng input : [Sl ví dụ, input size, sl từ tối đa trong câu]
        // Định dạng output : [Sl ví dụ, ouput size, sl từ tối đa trong câu]
        inputArray = Nd4j.zeros(numExample, nIn, timeSeries);
        inputLabels = Nd4j.zeros(numExample, nOut, timeSeries);

        createDataset(loadSentences(400, numExample), loadLabels(400, numExample));

        DataSet dataSet = new DataSet(inputArray, inputLabels);

        for (int z = 0; z < 2000; z++) {
            network.fit(dataSet);
            network.rnnClearPreviousState();
            System.out.println(network.score());
        }

//        ArrayList<INDArray> test = convert2Vector.sentences2Vector("thầy dạy rõ_ràng dễ hiểu");
//        INDArray arrTest = Nd4j.zeros(1, inputLayer.getNIn(), timeSeries);
//        ArrayList<INDArray> test1 = convert2Vector.sentences2Vector("môn_học thú_vị đưa phục_vụ bài_học");
//        INDArray arrTest1 = Nd4j.zeros(1, inputLayer.getNIn(), timeSeries);
//        ArrayList<INDArray> test2 = convert2Vector.sentences2Vector("nâng_cấp dụng_cụ học_tập micro loa");
//        INDArray arrTest2 = Nd4j.zeros(1, inputLayer.getNIn(), timeSeries);
//
//        for (int i = 0; i < test.size(); i++) {
//            for (int j = 0; j < 100; j++) {
//                arrTest.putScalar(new int[]{0, j, i}, test.get(i).getDouble(j));
//                arrTest1.putScalar(new int[]{0, j, i}, test1.get(i).getDouble(j));
//                arrTest2.putScalar(new int[]{0, j, i}, test2.get(i).getDouble(j));
//            }
//        }


        ArrayList<INDArray> arrayTest = loadTestData(0, 400);
        ArrayList<Integer> testLabel =  loadLabels(0, 400);

        int[][] confusionMatrix = new int[4][4];
        
        int predict, real;
        for (int i = 0; i < arrayTest.size(); i++) {
            predict = getMax(network.output(arrayTest.get(i)));
            real = testLabel.get(i);
            confusionMatrix[real][predict] += 1;
            System.out.println(i + " predict: " + predict + " - real: " + real);
        }
        
        Measures m = compute(confusionMatrix);
        m.display();

        //System.out.println(arrTest.length());
    }

    public static void main(String[] args) {
        //ArrayList<ArrayList<INDArray>> listSentences = loadSentences(0, 100);
        train();

    }
}
