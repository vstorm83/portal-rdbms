package org.exoplatform.portal.jdbc.entity;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.PrePersist;

import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import org.exoplatform.commons.api.persistence.ExoEntity;

@Entity
@ExoEntity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class ComponentEntity implements Serializable {
  private static final long  serialVersionUID     = 1181255637761644181L;

  @Id
  @Column(name = "ID", length = 200)
  private String             id;

  public static final String PERMISSION_SEPARATOR = ";";

  @Column(name = "ACCESS_PERMISSIONS", length = 2000)
  private String             accessPermissions;

  @PrePersist
  public void ensureId() {
    id = UUID.randomUUID().toString();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setAccessPermissions(String accessPermissions) {
    this.accessPermissions = accessPermissions;
  }

  public String getAccessPermissions() {
    return accessPermissions;
  }

  public JSONObject toJSON() {
    JSONObject obj = new JSONObject();
    obj.put("type", getType().name());
    obj.put("id", getId());
    return obj;
  }

  public abstract TYPE getType();

  public static enum TYPE {
    PAGE, CONTAINER, WINDOW
  }

  public static List<String> convert(String permissions) {
    List<String> results = new LinkedList<String>();
    if (permissions != null) {
      for (String per : permissions.split(PERMISSION_SEPARATOR)) {
        if (!per.isEmpty()) {
          results.add(per);
        }
      }
    }
    return results;
  }

  public static String convert(List<String> permissions) {
    StringBuilder result = new StringBuilder();
    if (permissions != null) {
      result.append(PERMISSION_SEPARATOR);
      result.append(StringUtils.join(permissions, PERMISSION_SEPARATOR));
      result.append(PERMISSION_SEPARATOR);
    }
    return result.toString();
  }
}
