/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.ntua.iccs.imu.metric.model;

/**
 *
 * @author kostas
 */
public class RocPoint {

    public RocPoint(double TruePositiveRate, double FalsePositiveRate) {
        this.TruePositiveRate = TruePositiveRate;
        this.FalsePositiveRate = FalsePositiveRate;
    }
    private double TruePositiveRate;
    private double FalsePositiveRate;

    public double getTruePositiveRate() {
        return TruePositiveRate;
    }

    public void setTruePositiveRate(double TruePositiveRate) {
        this.TruePositiveRate = TruePositiveRate;
    }

    public double getFalsePositiveRate() {
        return FalsePositiveRate;
    }

    public void setFalsePositiveRate(double FalsePositiveRate) {
        this.FalsePositiveRate = FalsePositiveRate;
    }
}
