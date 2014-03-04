/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.ntua.iccs.imu.recommendersimulation.persistence.model.controllers;

import gr.ntua.iccs.imu.recommendersimulation.persistence.model.socrates.IssueSubjectView;
import gr.ntua.iccs.imu.recommendersimulation.persistence.model.controllers.exceptions.NonexistentEntityException;
import gr.ntua.iccs.imu.recommendersimulation.persistence.model.controllers.exceptions.PreexistingEntityException;
import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 *
 * @author Kostas
 */
public class IssueSubjectViewJpaController implements Serializable {

    public IssueSubjectViewJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(IssueSubjectView issueSubjectView) throws PreexistingEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(issueSubjectView);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findIssueSubjectView(issueSubjectView.getId()) != null) {
                throw new PreexistingEntityException("IssueSubjectView " + issueSubjectView + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(IssueSubjectView issueSubjectView) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            issueSubjectView = em.merge(issueSubjectView);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                String id = issueSubjectView.getId();
                if (findIssueSubjectView(id) == null) {
                    throw new NonexistentEntityException("The issueSubjectView with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(String id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            IssueSubjectView issueSubjectView;
            try {
                issueSubjectView = em.getReference(IssueSubjectView.class, id);
                issueSubjectView.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The issueSubjectView with id " + id + " no longer exists.", enfe);
            }
            em.remove(issueSubjectView);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<IssueSubjectView> findIssueSubjectViewEntities() {
        return findIssueSubjectViewEntities(true, -1, -1);
    }

    public List<IssueSubjectView> findIssueSubjectViewEntities(int maxResults, int firstResult) {
        return findIssueSubjectViewEntities(false, maxResults, firstResult);
    }

    private List<IssueSubjectView> findIssueSubjectViewEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(IssueSubjectView.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public IssueSubjectView findIssueSubjectView(String id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(IssueSubjectView.class, id);
        } finally {
            em.close();
        }
    }

    public int getIssueSubjectViewCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<IssueSubjectView> rt = cq.from(IssueSubjectView.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

    public List<IssueSubjectView> findIssueSubjectViewByIssueId(String issueId) {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select object(o) from IssueSubjectView as o where  o.issueId ='" + issueId + "'");
//           q.setParameter(1,date, TemporalType.DATE);
            return q.getResultList();
        } finally {
            em.close();
        }
    }
}
