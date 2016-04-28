/*
 * Copyright (C) 2016 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.portal.jdbc.entity;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import org.exoplatform.commons.api.persistence.ExoEntity;
import org.exoplatform.portal.config.UserACL;
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
public class PageEntity extends ComponentEntity implements Serializable {

  private static final long serialVersionUID = -6195451978995765259L;

  @Column(name = "OWNER_TYPE")
  private SiteType          ownerType;

  @Column(name = "OWNER_ID", length = 200)
  private String            ownerId;

  @Column(name = "SHOW_MAX_WINDOW")
  private boolean           showMaxWindow;

  @Column(name = "DISPLAY_NAME", length = 200)
  private String            displayName;
  
  @Column(name = "NAME", length = 200)
  private String                name;
  
  @Column(name = "DESCRIPTION", length = 2000)
  private String                description;
  
  @Column(name = "FACTORY_ID", length = 200)
  private String                factoryId;

  @Column(name = "EDIT_PERMISSION", length = 500)
  private String            editPermission;

  @Column(name = "PAGE_BODY", length = 5000)
  private String            pageBody = new JSONArray().toJSONString();
  
  @Transient
  private List<ComponentEntity> children         = new LinkedList<ComponentEntity>();
  
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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getFactoryId() {
    return factoryId;
  }

  public void setFactoryId(String factoryId) {
    this.factoryId = factoryId;
  }

  public List<ComponentEntity> getChildren() {
    return children;
  }

  public void setChildren(List<ComponentEntity> children) {
    this.children = children;
  }

  @Override
  public JSONObject toJSON() {
    JSONObject obj = super.toJSON();

    JSONArray jChildren = new JSONArray();
    for (ComponentEntity child : getChildren()) {
      jChildren.add(child.toJSON());
    }
    obj.put("children", jChildren);

    return obj;
  }

  @Override
  public TYPE getType() {
    return TYPE.PAGE;
  }

}
