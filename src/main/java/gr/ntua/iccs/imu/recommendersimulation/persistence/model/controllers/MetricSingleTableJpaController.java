/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.ntua.iccs.imu.recommendersimulation.persistence.model.controllers;

import gr.ntua.iccs.imu.metric.model.RecommendedItem;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import gr.ntua.iccs.imu.recommendersimulation.persistence.model.stardom.Identity;
import gr.ntua.iccs.imu.recommendersimulation.persistence.model.stardom.MetricSingleTable;
import gr.ntua.iccs.imu.recommendersimulation.persistence.model.controllers.exceptions.NonexistentEntityException;
import gr.ntua.iccs.imu.recommendersimulation.persistence.model.controllers.exceptions.PreexistingEntityException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

/**
 *
 * @author Kostas
 */
public class MetricSingleTableJpaController implements Serializable {

    public MetricSingleTableJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(MetricSingleTable metricSingleTable) throws PreexistingEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Identity identityId = metricSingleTable.getIdentityId();
            if (identityId != null) {
                identityId = em.getReference(identityId.getClass(), identityId.getId());
                metricSingleTable.setIdentityId(identityId);
            }
            em.persist(metricSingleTable);
            if (identityId != null) {
                identityId.getMetricSingleTableCollection().add(metricSingleTable);
                identityId = em.merge(identityId);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findMetricSingleTable(metricSingleTable.getId()) != null) {
                throw new PreexistingEntityException("MetricSingleTable " + metricSingleTable + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(MetricSingleTable metricSingleTable) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            MetricSingleTable persistentMetricSingleTable = em.find(MetricSingleTable.class, metricSingleTable.getId());
            Identity identityIdOld = persistentMetricSingleTable.getIdentityId();
            Identity identityIdNew = metricSingleTable.getIdentityId();
            if (identityIdNew != null) {
                identityIdNew = em.getReference(identityIdNew.getClass(), identityIdNew.getId());
                metricSingleTable.setIdentityId(identityIdNew);
            }
            metricSingleTable = em.merge(metricSingleTable);
            if (identityIdOld != null && !identityIdOld.equals(identityIdNew)) {
                identityIdOld.getMetricSingleTableCollection().remove(metricSingleTable);
                identityIdOld = em.merge(identityIdOld);
            }
            if (identityIdNew != null && !identityIdNew.equals(identityIdOld)) {
                identityIdNew.getMetricSingleTableCollection().add(metricSingleTable);
                identityIdNew = em.merge(identityIdNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = metricSingleTable.getId();
                if (findMetricSingleTable(id) == null) {
                    throw new NonexistentEntityException("The metricSingleTable with id " + id + " no longer exists.");
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
            MetricSingleTable metricSingleTable;
            try {
                metricSingleTable = em.getReference(MetricSingleTable.class, id);
                metricSingleTable.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The metricSingleTable with id " + id + " no longer exists.", enfe);
            }
            Identity identityId = metricSingleTable.getIdentityId();
            if (identityId != null) {
                identityId.getMetricSingleTableCollection().remove(metricSingleTable);
                identityId = em.merge(identityId);
            }
            em.remove(metricSingleTable);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<MetricSingleTable> findMetricSingleTableEntities() {
        return findMetricSingleTableEntities(true, -1, -1);
    }

    public List<MetricSingleTable> findMetricSingleTableEntities(int maxResults, int firstResult) {
        return findMetricSingleTableEntities(false, maxResults, firstResult);
    }

    private List<MetricSingleTable> findMetricSingleTableEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(MetricSingleTable.class));
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

    public MetricSingleTable findMetricSingleTable(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(MetricSingleTable.class, id);
        } finally {
            em.close();
        }
    }

    public int getMetricSingleTableCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<MetricSingleTable> rt = cq.from(MetricSingleTable.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

    public List<RecommendedItem> findIirmRankEntitiesBeforeDate(Date dateFixed) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            String queryString = "drop view if exists iirm_rank;";
            Query q = em.createNativeQuery(queryString);
            q.executeUpdate();
            java.sql.Date sqlDate = new java.sql.Date(dateFixed.getTime());
            queryString = "create view iirm_rank as SELECT identity_id as id, max(quantity) as iirm_rank FROM metric_single_table where DATE('" + sqlDate.toString() + "') > created_at  and type='its_issues_resolved_metric' group by identity_id;";
//            System.out.println(queryString);
            q = em.createNativeQuery(queryString);
            q.executeUpdate();
            queryString = "select sum(iirm_rank) from iirm_rank;";
            q = em.createNativeQuery(queryString);
            BigDecimal totalQuantity = (BigDecimal) q.getSingleResult();
//            System.out.println(totalQuantity);
            queryString = "select (iirm_rank/" + totalQuantity + ") as similarity,CONCAT(id,'') as id from iirm_rank;";
            q = em.createNativeQuery(queryString, RecommendedItem.class);
            em.getTransaction().commit();
            return q.getResultList();

        } finally {
            em.close();
        }

    }

    public List<RecommendedItem> findScmtRankEntitiesBeforeDate(Date dateFixed) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            String queryString = "drop view if exists scmt_rank;";
            Query q = em.createNativeQuery(queryString);
            q.executeUpdate();
            java.sql.Date sqlDate = new java.sql.Date(dateFixed.getTime());
            queryString = "create view scmt_rank as SELECT identity_id as id, 1/ DATEDIFF(CURDATE(),max(temporal)) as scmt_rank FROM metric_single_table where  DATE('" + sqlDate.toString() + "') > temporal  and type='scm_temporal_metric' group by identity_id ";
//            System.out.println(queryString);
            q = em.createNativeQuery(queryString);
            q.executeUpdate();
            queryString = "select sum(scmt_rank) from scmt_rank;";
            q = em.createNativeQuery(queryString);
            BigDecimal totalQuantity = (BigDecimal) q.getSingleResult();
//            System.out.println(totalQuantity);
            queryString = "select CONCAT(id,'') as ID,(scmt_rank/" + totalQuantity + ") as SIMILARITY from scmt_rank;";
            q = em.createNativeQuery(queryString, RecommendedItem.class);
            em.getTransaction().commit();
            return q.getResultList();
        } finally {
            em.close();
        }

    }

    public List<RecommendedItem> findMltRankEntitiesBeforeDate(Date dateFixed) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            String queryString = "drop view if exists mlt_rank;";
            Query q = em.createNativeQuery(queryString);
            q.executeUpdate();
            java.sql.Date sqlDate = new java.sql.Date(dateFixed.getTime());
            queryString = "create view mlt_rank as SELECT identity_id as id, 1/ DATEDIFF(CURDATE(),max(temporal)) as mlt_rank FROM metric_single_table where  DATE('" + sqlDate.toString() + "') > temporal  and type='mailing_list_temporal_metric' group by identity_id ";
//            System.out.println(queryString);
            q = em.createNativeQuery(queryString);
            q.executeUpdate();
            queryString = "select sum(mlt_rank) from mlt_rank;";
            q = em.createNativeQuery(queryString);
            BigDecimal totalQuantity = (BigDecimal) q.getSingleResult();
//            System.out.println(totalQuantity);
            queryString = "select CONCAT(id,'') as ID,(mlt_rank/" + totalQuantity + ") as SIMILARITY from mlt_rank;";
            q = em.createNativeQuery(queryString, RecommendedItem.class);
            em.getTransaction().commit();
            return q.getResultList();

        } finally {
            em.close();
        }

    }

    public List<RecommendedItem> findSaiRankEntitiesBeforeDate(Date dateFixed) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            String queryString = "drop view if exists sai_rank;";
            Query q = em.createNativeQuery(queryString);
            q.executeUpdate();
            java.sql.Date sqlDate = new java.sql.Date(dateFixed.getTime());
            queryString = "create view sai_rank as SELECT identity_id as id, max(quantity) as sai_rank FROM metric_single_table where DATE('" + sqlDate.toString() + "') > created_at  and type='scm_api_introduced' group by identity_id;";
//            System.out.println(queryString);
            q = em.createNativeQuery(queryString);
            q.executeUpdate();
            queryString = "select sum(sai_rank) from sai_rank;";
            q = em.createNativeQuery(queryString);
            BigDecimal totalQuantity = (BigDecimal) q.getSingleResult();
//            System.out.println(totalQuantity);
            queryString = "select CONCAT(id,'') as ID,(sai_rank/" + totalQuantity + ") as SIMILARITY from sai_rank;";
            q = em.createNativeQuery(queryString, RecommendedItem.class);
            em.getTransaction().commit();
            return q.getResultList();

        } finally {
            em.close();
        }

    }

    public List<RecommendedItem> findItstRankEntitiesBeforeDate(Date dateFixed) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            String queryString = "drop view if exists itst_rank;";
            Query q = em.createNativeQuery(queryString);
            q.executeUpdate();
            java.sql.Date sqlDate = new java.sql.Date(dateFixed.getTime());
            queryString = "create view itst_rank as SELECT identity_id as id, 1/ DATEDIFF(CURDATE(),max(temporal)) as itst_rank FROM metric_single_table where  DATE('" + sqlDate.toString() + "') > temporal  and type='its_temporal_metric' group by identity_id ";
//            System.out.println(queryString);
            q = em.createNativeQuery(queryString);
            q.executeUpdate();
            queryString = "select sum(itst_rank) from itst_rank;";
            q = em.createNativeQuery(queryString);
            BigDecimal totalQuantity = (BigDecimal) q.getSingleResult();
//            System.out.println(totalQuantity);
            queryString = "select CONCAT(id,'') as ID,(itst_rank/" + totalQuantity + ") as SIMILARITY from itst_rank;";
            q = em.createNativeQuery(queryString, RecommendedItem.class);
            em.getTransaction().commit();
            return q.getResultList();

        } finally {
            em.close();
        }

    }
}
