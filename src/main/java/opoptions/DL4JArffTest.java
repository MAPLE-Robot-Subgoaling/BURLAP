package opoptions;

import java.util.Arrays;
import java.util.Collections;

import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.api.IterationListener;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class DL4JArffTest {
    /**
     * Converts a set of training instances to a DataSet. Assumes that the
     * instances have been suitably preprocessed - i.e. missing values replaced
     * and nominals converted to binary/numeric. Also assumes that the class index
     * has been set
     *
     * @param insts the instances to convert
     * @return a DataSet
     */
    public static DataSet instancesToDataSet(Instances insts) {
        INDArray data = Nd4j.ones(insts.numInstances(), insts.numAttributes() - 1);
        double[][] outcomes =
                new double[insts.numInstances()][(insts.classAttribute().numValues() == 0) ? 1 : insts.classAttribute().numValues() ];

        for (int i = 0; i < insts.numInstances(); i++) {
            double[] independent = new double[insts.numAttributes() - 1];
            int index = 0;
            Instance current = insts.instance(i);
            for (int j = 0; j < insts.numAttributes(); j++) {
                if (j != insts.classIndex()) {
                    independent[index++] = current.value(j);
                } else {
                    // if classification
                    if(insts.numClasses() > 1)
                        outcomes[i][(int) current.classValue()] = 1;
                    else
                        outcomes[i][0] = current.classValue();
                }
            }
            data.putRow(i, Nd4j.create(independent));
        }

        DataSet dataSet = new DataSet(data, Nd4j.create(outcomes));
        return dataSet;
    }

    public static void main(String[] args) throws Exception {

        DataSource ds = new DataSource("./out_opos_cleanup/moveToDoor/moveToDoor.arff");
        Instances data = ds.getDataSet();
        data.setClassIndex( data.numAttributes() - 1 );

        DataSet dataset = instancesToDataSet(data);

        int numChannels = data.numAttributes();
        int numClasses = data.numClasses();
        int numIters = 1000;
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(1)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .iterations(numIters)
                .learningRate(0.01)
//                .momentum(0.9)
                .list()
                .layer(0, new ConvolutionLayer.Builder(5, 5)
                        //nIn and nOut specify depth. nIn here is the nChannels and nOut is the number of filters to be applied
                        .nIn(numChannels)
                        .stride(1, 1)
                        .nOut(20)
                        .activation(Activation.IDENTITY)
                        .build())
                .layer(1, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2,2)
                        .stride(2,2)
                        .build())
                .layer(2, new ConvolutionLayer.Builder(5, 5)
                        //Note that nIn need not be specified in later layers
                        .stride(1, 1)
                        .nOut(50)
                        .activation(Activation.IDENTITY)
                        .build())
                .layer(3, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2,2)
                        .stride(2,2)
                        .build())
                .layer(4, new DenseLayer.Builder().activation(Activation.RELU)
                        .nOut(500).build())
                .layer(5, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nOut(numClasses)
                        .activation(Activation.SOFTMAX)
                        .build())
                .setInputType(InputType.convolutionalFlat(28,28,1)) //See note below
                .backprop(true).pretrain(false).build();
//                .layer(0, new DenseLayer.Builder()
//                        .nIn(data.numAttributes()-1)
//                        .nOut(10)
//                        .activation("relu")
//                        .weightInit(WeightInit.XAVIER)
//                        .build()
//                )
//                .layer(1, new OutputLayer.Builder(LossFunction.MCXENT)
//                        .nIn(10)
//                        .nOut(data.numClasses())
//                        .weightInit(WeightInit.XAVIER)
//                        .build()
//                )
//                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(Arrays.asList((IterationListener) new ScoreIterationListener(1)));

        model.fit(dataset);




    }

}
