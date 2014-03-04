/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.ntua.iccs.imu.recommendersimulation.persistence.model.controllers;

import gr.ntua.iccs.imu.recommendersimulation.persistence.model.controllers.exceptions.NonexistentEntityException;
import gr.ntua.iccs.imu.recommendersimulation.persistence.model.controllers.exceptions.PreexistingEntityException;
import gr.ntua.iccs.imu.recommendersimulation.persistence.model.stardom.IdentityProfileView;
import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.UserTransaction;

/**
 *
 * @author Kostas
 */
public class IdentityProfileViewJpaController implements Serializable {

    public IdentityProfileViewJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(IdentityProfileView identityProfileView) throws PreexistingEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(identityProfileView);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findIdentityProfileView(identityProfileView.getProfileId()) != null) {
                throw new PreexistingEntityException("IdentityProfileView " + identityProfileView + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(IdentityProfileView identityProfileView) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            identityProfileView = em.merge(identityProfileView);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                int id = identityProfileView.getProfileId();
                if (findIdentityProfileView(id) == null) {
                    throw new NonexistentEntityException("The identityProfileView with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(int id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            IdentityProfileView identityProfileView;
            try {
                identityProfileView = em.getReference(IdentityProfileView.class, id);
                identityProfileView.getProfileId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The identityProfileView with id " + id + " no longer exists.", enfe);
            }
            em.remove(identityProfileView);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<IdentityProfileView> findIdentityProfileViewEntities() {
        return findIdentityProfileViewEntities(true, -1, -1);
    }

    public List<IdentityProfileView> findIdentityProfileViewEntities(int maxResults, int firstResult) {
        return findIdentityProfileViewEntities(false, maxResults, firstResult);
    }

    private List<IdentityProfileView> findIdentityProfileViewEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(IdentityProfileView.class));
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

    public IdentityProfileView findIdentityProfileView(int id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(IdentityProfileView.class, id);
        } finally {
            em.close();
        }
    }

    public IdentityProfileView findIdentityProfileViewEntitiesByUserEmail(String email) {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select object(o) from IdentityProfileView as o where o.email like '%" + email.trim() + "%' ");
//           q.setParameter(1,date, TemporalType.DATE);
            q.setMaxResults(1);
            return (IdentityProfileView) q.getSingleResult();
//            return q.getResultList();
        } catch (NoResultException e) {
            return null;

        } finally {
            em.close();
        }
    }

    public int getIdentityProfileViewCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<IdentityProfileView> rt = cq.from(IdentityProfileView.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
}
