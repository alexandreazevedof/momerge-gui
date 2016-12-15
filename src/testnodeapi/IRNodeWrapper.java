/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package testnodeapi;

import edu.cmu.cs.fluid.ir.IRLocation;
import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.ir.IRSequence;
import edu.cmu.cs.fluid.ir.InsertionPoint;
import edu.cmu.cs.fluid.ir.PlainIRNode;
import edu.cmu.cs.fluid.ir.SlotInfo;
import edu.cmu.cs.fluid.tree.Tree;
import edu.cmu.cs.fluid.version.Version;
import edu.cmu.cs.fluid.version.VersionTracker;
import edu.cmu.cs.fluid.version.VersionedSlotFactory;
import edu.uwm.cs.molhado.util.Property;
import edu.uwm.cs.molhado.xml.simple.SimpleXmlParser;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.actions.RenameAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Index;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author chengt
 */
public class IRNodeWrapper extends Observable {

  private momergeui app;
  private SimpleXmlParser parser = null;
  private Tree tree = SimpleXmlParser.tree;
  private VersionTracker tracker;
  private IRNode node;
  private List<IRNodeWrapper> children;

  public IRNodeWrapper(momergeui app, SimpleXmlParser parser, IRNode node, VersionTracker tracker) {
    this.app = app;
    this.parser = parser;
    this.tracker = tracker;
    this.node = node;
    children = getChildren();
  }

  public IRNode getIRNode() {
    return node;
  }

  public String getName() {
    Version.saveVersion(tracker.getVersion());
    String name = node.getSlotValue(SimpleXmlParser.tagNameAttr);
    Version.restoreVersion();
    return name;
  }

  public int getUID() {
    Version.saveVersion(tracker.getVersion());
    int id = node.getSlotValue(SimpleXmlParser.mouidAttr);
    Version.restoreVersion();
    return id;
  }

  public List<IRNodeWrapper> getChildren() {
    if (children == null) {
      children = new Vector<IRNodeWrapper>();
      Version.saveVersion(tracker.getVersion());
      int numChildren = SimpleXmlParser.tree.numChildren(node);
      for (int i = 0; i < numChildren; i++) {
        children.add(new IRNodeWrapper(app, parser, tree.getChild(node, i), tracker));
      }
      Version.restoreVersion();
    }
    return children;
  }

  public void addChild(final IRNodeWrapper wrapper) {
    tracker.executeIn(new Runnable() {

      public void run() {
        IRNode newChild = wrapper.getIRNode();
        tree.addChild(node, newChild);
      }
    });
    children.add(wrapper);
    setChanged();
    notifyObservers();
  }

  public void newChild2(){
    final IRNode newNode = new PlainIRNode();
    tracker.executeIn(new Runnable() {
      public void run() {
        parser.initNode2(newNode, "noname", null);
        tree.addChild(node, newNode);
      }
    });
    children.add(new IRNodeWrapper(app, parser, newNode, tracker));
    setChanged();
    notifyObservers();
  }
//  public void newChild() {
//    final IRNode newNode = new PlainIRNode();
//    tracker.executeIn(new Runnable() {
//
//      public void run() {
//        newNode.setSlotValue(SimpleXmlParser.tagNameAttr, "noname");
//        IRSequence<Property> p = VersionedSlotFactory.prototype.newSequence(-1);
//        newNode.setSlotValue(SimpleXmlParser.attrListAttr, p);
//        tree.initNode(newNode);
//        tree.addChild(node, newNode);
//      }
//    });
//    children.add(new IRNodeWrapper(app, parser, newNode, tracker));
//    setChanged();
//    notifyObservers();
//  }

  public void addChild(final int index, final IRNodeWrapper wrapper) {
    tracker.executeIn(new Runnable() {

      public void run() {
        IRLocation loc = IRLocation.get(index);
        InsertionPoint ip = InsertionPoint.createBefore(loc);
        tree.insertSubtree(node, wrapper.getIRNode(), ip);
      }
    });
    children.add(index, wrapper);
    setChanged();
    notifyObservers();
  }

  public void remove(final IRNodeWrapper wrapper) {
    tracker.executeIn(new Runnable() {

      public void run() {
        tree.removeChild(node, wrapper.getIRNode());
      }
    });
    children.remove(wrapper);
    setChanged();
    notifyObservers();
  }

  public int numChildren() {
    Version.saveVersion(tracker.getVersion());
    int numChildren = tree.numChildren(node);
    Version.restoreVersion();
    return numChildren;
  }

  public void printChildren() {
    Version.saveVersion(tracker.getVersion());
    for (IRNodeWrapper n : getChildren()) {
      System.out.println(n.getName());
    }
    Version.restoreVersion();
  }

  //TODO: broken
  @Override
  public IRNodeWrapper clone() {
    final IRNode newNode = new PlainIRNode();
//    tracker.executeIn(new Runnable() {
//
//      public void run() {
//        tree.initNode(newNode);
//        List<SlotInfo> list = SimpleXmlParser.attrList;
//        for(SlotInfo si:list){
//          Object val = null;
//          if (node.valueExists(si)) {
//            val = node.getSlotValue(si);
//          }
//          if (val != null) {
//            newNode.setSlotValue(si, val);
//          }
//        }
//      }
//    });
//    List<IRNodeWrapper> list = this.getChildren();
//    List<IRNodeWrapper> cloneChildren = new Vector<IRNodeWrapper>();
//    for(IRNodeWrapper nw:list){
//      cloneChildren.add(nw.clone());
//    }
//    IRNodeWrapper newWrapperNode = new IRNodeWrapper(app, parser, newNode, tracker);
//    for(IRNodeWrapper nw:cloneChildren){
//      newWrapperNode.addChild(nw);
//      System.out.println(nw.getName());
//    }
//    return newWrapperNode;
		return null;
  }

  public void reorderChildren() {
    tracker.executeIn(new Runnable() {

      public void run() {
        tree.removeChildren(node);
        for (int i = 0; i < children.size(); i++) {
          tree.addChild(node, children.get(i).getIRNode());
        }
      }
    });
    setChanged();
    notifyObservers();
  }

  public void setName(final String s) {
    System.out.println("setname");
    tracker.executeIn(new Runnable() {

      public void run() {
        node.setSlotValue(SimpleXmlParser.tagNameAttr, s);
      }
    });
    setChanged();
    notifyObservers();
    app.merge();
  }
}


class IRVisualNode extends AbstractNode {

  private momergeui app;
  private TreeViewPanel pane;
  private IRNodeWrapper node;
  private Index indexImpl;

  public IRVisualNode(momergeui app, TreeViewPanel pane, IRNodeWrapper node) {
    super(new IRVisualChildren(app, pane,  node),  Lookups.singleton(node));
    this.app = app;
    this.pane = pane;
    this.pane.nodeMap.put(node.getIRNode(), this);
    node.addObserver((Observer) getChildren());
    indexImpl = ((IRVisualChildren) getChildren()).getIndex();
    this.node = node;
    setIconBaseWithExtension("testnodeapi/dot.gif");
  }

  @Override
  public String getDisplayName() {
    return "["+node.getUID() +"] " + node.getName();
  }

  private boolean isConflict = false;
  private int conflictNum = 0;

  public void unmarkAsConflict(){
    isConflict = false;
    setIconBaseWithExtension("testnodeapi/dot.gif");
    fireIconChange();
    fireDisplayNameChange(null, null);
  }

  public void markAsConflict(int n, String desc){
    isConflict= true;
    conflictNum = n;
    setShortDescription(desc);
    fireShortDescriptionChange(null,null);
    fireDisplayNameChange(null, null);
    setIconBaseWithExtension("testnodeapi/red.gif");
    fireIconChange();
  }

  @Override
  public String getHtmlDisplayName() {
    String displayName = null;
    if (isConflict){
      displayName = "<b>" + getDisplayName() +
              "</b> [" + conflictNum + "]" ;
    } else {
      displayName = getDisplayName();
    }
    return displayName;
  }


  @Override
  public void setName(String s) {
    String o = getDisplayName();
    node.setName(s);
    super.setName(s);
    this.fireNameChange(o, s);
  }

  @Override
  public <T extends Node.Cookie> T getCookie(Class<T> clazz) {
    if (clazz.isInstance(indexImpl)) {
      return clazz.cast(indexImpl);
    }
    Children ch = getChildren();
    if (clazz.isInstance(ch)) {
      return clazz.cast(ch);
    }
    return super.getCookie(clazz);
  }

  @Override
  public boolean canCut() {
    return true;
  }

  @Override
  public boolean canCopy() {
    return true;
  }

  @Override
  public boolean canDestroy() {
    return true;
  }

  @Override
  public boolean canRename() {
    return true;
  }

  @Override
  public SystemAction getDefaultAction() {
    return SystemAction.get(RenameAction.class);
  }

  @Override
  public Action[] getActions(boolean popup) {
    return new Action[]{
              //new PrintIndexAction(),
              new NewAction(),
              SystemAction.get(RenameAction.class),
              //null,
              //SystemAction.get(CopyAction.class),
              //SystemAction.get(CutAction.class),
              //    SystemAction.get(PasteAction.class),
              //    null,
              //    SystemAction.get(DeleteAction.class),
              new DeleteAction(),
              null,
              new MoveUpAction(), 
              new MoveDownAction(),
              new IgnoreNodeOrderAction(),
              new ResolveAttrConflict()};
    //new ChangeIcoAction()};
  }

  private class IgnoreNodeOrderAction extends AbstractAction{

    public IgnoreNodeOrderAction(){
      putValue(NAME, "Ignore order of node");
    }

    public void actionPerformed(ActionEvent e) {
    }
    
  }

  private class ResolveAttrConflict extends AbstractAction{
    public ResolveAttrConflict(){
      putValue(NAME, "Resolve Attr conflict");
    }
    public void actionPerformed(ActionEvent e){
      IRNodeWrapper w = getLookup().lookup(IRNodeWrapper.class);
      app.resolveAttrConflict(w.getIRNode());
    }
  }
  private class PrintIndexAction extends AbstractAction {

    public PrintIndexAction() {
      putValue(NAME, "IndexOf");
    }

    public void actionPerformed(ActionEvent e) {
      System.out.println(IRVisualNode.this.getDisplayName() + ":index=" +
              getParentNode().getCookie(Index.class).indexOf(IRVisualNode.this));
    }
  }

  private class ChangeIcoAction extends AbstractAction {

    public ChangeIcoAction() {
      putValue(NAME, "ChangeIco");
    }

    public void actionPerformed(ActionEvent e) {
      markAsConflict(2, "testing");
    }
  }
  private class NewAction extends AbstractAction {

    public NewAction() {
      putValue(NAME, "New child node");
    }

    public void actionPerformed(ActionEvent e) {
      IRNodeWrapper w = getLookup().lookup(IRNodeWrapper.class);
      w.newChild2();
    }
  }

  private class MoveUpAction extends AbstractAction {

    public MoveUpAction() {
      putValue(NAME, "Move up");
    }

    public void actionPerformed(ActionEvent e) {
      IRNodeWrapper w = getLookup().lookup(IRNodeWrapper.class);
      Node parent = getParentNode();
      Index indexer = parent.getCookie(Index.class);
      int index = indexer.indexOf(IRVisualNode.this);
      if (index == 0) return;
      indexer.moveUp(index);
      if (app.isInConflictMode()) app.merge();
    }
  }
  private class MoveDownAction extends AbstractAction {

    public MoveDownAction() {
      putValue(NAME, "Move down");
    }

    public void actionPerformed(ActionEvent e) {
      IRNodeWrapper w = getLookup().lookup(IRNodeWrapper.class);
      Node parent = getParentNode();
      Index indexer = parent.getCookie(Index.class);
      int index = indexer.indexOf(IRVisualNode.this);
      if (index == parent.getChildren().getNodesCount()-1) return;
      indexer.moveDown(index);
      if (app.isInConflictMode()) app.merge();
    }
  }
  private class DeleteAction extends AbstractAction {

    public DeleteAction() {
      putValue(NAME, "Delete");
    }

    public void actionPerformed(ActionEvent e) {
      IRNodeWrapper w = getLookup().lookup(IRNodeWrapper.class);
      IRNodeWrapper parent = getParentNode().getLookup().lookup(
              IRNodeWrapper.class);
      parent.remove(w);
      if (app.isInConflictMode()) app.merge();
    }
  }

  @Override
  public PasteType getDropType(Transferable t, final int action, final int index) {
    final Node dropNode = NodeTransfer.node(t, DnDConstants.ACTION_COPY_OR_MOVE
            + NodeTransfer.CLIPBOARD_CUT);
    return new PasteType() {

      @Override
      public Transferable paste() throws IOException {
        IRNodeWrapper parent = (IRNodeWrapper) dropNode.getParentNode().
                getLookup().lookup(IRNodeWrapper.class);
        final IRNodeWrapper dataNode = (IRNodeWrapper) dropNode.getLookup().
                lookup(IRNodeWrapper.class);
        if (dataNode == node) {
          return null;
        }
        if ((action & DnDConstants.ACTION_COPY) != 0) {
          IRNodeWrapper newNode = (IRNodeWrapper) dataNode.clone();
          if (index == -1) {
            node.addChild(newNode);
          } else {
            node.addChild(index, newNode);
          }
        } else if ((action & DnDConstants.ACTION_MOVE) != 0) {
          parent.remove(dataNode);
          if (index == -1) {
            node.addChild(dataNode);
          } else {
            node.addChild(index, dataNode);
          }
        }
      if (app.isInConflictMode()) app.merge();
        return null;

      }
    };
  }
}
class IRVisualChildren extends Index.KeysChildren<IRNodeWrapper>
        implements Observer {

  private momergeui app;
  private TreeViewPanel pane;
  private final IRNodeWrapper node;

  public IRVisualChildren(momergeui app, TreeViewPanel pane,  IRNodeWrapper node) {
    super(node.getChildren());
    this.app = app;
    this.pane = pane;
    this.node = node;
  }

  public void update(Observable o, Object arg) {
    update();
    refresh();
  }

  @Override
  protected void addNotify() {
    setKeys(node.getChildren());
  }

  @Override
  protected void reorder(final int[] perm) {
    synchronized (lock()) {
      super.reorder(perm);
      node.reorderChildren();
    }
  }

  @Override
  protected Node[] createNodes(IRNodeWrapper key) {
    IRVisualNode vn = new IRVisualNode(app, pane, key);
    return new Node[]{vn};
  }
}