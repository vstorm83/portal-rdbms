package org.exoplatform.portal.jdbc.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.exoplatform.portal.jdbc.entity.SiteEntity;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;

public class SiteDAOImpl extends AbstractDAO<SiteEntity> implements SiteDAO {

  @Override
  public SiteEntity findByKey(SiteKey siteKey) {
    TypedQuery<SiteEntity> query = getEntityManager().createNamedQuery("SiteEntity.findByKey", SiteEntity.class);

    query.setParameter("siteType", siteKey.getType());
    query.setParameter("name", siteKey.getName());
    try {
        return query.getSingleResult();
    } catch (NoResultException ex) {
        return null;
    }
  }

  @Override
  public List<SiteEntity> findByType(SiteType siteType) {
    TypedQuery<SiteEntity> query = getEntityManager().createNamedQuery("SiteEntity.findByType", SiteEntity.class);
    query.setParameter("siteType", siteType);
    
    return query.getResultList();
  }

  @Override
  public List<SiteKey> findSiteKey(SiteType siteType) {
    List<SiteKey> keys = new ArrayList<SiteKey>();

    TypedQuery<String> query = getEntityManager().createNamedQuery("SiteEntity.findSiteKey", String.class);
    query.setParameter("siteType", siteType);
    
    for (String name : query.getResultList()) {
      keys.add(new SiteKey(siteType, name));
    }
    return keys;
  }

}
