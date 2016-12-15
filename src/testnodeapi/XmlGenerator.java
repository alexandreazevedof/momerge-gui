package testnodeapi;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class XmlGenerator {


  private int breadth;
  private int depth;
  private int attrs;
  private int valCount;
  private int nodeCount;
  private int limit;

  public XmlGenerator(int limit, int breadth, int depth, int attrs){
    this.limit = limit;
    this.breadth = breadth;
    this.depth = depth;
    this.attrs = attrs;
  }

  private void indent(FileWriter w, int spaces) throws IOException{
    for(int i=0; i<spaces; i++){
      w.write(' ');
    }
  }

  public void generate(File f) throws IOException{
    FileWriter w = new FileWriter(f);
    w.write("<n0");
    w.write(" moumid=\"" + 0 + "\"");
    generateAttrs(w, false);
    w.write(">\n");
    generate(w, 2, breadth, depth, false);
    w.write("</n1>\n");
    w.flush();
    w.close();
  }
  private void generateAttrs(FileWriter w, boolean random) throws IOException{
    for(int i=0; i<attrs; i++){
      w.write(" a" + i + "=\"" + (valCount++)+"\"");
    }
  }

  private void generate(FileWriter w, int indent, int b, int d, boolean random)
          throws IOException{
    if (d == 1) return;
    for(int i=0; i<b && nodeCount < limit; i++){
      int n = nodeCount+=1;
      indent(w, indent+2);
      w.write("<n"+n);
      w.write(" moumid=\"" + nodeCount + "\"");
      generateAttrs(w, false);
      w.write(">\n");
      generate(w, indent+2, b, d-1, false);
      indent(w, indent+2);
      w.write("</n"+n+">\n");
    }
  }

  public static void main(String[] args) throws IOException{
    XmlGenerator g = new XmlGenerator(2000, 4,10,4);
    g.generate(new File("2000.xml"));
    System.out.println("2000.xml done");
    XmlGenerator g2 = new XmlGenerator(4000, 4,10,4);
    g2.generate(new File("4000.xml"));
    System.out.println("4000.xml done");
    XmlGenerator g3 = new XmlGenerator(6000, 4,10,4);
    g3.generate(new File("6000.xml"));
    System.out.println("6000.xml done");
    XmlGenerator g4 = new XmlGenerator(8000, 4,10,4);
    g4.generate(new File("8000.xml"));
    System.out.println("8000.xml done");
    XmlGenerator g5 = new XmlGenerator(10000, 4,10,4);
    g5.generate(new File("10000.xml"));
    System.out.println("10000.xml done");
  }
}
