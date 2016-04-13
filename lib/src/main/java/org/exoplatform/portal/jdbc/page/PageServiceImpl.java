package org.exoplatform.portal.jdbc.page;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.gatein.api.page.PageQuery;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.portal.mop.QueryResult;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageError;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.page.PageService;
import org.exoplatform.portal.mop.page.PageServiceException;
import org.exoplatform.portal.mop.page.PageState;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class PageServiceImpl implements PageService {

  private static Log LOG = ExoLogger.getExoLogger(PageServiceImpl.class);
  
  private final PageDAO store;

  /**
   * Create an instance that uses the provided persistence.
   *
   * @param store the persistence
   * @throws NullPointerException if the persistence argument is null
   */
  public PageServiceImpl(PageDAO store) throws NullPointerException {
    if (store == null) {
      throw new NullPointerException("No null persistence allowed");
    }
    this.store = store;
  }

  @Override
  public PageContext loadPage(PageKey key) {
    if (key == null) {
      throw new NullPointerException("No null key accepted");
    }

    //
    PageEntity entity = store.findByKey(key.format());
    if (entity != null) {
      return entity.buildPageContext();
    } else {
      return null;
    }
  }

  /**
   * <p>
   * Load all the pages of a specific site. Note that this method can
   * potentially raise performance issues if the number of pages is very large
   * and should be used with cautions. That's the motiviation for not having
   * this method on the {@link PageService} interface.
   * </p>
   *
   * @param siteKey the site key
   * @return the list of pages
   * @throws NullPointerException if the site key argument is null
   * @throws PageServiceException anything that would prevent the operation to
   *           succeed
   */
  public List<PageContext> loadPages(SiteKey siteKey) throws NullPointerException, PageServiceException {
    if (siteKey == null) {
      throw new NullPointerException("No null site key accepted");
    }

    //
    QueryResult<PageContext> pages = this.findPages(0, -1, siteKey.getType(), siteKey.getName(), null, null);
    List<PageContext> list = new LinkedList<PageContext>();
    for (PageContext page : pages) {
      list.add(page);
    }
    return list;
  }

  @Override
  public boolean savePage(PageContext page) {
    if (page == null) {
      throw new NullPointerException();
    }

    PageEntity entity = buildPageEntity(page);
    // 
    if (store.findByKey(page.getKey().format()) == null) {
      store.create(entity);
      return true;
    } else {
      store.update(entity);
      return false;
    }
  }

  @Override
  public boolean destroyPage(PageKey key) {
    if (key == null) {
      throw new NullPointerException("No null page argument");
    }

    //
    PageEntity page = store.findByKey(key.format()); 
    if (page != null) {
      store.delete(page);
      return true;
    } else {
      return false;
    }
  }

  @Override
  
  public PageContext clone(PageKey src, PageKey dst) {
    if (src == null) {
      throw new NullPointerException("No null source accepted");
    }
    if (dst == null) {
      throw new NullPointerException("No null destination accepted");
    }

    //TODO check no source site
    PageEntity pageSrc = store.findByKey(src.format());
    if (pageSrc == null) {
      throw new PageServiceException(PageError.CLONE_NO_SRC_PAGE, "Could not clone non existing page " + src.getName()
        + " from site of type " + src.getSite().getType() + " with id " + src.getSite().getName());
    } else {
      //TODO CHECK no destination site
      PageEntity pageDst = store.findByKey(dst.format());
      if (pageDst != null) {
        throw new PageServiceException(PageError.CLONE_DST_ALREADY_EXIST, "Could not clone page " + dst.getName()
                                       + "to existing page " + dst.getSite().getType() + " with id " + dst.getSite().getName());
      } else {
        pageDst = buildPageEntity(pageSrc.buildPageContext());
        pageDst.setPageKey(dst.format());
        
        store.create(pageDst);
        return pageDst.buildPageContext();        
      }      
    }    
  }

  @Override
  public QueryResult<PageContext> findPages(int from,
                                            int to,
                                            SiteType siteType,
                                            String siteName,
                                            String pageName,
                                            String pageTitle) {
    PageQuery.Builder builder = new PageQuery.Builder();
    builder.withDisplayName(pageTitle).withSiteType(org.gatein.api.site.SiteType.forName(siteType.getName())).withSiteName(siteName);
    builder.withPagination(from, to - from);
    ListAccess<PageEntity> dataSet = store.findByQuery(builder.build());
    try {
      ArrayList<PageContext> pages = new ArrayList<PageContext>(dataSet.getSize());
      for (PageEntity data : dataSet.load(from, to - from)) {
        pages.add(data.buildPageContext());
      }
      return new QueryResult<PageContext>(from, dataSet.getSize(), pages);      
    } catch (Exception ex) {      
      LOG.error(ex);
      return new QueryResult<PageContext>(from, 0, Collections.<PageContext>emptyList());
    }
  }
  
  private PageEntity buildPageEntity(PageContext page) {
    PageEntity entity = new PageEntity();
    PageState stage = page.getState();
    entity.setAccessPermissions(new HashSet<String>(stage.getAccessPermissions()));
    entity.setDescription(stage.getDescription());
    entity.setDisplayName(stage.getDisplayName());
    entity.setEditPermission(stage.getEditPermission());
    entity.setFactoryId(stage.getFactoryId());
    entity.setMoveAppsPermissions(new HashSet<String>(stage.getMoveAppsPermissions()));
    entity.setMoveContainersPermissions(new HashSet<String>(stage.getMoveContainersPermissions()));
    entity.setPageKey(page.getKey().format());
    entity.setShowMaxWindow(stage.getShowMaxWindow());
    return entity;
  }
}
