/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package testnodeapi;

import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.version.Version;
import edu.cmu.cs.fluid.version.VersionTracker;
import edu.uwm.cs.molhado.xml.simple.SimpleXmlParser;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author chengt
 */
public class TextViewPanel extends JPanel {
  private JScrollPane scrollPane = new JScrollPane();
  private JTextArea textPane = new JTextArea();

  private IRNode irRoot;
  private VersionTracker tracker;
  public TextViewPanel(String title){
    initContent(title);
  }

  private boolean isDirty = false;

  public boolean isDirty(){
    return isDirty;
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
    scrollPane.setViewportView(textPane);
    textPane.setAutoscrolls(false);
    textPane.setFont(Font.getFont("courier new"));
    textPane.addKeyListener(new KeyListener(){
      public void keyTyped(KeyEvent e) { isDirty = true; }
      public void keyPressed(KeyEvent e) { }
      public void keyReleased(KeyEvent e) { }
    });
    add(scrollPane, BorderLayout.CENTER);
  }


  public String getText(){
    return textPane.getText();
  }

  public void updateContent(IRNode irRoot, VersionTracker tracker) {
    this.irRoot = irRoot;
    Version.saveVersion(tracker.getVersion());
    textPane.setText(SimpleXmlParser.toStringWithID(irRoot));
    Version.restoreVersion();
    textPane.setVisible(true);
  }
  public void hideContent(){
    textPane.setVisible(false);
  }
  
}
