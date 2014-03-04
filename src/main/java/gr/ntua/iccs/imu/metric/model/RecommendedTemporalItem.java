/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.ntua.iccs.imu.metric.model;

import com.sun.istack.internal.NotNull;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
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
public class RecommendedTemporalItem implements Comparable<RecommendedTemporalItem>, Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @NotNull
    @Basic(optional = false)
    @Column(name = "id")
    private String id;
    @Basic(optional = false)
    @NotNull
    @Column(name = "date")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date date;

    public RecommendedTemporalItem() {
    }

    public RecommendedTemporalItem(String id) {
        this.id = id;
    }

    public RecommendedTemporalItem(String id, Date date) {
        this.id = id;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
        if (!(object instanceof RecommendedTemporalItem)) {
            return false;
        }
        RecommendedTemporalItem other = (RecommendedTemporalItem) object;
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
    public int compareTo(RecommendedTemporalItem t) {

        return (int) ((int) t.date.getTime() - this.date.getTime());

    }
}
