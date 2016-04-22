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

import org.exoplatform.commons.api.persistence.DataInitializer;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.portal.jdbc.dao.ContainerDAO;
import org.exoplatform.portal.jdbc.dao.PageDAO;
import org.exoplatform.portal.jdbc.dao.WindowDAO;
import org.exoplatform.portal.jdbc.entity.ComponentEntity;
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
  
  private PageDAO pageDAO;
  
  private ContainerDAO containerDAO;
  
  private WindowDAO windowDAO;
  
  private static Log log = ExoLogger.getExoLogger(PageServiceImpl.class);

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
    this.pageDAO = pageDAO;
    this.windowDAO = windowDAO;
    this.containerDAO = containerDAO;
  }

  @Override
  public PageContext loadPage(PageKey key) {
    if (key == null) {
      throw new NullPointerException("No null key accepted");
    }

    //
    PageEntity entity = pageDAO.findByKey(key);
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

    PageEntity entity = pageDAO.findByKey(page.getKey());
    // 
    if (entity == null) {
      entity = buildPageEntityContext(null, page);
      pageDAO.create(entity);
      return true;
    } else {
      entity = buildPageEntityContext(entity, page);
      pageDAO.update(entity);
      return false;
    }
  }

  @Override
  public boolean destroyPage(PageKey key) {
    if (key == null) {
      throw new NullPointerException("No null page argument");
    }

    //
    PageEntity page = pageDAO.findByKey(key); 
    if (page != null) {
      String pageBody = page.getPageBody();
      JSONArray children = parse(pageBody);
      deleteChildren(children);
      pageDAO.delete(page);
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
          JSONArray dashboardChilds = parse(container.getContainerBody());
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
    PageEntity pageSrc = pageDAO.findByKey(src);
    if (pageSrc == null) {
      throw new PageServiceException(PageError.CLONE_NO_SRC_PAGE, "Could not clone non existing page " + src.getName()
        + " from site of type " + src.getSite().getType() + " with id " + src.getSite().getName());
    } else {
      //TODO CHECK no destination site
      PageEntity pageDst = pageDAO.findByKey(dst);
      if (pageDst != null) {
        throw new PageServiceException(PageError.CLONE_DST_ALREADY_EXIST, "Could not clone page " + dst.getName()
                                       + "to existing page " + dst.getSite().getType() + " with id " + dst.getSite().getName());
      } else {
        pageDst = buildPageEntityContext(null, pageSrc.buildPageContext());        
        List<ComponentEntity> children = clone(pageSrc.getPageBody());
        pageDst.setChildren(children);
        pageDst.setPageBody(((JSONArray)pageDst.toJSON().get("children")).toJSONString());
        //
        SiteKey siteKey = dst.getSite();
        pageDst.setOwnerId(siteKey.getName());
        pageDst.setOwnerType(siteKey.getType());
        pageDst.setName(dst.getName());
        
        pageDAO.create(pageDst);
        return pageDst.buildPageContext();
      }      
    }    
  }

  private List<ComponentEntity> clone(String pageBody) {
    List<ComponentEntity> results = new LinkedList<ComponentEntity>();
    
    JSONArray children = parse(pageBody);
    
    for (Object child : children) {
      JSONObject c = (JSONObject)child;
      String id = c.get("id").toString();
      TYPE type = TYPE.valueOf(c.get("type").toString());
      
      switch (type) {
      case CONTAINER:
        ContainerEntity srcC = containerDAO.find(id);
        ContainerEntity dstC = clone(srcC);        
        
        JSONArray descendants = parse(srcC.getContainerBody());
        if (descendants.size() > 0) {
          //dashboard
          dstC.setChildren(clone(srcC.getContainerBody()));
        } else {
          //normal container
          dstC.setChildren(clone(((JSONArray)c.get("children")).toJSONString()));
        }
        dstC.setContainerBody(((JSONArray)dstC.toJSON().get("children")).toJSONString());
        
        containerDAO.create(dstC);
        results.add(dstC);
        break;
      case WINDOW:
        WindowEntity srcW = windowDAO.find(id);
        WindowEntity dstW = clone(srcW);
        
        windowDAO.create(dstW);
        results.add(dstW);
        break;
      default:
        throw new IllegalStateException("Can't handle type: " + type);
      }
      
    }
    return results;
  }

  private WindowEntity clone(WindowEntity src) {
    WindowEntity dst = new WindowEntity();    
    dst.setAppType(src.getAppType());
    dst.setContentId(src.getContentId());
    dst.setCustomization(src.getCustomization());    
    dst.setAccessPermissions(src.getAccessPermissions());
    dst.setDescription(src.getDescription());
    dst.setHeight(src.getHeight());
    dst.setIcon(src.getIcon());
    dst.setProperties(src.getProperties());
    dst.setShowApplicationMode(src.isShowApplicationMode());
    dst.setShowApplicationState(src.isShowApplicationState());
    dst.setShowInfoBar(src.isShowInfoBar());
    dst.setTheme(src.getTheme());
    dst.setTitle(src.getTitle());
    dst.setWidth(src.getWidth());

    return dst;
  }

  private JSONArray parse(String body) {
    JSONParser parser = new JSONParser();
    JSONArray children;
    try {
      children = (JSONArray)parser.parse(body);
      return children;
    } catch (ParseException e) {
      log.error(e);
      throw new IllegalStateException("Can't parse body: " + body);
    }
  }

  private ContainerEntity clone(ContainerEntity src) {
    ContainerEntity dst = new ContainerEntity();

    dst.setAccessPermissions(src.getAccessPermissions());
    dst.setDescription(src.getDescription());
    dst.setFactoryId(src.getFactoryId());
    dst.setHeight(src.getHeight());
    dst.setIcon(src.getIcon());
    dst.setMoveAppsPermissions(src.getMoveAppsPermissions());
    dst.setMoveContainersPermissions(src.getMoveContainersPermissions());
    dst.setName(src.getName());
    dst.setProperties(src.getProperties());
    dst.setTemplate(src.getTemplate());
    dst.setTitle(src.getTitle());
    dst.setWidth(src.getWidth());
    dst.setContainerBody(src.getContainerBody());

    return dst;
  }

  @Override
  public QueryResult<PageContext> findPages(int from,
                                            int to,
                                            SiteType siteType,
                                            String siteName,
                                            String pageName,
                                            String pageTitle) {
    PageQuery.Builder builder = new PageQuery.Builder();
    builder.withDisplayName(pageTitle).withSiteType(convert(siteType)).withSiteName(siteName);
    builder.withPagination(from, to - from);
    ListAccess<PageEntity> dataSet = pageDAO.findByQuery(builder.build());
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
  
  private org.gatein.api.site.SiteType convert(SiteType siteType) {
    if (siteType == null) {
      return null;
    }
    switch (siteType) {
    case GROUP:
      return org.gatein.api.site.SiteType.SPACE;
    case PORTAL:
      return org.gatein.api.site.SiteType.SITE;
    case USER:
      return org.gatein.api.site.SiteType.DASHBOARD;
    }
    return null;
  }

  private PageEntity buildPageEntityContext(PageEntity entity, PageContext page) {
    if (entity == null) {
      entity = new PageEntity();
    }
    PageState state = page.getState();
    if (state != null) {
      entity.setAccessPermissions(PageEntity.convert(state.getAccessPermissions()));
      entity.setDescription(state.getDescription());
      entity.setDisplayName(state.getDisplayName());
      entity.setEditPermission(state.getEditPermission());
      entity.setFactoryId(state.getFactoryId());
      entity.setShowMaxWindow(state.getShowMaxWindow());
      
      entity.setMoveAppsPermissions(PageEntity.convert(state.getMoveAppsPermissions()));
      entity.setMoveContainersPermissions(PageEntity.convert(state.getMoveContainersPermissions()));
    }

    SiteKey siteKey = page.getKey().getSite();
    entity.setOwnerId(siteKey.getName());
    entity.setOwnerType(siteKey.getType());
    entity.setName(page.getKey().getName());

    return entity;
  }
}
