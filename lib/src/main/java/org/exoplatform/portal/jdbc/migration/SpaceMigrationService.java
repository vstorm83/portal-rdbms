package org.exoplatform.portal.jdbc.migration;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.exoplatform.commons.api.event.EventManager;
import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;
import org.exoplatform.portal.pom.data.PortalData;

@Managed
@ManagedDescription("Portal migration Sites from JCR to RDBMS.")
@NameTemplate({@Property(key = "service", value = "portal"), @Property(key = "view", value = "migration-sites") })
public class SpaceMigrationService extends AbstractMigrationService<PortalData> {
  public static final String EVENT_LISTENER_KEY = "PORTAL_SITES_MIGRATION";
  private String spaceQuery;

  public SpaceMigrationService(InitParams initParams,
                                      EventManager<PortalData, String> eventManager,
                                      EntityManagerService entityManagerService) {

    super(initParams, eventManager, entityManagerService);
    this.LIMIT_THRESHOLD = getInteger(initParams, LIMIT_THRESHOLD_KEY, 200);
  }

  @Override
  protected void beforeMigration() throws Exception {
    MigrationContext.setSiteDone(false);
  }

  @Override
  @Managed
  @ManagedDescription("Manual to start run migration data of sites from JCR to RDBMS.")
  public void doMigration() throws Exception {
      boolean begunTx = startTx();
      long offset = 0;

      long t = System.currentTimeMillis();
      try {
        LOG.info("| \\ START::Sites migration ---------------------------------");
        NodeIterator nodeIter  = getSiteNodes(offset, LIMIT_THRESHOLD);
        if(nodeIter == null || nodeIter.getSize() == 0) {
          return;
        }
        
        Node siteNode = null;
        while (nodeIter.hasNext()) {
          if(forkStop) {
            break;
          }
          siteNode = nodeIter.nextNode();
          LOG.info(String.format("|  \\ START::site number: %s (%s site)", offset, siteNode.getName()));
          long t1 = System.currentTimeMillis();

          PortalData space = migrateSite(siteNode);
          
          //
          offset++;
          if (offset % LIMIT_THRESHOLD == 0) {
            endTx(begunTx);
            RequestLifeCycle.end();
            RequestLifeCycle.begin(PortalContainer.getInstance());
            begunTx = startTx();
            nodeIter  = getSpaceNodes(offset, LIMIT_THRESHOLD);
          }
          
          broadcastListener(space, space.getKey().toString());
          LOG.info(String.format("|  / END::site number %s (%s site) consumed %s(ms)", offset - 1, siteNode.getName(), System.currentTimeMillis() - t1));
        }
        
      } finally {
        endTx(begunTx);
        RequestLifeCycle.end();
        RequestLifeCycle.begin(PortalContainer.getInstance());
        LOG.info(String.format("| / END::Site migration for (%s) site(s) consumed %s(ms)", offset, System.currentTimeMillis() - t));
      }
  }
  
  private PortalData migrateSite(Node spaceNode) {
    // TODO Auto-generated method stub
    return null;
  }

  private NodeIterator getSiteNodes(long offset, int lIMIT_THRESHOLD) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected void afterMigration() throws Exception {
    if(forkStop) {
      return;
    }
    MigrationContext.setSiteDone(true);
  }

  public void doRemove() throws Exception {
    LOG.info("| \\ START::cleanup Spaces ---------------------------------");
    long t = System.currentTimeMillis();
    long timePerSpace = System.currentTimeMillis();
    RequestLifeCycle.begin(PortalContainer.getInstance());
    int offset = 0;
    try {
      NodeIterator nodeIter  = getSpaceNodes(offset, LIMIT_THRESHOLD);
      if(nodeIter == null || nodeIter.getSize() == 0) {
        return;
      }

      while (nodeIter.hasNext()) {
        Node node = nodeIter.nextNode();
        LOG.info(String.format("|  \\ START::cleanup Space number: %s (%s space)", offset, node.getName()));
        offset++;

        node.remove();

        LOG.info(String.format("|  / END::cleanup (%s space) consumed time %s(ms)", node.getName(), System.currentTimeMillis() - timePerSpace));
        
        timePerSpace = System.currentTimeMillis();
        if(offset % LIMIT_THRESHOLD == 0) {
          getSession().save();
          RequestLifeCycle.end();
          RequestLifeCycle.begin(PortalContainer.getInstance());
          nodeIter = getSpaceNodes(offset, LIMIT_THRESHOLD);
        }
      }
      LOG.info(String.format("| / END::cleanup Spaces migration for (%s) space consumed %s(ms)", offset, System.currentTimeMillis() - t));
    } finally {
      getSession().save();
      RequestLifeCycle.end();
    }
  }

  @Override
  @Managed
  @ManagedDescription("Manual to stop run miguration data of spaces from JCR to RDBMS.")
  public void stop() {
    super.stop();
  }

  protected String getListenerKey() {
    return EVENT_LISTENER_KEY;
  }
  
  
  private NodeIterator getSpaceNodes(long offset, int lIMIT_THRESHOLD) {
    if(spaceQuery == null) {
      spaceQuery = new StringBuffer().append("SELECT * FROM soc:spacedefinition").toString();
    }
    return nodes(spaceQuery, offset, LIMIT_THRESHOLD);
  }  
}
