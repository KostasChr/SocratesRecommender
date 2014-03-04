/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.ntua.iccs.imu.recommendersimulation.persistence.model.stardom;

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
@Table(name = "identity_profile_view")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "IdentityProfileView.findAll", query = "SELECT i FROM IdentityProfileView i"),
    @NamedQuery(name = "IdentityProfileView.findById", query = "SELECT i FROM IdentityProfileView i WHERE i.id = :id"),
    @NamedQuery(name = "IdentityProfileView.findByUuid", query = "SELECT i FROM IdentityProfileView i WHERE i.uuid = :uuid"),
    @NamedQuery(name = "IdentityProfileView.findByProfileId", query = "SELECT i FROM IdentityProfileView i WHERE i.profileId = :profileId"),
    @NamedQuery(name = "IdentityProfileView.findByName", query = "SELECT i FROM IdentityProfileView i WHERE i.name = :name"),
    @NamedQuery(name = "IdentityProfileView.findByLastname", query = "SELECT i FROM IdentityProfileView i WHERE i.lastname = :lastname"),
    @NamedQuery(name = "IdentityProfileView.findByUsername", query = "SELECT i FROM IdentityProfileView i WHERE i.username = :username"),
    @NamedQuery(name = "IdentityProfileView.findByEmail", query = "SELECT i FROM IdentityProfileView i WHERE i.email = :email"),
    @NamedQuery(name = "IdentityProfileView.findBySource", query = "SELECT i FROM IdentityProfileView i WHERE i.source = :source"),
    @NamedQuery(name = "IdentityProfileView.findBySourceId", query = "SELECT i FROM IdentityProfileView i WHERE i.sourceId = :sourceId")})
public class IdentityProfileView implements Serializable {
    private static final long serialVersionUID = 1L;
    @Basic(optional = false)
    @Column(name = "id")
    @Id
    private int id;
    @Basic(optional = false)
    @Column(name = "uuid")
    private String uuid;
    @Basic(optional = false)
    @Column(name = "profile_id")
    private int profileId;
    @Column(name = "name")
    private String name;
    @Column(name = "lastname")
    private String lastname;
    @Column(name = "username")
    private String username;
    @Column(name = "email")
    private String email;
    @Column(name = "source")
    private String source;
    @Column(name = "source_id")
    private String sourceId;

    public IdentityProfileView() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getProfileId() {
        return profileId;
    }

    public void setProfileId(int profileId) {
        this.profileId = profileId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }
    
}
