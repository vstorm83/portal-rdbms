package org.exoplatform.portal.jdbc.service;

import java.util.Iterator;

import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.portal.config.TestDataStorage;
import org.exoplatform.portal.mop.QueryResult;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageService;

@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/portal-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.portal.jdbc.test.configuration.xml") })
public class TestModelStorage extends TestDataStorage {

  private PageService pageService;
  
  public TestModelStorage(String name) {
    super(name);
  }
  
  @Override
  public void setUp() throws Exception {    
    super.setUp();
    this.pageService = getContainer().getComponentInstanceOfType(PageService.class);
  }

  @Override
  protected void tearDown() throws Exception {
    QueryResult<PageContext> results = pageService.findPages(0, -1, null, null, null, null);
    Iterator<PageContext> iter = results.iterator();
    while (iter.hasNext()) {
      PageContext page = iter.next();
      pageService.destroyPage(page.getKey());
    }
    RequestLifeCycle.end();
  }
  
  public void testCreatePortal() throws Exception {
    
  }
  
  public void testPortalConfigSave() throws Exception {
    
  }
  
  public void testPortalConfigRemove() throws Exception {
    
  }
  
  
}
