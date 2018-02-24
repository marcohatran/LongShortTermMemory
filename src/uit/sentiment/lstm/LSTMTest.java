/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.sentiment.lstm;

import java.util.ArrayList;
import java.util.List;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
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
import uit.sentiment.doc2vec.Document2Vector;
import uit.sentiment.test.Measures;
import static uit.sentiment.test.LoadingData.compute;
import static uit.sentiment.test.LoadingData.getLabelList;
import static uit.sentiment.test.LoadingData.getVectorList;

/**
 *
 * @author Phu
 */
public class LSTMTest {
    private static int numberTrainingExample = 999;
    private static Document2Vector doc = new Document2Vector();
    private static int numInput = 100;
    private static int numOutput = 4;
    private static MultiLayerNetwork network;
    private static int nEpochs = 800;
    
    public static void trainData() {
        int inputColumn = 100;
        int lstmLayerSize = 200;
        int tbpttLength = 50;
        int nOut = 4;
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT).iterations(1)
                .learningRate(0.01)
                .seed(12345)
                .regularization(true)
                .l2(0.001)
                .weightInit(WeightInit.XAVIER)
                .updater(Updater.RMSPROP)
                .list()
                .layer(0, new GravesLSTM.Builder().nIn(inputColumn).nOut(lstmLayerSize)
                        .activation(Activation.TANH).build())
                .layer(1, new GravesLSTM.Builder().nIn(lstmLayerSize).nOut(lstmLayerSize)
                        .activation(Activation.TANH).build())
                .layer(2, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MCXENT).activation(Activation.SOFTMAX) //MCXENT + softmax for classification
                        .nIn(lstmLayerSize).nOut(nOut).build())
                .backpropType(BackpropType.TruncatedBPTT).tBPTTForwardLength(tbpttLength).tBPTTBackwardLength(tbpttLength)
                .pretrain(false).backprop(true)
                .build();

        network = new MultiLayerNetwork(conf);
        network.init();
        network.setListeners(new ScoreIterationListener(1));

        INDArray inputArray, inputLabels;

        inputArray = Nd4j.zeros(numberTrainingExample, numInput, 1);
        inputLabels = Nd4j.zeros(numberTrainingExample, numOutput, 1);
        
        List<INDArray> inputArr = new ArrayList<>();
        inputArr = getVectorList(0, 999);

        List<Integer> inputLabel = new ArrayList<>();
        inputLabel = getLabelList(0, 999);

        // load các câu test
        List<INDArray> testingSentences = new ArrayList<>();
        testingSentences = getVectorList(0, 100);

        // load kết quả
        List<Integer> result = new ArrayList<>();
        result = getLabelList(0, 100);
       
        for (int i = 0; i < numberTrainingExample; i++) {
            for (int j = 0; j < numInput; j++) {
                inputArray.putScalar(new int[]{i, j, 0}, inputArr.get(i).getDouble(j));
            }
            inputLabels.putScalar(new int[]{i, inputLabel.get(i), 0}, 1);
        }
        

        DataSet dataSet = new DataSet(inputArray, inputLabels);

        for (int z = 0; z < nEpochs; z++) {
            network.fit(dataSet);
            network.rnnClearPreviousState();
        }
        int predict;
        int real;
        int i = 0;
        int [][] confMatrix = new int[4][4];
        for (INDArray arr : testingSentences) {
            predict = LSTM.getMax(network.rnnTimeStep(arr));
            real = result.get(i);
            System.out.println("Real : " + real + " Predict : " + predict);
            confMatrix[real][predict] += 1;
            i++;
        } 

        Measures me = compute(confMatrix);
        me.display();       
    }
    
    public static void main(String[] args) {
        trainData();
    }
}
