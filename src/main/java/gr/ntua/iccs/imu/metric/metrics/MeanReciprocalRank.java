/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.ntua.iccs.imu.metric.metrics;

import gr.ntua.iccs.imu.metric.model.RecommendedItem;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author kostas
 */
public class MeanReciprocalRank {


    private ArrayList<Double> reciprocalRankList = new ArrayList<Double>();
    
    public MeanReciprocalRank() {
    }
    
    public void addMrrValue(ArrayList<RecommendedItem> recommendationsList,RecommendedItem correctAnswer){
        
//        Collections.sort(recommendationsList);
//        System.out.println("Items Recommended for " + correctAnswer.getItemId());
//        for (RecommendedItem ri : recommendationsList){
//             System.out.println(ri.getItemId() + "  " + ri.getSimilarity());
//        }
//        System.out.println("===================================================" );
//      
        int i=0;
        for (RecommendedItem ri : recommendationsList){
            i++;
            if (ri.getId().equals(correctAnswer.getId())){
                
                reciprocalRankList.add(1.0/i);
                break;
            }
        }
        
    }

    public double getMrr() {
        Double meanReciprocalRank = 0.0;
        for (Double d : reciprocalRankList){
        meanReciprocalRank += d;    
        }
        meanReciprocalRank= meanReciprocalRank/(reciprocalRankList.size());
        return meanReciprocalRank;

    }
}
