package lib;

import java.util.Arrays;
import java.util.HashSet;

import org.gatein.api.page.PageQuery;
import org.gatein.api.site.SiteType;

import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.portal.jdbc.page.PageDAO;
import org.exoplatform.portal.jdbc.page.PageEntity;
import org.exoplatform.portal.mop.page.PageKey;

@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.portal.jdbc.test.configuration.xml")  
})
public class PageDAOTest extends AbstractKernelTest {
  private PageDAO pageDAO;
  
  @Override
  protected void setUp() throws Exception {    
    begin();
    super.setUp();
    this.pageDAO = getService(PageDAO.class);
  }

  @Override
  protected void tearDown() throws Exception {
    pageDAO.deleteAll();
    super.tearDown();
    end();
  }

  public void testCreatePage() {
    PageEntity entity = createInstance("a::b::c", "testCreatePage", "create page description");
    pageDAO.create(entity);
    end();
    begin();
    
    PageEntity result = pageDAO.find(entity.getId());
    assertNotNull(result);
    assertPage(entity, result);
  }
  
  public void testFindByKey() {
    PageEntity entity = createInstance("a::b::c", "testPage", null);
    pageDAO.create(entity);
    end();
    begin();
    
    PageEntity result = pageDAO.findByKey("a::b::c");
    assertNotNull(result);
    assertPage(entity, result);
  }
  
  public void testFindByQuery() throws Exception {
    PageEntity page1 = createInstance("portal::b::c1", "aBc dEf", null);
    pageDAO.create(page1);    
    PageEntity page2 = createInstance("portal::b::c2", "Efg Hik", null);
    pageDAO.create(page2);    
    end();
    begin();
        
    PageQuery.Builder query1 = new PageQuery.Builder();    
    query1.withSiteType(SiteType.SITE).withSiteName("b").withDisplayName("ef");
    assertEquals(2, pageDAO.findByQuery(query1.build()).getSize());
    
    PageQuery.Builder query2 = new PageQuery.Builder();    
    query2.withSiteType(SiteType.SITE).withSiteName("b").withDisplayName("hik");
    assertEquals(1, pageDAO.findByQuery(query2.build()).getSize());
  }

  public <T> T getService(Class<T> clazz) {
    return (T) getContainer().getComponentInstanceOfType(clazz);
  }

  private PageEntity createInstance(String key, String displayName, String description) {
    PageEntity entity = new PageEntity();
    entity.setPageKey(key);
    entity.setDisplayName(displayName);
    entity.setDescription(description);
    entity.setShowMaxWindow(true);
    entity.setEditPermission("testEditPermission");
    return entity;
  }
  
  private void assertPage(PageEntity entity, PageEntity result) {
    assertEquals(entity.getId(), result.getId());
    assertEquals(entity.getDescription(), result.getDescription());
    assertEquals(entity.getDisplayName(), result.getDisplayName());
    assertEquals(entity.getEditPermission(), result.getEditPermission());
    assertEquals(entity.getAccessPermissions().size(), result.getAccessPermissions().size());
    assertEquals(entity.getFactoryId(), result.getFactoryId());
    assertEquals(entity.getPageKey(), result.getPageKey());
  }
}
