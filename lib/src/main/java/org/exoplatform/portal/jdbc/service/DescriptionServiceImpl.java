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

package org.exoplatform.portal.jdbc.service;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.mop.description.DescriptionService;

public class DescriptionServiceImpl implements DescriptionService {

    public Described.State resolveDescription(String id, Locale locale) throws NullPointerException {
        return resolveDescription(id, null, locale);
    }

    public Described.State resolveDescription(String id, Locale locale2, Locale locale1) throws NullPointerException {
        return null;
    }

    public Described.State getDescription(String id, Locale locale) {
        return null;
    }

    public void setDescription(String id, Locale locale, Described.State description) {

    }

    public Described.State getDescription(String id) {
        return null;
    }

    public void setDescription(String id, Described.State description) {

    }

    public Map<Locale, Described.State> getDescriptions(String id) {
        return Collections.emptyMap();
    }

    public void setDescriptions(String id, Map<Locale, Described.State> descriptions) {
    }
}
