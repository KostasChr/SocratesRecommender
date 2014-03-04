/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.ntua.iccs.imu.recommendersimulation.persistence.model.controllers;

import gr.ntua.iccs.imu.recommendersimulation.persistence.model.stardom.Identity;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import gr.ntua.iccs.imu.recommendersimulation.persistence.model.stardom.MetricSingleTable;
import gr.ntua.iccs.imu.recommendersimulation.persistence.model.controllers.exceptions.IllegalOrphanException;
import gr.ntua.iccs.imu.recommendersimulation.persistence.model.controllers.exceptions.NonexistentEntityException;
import gr.ntua.iccs.imu.recommendersimulation.persistence.model.controllers.exceptions.PreexistingEntityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

/**
 *
 * @author Kostas
 */
public class IdentityJpaController implements Serializable {

    public IdentityJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Identity identity) throws PreexistingEntityException, Exception {
        if (identity.getMetricSingleTableCollection() == null) {
            identity.setMetricSingleTableCollection(new ArrayList<MetricSingleTable>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Collection<MetricSingleTable> attachedMetricSingleTableCollection = new ArrayList<MetricSingleTable>();
            for (MetricSingleTable metricSingleTableCollectionMetricSingleTableToAttach : identity.getMetricSingleTableCollection()) {
                metricSingleTableCollectionMetricSingleTableToAttach = em.getReference(metricSingleTableCollectionMetricSingleTableToAttach.getClass(), metricSingleTableCollectionMetricSingleTableToAttach.getId());
                attachedMetricSingleTableCollection.add(metricSingleTableCollectionMetricSingleTableToAttach);
            }
            identity.setMetricSingleTableCollection(attachedMetricSingleTableCollection);
            em.persist(identity);
            for (MetricSingleTable metricSingleTableCollectionMetricSingleTable : identity.getMetricSingleTableCollection()) {
                Identity oldIdentityIdOfMetricSingleTableCollectionMetricSingleTable = metricSingleTableCollectionMetricSingleTable.getIdentityId();
                metricSingleTableCollectionMetricSingleTable.setIdentityId(identity);
                metricSingleTableCollectionMetricSingleTable = em.merge(metricSingleTableCollectionMetricSingleTable);
                if (oldIdentityIdOfMetricSingleTableCollectionMetricSingleTable != null) {
                    oldIdentityIdOfMetricSingleTableCollectionMetricSingleTable.getMetricSingleTableCollection().remove(metricSingleTableCollectionMetricSingleTable);
                    oldIdentityIdOfMetricSingleTableCollectionMetricSingleTable = em.merge(oldIdentityIdOfMetricSingleTableCollectionMetricSingleTable);
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findIdentity(identity.getId()) != null) {
                throw new PreexistingEntityException("Identity " + identity + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Identity identity) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Identity persistentIdentity = em.find(Identity.class, identity.getId());
            Collection<MetricSingleTable> metricSingleTableCollectionOld = persistentIdentity.getMetricSingleTableCollection();
            Collection<MetricSingleTable> metricSingleTableCollectionNew = identity.getMetricSingleTableCollection();
            List<String> illegalOrphanMessages = null;
            for (MetricSingleTable metricSingleTableCollectionOldMetricSingleTable : metricSingleTableCollectionOld) {
                if (!metricSingleTableCollectionNew.contains(metricSingleTableCollectionOldMetricSingleTable)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain MetricSingleTable " + metricSingleTableCollectionOldMetricSingleTable + " since its identityId field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Collection<MetricSingleTable> attachedMetricSingleTableCollectionNew = new ArrayList<MetricSingleTable>();
            for (MetricSingleTable metricSingleTableCollectionNewMetricSingleTableToAttach : metricSingleTableCollectionNew) {
                metricSingleTableCollectionNewMetricSingleTableToAttach = em.getReference(metricSingleTableCollectionNewMetricSingleTableToAttach.getClass(), metricSingleTableCollectionNewMetricSingleTableToAttach.getId());
                attachedMetricSingleTableCollectionNew.add(metricSingleTableCollectionNewMetricSingleTableToAttach);
            }
            metricSingleTableCollectionNew = attachedMetricSingleTableCollectionNew;
            identity.setMetricSingleTableCollection(metricSingleTableCollectionNew);
            identity = em.merge(identity);
            for (MetricSingleTable metricSingleTableCollectionNewMetricSingleTable : metricSingleTableCollectionNew) {
                if (!metricSingleTableCollectionOld.contains(metricSingleTableCollectionNewMetricSingleTable)) {
                    Identity oldIdentityIdOfMetricSingleTableCollectionNewMetricSingleTable = metricSingleTableCollectionNewMetricSingleTable.getIdentityId();
                    metricSingleTableCollectionNewMetricSingleTable.setIdentityId(identity);
                    metricSingleTableCollectionNewMetricSingleTable = em.merge(metricSingleTableCollectionNewMetricSingleTable);
                    if (oldIdentityIdOfMetricSingleTableCollectionNewMetricSingleTable != null && !oldIdentityIdOfMetricSingleTableCollectionNewMetricSingleTable.equals(identity)) {
                        oldIdentityIdOfMetricSingleTableCollectionNewMetricSingleTable.getMetricSingleTableCollection().remove(metricSingleTableCollectionNewMetricSingleTable);
                        oldIdentityIdOfMetricSingleTableCollectionNewMetricSingleTable = em.merge(oldIdentityIdOfMetricSingleTableCollectionNewMetricSingleTable);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = identity.getId();
                if (findIdentity(id) == null) {
                    throw new NonexistentEntityException("The identity with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Identity identity;
            try {
                identity = em.getReference(Identity.class, id);
                identity.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The identity with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<MetricSingleTable> metricSingleTableCollectionOrphanCheck = identity.getMetricSingleTableCollection();
            for (MetricSingleTable metricSingleTableCollectionOrphanCheckMetricSingleTable : metricSingleTableCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Identity (" + identity + ") cannot be destroyed since the MetricSingleTable " + metricSingleTableCollectionOrphanCheckMetricSingleTable + " in its metricSingleTableCollection field has a non-nullable identityId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(identity);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Identity> findIdentityEntities() {
        return findIdentityEntities(true, -1, -1);
    }

    public List<Identity> findIdentityEntities(int maxResults, int firstResult) {
        return findIdentityEntities(false, maxResults, firstResult);
    }

    private List<Identity> findIdentityEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Identity.class));
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

    public Identity findIdentity(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Identity.class, id);
        } finally {
            em.close();
        }
    }

    public int getIdentityCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Identity> rt = cq.from(Identity.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
