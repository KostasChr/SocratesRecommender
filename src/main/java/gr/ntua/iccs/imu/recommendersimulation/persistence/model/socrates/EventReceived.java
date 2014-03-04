/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.ntua.iccs.imu.recommendersimulation.persistence.model.socrates;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
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
@Table(name = "event_received")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "EventReceived.findAll", query = "SELECT e FROM EventReceived e"),
    @NamedQuery(name = "EventReceived.findById", query = "SELECT e FROM EventReceived e WHERE e.id = :id"),
    @NamedQuery(name = "EventReceived.findByIssueId", query = "SELECT e FROM EventReceived e WHERE e.issueId = :issueId"),
    @NamedQuery(name = "EventReceived.findByStatus", query = "SELECT e FROM EventReceived e WHERE e.status = :status"),
    @NamedQuery(name = "EventReceived.findByEmail", query = "SELECT e FROM EventReceived e WHERE e.email = :email"),
    @NamedQuery(name = "EventReceived.findByDate", query = "SELECT e FROM EventReceived e WHERE e.date = :date"),
    @NamedQuery(name = "EventReceived.findByIssueCreated", query = "SELECT e FROM EventReceived e WHERE e.issueCreated = :issueCreated")})
public class EventReceived implements Serializable, Comparable<EventReceived> {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "issue_id")
    private String issueId;
    @Basic(optional = false)
    @Column(name = "status")
    private String status;
    @Basic(optional = false)
    @Lob
    @Column(name = "description")
    private byte[] description;
    @Basic(optional = false)
    @Column(name = "email")
    private String email;
    @Basic(optional = false)
    @Column(name = "date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;
    @Basic(optional = false)
    @Column(name = "issue_created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date issueCreated;

    public EventReceived() {
    }

    public EventReceived(Integer id) {
        this.id = id;
    }

    public EventReceived(Integer id, String issueId, String status, byte[] description, String email, Date date, Date issueCreated) {
        this.id = id;
        this.issueId = issueId;
        this.status = status;
        this.description = description;
        this.email = email;
        this.date = date;
        this.issueCreated = issueCreated;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getIssueId() {
        return issueId;
    }

    public void setIssueId(String issueId) {
        this.issueId = issueId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public byte[] getDescription() {
        return description;
    }

    public void setDescription(byte[] description) {
        this.description = description;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getIssueCreated() {
        return issueCreated;
    }

    public void setIssueCreated(Date issueCreated) {
        this.issueCreated = issueCreated;
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
        if (!(object instanceof EventReceived)) {
            return false;
        }
        EventReceived other = (EventReceived) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gr.ntua.iccs.imu.recommendersimulation.persistence.model.EventReceived[ id=" + id + " ]";
    }
    
    public int compareTo(EventReceived t) {
        return (t.date.compareTo(this.date));
    }

}
