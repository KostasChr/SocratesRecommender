/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.ntua.iccs.imu.recommendersimulation.persistence.model.stardom;

import java.io.Serializable;
import java.util.Collection;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Kostas
 */
@Entity
@Table(name = "identity")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Identity.findAll", query = "SELECT i FROM Identity i"),
    @NamedQuery(name = "Identity.findById", query = "SELECT i FROM Identity i WHERE i.id = :id"),
    @NamedQuery(name = "Identity.findByUuid", query = "SELECT i FROM Identity i WHERE i.uuid = :uuid")})
public class Identity implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "uuid")
    private String uuid;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "identityId")
    private Collection<MetricSingleTable> metricSingleTableCollection;

    public Identity() {
    }

    public Identity(Integer id) {
        this.id = id;
    }

    public Identity(Integer id, String uuid) {
        this.id = id;
        this.uuid = uuid;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @XmlTransient
    public Collection<MetricSingleTable> getMetricSingleTableCollection() {
        return metricSingleTableCollection;
    }

    public void setMetricSingleTableCollection(Collection<MetricSingleTable> metricSingleTableCollection) {
        this.metricSingleTableCollection = metricSingleTableCollection;
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
        if (!(object instanceof Identity)) {
            return false;
        }
        Identity other = (Identity) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gr.ntua.iccs.imu.recommendersimulation.persistence.model.Identity[ id=" + id + " ]";
    }
    
}
