package org.exoplatform.portal.jdbc.dao;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityTransaction;

import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.portal.jdbc.entity.NavigationEntity;
import org.exoplatform.portal.jdbc.entity.NodeEntity;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.Visibility;

@ConfiguredBy({
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.portal.jdbc.test.configuration.xml") })
public class NavigationDAOTest extends AbstractKernelTest {
  private NavigationDAO     navigationDAO;
  
  private NodeDAO nodeDAO;

  private EntityTransaction transaction;

  @Override
  protected void setUp() throws Exception {
    begin();
    super.setUp();
    this.navigationDAO = getContainer().getComponentInstanceOfType(NavigationDAO.class);
    this.nodeDAO = getContainer().getComponentInstanceOfType(NodeDAO.class);

    EntityManagerService managerService = getContainer().getComponentInstanceOfType(EntityManagerService.class);
    transaction = managerService.getEntityManager().getTransaction();
    transaction.begin();
  }

  @Override
  protected void tearDown() throws Exception {
    if (transaction.isActive()) {
      transaction.rollback();
    }
    super.tearDown();
    end();
  }
  
  public void testCreateNav() {
    NavigationEntity nav = createNav("classic");
    navigationDAO.create(nav);
    
    NavigationEntity expected = navigationDAO.find(nav.getId());
    assertNotNull(expected);
    assertNav(expected, nav);
  }
  
  public void testCreateNode() {
    NodeEntity node1 = createNode("node1");
    
    NodeEntity node2 = createNode("node2");
    node2.setParent(node1);
    
    NodeEntity node3 = createNode("node3");
    node3.setParent(node1);
    
    node1.setChildren(Arrays.asList(node3, node2));    
    nodeDAO.create(node1);
    
    end();
    begin();
    
    NodeEntity expected = nodeDAO.find(node1.getId());
    assertNotNull(expected);
    assertNode(expected, node1);
    
    List<NodeEntity> children = expected.getChildren();
    assertEquals(2, children.size());
    assertNode(children.get(0), node3);
    assertNode(children.get(1), node2);
  }
  
  public void testUpdateNav() {
      NodeEntity node1 = createNode("node1");
      nodeDAO.create(node1);
      
      NavigationEntity nav = createNav("classic");
      nav.setRootNode(node1);
      navigationDAO.create(nav);
      
      end();
      begin();
      
      NavigationEntity expected = navigationDAO.find(nav.getId());
      assertNotNull(expected);
      expected.setOwnerId("testClassic");
      navigationDAO.update(expected);
  }

  /**
 * @return
 */
private NavigationEntity createNav(String ownerId) {
    NavigationEntity nav = new NavigationEntity();
    nav.setOwnerId(ownerId);
    nav.setOwnerType(SiteType.PORTAL);
    nav.setPriority(1);
    nav.setId(UUID.randomUUID().toString());
    return nav;
}

private void assertNode(NodeEntity expected, NodeEntity node) {
    assertEquals(expected.getEndTime(), node.getEndTime());
    assertEquals(expected.getIcon(), node.getIcon());
    assertEquals(expected.getIndex(), node.getIndex());
    assertEquals(expected.getLabel(), node.getLabel());
    assertEquals(expected.getName(), node.getName());
    assertEquals(expected.getStartTime(), node.getStartTime());
    assertEquals(expected.getId(), node.getId());
    assertEquals(expected.getVisibility(), node.getVisibility());    
  }

  private NodeEntity createNode(String name) {
    NodeEntity node = new NodeEntity();
    node.setId(UUID.randomUUID().toString());
    node.setName(name);
    node.setEndTime(1);
    node.setIcon("icon");
    node.setIndex(1);
    node.setLabel("label");
    node.setStartTime(2);
    node.setVisibility(Visibility.SYSTEM);
    return node;
  }

  private void assertNav(NavigationEntity expected, NavigationEntity nav) {
    assertEquals(expected.getOwnerId(), nav.getOwnerId());
    assertEquals(expected.getPriority(), nav.getPriority());
    assertEquals(expected.getId(), nav.getId());
    assertEquals(expected.getOwnerType(), nav.getOwnerType());    
  }
}
