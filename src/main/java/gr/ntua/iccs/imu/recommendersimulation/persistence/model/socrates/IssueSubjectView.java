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
@Table(name = "issue_subject_view")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "IssueSubjectView.findAll", query = "SELECT i FROM IssueSubjectView i"),
    @NamedQuery(name = "IssueSubjectView.findByIssueId", query = "SELECT i FROM IssueSubjectView i WHERE i.issueId = :issueId"),
    @NamedQuery(name = "IssueSubjectView.findByWeight", query = "SELECT i FROM IssueSubjectView i WHERE i.weight = :weight")})
public class IssueSubjectView implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Lob
    @Column(name = "id")
    private String id;
    @Basic(optional = false)
    @Column(name = "issue_id")
    private int issueId;
    @Basic(optional = false)
    @Lob
    @Column(name = "subject")
    private String subject;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "weight")
    private Double weight;

    public IssueSubjectView() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getIssueId() {
        return issueId;
    }

    public void setIssueId(int issueId) {
        this.issueId = issueId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }
    
}
