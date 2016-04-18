package org.exoplatform.portal.jdbc.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.json.simple.JSONArray;

import org.exoplatform.commons.api.persistence.ExoEntity;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.page.PageState;

@Entity
@ExoEntity
@Table(name = "PORTAL_PAGES")
@NamedQueries({
    @NamedQuery(name = "PageEntity.findByKey", query = "SELECT p FROM PageEntity p WHERE p.ownerType = :ownerType AND p.ownerId = :ownerId AND p.name = :name") })
public class PageEntity extends ContainerEntity implements Serializable {

  private static final long serialVersionUID = -6195451978995765259L;

  @Column(name = "OWNER_TYPE")
  private SiteType          ownerType;

  @Column(name = "OWNER_ID", length = 200)
  private String            ownerId;

  @Column(name = "SHOW_MAX_WINDOW")
  private boolean           showMaxWindow;

  @Column(name = "DISPLAY_NAME", length = 200)
  private String            displayName;

  @Column(name = "EDIT_PERMISSION", length = 500)
  private String            editPermission;

  @Column(name = "PAGE_BODY", length = 5000)
  private String            pageBody = new JSONArray().toJSONString();
  
  public SiteType getOwnerType() {
    return ownerType;
  }

  public void setOwnerType(SiteType ownerType) {
    this.ownerType = ownerType;
  }

  public String getOwnerId() {
    return ownerId;
  }

  public void setOwnerId(String ownerId) {
    this.ownerId = ownerId;
  }

  public String getEditPermission() {
    return editPermission;
  }

  public void setEditPermission(String editPermission) {
    this.editPermission = editPermission;
  }

  public boolean isShowMaxWindow() {
    return showMaxWindow;
  }

  public void setShowMaxWindow(boolean showMaxWindow) {
    this.showMaxWindow = showMaxWindow;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getPageBody() {
    return pageBody;
  }

  public void setPageBody(String pageBody) {
    this.pageBody = pageBody;
  }

  public PageContext buildPageContext() {
    PageState state = new PageState(getDisplayName(),
                                    getDescription(),
                                    isShowMaxWindow(),
                                    getFactoryId(),
                                    convert(getAccessPermissions()),
                                    getEditPermission(),
                                    convert(getMoveAppsPermissions()),
                                    convert(getMoveContainersPermissions()));

    SiteKey siteKey = new SiteKey(getOwnerType(), getOwnerId());
    PageKey pageKey = new PageKey(siteKey, getName());

    PageContext context = new PageContext(pageKey, state);
    return context;
  }

  @Override
  public TYPE getType() {
    return TYPE.PAGE;
  }

}
