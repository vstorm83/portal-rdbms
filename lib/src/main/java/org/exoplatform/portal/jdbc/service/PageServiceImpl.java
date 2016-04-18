package org.exoplatform.portal.jdbc.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.gatein.api.page.PageQuery;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.portal.jdbc.dao.ContainerDAO;
import org.exoplatform.portal.jdbc.dao.PageDAO;
import org.exoplatform.portal.jdbc.dao.WindowDAO;
import org.exoplatform.portal.jdbc.entity.ComponentEntity.TYPE;
import org.exoplatform.portal.jdbc.entity.ContainerEntity;
import org.exoplatform.portal.jdbc.entity.PageEntity;
import org.exoplatform.portal.jdbc.entity.WindowEntity;
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
  
  private PageDAO store;
  
  private ContainerDAO containerDAO;
  
  private WindowDAO windowDAO;

  /**
   * Create an instance that uses the provided persistence.
   *
   * @param pageDAO the persistence
   * @throws NullPointerException if the persistence argument is null
   */
  public PageServiceImpl(PageDAO pageDAO, ContainerDAO containerDAO, WindowDAO windowDAO) throws NullPointerException {
    if (pageDAO == null) {
      throw new NullPointerException("No null persistence allowed");
    }
    this.store = pageDAO;
    this.windowDAO = windowDAO;
    this.containerDAO = containerDAO;
  }

  @Override
  public PageContext loadPage(PageKey key) {
    if (key == null) {
      throw new NullPointerException("No null key accepted");
    }

    //
    PageEntity entity = store.findByKey(key);
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

    PageEntity entity = store.findByKey(page.getKey());
    // 
    if (entity == null) {
      entity = buildPageEntityContext(null, page);
      store.create(entity);
      return true;
    } else {
      entity = buildPageEntityContext(entity, page);
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
    PageEntity page = store.findByKey(key); 
    if (page != null) {
      String pageBody = page.getPageBody();
      JSONParser parser = new JSONParser();
      JSONArray children;
      try {
        children = (JSONArray)parser.parse(pageBody);
      } catch (ParseException e) {
        throw new IllegalStateException("Can't parse page body");
      }
      deleteChildren(children);
      store.delete(page);
      return true;
    } else {
      return false;
    }
  }

  private void deleteChildren(JSONArray children) {
    for (Object child : children) {      
      JSONObject c = (JSONObject)child;
      String id = c.get("id").toString();
      TYPE t = TYPE.valueOf(c.get("type").toString());
      
      if (TYPE.CONTAINER.equals(t)) {        
        JSONArray descendants = (JSONArray)c.get("children");
        if (descendants != null) {
          deleteChildren(descendants);
        }
        
        ContainerEntity container = containerDAO.find(id);
        if (container != null) {
          JSONParser parser = new JSONParser();
          JSONArray dashboardChilds;
          try {
            dashboardChilds = (JSONArray)parser.parse(container.getContainerBody());
          } catch (ParseException e) {
            throw new IllegalStateException("Can't parse dashboard body");
          }
          deleteChildren(dashboardChilds);
          
          containerDAO.delete(container);
        }
      } else if (TYPE.WINDOW.equals(t)) {
        WindowEntity window = windowDAO.find(id);
        if (window != null) {
          windowDAO.delete(window);
        }
      } else {
        throw new IllegalArgumentException("Can't delete child with type: " + t);
      }
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
    PageEntity pageSrc = store.findByKey(src);
    if (pageSrc == null) {
      throw new PageServiceException(PageError.CLONE_NO_SRC_PAGE, "Could not clone non existing page " + src.getName()
        + " from site of type " + src.getSite().getType() + " with id " + src.getSite().getName());
    } else {
      //TODO CHECK no destination site
      PageEntity pageDst = store.findByKey(dst);
      if (pageDst != null) {
        throw new PageServiceException(PageError.CLONE_DST_ALREADY_EXIST, "Could not clone page " + dst.getName()
                                       + "to existing page " + dst.getSite().getType() + " with id " + dst.getSite().getName());
      } else {
        pageDst = buildPageEntityContext(null, pageSrc.buildPageContext());
        //TODO create new instance of container and window
        pageDst.setPageBody(pageSrc.getPageBody());
        //
        SiteKey siteKey = dst.getSite();
        pageDst.setOwnerId(siteKey.getName());
        pageDst.setOwnerType(siteKey.getType());
        pageDst.setName(dst.getName());
        
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
  
  private PageEntity buildPageEntityContext(PageEntity entity, PageContext page) {
    if (entity == null) {
      entity = new PageEntity();
    }
    PageState state = page.getState();
    entity.setAccessPermissions(PageEntity.convert(state.getAccessPermissions()));
    entity.setDescription(state.getDescription());
    entity.setDisplayName(state.getDisplayName());
    entity.setEditPermission(state.getEditPermission());
    entity.setFactoryId(state.getFactoryId());
    entity.setShowMaxWindow(state.getShowMaxWindow());
    
    SiteKey siteKey = page.getKey().getSite();
    entity.setOwnerId(siteKey.getName());
    entity.setOwnerType(siteKey.getType());
    entity.setName(page.getKey().getName());
    
    entity.setMoveAppsPermissions(PageEntity.convert(state.getMoveAppsPermissions()));
    entity.setMoveContainersPermissions(PageEntity.convert(state.getMoveContainersPermissions()));
    
    return entity;
  }
}
