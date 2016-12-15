/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package testnodeapi;

import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.version.VersionTracker;
import edu.uwm.cs.molhado.xml.simple.SimpleXmlParser;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Hashtable;
import javax.swing.ActionMap;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.text.DefaultEditorKit;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;

/**
 *
 * @author chengt
 */
public class TreeViewPanel extends JPanel implements ExplorerManager.Provider {

  private momergeui app;
  private JTabbedPane tabbedPane = new JTabbedPane();
  private JScrollPane scrollPane = new JScrollPane();
  protected BeanTreeView treeView = new BeanTreeView();
  protected ExplorerManager manager = new ExplorerManager();
  protected IRNodeWrapper root;
  protected VersionTracker tracker;
  protected Hashtable<IRNode, IRVisualNode> nodeMap =
          new Hashtable<IRNode, IRVisualNode>();

  public ExplorerManager getExplorerManager() {
    return manager;
  }

  public TreeViewPanel(momergeui app, String title) {
    setLayout(new BorderLayout());
    this.app = app;
    initContent(title);
  }

  public void updateContent(SimpleXmlParser parser, IRNode irRoot, VersionTracker tracker) {
    this.tracker = tracker;
    this.root = new IRNodeWrapper(app, parser, irRoot, tracker);
    manager.setRootContext(new IRVisualNode(app, this,root));
    //treeView.expandAll();
    treeView.setVisible(true);
  }

  public IRNode getIRRootNode() {
    return root.getIRNode();
  }
  public void hideContent(){
    treeView.setVisible(false);
  }

  private void initContent(String title) {
    setLayout(new BorderLayout());
    JPanel labelPanel = new JPanel();
    labelPanel.setLayout(new GridLayout(1, 1));
    JLabel label = new JLabel(title);
    label.setHorizontalAlignment(JLabel.CENTER);
    label.setFont(Font.getFont("courier new"));
    labelPanel.add(label);
    add(labelPanel, BorderLayout.NORTH);
    treeView.setPreferredSize(new Dimension(150, 400));
    treeView.setAutoscrolls(true);
   // treeView.setRootVisible(false);
    add(scrollPane, BorderLayout.CENTER);
    scrollPane.setViewportView(treeView);
    //treeView.expandAll();

    ActionMap map = treeView.getActionMap();
    map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(manager));
    map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(manager));
    map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(manager));
    map.put("delete", ExplorerUtils.actionDelete(manager, true));
  }
}
