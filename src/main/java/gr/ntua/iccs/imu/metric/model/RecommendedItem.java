/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.ntua.iccs.imu.metric.model;

import com.sun.istack.internal.NotNull;
import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Kostas
 */
@Entity
@Table(name = "recommended_item")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "RecommendedItem.findAll", query = "SELECT r FROM RecommendedItem r"),
    @NamedQuery(name = "RecommendedItem.findById", query = "SELECT r FROM RecommendedItem r WHERE r.id = :id"),
    @NamedQuery(name = "RecommendedItem.findBySimilarity", query = "SELECT r FROM RecommendedItem r WHERE r.similarity = :similarity")})
public class RecommendedItem implements Comparable<RecommendedItem>, Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @NotNull
    @Basic(optional = false)
    @Column(name = "id")
    private String id;
    @Basic(optional = false)
    @NotNull
    @Column(name = "similarity")
    private double similarity;

    public RecommendedItem() {
    }

    public RecommendedItem(String id) {
        this.id = id;
    }

    public RecommendedItem(String id, double similarity) {
        this.id = id;
        this.similarity = similarity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(double similarity) {
        this.similarity = similarity;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof RecommendedItem)) {
            return false;
        }
        RecommendedItem other = (RecommendedItem) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gr.ntua.iccs.imu.metric.model.RecommendedItem[ id=" + id + " ]";
    }

    /**
     *
     * @param t the item to compare to 
     * @return integer showing if it is higher or lower
     */
    @Override
    public int compareTo(RecommendedItem t) {

        return (int) Math.round((t.similarity - this.similarity) * 10000);

    }
}
