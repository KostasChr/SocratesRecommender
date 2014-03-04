/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.ntua.iccs.imu.metric.metrics;

import gr.ntua.iccs.imu.metric.model.RecommendedItem;
import gr.ntua.iccs.imu.metric.model.RocPoint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author kostas
 */
public class RocCurve {
    
    private ArrayList<RocPoint> rocCurveToReturn= new ArrayList<RocPoint>();
    private ArrayList<ArrayList<RocPoint>> rocCurves= new ArrayList<ArrayList<RocPoint>>();
    
    public RocCurve() {
    }
    
    public void addSingleCurve(ArrayList<RecommendedItem> recommendationsList,RecommendedItem correctAnswer){
        ArrayList<RocPoint> currentCurve= new ArrayList<RocPoint>();
        Collections.sort(recommendationsList);
//        System.out.println("Items Recommended for " + correctAnswer.getItemId());
//        for (RecommendedItem ri : recommendationsList){
//             System.out.println(ri.getItemId() + "  " + ri.getSimilarity());
//        }
//        System.out.println("===================================================" );
      
        Integer tpCount=0;
        Integer fpCount=0;
        int i=0;
        for (RecommendedItem ri : recommendationsList){
            i++;
            if (ri.getId().equals(correctAnswer.getId())){
                tpCount++;
            }else{
                fpCount++;
            }
        currentCurve.add(new RocPoint((1.0*tpCount/i), (1.0*tpCount/1)));
        }
        for (i = 0; i < currentCurve.size(); i++) {
            Double tpr = currentCurve.get(i).getTruePositiveRate();
            Double fpr = currentCurve.get(i).getFalsePositiveRate() ;
//                       System.out.println("Item in position " + i + "\t" + tpr + "\t" + fpr);
        }
        rocCurves.add(currentCurve);
    }

    public ArrayList<RocPoint> getCurve() {
        HashMap<Integer,Integer> sizesOfPoints=new HashMap<Integer, Integer>();
        for (ArrayList<RocPoint> rocCurve: rocCurves){
            int i=0;
            for (RocPoint rp : rocCurve) {
                if (rocCurveToReturn.size()<i+1) {
                     rocCurveToReturn.add(i, new RocPoint(rp.getTruePositiveRate(), rp.getFalsePositiveRate()));
                } else {
                    Double tpr = rocCurveToReturn.get(i).getTruePositiveRate() + rp.getTruePositiveRate();
                    Double fpr = rocCurveToReturn.get(i).getFalsePositiveRate() + rp.getFalsePositiveRate();
                    rocCurveToReturn.remove(i);
                    rocCurveToReturn.add(i, new RocPoint(tpr, fpr));
                }
                    if (sizesOfPoints.containsKey(i)) {
                        Integer previousValue = sizesOfPoints.get(i);
                        sizesOfPoints.remove(i);
                        sizesOfPoints.put(i, previousValue + 1);
                    } else {
//                        System.out.println("added" + i );
                        sizesOfPoints.put(i, 1);
                    }
                
             i++;
            }
        }
        for (int i = 0; i < rocCurveToReturn.size(); i++) {
            Double tpr = rocCurveToReturn.get(i).getTruePositiveRate() / sizesOfPoints.get(i);
            Double fpr = rocCurveToReturn.get(i).getFalsePositiveRate() / sizesOfPoints.get(i);
            rocCurveToReturn.remove(i);
            rocCurveToReturn.add(i, new RocPoint(tpr, fpr));
            if (i<30) {System.out.println("Item in position " + i + "\t" + tpr + "\t" + fpr);
            }
        }
        return rocCurveToReturn;

    }
}
