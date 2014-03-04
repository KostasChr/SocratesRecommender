/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.ntua.iccs.imu.recommendersimulation.persistence.model.socrates;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Kostas
 */
@Entity
@Table(name = "issue_text")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "IssueText.findAll", query = "SELECT i FROM IssueText i"),
    @NamedQuery(name = "IssueText.findByIssueId", query = "SELECT i FROM IssueText i WHERE i.issueId = :issueId")})
public class IssueText implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "issue_id")
    private String issueId;
    @Basic(optional = false)
    @Lob
    @Column(name = "text")
    private String text;

    public IssueText() {
    }

    public IssueText(String issueId) {
        this.issueId = issueId;
    }

    public IssueText(String issueId, String text) {
        this.issueId = issueId;
        this.text = text;
    }

    public String getIssueId() {
        return issueId;
    }

    public void setIssueId(String issueId) {
        this.issueId = issueId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (issueId != null ? issueId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof IssueText)) {
            return false;
        }
        IssueText other = (IssueText) object;
        if ((this.issueId == null && other.issueId != null) || (this.issueId != null && !this.issueId.equals(other.issueId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gr.ntua.iccs.imu.recommendersimulation.persistence.model.IssueText[ issueId=" + issueId + " ]";
    }
    
}
