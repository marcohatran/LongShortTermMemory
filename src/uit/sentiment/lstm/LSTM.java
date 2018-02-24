package uit.sentiment.lstm;

import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.GravesLSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import uit.sentiment.doc2vec.Document2Vector;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import uit.sentiment.test.Measures;
import static uit.sentiment.test.LoadingData.getLabelList;
import static uit.sentiment.test.LoadingData.getVectorList;

public class LSTM {

    private static int numberTrainingExample = 800;
    private static Document2Vector doc = new Document2Vector();
    private static int numInput = 100;
    private static int numOutput = 4;
    private static MultiLayerNetwork network;
    private static int nEpochs = 5;

    public static MultiLayerNetwork getNetWork() {
        return LSTM.network;
    }

    public void trainData(List<INDArray> inputArr, List<Integer> inputLabel, int numTrainingExample) {
        GravesLSTM.Builder lstmBuilder = new GravesLSTM.Builder();
        lstmBuilder.activation(Activation.TANH);

        // input
        lstmBuilder.nIn(numInput);
        lstmBuilder.nOut(30); // Hidden
        GravesLSTM inputLayer = lstmBuilder.build();

        RnnOutputLayer.Builder outputBuilder = new RnnOutputLayer.Builder();
        outputBuilder.lossFunction(LossFunctions.LossFunction.MSE);
        outputBuilder.activation(Activation.SOFTMAX);
        outputBuilder.nIn(30); // Hidden

        // output
        outputBuilder.nOut(numOutput);
        RnnOutputLayer outputLayer = outputBuilder.build();

        NeuralNetConfiguration.Builder nnBuilder = new NeuralNetConfiguration.Builder();
        nnBuilder.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT);
        nnBuilder.updater(Updater.ADAM);
        nnBuilder.weightInit(WeightInit.XAVIER);
        nnBuilder.learningRate(0.01);
        nnBuilder.miniBatch(true);

        network = new MultiLayerNetwork(
                nnBuilder.list().layer(0, inputLayer)
                        .layer(1, outputLayer)
                        .backprop(true).pretrain(false)
                        .build());

        network.init();

        INDArray inputArray, inputLabels;

        inputArray = Nd4j.zeros(1, numInput, numTrainingExample);
        inputLabels = Nd4j.zeros(1, numOutput, numTrainingExample);
        
        for (int i = 0; i < numTrainingExample; i++) {

            for (int j = 0; j < numInput; j++) {
                inputArray.putScalar(new int[]{0, j, i}, inputArray.getDouble(j));
            }
            inputLabels.putScalar(new int[]{0, inputLabel.get(i), i}, 1);
        }

        DataSet dataSet = new DataSet(inputArray, inputLabels);

        for (int z = 0; z < nEpochs; z++) {
            network.fit(dataSet);
            network.rnnClearPreviousState();
        }
    }

    public void trainData2(List<INDArray> inputArr, List<Integer> inputLabel, int numTrainingExample) {
        int inputColumn = 100;
        int lstmLayerSize = 200;
        int tbpttLength = 50;
        int nOut = 4;
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT).iterations(1)
                .learningRate(0.1)
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

        inputArray = Nd4j.zeros(1, numInput, numTrainingExample);
        inputLabels = Nd4j.zeros(1, numOutput, numTrainingExample);
        
        

        for (int i = 0; i < numTrainingExample; i++) {

            for (int j = 0; j < numInput; j++) {
                inputArray.putScalar(new int[]{0, j, i}, inputArray.getDouble(j));
            }
            inputLabels.putScalar(new int[]{0, inputLabel.get(i), i}, 1);
        }

        DataSet dataSet = new DataSet(inputArray, inputLabels);

        for (int z = 0; z < nEpochs; z++) {
            network.fit(dataSet);
            network.rnnClearPreviousState();
        }
    }


    public static void main(String[] args) {
    }

    public static int getMax(INDArray arr) {
        double max = -1;
        int index = -1;
        for (int i = 0; i < arr.length(); i++) {
            if (arr.getDouble(i) > max) {
                max = arr.getDouble(i);
                index = i;
            }
        }
        return index;
    }

}
