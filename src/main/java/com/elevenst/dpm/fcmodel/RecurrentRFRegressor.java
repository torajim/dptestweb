package com.elevenst.dpm.fcmodel;

import com.elevenst.dpm.repository.RecurrentRFFeatureMatrixV1;
import com.elevenst.dpm.repository.RecurrentRFMapper;
import com.elevenst.dpm.service.InventoryChecks;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.linalg.DenseVector;
import org.apache.spark.ml.regression.RandomForestRegressionModel;
import org.apache.spark.ml.tree.Node;
import org.apache.spark.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.*;

@RegisterForecast
@Component
@Slf4j
public class RecurrentRFRegressor implements ForecastModel {

    private static final Logger logger = LoggerFactory.getLogger(RecurrentRFRegressor.class);

    @Value("${spring.rrfr.model-path}")
    String modelRootPath;
    @Value("${spring.rrfr.pipeline-path}")
    String pipelinePath;

    @Autowired
    private RecurrentRFMapper mapper;

    private transient SparkSession spark;
    private transient PipelineModel pipelineModel = null;
    private HashMap<String, RandomForestRegressionModel> models;
    private HashMap<String, Node[]> modelTrees;
    private LinkedList<String> loadedModels;
    private LinkedList<String> loadedModelTrees;
    private SimpleDateFormat dateFormat;
    //private Dataset<Row> testData;

    private transient final Column mdt = new Column("mdt");
    private transient final Column prediction = new Column("prediction");


    private static final int cacheSize = 100;

    public class NoModelFileException extends Exception {
        public NoModelFileException(String msg) {
            super(msg);
        }
        public NoModelFileException(String msg, Throwable t) {
            super(msg, t);
        }
    }

    public class UnsupportedPrdException extends Exception {
        public UnsupportedPrdException(String msg, Throwable t) {
            super(msg, t);
        }
    }

    public RecurrentRFRegressor() throws NoModelFileException {
        spark = SparkSession.builder().master("local").getOrCreate();
        models = new HashMap<>(cacheSize);
        loadedModels = new LinkedList<>();

        modelTrees = new HashMap<>(cacheSize);
        loadedModelTrees = new LinkedList<>();

        dateFormat = new SimpleDateFormat("yyyyMMdd");

        //testData = spark.read().parquet("test/rrfr-v0.5/");
    }

    @Override
    public String getModelName() {
        return "Recurrent RandomForest Model";
    }

    private void setCurrentFeature(RecurrentRFFeatureMatrixV1 current, int index, long finalDscPrc, InventoryChecks inventoryChecks, List<ForecastResult> prevResult) {
        current.setMod_dsc_prc(finalDscPrc);
        current.setEvnt_mw_rthit(inventoryChecks.isSet(InventoryChecks.INVENTORY.HIT) ? 1 : 0);
        current.setEvnt_mw_shckdeal(inventoryChecks.isSet(InventoryChecks.INVENTORY.SHOCKING_DEAL) ? 1 : 0);
        current.setEvnt_mw_mvbanner(inventoryChecks.isSet(InventoryChecks.INVENTORY.MOVIE_CLIP) ? 1 : 0);
        current.setEvnt_mw_mdrecom(inventoryChecks.isSet(InventoryChecks.INVENTORY.MD) ? 1 : 0);

        switch (index) {
            case 0:
                break;
            case 1:
                current.setQty_sum_d1(prevResult.get(0).getOrdQty());
                break;
            case 2:
                current.setQty_sum_d2(prevResult.get(0).getOrdQty());
                current.setQty_sum_d1(prevResult.get(1).getOrdQty());
                break;
            case 3:
                current.setQty_sum_d3(prevResult.get(0).getOrdQty());
                current.setQty_sum_d2(prevResult.get(1).getOrdQty());
                current.setQty_sum_d1(prevResult.get(2).getOrdQty());
                break;
            case 4:
                current.setQty_sum_d4(prevResult.get(0).getOrdQty());
                current.setQty_sum_d3(prevResult.get(1).getOrdQty());
                current.setQty_sum_d2(prevResult.get(2).getOrdQty());
                current.setQty_sum_d1(prevResult.get(3).getOrdQty());
                break;
            case 5:
                current.setQty_sum_d5(prevResult.get(0).getOrdQty());
                current.setQty_sum_d4(prevResult.get(1).getOrdQty());
                current.setQty_sum_d3(prevResult.get(2).getOrdQty());
                current.setQty_sum_d2(prevResult.get(3).getOrdQty());
                current.setQty_sum_d1(prevResult.get(4).getOrdQty());
                break;
            case 6:
                current.setQty_sum_d6(prevResult.get(0).getOrdQty());
                current.setQty_sum_d5(prevResult.get(1).getOrdQty());
                current.setQty_sum_d4(prevResult.get(2).getOrdQty());
                current.setQty_sum_d3(prevResult.get(3).getOrdQty());
                current.setQty_sum_d2(prevResult.get(4).getOrdQty());
                current.setQty_sum_d1(prevResult.get(5).getOrdQty());
                break;
        }
    }

    private DenseVector makeVector(RecurrentRFFeatureMatrixV1 data) {
        ArrayList<Double> features = new ArrayList<>();

        String dow = data.getDow();
        features.add(dow.equals("1") ? 1.0 : 0.0);
        features.add(dow.equals("2") ? 1.0 : 0.0);
        features.add(dow.equals("3") ? 1.0 : 0.0);
        features.add(dow.equals("4") ? 1.0 : 0.0);
        features.add(dow.equals("5") ? 1.0 : 0.0);
        features.add(dow.equals("6") ? 1.0 : 0.0);
        features.add(dow.equals("7") ? 1.0 : 0.0);
        features.add((double)data.getHoliday());
        features.add((double)data.getDist_next_wkdy());
        features.add((double)data.getEvnt_mw_rthit());
        features.add((double)data.getEvnt_mw_shckdeal());
        features.add((double)data.getEvnt_mw_mvbanner());
        features.add((double)data.getEvnt_mw_mdrecom());
        features.add((double)data.getMod_dsc_prc());
        features.add((double)data.getMod_sel_prc());
        features.add((double)data.getQty_sum_d1());
        features.add((double)data.getQty_sum_d2());
        features.add((double)data.getQty_sum_d3());
        features.add((double)data.getQty_sum_d4());
        features.add((double)data.getQty_sum_d5());
        features.add((double)data.getQty_sum_d6());
        features.add((double)data.getQty_sum_d7());
        features.add((double)data.getQty_sum_d8());
        features.add((double)data.getQty_sum_d9());
        features.add((double)data.getQty_sum_d10());
        features.add((double)data.getQty_sum_d11());
        features.add((double)data.getQty_sum_d12());
        features.add((double)data.getQty_sum_d13());
        features.add((double)data.getQty_sum_d14());
        features.add(data.getQty_sum_avg());

        return new DenseVector(ArrayUtils.toPrimitive(features.toArray(new Double[features.size()])));
    }

    private void printResult(String prefix, List<ForecastResult> results) {
        StringBuffer sb = new StringBuffer();

        for(ForecastResult result : results) {
            sb.append(result.getOrdDt());
            sb.append('-');
            sb.append(result.getOrdQty());
            sb.append(',');
        }

        logger.info(prefix + sb.toString());
    }

    private double predict(Node[] modelTree, DenseVector vector) {
        double sum = 0.0;
        for (Node tree : modelTree) {
            sum += tree.predictImpl(vector).prediction();
        }

        return sum / modelTree.length;
    }

    @Override
    public List<ForecastResult> forecastNextWeek(String prdNo, long finalDscPrc, InventoryChecks inventoryChecks) {
        try {
            logger.info("[FORECAST] Request received");

            Calendar today = new GregorianCalendar();

            //RandomForestRegressionModel model = getModel(prdNo);
            Node[] modelTree = getModelTree(prdNo);
            logger.info("[FORECAST] Model loaded from DISK");
            //pipelineModel = getPipeline();
            List<RecurrentRFFeatureMatrixV1> featureMatrix = mapper.getThisWeekFeatureMatrix(dateFormat.format(today.getTime()), Long.parseLong(prdNo));
            logger.info("[FORECAST] Data loaded from DB");
            List<ForecastResult> results = new ArrayList<>(7);

            for (int i = 0; i < featureMatrix.size(); i++) {
                RecurrentRFFeatureMatrixV1 current = featureMatrix.get(i);
                setCurrentFeature(current, i, finalDscPrc, inventoryChecks, results);
                DenseVector vector = makeVector(current);
                //Double result = model.predict(vector);
                double result = predict(modelTree, vector);

                //System.out.println(result + "\t" + result2);

                results.add(new ForecastResult(new java.sql.Date(dateFormat.parse(current.getMdt()).getTime()), (long) result));
            }

            logger.info("[FORECAST] DONE");
            printResult("[FORECAST] ", results);

            return results;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<ForecastResult> forecastLastWeek(String prdNo) {
        try {
            logger.info("[EVALUATE] Request received");

            Calendar lastWeek = new GregorianCalendar();
            lastWeek.add(Calendar.DATE, -7);
            //RandomForestRegressionModel model = getModel(prdNo);
            Node[] modelTree = getModelTree(prdNo);
            logger.info("[EVALUATE] Model loaded from DISK");
            //pipelineModel = getPipeline();

            List<RecurrentRFFeatureMatrixV1> featureMatrix = mapper.getLastWeekFeatureMatrix(dateFormat.format(lastWeek.getTime()), Long.parseLong(prdNo));
            logger.info("[EVALUATE] Data loaded from DB");
            List<ForecastResult> results = new ArrayList<>(featureMatrix.size());

            for (RecurrentRFFeatureMatrixV1 data : featureMatrix) {
                DenseVector vector = makeVector(data);
                //Double result = model.predict(vector);
                double result = predict(modelTree, vector);

                //System.out.println(result + "\t" + result2);

                results.add(new ForecastResult(new java.sql.Date(dateFormat.parse(data.getMdt()).getTime()), (long) result));
            }

            logger.info("[EVALUATE] DONE");
            printResult("[EVALUATE] ", results);

            return results;

            /*
            Dataset<Row> data = spark.createDataFrame(featureMatrix, RecurrentRFFeatureMatrixV1.class);
            //data.show();
            Dataset<ForecastResult> result = model.transform(pipelineModel.transform(data)).
                    select(
                            functions.to_date(mdt).as("ordDt")
                            , functions.round(prediction).cast(DataTypes.LongType).as("ordQty")
                    ).
                    map((MapFunction<Row, ForecastResult>) x -> new ForecastResult((Date)x.get(x.fieldIndex("ordDt")), x.getLong(x.fieldIndex("ordQty")))
                    , Encoders.bean(ForecastResult.class));

            return result.collectAsList();
            */
            /*
            Row row = testData.where("prd_key = 'prd_" + prdNo + "'").first();
            long label = row.getLong(row.fieldIndex("label"));
            SparseVector features = (SparseVector) row.get(row.fieldIndex("features"));

            double predicted = model.predict(features);
            System.out.println(label + "\t" + predicted);
            */
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private RandomForestRegressionModel getModel(String prdNo) throws NoModelFileException {
        if (models.containsKey(prdNo)) {
            return models.get(prdNo);
        } else {
            RandomForestRegressionModel newModel = loadModel(prdNo);

            if (loadedModels.size() >= cacheSize) {
                String removed = loadedModels.removeFirst();
                models.remove(removed);
            }

            models.put(prdNo, newModel);
            loadedModels.addLast(prdNo);

            return newModel;
        }
    }

    private Node[] getModelTree(String prdNo) throws NoModelFileException {
        if (modelTrees.containsKey(prdNo)) {
            return modelTrees.get(prdNo);
        } else {
            Node[] newModelTree = loadModelTree(prdNo);

            if (loadedModelTrees.size() >= cacheSize) {
                String removed = loadedModelTrees.removeFirst();
                modelTrees.remove(removed);
            }

            modelTrees.put(prdNo, newModelTree);
            loadedModelTrees.addLast(prdNo);

            return newModelTree;
        }
    }

    private RandomForestRegressionModel loadModel(String prdNo) throws NoModelFileException {
        String modelRootPath = "D:\\Dropbox\\SK_Planet\\project\\PRC\\dmfc\\model_v2";
        File modelRoot = new File(modelRootPath);
        if (!modelRoot.exists() || !modelRoot.isDirectory())
            throw new NoModelFileException("The model root directory doesn't exist. model/rrfr-v0.5 is required");

        File[] modelDirs = modelRoot.listFiles();
        if (modelDirs.length == 0)
            throw new NoModelFileException("Models don't exist under the model root directory.");

        for(File modelDir : modelDirs) {
            if (modelDir.isDirectory()) {
                if (("prd_" + prdNo).equals(modelDir.getName())) {
                    return RandomForestRegressionModel.load(modelRootPath + "/prd_" + prdNo);
                }
            }
        }

        throw new NoModelFileException("No model file exist for the given prdNo: " + prdNo);
    }

    private Node[] loadModelTree(String prdNo) throws NoModelFileException {
        File modelRoot = new File(modelRootPath);

        if (!modelRoot.exists() || !modelRoot.isDirectory())
            throw new NoModelFileException("The model root directory doesn't exist. model/rrfr-v0.5 is required");

        File[] modelDirs = modelRoot.listFiles();
        if (modelDirs.length == 0)
            throw new NoModelFileException("Models don't exist under the model root directory.");

        for(File modelDir : modelDirs) {
            if (modelDir.isDirectory()) {
                if (("prd_" + prdNo).equals(modelDir.getName())) {
                    File[] treeFiles = modelDir.listFiles();
                    Node[] treeNodes = new Node[treeFiles.length];

                    for (int i = 0; i < treeFiles.length; i++) {
                        try {
                            FileInputStream fis = new FileInputStream(treeFiles[i]);
                            BufferedInputStream bis = new BufferedInputStream(fis);
                            ObjectInputStream ois = new ObjectInputStream(bis);

                            treeNodes[i] = (Node) ois.readObject();

                            ois.close();
                            bis.close();
                            fis.close();
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    }

                    return treeNodes;
                }
            }
        }

        throw new NoModelFileException("No model file exist for the given prdNo: " + prdNo);
    }

    private PipelineModel getPipeline() {
        if (pipelineModel == null) {
            pipelineModel = PipelineModel.load(pipelinePath);
        }

        return pipelineModel;
    }
}