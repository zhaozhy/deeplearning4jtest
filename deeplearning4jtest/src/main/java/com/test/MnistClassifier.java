package com.test;


import org.datavec.api.io.labels.ParentPathLabelGenerator;
import org.datavec.api.split.FileSplit;
import org.datavec.image.loader.NativeImageLoader;
import org.datavec.image.recordreader.ImageRecordReader;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.linalg.schedule.MapSchedule;
import org.nd4j.linalg.schedule.ScheduleType;


import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MnistClassifier {
  private  static  final   String basePath ="D:/test2" + "/mnist";
  private  static  final   String dataUrl = "http://github.com/myleott/mnist_png/raw/master/mnist_png.tar.gz";
  private  static  final   String localFilePath = basePath + "/mnist_png.tar.gz";

    public static void main(String[] args) throws Exception {
          int height=28;
          int width=28;
          int channels=1;
          int outputNum=10;
          int batchSize=54;
          int nEpochs=1;
          int iterations=1;
          int seed =1234;
        Random random=new Random(seed);
        System.out.println("Data load and vectorization...");
        String localFilePath=basePath+ "/mnist_png.tar.gz";
        if(DataUtilities.downloadFile(dataUrl,localFilePath))
            System.out.println("Data downloaded from "+dataUrl);
         if(!new File(basePath+"/mnist_png").exists())
             DataUtilities.extractTarGz(localFilePath,basePath);
         File trainData=new File(basePath,"/mnist_png/training");
        FileSplit trainSplit=new FileSplit(trainData, NativeImageLoader.ALLOWED_FORMATS,random);
        ParentPathLabelGenerator labelMaker=new ParentPathLabelGenerator();
        ImageRecordReader trainRR=new ImageRecordReader(height,width,channels,labelMaker);
        trainRR.initialize(trainSplit);
        DataSetIterator trainIter=new RecordReaderDataSetIterator(trainRR,batchSize,1,outputNum);

        DataNormalization scaler=new ImagePreProcessingScaler(0,1);
        scaler.fit(trainIter);
        trainIter.setPreProcessor(scaler);

       File testData=new File(basePath + "/mnist_png/testing");
       FileSplit testSplit=new FileSplit(testData,NativeImageLoader.ALLOWED_FORMATS,random);
       ImageRecordReader testRR=new ImageRecordReader(height,width,channels,labelMaker);
       testRR.initialize(testSplit);
       DataSetIterator testIter=new RecordReaderDataSetIterator(testRR,batchSize,1,outputNum);
       testIter.setPreProcessor(scaler);

        System.out.println("网络配置和训练");
        Map<Integer,Double>  Schedule= new HashMap<>();
        Schedule.put(0,0.06);
        Schedule.put(200,0.05);
        Schedule.put(600,0.028);
        Schedule.put(800,0.0060);
        Schedule.put(1000,0.001);
        MultiLayerConfiguration  configuration=new NeuralNetConfiguration.Builder()
            .seed(seed)
            .l2(0.0005)
            .updater(new Nesterovs(new MapSchedule(ScheduleType.ITERATION,Schedule)))
            .weightInit(WeightInit.XAVIER)
            .list()
            .layer(0,new ConvolutionLayer.Builder(5,5)
                .nIn(channels)
                .stride(1,1)
                .nOut(20)
                .activation(Activation.IDENTITY)
                .build())
            .layer(1,new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                    .kernelSize(2,2)
                    .stride(2,2)
                    .build())
            .layer(2,new ConvolutionLayer.Builder(5,5)
                .stride(1,1)
                .nOut(50)
                .activation(Activation.IDENTITY)
                .build())
            .layer(3,new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                .kernelSize(2,2)
                .stride(2,2)
                .build())
            .layer(4,new DenseLayer.Builder().activation(Activation.RELU)
                .nOut(500)
                .build())
            .layer(5,new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                .nOut(outputNum)
                .activation(Activation.SOFTMAX)
                .build()
            )
            .setInputType(InputType.convolutionalFlat(28,28,1))
            .backprop(true).pretrain(false).build();
        MultiLayerNetwork  network=new MultiLayerNetwork(configuration);
        network.init();
        network.setListeners(new ScoreIterationListener(10));
        System.out.println("Total num of params: "+ network.numParams());


         network.fit(trainIter);
        Evaluation evaluation=network.evaluate(testIter);
        System.out.println(evaluation.stats());
        trainIter.reset();
        testIter.reset();

        ModelSerializer.writeModel(network,new File(basePath + "/minist-model.zip"),true);

        //MultiLayerConfiguration configuration=new NeuralNetConfiguration();

    }

}
