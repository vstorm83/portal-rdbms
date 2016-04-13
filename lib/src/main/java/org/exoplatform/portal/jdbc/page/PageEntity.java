package org.exoplatform.portal.jdbc.page;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.exoplatform.commons.api.persistence.ExoEntity;
import org.exoplatform.portal.mop.page.PageContext;

@Entity
@ExoEntity
@Table(name = "PORTAL_PAGES")
@NamedQueries({
  @NamedQuery(name = "PageEntity.findByKey", query = "SELECT p FROM PageEntity p WHERE p.pageKey = :pageKey") })
public class PageEntity implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @SequenceGenerator(name = "SEQ_PORTAL_PAGES_ID", sequenceName = "SEQ_PORTAL_PAGES_ID")
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_PORTAL_PAGES_ID")
  @Column(name = "PAGE_ID")
  private Long              id;

  @Column(name = "KEY", length = 500, unique = true)
  private String            pageKey;

  @Column(name = "EDIT_PERMISSION", length = 500)
  private String            editPermission;

  @Column(name = "SHOW_MAX_WINDOW")
  private boolean           showMaxWindow;

  @Column(name = "FACTORY_ID", length = 200)
  private String            factoryId;

  @Column(name = "DISPLAY_NAME", length = 200)
  private String            displayName;

  @Column(name = "DESCRIPTION", length = 2000)
  private String            description;

  @ElementCollection
  @CollectionTable(name = "PORTAL_PAGE_ACCESS_PERMISSIONS", joinColumns = @JoinColumn(name = "PAGE_ID") )
  @Column(name = "ACCESS_PERMISSION_ID")
  private Set<String>      accessPermissions = new HashSet<String>();

  @ElementCollection
  @CollectionTable(name = "PORTAL_PAGE_MOVE_APPS", joinColumns = @JoinColumn(name = "PAGE_ID") )
  @Column(name = "MOVE_APP_ID")
  private Set<String>      moveAppsPermissions = new HashSet<String>();

  @ElementCollection
  @CollectionTable(name = "PORTAL_MOVE_CONTAINERS", joinColumns = @JoinColumn(name = "PAGE_ID") )
  @Column(name = "MOVE_CONTAINER_ID")
  private Set<String>      moveContainersPermissions = new HashSet<String>();

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getPageKey() {
    return pageKey;
  }

  public void setPageKey(String pageKey) {
    this.pageKey = pageKey;
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

  public String getFactoryId() {
    return factoryId;
  }

  public void setFactoryId(String factoryId) {
    this.factoryId = factoryId;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Set<String> getAccessPermissions() {
    return accessPermissions;
  }

  public void setAccessPermissions(Set<String> accessPermissions) {
    this.accessPermissions = accessPermissions;
  }

  public Set<String> getMoveAppsPermissions() {
    return moveAppsPermissions;
  }

  public void setMoveAppsPermissions(Set<String> moveAppsPermissions) {
    this.moveAppsPermissions = moveAppsPermissions;
  }

  public Set<String> getMoveContainersPermissions() {
    return moveContainersPermissions;
  }

  public void setMoveContainersPermissions(Set<String> moveContainersPermissions) {
    this.moveContainersPermissions = moveContainersPermissions;
  }

  public PageContext buildPageContext() {
    // TODO Auto-generated method stub
    return null;
  }

}
