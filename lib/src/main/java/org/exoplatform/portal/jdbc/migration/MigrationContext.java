/*
 * Copyright (C) 2016 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.portal.jdbc.migration;

public final class MigrationContext {
  public static final String PORTAL_RDBMS_MIGRATION_STATUS_KEY = "PORTAL_RDBMS_MIGRATION_DONE";
  public static final String PORTAL_RDBMS_SITE_MIGRATION_KEY = "PORTAL_RDBMS_SITE_MIGRATION_DONE";  
  public static final String PORTAL_RDBMS_SITE_CLEANUP_KEY = "PORTAL_RDBMS_SITE_CLEANUP_DONE";
    
  //
  private static boolean isDone = false;
  private static boolean isSiteDone = false;
  private static boolean isSiteCleanupDone = false;

  public static boolean isDone() {
    return isDone;
  }

  public static void setDone(boolean isDoneArg) {
    isDone = isDoneArg;
  }

  public static boolean isSiteDone() {
    return isSiteDone;
  }

  public static void setSiteDone(boolean isSiteDone) {
    MigrationContext.isSiteDone = isSiteDone;
  }

  public static boolean isSiteCleanupDone() {
    return isSiteCleanupDone;
  }

  public static void setSiteCleanupDone(boolean isSiteCleanupDone) {
    MigrationContext.isSiteCleanupDone = isSiteCleanupDone;
  }
}
