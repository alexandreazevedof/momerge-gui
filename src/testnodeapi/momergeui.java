package testnodeapi;

import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.ir.IRSequence;
import edu.cmu.cs.fluid.tree.PropagateUpTree;
import edu.cmu.cs.fluid.version.Version;
import edu.cmu.cs.fluid.version.VersionMarker;
import edu.cmu.cs.fluid.version.VersionTracker;
import edu.uwm.cs.molhado.merge.IRTreeMerge;
import edu.uwm.cs.molhado.util.Attribute;
import edu.uwm.cs.molhado.util.AttributeList;
import edu.uwm.cs.molhado.util.Property;
import edu.uwm.cs.molhado.xml.simple.SimpleXmlParser;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Stack;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.Exceptions;

public class momergeui extends JDialog{

	private boolean standalone = true;
	private int nodeCount = 0;
	public static IRNode rootNode;
	public static VersionTracker[] trackers = new VersionTracker[4];
	public static TreeViewPanel[] treePanels = new TreeViewPanel[4];
	public static TextViewPanel[] textPanels = new TextViewPanel[4];
	public static JTabbedPane[] tabbedPanes = new JTabbedPane[4];
	JButton[] dumpButtons = new JButton[4];
	JButton[] loadButtons = new JButton[4];
	JButton[] syncButtons = new JButton[4];
	JButton[] saveButtons = new JButton[4];
	SimpleXmlParser parser = new SimpleXmlParser(0);

	public momergeui() {
		setTitle("Molhado SVG Merge Tool");
		Font f = Font.getFont("courier new");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(800, 600);
		SimpleXmlParser.tagNameAttr.addDefineObserver(SimpleXmlParser.changeRecord);
		//SimpleXmlParser.attrsSeqAttr.addDefineObserver(changeRecord);
		SimpleXmlParser.attrListAttr.addDefineObserver(SimpleXmlParser.changeRecord);
		SimpleXmlParser.tree.addObserver(SimpleXmlParser.changeRecord);
		//SimpleXmlParser.tree.addObserver(SimpleXmlParser.childrenChange);
		PropagateUpTree.attach(SimpleXmlParser.changeRecord, SimpleXmlParser.tree);

		initContent();
	}

	public momergeui(Version v0, Version v1, Version v2 , IRNode root){
		super((Frame) null, true);
		standalone = false;
		setTitle("Molhado SVG Merge Tool");
		Font f = Font.getFont("courier new");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setSize(800, 600);
		trackers[0] = new VersionMarker(v0);
		trackers[1] = new VersionMarker(v1);
		trackers[2] = new VersionMarker(v2);
	  this.rootNode = root;
		initContent();
		treePanels[0].updateContent(parser, rootNode, trackers[0]);
		treePanels[1].updateContent(parser, rootNode, trackers[1]);
		treePanels[2].updateContent(parser, rootNode, trackers[2]);
	}

	public VersionTracker[] getTrackers() {
		return trackers;
	}

	public void updateTree(final TreeViewPanel p, final String text,
					VersionTracker tracker) {
		try {
			tracker.executeIn(new Runnable() {

				public void run() {
					try {
						parser.parse(rootNode, text);
					} catch (Exception ex) {
						Exceptions.printStackTrace(ex);
					}
				}
			});
			p.updateContent(parser, rootNode, tracker);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateTree(TreeViewPanel treeview, TextViewPanel textview,
					VersionTracker tracker) {
		if (rootNode != null && tracker != null) {
			if (textview.isDirty()) {
				updateTree(treeview, textview.getText(), tracker);
			}
		}
	}

	private void updateText(TextViewPanel textview, VersionTracker tracker) {
		if (rootNode != null && tracker != null) {
			textview.updateContent(rootNode, tracker);
		}
	}

	public void resolveAttrConflict(final IRNode n) {
		AttrConflictDialog d = new AttrConflictDialog(this);
		d.setLocationRelativeTo(this);
		if (AttrConflictDialog.SAVE == d.editNodeAttributes(trackers[1],
						trackers[0], trackers[2], n)) {

			final AttributeList v1properties = d.getV1Content();
			trackers[1].executeIn(new Runnable() {

				public void run() {
					AttributeList seq = n.getSlotValue(SimpleXmlParser.attrListAttr);
					for (int j = 0; j < v1properties.size(); j++) {
						Attribute p = v1properties.get(j);
						for (int i = 0; i < seq.size(); i++) {
							Attribute p2 = seq.get(i);
							if (p.getName().equals(p2.getName())) {
								p2.setValue(p.getValue());
							}
						}
					}
				}
		});

			final AttributeList v2properties = d.getV2Content();
			trackers[2].executeIn(new Runnable() {

				public void run() {
					AttributeList seq = n.getSlotValue(SimpleXmlParser.attrListAttr);
					for (int j = 0; j < v2properties.size(); j++) {
						Attribute p = v2properties.get(j);
							for (int i = 0; i < seq.size(); i++) {
								Attribute p2 = seq.get(i);
								if (p.getName().equals(p2.getName())) {
									p2.setValue(p.getValue());
								}
							}
						}
					}
			});

			this.merge();

		}
	}

	private JTabbedPane makeTabbedPane(final TextViewPanel textPanel,
					final TreeViewPanel treePanel) {

		final JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				final IRNode root = rootNode;
				if (tabbedPane.getSelectedIndex() == 1) {
					Version.printVersionTree();
					updateText(textPanel, treePanel.tracker);
				} else if (tabbedPane.getSelectedIndex() == 0) {
					updateTree(treePanel, textPanel, treePanel.tracker);
				}
			}
		});
		tabbedPane.setBorder(new EmptyBorder(1, 1, 1, 1));
		tabbedPane.setAutoscrolls(true);
		tabbedPane.insertTab("Tree View", null, treePanel, "Tree view", 0);
		tabbedPane.insertTab("Text View", null, textPanel, "Text view", 1);

		return tabbedPane;
	}

	private JButton makeButton(String name, ActionListener l) {
		JButton b = new JButton(name);
		b.setFont(Font.getFont("courier new"));
		b.addActionListener(l);
		return b;
	}

	private void initContent() {
		JPanel[] vbuttonsPanels = new JPanel[4];
		for (int i = 0; i < 4; i++) {
			treePanels[i] = new TreeViewPanel(this, "v" + i);
			textPanels[i] = new TextViewPanel("v" + i);
			tabbedPanes[i] = makeTabbedPane(textPanels[i], treePanels[i]);
			loadButtons[i] = makeButton("Load", new LoadActionListener());
			dumpButtons[i] = makeButton("Dump", new DumpActionListener());
			saveButtons[i] = makeButton("Save", new SaveActionListener());
			syncButtons[i] = makeButton("Sync", new SyncActionListener());
			treePanels[i].hideContent();
			textPanels[i].hideContent();
			vbuttonsPanels[i] = new JPanel();
		}

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(1, 4));
		mainPanel.add(tabbedPanes[1]);
		mainPanel.add(tabbedPanes[0]);
		mainPanel.add(tabbedPanes[2]);
		mainPanel.add(tabbedPanes[3]);
		add(mainPanel, BorderLayout.CENTER);

		JButton newTreeButton = makeButton("New", new NewTreeActionListener());
		JButton importButton = makeButton("Import", new ImportActionListener());
		JButton mergeButton = makeButton("Merge", new MergeActionListener());

		if (standalone){
			//vbuttonsPanels[0].add(newTreeButton);
			vbuttonsPanels[0].add(importButton);
			vbuttonsPanels[0].add(mergeButton);
			vbuttonsPanels[0].add(saveButtons[0]);
			vbuttonsPanels[1].add(loadButtons[1]);
			vbuttonsPanels[1].add(syncButtons[1]);
			vbuttonsPanels[1].add(saveButtons[1]);
			vbuttonsPanels[2].add(loadButtons[2]);
			vbuttonsPanels[2].add(syncButtons[2]);
			vbuttonsPanels[2].add(saveButtons[2]);
			vbuttonsPanels[3].add(saveButtons[3]);

			JPanel buttonsPanel = new JPanel();
			buttonsPanel.setLayout(new GridLayout(1, 4));
			buttonsPanel.add(vbuttonsPanels[1]);
			buttonsPanel.add(vbuttonsPanels[0]);
			buttonsPanel.add(vbuttonsPanels[2]);
			buttonsPanel.add(vbuttonsPanels[3]);
			add(buttonsPanel, BorderLayout.SOUTH);
		} else {
			JPanel buttonsPanel = new JPanel();
			add(buttonsPanel, BorderLayout.SOUTH);
			JButton closeButton = new JButton("Done");
			buttonsPanel.add(closeButton);
			closeButton.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
				  momergeui.this.dispose();
				}
			});
		}
		pack();
	}

	private class SaveActionListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			JFileChooser chooser = new JFileChooser();
			chooser.setCurrentDirectory(new File("."));
			if (chooser.showSaveDialog(momergeui.this)
							!= JFileChooser.APPROVE_OPTION) {
				return;
			}
			File file = chooser.getSelectedFile();
			try {
				FileWriter fw = new FileWriter(file);
				VersionTracker tracker = null;
				if (e.getSource() == saveButtons[0]) {
					tracker = trackers[0];
				} else if (e.getSource() == saveButtons[1]) {
					tracker = trackers[1];
				} else if (e.getSource() == saveButtons[2]) {
					tracker = trackers[2];
				} else if (e.getSource() == saveButtons[3]) {
					tracker = trackers[3];
				}
				Version.saveVersion(tracker.getVersion());
				fw.write(SimpleXmlParser.toStringWithID(rootNode));
				fw.flush();
				fw.close();
				Version.restoreVersion();
			} catch (IOException ex) {
				Exceptions.printStackTrace(ex);
			}
		}
	}

	private class SyncActionListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			JFileChooser chooser = new JFileChooser();
			chooser.setCurrentDirectory(new File("."));
			if (chooser.showOpenDialog(momergeui.this)
							!= JFileChooser.APPROVE_OPTION) {
				return;
			}
			final File file = chooser.getSelectedFile();
			VersionTracker tracker = null;
			TreeViewPanel treePanel = null;
			TextViewPanel textPanel = null;
			JTabbedPane tabbedPane = null;
			if (e.getSource() == syncButtons[0]) {
				tracker = trackers[0];
				treePanel = treePanels[0];
				textPanel = textPanels[0];
				tabbedPane = tabbedPanes[0];
			} else if (e.getSource() == syncButtons[1]) {
				tracker = trackers[1];
				treePanel = treePanels[1];
				textPanel = textPanels[1];
				tabbedPane = tabbedPanes[1];
			} else if (e.getSource() == syncButtons[2]) {
				tracker = trackers[2];
				treePanel = treePanels[2];
				textPanel = textPanels[2];
				tabbedPane = tabbedPanes[2];
			} else if (e.getSource() == syncButtons[3]) {
				tracker = trackers[3];
				treePanel = treePanels[3];
				textPanel = textPanels[3];
				tabbedPane = tabbedPanes[3];
			}
			tracker.executeIn(new Runnable() {

				public void run() {
					try {
						parser.parse(rootNode, file);
					} catch (Exception ex) {
						Exceptions.printStackTrace(ex);
					}
				}
			});
			if (tabbedPane.getSelectedIndex() == 0) {
				treePanel.updateContent(parser, rootNode, tracker);
			} else {
				textPanel.updateContent(rootNode, tracker);
			}
		}
	}

	private VersionTracker importBaseFile(String f) throws IOException, Exception {
		Version.saveVersion(Version.getInitialVersion());
		parser.setMouid(0); //reset the uid to zero
		rootNode = parser.parse(new File(f));
		trackers[0] = new VersionMarker(Version.getVersion());
		return trackers[0];
	}

	private VersionTracker importFile1(final String f) throws IOException, Exception {
		Version.saveVersion(trackers[0].getVersion());
		Version.bumpVersion();
		Version v = Version.getVersion();
		trackers[1] = new VersionMarker(v);
		trackers[1].executeIn(new Runnable() {

			public void run() {
				try {
					parser.parse(rootNode, new File(f));
				} catch (Exception ex) {
					Exceptions.printStackTrace(ex);
				}
			}
		});
		Version.restoreVersion();
		return trackers[1];
	}

	private VersionTracker importFile2(final String f) throws IOException, Exception {
		Version.saveVersion(trackers[0].getVersion());
		Version.bumpVersion();
		Version v = Version.getVersion();
		trackers[2] = new VersionMarker(v);
		trackers[2].executeIn(new Runnable() {

			public void run() {
				try {
					parser.parse(rootNode, new File(f));
				} catch (Exception ex) {
					Exceptions.printStackTrace(ex);
				}
			}
		});
		Version.restoreVersion();
		return trackers[2];
	}

	private class ImportActionListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			JFileChooser chooser = new JFileChooser();
			chooser.setCurrentDirectory(new File("."));
			if (chooser.showOpenDialog(momergeui.this)
							!= JFileChooser.APPROVE_OPTION) {
				return;
			}
			File file = chooser.getSelectedFile();
			try {
				Version.saveVersion(Version.getInitialVersion());
				parser.setMouid(0); //reset the uid to zero
				rootNode = parser.parse(file);
				trackers[0] = new VersionMarker(Version.getVersion());
				treePanels[0].updateContent(parser, rootNode, trackers[0]);
				textPanels[0].updateContent(rootNode, trackers[0]);
				Version.restoreVersion();
				treePanels[1].hideContent();
				treePanels[2].hideContent();
				treePanels[3].hideContent();
			} catch (Exception ex) {
				Exceptions.printStackTrace(ex);
			}
		}
	}

	private class LoadActionListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			updateTree(treePanels[0], textPanels[0], trackers[0]);
			if (e.getSource() == loadButtons[1]) {
				Version.saveVersion(trackers[0].getVersion());
				Version.bumpVersion();
				Version v = Version.getVersion();
				trackers[1] = new VersionMarker(v);
				treePanels[1].updateContent(parser, rootNode, trackers[1]);
			} else if (e.getSource() == loadButtons[2]) {
				Version.saveVersion(trackers[0].getVersion());
				Version.bumpVersion();
				Version v = Version.getVersion();
				trackers[2] = new VersionMarker(v);
				treePanels[2].updateContent(parser, rootNode, trackers[2]);
			}
			treePanels[3].hideContent();
		}
	}

	private class DumpActionListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			VersionTracker tracker = null;
			if (e.getSource() == dumpButtons[1]) {
				tracker = trackers[1];
			} else if (e.getSource() == dumpButtons[0]) {
				tracker = trackers[0];
			} else if (e.getSource() == dumpButtons[2]) {
				tracker = trackers[2];
			} else if (e.getSource() == dumpButtons[3]) {
				tracker = trackers[3];
			}
			if (tracker == null) {
				return;
			}

			tracker.getVersion().executeIn(new Runnable() {

				public void run() {
					System.out.println(SimpleXmlParser.toString(rootNode));
				}
			});
		}
	}

	private class MergeActionListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			if (tabbedPanes[1].getSelectedIndex() == 1) {
				updateTree(treePanels[1], textPanels[1], trackers[1]);
			}
			if (tabbedPanes[2].getSelectedIndex() == 1) {
				updateTree(treePanels[2], textPanels[2], trackers[2]);
			}

//      Version.printVersionTree();
//
//			System.out.println("Version 0");
//		  Version.setVersion(trackers[0].getVersion());
//			System.out.println(SimpleXmlParser.toStringWithID(rootNode));
//			Version.restoreVersion();
//			Version.setVersion(trackers[1].getVersion());
//			System.out.println(SimpleXmlParser.toStringWithID(rootNode));
//			Version.restoreVersion();
//			Version.setVersion(trackers[2].getVersion());
//			System.out.println(SimpleXmlParser.toStringWithID(rootNode));
//			Version.restoreVersion();
//      System.out.println("v0=" + trackers[0].getVersion());
//      System.out.println("v1=" + trackers[1].getVersion());
//      System.out.println("v2=" + trackers[2].getVersion());
//      System.out.println("Merging started.");
			merge();
			System.out.println("Merging is done.");
		}
	}

	private class NewTreeActionListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			try {
				Version.saveVersion(Version.getInitialVersion());
				nodeCount = 0;
				parser.setMouid(0);
				rootNode = parser.parse("<root/>");
				trackers[0] = new VersionMarker(Version.getVersion());
				Version.restoreVersion();
				treePanels[0].updateContent(parser, rootNode, trackers[0]);
				treePanels[1].hideContent();
				treePanels[2].hideContent();
				treePanels[3].hideContent();
			} catch (Exception ex) {
				Exceptions.printStackTrace(ex);
			}
		}
	}

	private void clearNodeConflictMarkings() {

		if (conflicts == null) {
			return;
		}
		for (IRTreeMerge.ConflictInfo ci : conflicts) {
			IRVisualNode vnode0 = treePanels[0].nodeMap.get(ci.node);
			IRVisualNode vnode1 = treePanels[1].nodeMap.get(ci.node);
			IRVisualNode vnode2 = treePanels[2].nodeMap.get(ci.node);
			if (vnode0 != null) {
				vnode0.unmarkAsConflict();
			}
			if (vnode1 != null) {
				vnode1.unmarkAsConflict();
			}
			if (vnode2 != null) {
				vnode2.unmarkAsConflict();
			}
		}
	}

	private void expand(TreeViewPanel p, IRTreeMerge.ConflictInfo ci, Version v) {
		Version.saveVersion(v);
		Stack<IRNode> stack = new Stack<IRNode>();
		boolean found = false;
		IRVisualNode vn = null;
		IRNode n = ci.node;
		while (!found) {
			vn = p.nodeMap.get(n);
			if (vn != null) {
				found = true;
			} else {
				try {
					n = SimpleXmlParser.tree.getParent(n);
					stack.push(n);
				} catch (Exception e) {
					break;
				}
			}
		}
		Version.restoreVersion();

		if (found) {
			while (!stack.isEmpty()) {
				vn = p.nodeMap.get(stack.pop());
				p.treeView.expandNode(vn);
			}
		}
	}

	private void markNodeConflicts(Vector<IRTreeMerge.ConflictInfo> conflicts) {
		int i = 0;
		for (IRTreeMerge.ConflictInfo ci : conflicts) {

			i = i + 1;
			expand(treePanels[0], ci, trackers[0].getVersion());
			IRVisualNode vnode0 = treePanels[0].nodeMap.get(ci.node);
			if (vnode0 != null) {
				vnode0.markAsConflict(i, ci.description);
			}

			expand(treePanels[1], ci, trackers[1].getVersion());
			IRVisualNode vnode1 = treePanels[1].nodeMap.get(ci.node);
			if (vnode1 != null) {
				vnode1.markAsConflict(i, ci.description);
			}

			expand(treePanels[2], ci, trackers[2].getVersion());
			IRVisualNode vnode2 = treePanels[2].nodeMap.get(ci.node);
			if (vnode2 != null) {
				vnode2.markAsConflict(i, ci.description);
			}
		}
	}
	private boolean conflictMode = false;

	public boolean isInConflictMode() {
		return conflictMode;
	}
	private Vector<IRTreeMerge.ConflictInfo> conflicts = null;

	public void ignoreOrder(IRNode n) {
		//  treeMerge.ignoreOrder(n);
	}

	public Version merge() {
		clearNodeConflictMarkings();
		treePanels[3].hideContent();

		if (rootNode == null || trackers[1] == null || trackers[0] == null) {
			return null;
		}

		IRTreeMerge tm = new IRTreeMerge(SimpleXmlParser.tree,
						SimpleXmlParser.changeRecord, rootNode, trackers[1],
						trackers[0], trackers[2]);

		Version v3 = tm.merge();
		if (v3 == null) {
			conflictMode = true;
			conflicts = tm.getConflicts();
			markNodeConflicts(conflicts);
			return null;
		} else {
			trackers[3] = new VersionMarker(v3);
			treePanels[3].updateContent(parser, rootNode, trackers[3]);
			conflictMode = false;
			return v3;
		}
	}

	public static void main(String[] args) {

		new momergeui().setVisible(true);
	}
}
