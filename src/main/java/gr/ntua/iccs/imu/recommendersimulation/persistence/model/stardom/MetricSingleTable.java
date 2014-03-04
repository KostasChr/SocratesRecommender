/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.ntua.iccs.imu.recommendersimulation.persistence.model.stardom;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Kostas
 */
@Entity
@Table(name = "metric_single_table")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "MetricSingleTable.findAll", query = "SELECT m FROM MetricSingleTable m"),
    @NamedQuery(name = "MetricSingleTable.findById", query = "SELECT m FROM MetricSingleTable m WHERE m.id = :id"),
    @NamedQuery(name = "MetricSingleTable.findByCreatedAt", query = "SELECT m FROM MetricSingleTable m WHERE m.createdAt = :createdAt"),
    @NamedQuery(name = "MetricSingleTable.findByTemporal", query = "SELECT m FROM MetricSingleTable m WHERE m.temporal = :temporal"),
    @NamedQuery(name = "MetricSingleTable.findByQuantity", query = "SELECT m FROM MetricSingleTable m WHERE m.quantity = :quantity"),
    @NamedQuery(name = "MetricSingleTable.findByMessageId", query = "SELECT m FROM MetricSingleTable m WHERE m.messageId = :messageId"),
    @NamedQuery(name = "MetricSingleTable.findByInReplyTo", query = "SELECT m FROM MetricSingleTable m WHERE m.inReplyTo = :inReplyTo"),
    @NamedQuery(name = "MetricSingleTable.findByAmount", query = "SELECT m FROM MetricSingleTable m WHERE m.amount = :amount"),
    @NamedQuery(name = "MetricSingleTable.findByComponent", query = "SELECT m FROM MetricSingleTable m WHERE m.component = :component"),
    @NamedQuery(name = "MetricSingleTable.findByType", query = "SELECT m FROM MetricSingleTable m WHERE m.type = :type")})
public class MetricSingleTable implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Basic(optional = false)
    @Column(name = "temporal")
    @Temporal(TemporalType.TIMESTAMP)
    private Date temporal;
    @Basic(optional = false)
    @Column(name = "quantity")
    private int quantity;
    @Column(name = "message_id")
    private String messageId;
    @Column(name = "in_reply_to")
    private String inReplyTo;
    @Basic(optional = false)
    @Column(name = "amount")
    private int amount;
    @Column(name = "component")
    private String component;
    @Basic(optional = false)
    @Column(name = "type")
    private String type;
    @JoinColumn(name = "identity_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Identity identityId;

    public MetricSingleTable() {
    }

    public MetricSingleTable(Integer id) {
        this.id = id;
    }

    public MetricSingleTable(Integer id, Date createdAt, Date temporal, int quantity, int amount, String type) {
        this.id = id;
        this.createdAt = createdAt;
        this.temporal = temporal;
        this.quantity = quantity;
        this.amount = amount;
        this.type = type;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getTemporal() {
        return temporal;
    }

    public void setTemporal(Date temporal) {
        this.temporal = temporal;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getInReplyTo() {
        return inReplyTo;
    }

    public void setInReplyTo(String inReplyTo) {
        this.inReplyTo = inReplyTo;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Identity getIdentityId() {
        return identityId;
    }

    public void setIdentityId(Identity identityId) {
        this.identityId = identityId;
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
        if (!(object instanceof MetricSingleTable)) {
            return false;
        }
        MetricSingleTable other = (MetricSingleTable) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gr.ntua.iccs.imu.recommendersimulation.persistence.model.MetricSingleTable[ id=" + id + " ]";
    }
    
}
