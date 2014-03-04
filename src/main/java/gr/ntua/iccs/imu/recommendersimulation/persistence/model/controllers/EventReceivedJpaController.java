/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.ntua.iccs.imu.recommendersimulation.persistence.model.controllers;

import gr.ntua.iccs.imu.recommendersimulation.persistence.model.socrates.EventReceived;
import gr.ntua.iccs.imu.recommendersimulation.persistence.model.controllers.exceptions.NonexistentEntityException;
import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.UserTransaction;

/**
 *
 * @author Kostas
 */
public class EventReceivedJpaController implements Serializable {

    public EventReceivedJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(EventReceived eventReceived) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(eventReceived);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(EventReceived eventReceived) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            eventReceived = em.merge(eventReceived);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = eventReceived.getId();
                if (findEventReceived(id) == null) {
                    throw new NonexistentEntityException("The eventReceived with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            EventReceived eventReceived;
            try {
                eventReceived = em.getReference(EventReceived.class, id);
                eventReceived.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The eventReceived with id " + id + " no longer exists.", enfe);
            }
            em.remove(eventReceived);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<EventReceived> findEventReceivedEntities() {
        return findEventReceivedEntities(true, -1, -1);
    }

    public List<EventReceived> findEventReceivedEntities(int maxResults, int firstResult) {
        return findEventReceivedEntities(false, maxResults, firstResult);
    }

    private List<EventReceived> findEventReceivedEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(EventReceived.class));
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

    public EventReceived findEventReceived(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(EventReceived.class, id);
        } finally {
            em.close();
        }
    }

    public int getEventReceivedCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<EventReceived> rt = cq.from(EventReceived.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

    public List<EventReceived> findEventReceivedByIssueId(String issueId) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<EventReceived> from = cq.from(EventReceived.class);
            Predicate whereClause = em.getCriteriaBuilder().equal(from.get("issueId"), issueId);
//            cq.select(cq.from(EventReceived.class));
            cq.where(whereClause);
            Query q = em.createQuery(cq);
            return q.getResultList();
        } finally {
            em.close();
        }
    }
    
}
