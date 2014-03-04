/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.ntua.iccs.imu.recommendersimulation.persistence.model.controllers;

import gr.ntua.iccs.imu.recommendersimulation.persistence.model.socrates.IssueText;
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
import javax.transaction.UserTransaction;

/**
 *
 * @author Kostas
 */
public class IssueTextJpaController implements Serializable {

    public IssueTextJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(IssueText issueText) throws PreexistingEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(issueText);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findIssueText(issueText.getIssueId()) != null) {
                throw new PreexistingEntityException("IssueText " + issueText + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(IssueText issueText) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            issueText = em.merge(issueText);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                String id = issueText.getIssueId();
                if (findIssueText(id) == null) {
                    throw new NonexistentEntityException("The issueText with id " + id + " no longer exists.");
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
            IssueText issueText;
            try {
                issueText = em.getReference(IssueText.class, id);
                issueText.getIssueId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The issueText with id " + id + " no longer exists.", enfe);
            }
            em.remove(issueText);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<IssueText> findIssueTextEntities() {
        return findIssueTextEntities(true, -1, -1);
    }

    public List<IssueText> findIssueTextEntities(int maxResults, int firstResult) {
        return findIssueTextEntities(false, maxResults, firstResult);
    }

    private List<IssueText> findIssueTextEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(IssueText.class));
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

    public IssueText findIssueText(String id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(IssueText.class, id);
        } finally {
            em.close();
        }
    }

    public int getIssueTextCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<IssueText> rt = cq.from(IssueText.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
