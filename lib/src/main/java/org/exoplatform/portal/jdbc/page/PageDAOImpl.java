package org.exoplatform.portal.jdbc.page;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.gatein.api.common.Pagination;
import org.gatein.api.page.PageQuery;

import org.exoplatform.commons.persistence.impl.EntityManagerHolder;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.page.PageKey;

public class PageDAOImpl extends GenericDAOJPAImpl<PageEntity, Long> implements PageDAO {

  @Override
  public PageEntity findByKey(String pageKey) {
    TypedQuery<PageEntity> query = getEntityManager().createNamedQuery("PageEntity.findByKey", PageEntity.class);
    query.setParameter("pageKey", pageKey);
    try {
      return query.getSingleResult();
    } catch (NoResultException ex) {
      return null;
    }
  }

  @Override
  public ListAccess<PageEntity> findByQuery(PageQuery query) {
    TypedQuery<PageEntity> q = buildQuery(query);
    final List<PageEntity> results = q.getResultList();
    
    return new ListAccess<PageEntity>() {

      @Override
      public int getSize() throws Exception {
        return results.size();
      }

      @Override
      public PageEntity[] load(int offset, int limit) throws Exception, IllegalArgumentException {
        return results.subList(offset, offset + limit).toArray(new PageEntity[limit]);
      }
    };
  }
  
  public TypedQuery<PageEntity> buildQuery(PageQuery query) {
    EntityManager em = EntityManagerHolder.get();
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<PageEntity> criteria = cb.createQuery(PageEntity.class);
    Root<PageEntity> pageEntity = criteria.from(PageEntity.class);
    //
    CriteriaQuery<PageEntity> select = criteria.select(pageEntity);
    select.distinct(true);
    
    List<Predicate> predicates = new LinkedList<Predicate>();
    
    String pageKey = null;
    if (query.getSiteName() != null && query.getSiteType() != null) {
      SiteKey siteKey = new SiteKey(convertSiteType(query.getSiteType()), query.getSiteName());
      pageKey = new PageKey(siteKey, "%").format();
    } else if (query.getSiteName() != null) {
      pageKey = "%::" + query.getSiteName() + "::%";
    } else if (query.getSiteType() != null) {
      pageKey = query.getSiteType().getName() + "::%";
    }
    if (pageKey != null) {
      predicates.add(cb.like(cb.lower(pageEntity.get(PageEntity_.pageKey)), pageKey.toLowerCase()));            
    }
    
    if (query.getDisplayName() != null) {
      predicates.add(cb.like(cb.lower(pageEntity.get(PageEntity_.displayName)), "%" + query.getDisplayName().toLowerCase() + "%"));
    }
    
    select.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
    
    //
    TypedQuery<PageEntity> typedQuery = em.createQuery(select);
    Pagination pagination = query.getPagination();
    if (pagination != null && pagination.getLimit() > 0) {
      typedQuery.setFirstResult(pagination.getOffset());
      typedQuery.setMaxResults(pagination.getLimit());
    }
    //
    return typedQuery;
  }

  private SiteType convertSiteType(org.gatein.api.site.SiteType siteType) {
    switch (siteType) {
    case SITE:
      return SiteType.PORTAL;
    case SPACE:
      return SiteType.GROUP;
    case DASHBOARD:
      return SiteType.USER;
    default:
      return null;
    }
  }

}
