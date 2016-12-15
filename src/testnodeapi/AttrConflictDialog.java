/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package testnodeapi;

import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.ir.IRSequence;
import edu.cmu.cs.fluid.version.Version;
import edu.cmu.cs.fluid.version.VersionTracker;
import edu.uwm.cs.molhado.merge.IRTreeMerge;
import edu.uwm.cs.molhado.util.Attribute;
import edu.uwm.cs.molhado.util.AttributeList;
import edu.uwm.cs.molhado.util.Property;
import edu.uwm.cs.molhado.xml.simple.SimpleXmlParser;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;

/**
 *
 * @author chengt
 */
public class AttrConflictDialog extends JDialog{

  private JTextArea textField1;
  private JTextArea textField0;
  private JTextArea textField2;
  private int action = 0;
	IRTreeMerge tm;

  public AttrConflictDialog(momergeui app){
    super(app);
    setModal(true);
    setTitle("Attribute Editor");
    setSize(600,200);
    initContent();
  }

  private void initContent(){
    JPanel labelsPanel = new JPanel();
    labelsPanel.setLayout(new GridLayout(1,3));
    labelsPanel.add(new JLabel("v1"));
    labelsPanel.add(new JLabel("v0"));
    labelsPanel.add(new JLabel("v2"));
    add(labelsPanel, BorderLayout.NORTH);

    JPanel textPanel = new JPanel();
    textPanel.setBorder(new LineBorder(Color.black));
    textPanel.setLayout(new GridLayout(1,3));
    textField1 = new JTextArea(10,20);
    textField1.setBorder(new LineBorder(Color.BLACK));

    textField0 = new JTextArea(10,20);
    textField0.setBorder(new LineBorder(Color.BLACK));
    textField2 = new JTextArea(10,20);
    textField2.setBorder(new LineBorder(Color.BLACK));
    textPanel.add(textField1);
    textPanel.add(textField0);
    textPanel.add(textField2);
    add(textPanel, BorderLayout.CENTER);

    JPanel buttonsPanel = new JPanel();
    JButton saveButton = new JButton("Save");
    JButton cancelButton = new JButton("Cancel");
    saveButton.addActionListener(new AbstractAction(){

      public void actionPerformed(ActionEvent e) {
        action = 1;
        //setVisible(false);
        dispose();
      }
    });

    cancelButton.addActionListener(new AbstractAction(){

      public void actionPerformed(ActionEvent e) {
        action = 2;
        dispose();
      }
    });
    buttonsPanel.add(saveButton);
    buttonsPanel.add(cancelButton);
    add(buttonsPanel, BorderLayout.SOUTH);

    pack();
  }

  public static final int SAVE = 1;
  public static final int CANCEL = 2;


  public AttributeList getV1Content(){
    String [] s = textField1.getText().split("\n");
    //Vector<Property> v = new Vector<Property>(s.length);
		AttributeList v = new AttributeList();
    for(String a:s){
      String []p = a.split("=");
      v.addAttribute(new Attribute(p[0], p[1]));
    }
    return v;
  }

  public AttributeList getV2Content(){

    String [] s = textField2.getText().split("\n");
    AttributeList v = new AttributeList();
    for(String a:s){
      String []p = a.split("=");
      v.addAttribute(new Attribute(p[0], p[1]));
    }
    return v;
  }

  public int editNodeAttributes(VersionTracker t1, VersionTracker t0, VersionTracker t2, IRNode n){
    action = 0;

    Version v0 = t0.getVersion();
    Version v1 = t1.getVersion();
    Version v2 = t2.getVersion();

    Vector<String> conflictAttrs = IRTreeMerge.conflictingAttributes2(v0, v1, v2,n);

		//System.out.println(conflictAttrs.size() + ":" + conflictAttrs);
		
    Version.saveVersion(v0);
    AttributeList seq0 = n.getSlotValue(SimpleXmlParser.attrListAttr);
    StringBuilder sb0 = new StringBuilder();
    for(int i=0; i<seq0.size(); i++){
      Attribute p = seq0.get(i);
			//System.out.println(p.getName() +":" + p.getValue());
      if (conflictAttrs.contains(p.getName())){
        sb0.append(p.getName()).append("=").append(p.getValue());
        sb0.append("\n");
      }
    }
    textField0.setText(sb0.toString());
    Version.restoreVersion();

    Version.saveVersion(v1);
    AttributeList list = n.getSlotValue(SimpleXmlParser.attrListAttr);
    StringBuilder sb1 = new StringBuilder();
    for(int i=0; i<list.size(); i++){
      Attribute p = list.get(i);
			//System.out.println(p.getName() +":" + p.getValue());
      if (conflictAttrs.contains(p.getName())){
        sb1.append(p.getName()).append("=").append(p.getValue());
        sb1.append("\n");
      }
    }
    textField1.setText(sb1.toString());

    Version.restoreVersion();

    Version.saveVersion(v2);
    AttributeList seq2 = n.getSlotValue(SimpleXmlParser.attrListAttr);
    StringBuilder sb2 = new StringBuilder();
    for(int i=0; i<seq2.size(); i++){
      Attribute p = seq2.get(i);
			//System.out.println(p.getName() +":" + p.getValue());
      if (conflictAttrs.contains(p.getName())){
        sb2.append(p.getName()).append("=").append(p.getValue());
        sb2.append("\n");
      }
    }
    textField2.setText(sb2.toString());

    Version.restoreVersion();
    setVisible(true);
    return action;
  }
//
//	//old way
//  public int editNodeAttributes2(VersionTracker t1, VersionTracker t0, VersionTracker t2, IRNode n){
//    action = 0;
//
//    Version v0 = t0.getVersion();
//    Version v1 = t1.getVersion();
//    Version v2 = t2.getVersion();
//
//    Vector<String> conflictAttrs = IRTreeMerge.conflictingAttributes2(v0, v1, v2, n);
//
//    Version.saveVersion(v0);
//    IRSequence<Property> seq0 = n.getSlotValue(SimpleXmlParser.attrsSeqAttr);
//    StringBuilder sb0 = new StringBuilder();
//    for(int i=0; i<seq0.size(); i++){
//      Property p = seq0.elementAt(i);
//      if (conflictAttrs.contains(p.getName())){
//        sb0.append(p.getName() + "=" + p.getValue());
//        sb0.append("\n");
//      }
//    }
//    textField0.setText(sb0.toString());
//    Version.restoreVersion();
//
//    Version.saveVersion(v1);
//    IRSequence<Property> seq1 = n.getSlotValue(SimpleXmlParser.attrsSeqAttr);
//    StringBuilder sb1 = new StringBuilder();
//    for(int i=0; i<seq1.size(); i++){
//      Property p = seq1.elementAt(i);
//      if (conflictAttrs.contains(p.getName())){
//        sb1.append(p.getName() + "=" + p.getValue());
//        sb1.append("\n");
//      }
//    }
//    textField1.setText(sb1.toString());
//
//    Version.restoreVersion();
//
//    Version.saveVersion(v2);
//    IRSequence<Property> seq2 = n.getSlotValue(SimpleXmlParser.attrsSeqAttr);
//    StringBuilder sb2 = new StringBuilder();
//    for(int i=0; i<seq2.size(); i++){
//      Property p = seq2.elementAt(i);
//      if (conflictAttrs.contains(p.getName())){
//        sb2.append(p.getName() + "=" + p.getValue());
//        sb2.append("\n");
//      }
//    }
//    textField2.setText(sb2.toString());
//
//    Version.restoreVersion();
//    setVisible(true);
//    return action;
//  }
//

  public static void main(String[] args){
    String []s = "a:10\nb:20\nc:30".split("\n");
    for(String a:s){
      System.out.println(a);
    }

  }




}
