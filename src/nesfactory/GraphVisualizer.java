package nesfactory;

import java.awt.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.*;

import flowmanager.FlowManager;
import network.NetworkElement;
import network.NetworkElementsLoader;
import org.jgraph.JGraph;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;

import org.jgrapht.ListenableGraph;
import org.jgrapht.WeightedGraph;
import org.jgrapht.alg.BellmanFordShortestPath;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.ListenableDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.ListenableDirectedWeightedGraph;
import org.jgrapht.graph.ListenableUndirectedGraph;
import topology.TopologyLink;
import topology.TopologyLoader;
import trafficmatrix.FileLoaderMatrix;
import trafficmatrix.SparseMatrix;

/**
 * A demo applet that shows how to use JGraph to visualize JGraphT graphs.
 *
 * @author Barak Naveh
 *
 * @since Aug 3, 2003
 */
public class GraphVisualizer extends JApplet implements ActionListener{
    private static final Color     DEFAULT_BG_COLOR = Color.decode( "#FAFBFF" );
    private static final Dimension DEFAULT_SIZE = new Dimension( 1024, 600 );
    ConcurrentHashMap<String,NetworkElement> networkElementList;
    SparseMatrix matrix;
    ListenableUndirectedGraph<NetworkElement,TopologyLink> topo;
    FlowManager flowManager;//=new FlowManager(networkElementList,topo);
    static String outDump="/home/amauri/mininet/log/sp.log";
    //
    private JGraphModelAdapter m_jgAdapter;
    private JGraph jgraph;
    //buttons
    Button btnApplyFlows;
    Button btnDelFlows;
    Button btnAnalizeTraffic;
    Button btnReload;
    PopupMenu popupMenu;

    /**
     * @see java.applet.Applet#init().
     */
    public void init(  ) {


        initGraph();

//        MenuItem mi;
//        popupMenu=new PopupMenu("Basic Actions");
//        popupMenu.add(mi=new MenuItem("Apply flows"));
//        mi.addActionListener(this);
//        mi.setName("applyflows");
//
//        popupMenu.add(mi=new MenuItem("Delete flows"));
//        mi.addActionListener(this);
//        mi.setName("delflows");
//
//        popupMenu.add(mi=new MenuItem("Analise New Traffic Matrix"));
//        mi.addActionListener(this);
//        mi.setName("newmatrix");
//
//
//        enableEvents (AWTEvent.MOUSE_EVENT_MASK);

        jgraph = new JGraph( m_jgAdapter );

        setLayout(new FlowLayout());


        //frame.add(popupMenu);

        //jgraph.add(popupMenu);
        jgraph.setAutoscrolls(true);

        adjustDisplaySettings(jgraph);
        getContentPane().add( jgraph );
        resize( DEFAULT_SIZE );
        //set weigths and calculate shortest path
        applyTrafficMatrix();
        drawTopology();

        List<String> flows=flowManager.getAllFlows();
        //System.out.println(flows);
        btnApplyFlows=new Button("Apply Flows");
        btnApplyFlows.addActionListener(this);
        btnApplyFlows.setName("applyflows");

        btnDelFlows=new Button("Delete Flows");
        btnDelFlows.addActionListener(this);
        btnDelFlows.setName("delflows");

        btnAnalizeTraffic=new Button("Analise New Traffic Matrix");
        btnAnalizeTraffic.addActionListener(this);
        btnAnalizeTraffic.setName("newmatrix");

        btnReload=new Button("Reload Flows");
        btnReload.addActionListener(this);
        btnReload.setName("reload");

        getContentPane().add(btnApplyFlows);
        getContentPane().add(btnDelFlows);
        getContentPane().add(btnAnalizeTraffic);
        getContentPane().add(btnReload);
        getContentPane().add(jgraph);


        //flowManager.applyAllFlows();


    }
    public void initGraph(){
        matrix=loadMatrix();
        //ConcurrentHashMap<String,NetworkElement> networkElementList=loadNetworkDic();
        networkElementList=loadNetworkDic();
        topo=loadTopology(networkElementList);
        flowManager=new FlowManager(networkElementList,topo);
        // create a visualization using JGraph, via an adapter
        m_jgAdapter = new JGraphModelAdapter( topo );
    }

    public void actionPerformed(ActionEvent e){
        if (e.getSource()==btnApplyFlows){
              flowManager.applyAllFlows();
        }
        else if (e.getSource()==btnDelFlows){
            flowManager.deleteAllFlows();

        }
        else if (e.getSource()==btnAnalizeTraffic){
            reloadMatrix();
            jgraph.getGraphLayoutCache().reload();
            jgraph.repaint();

        }
        else if (e.getSource()==btnReload){
            flowManager.deleteAllFlows();
            reloadMatrix();
            jgraph.getGraphLayoutCache().reload();
            jgraph.repaint();
            flowManager.applyAllFlows();

        }

    }
    protected void processMouseEvent (MouseEvent e) {
        if (e.isPopupTrigger())
            if (e.getComponent().getName()=="applyflows")  {
                flowManager.applyAllFlows();
            }
        else if (e.getComponent().getName()=="delflows")  {
            flowManager.deleteAllFlows();
        }
            else if (e.getComponent().getName()=="newmatrix")  {
                reloadMatrix();
            }
        super.processMouseEvent (e);
    }
    private void adjustDisplaySettings( JGraph jg ) {
        jg.setPreferredSize( DEFAULT_SIZE );

        Color  c        = DEFAULT_BG_COLOR;
        String colorStr = null;

        try {
            colorStr = getParameter( "bgcolor" );
        }
        catch( Exception e ) {}

        if( colorStr != null ) {
            c = Color.decode( colorStr );
        }

        jg.setBackground( c );

        //Font font=new Font("Arial",Font.TRUETYPE_FONT,12);
        //jg.setFont(font);
    }


    private void positionVertexAt( String vertex, int x, int y ) {
        DefaultGraphCell cell = m_jgAdapter.getVertexCell( networkElementList.get(vertex) );
        Map              attr = cell.getAttributes(  );
        Rectangle2D b    = GraphConstants.getBounds(attr);

        GraphConstants.setBounds( attr, new Rectangle( x, y, (int)b.getWidth()-60, (int)b.getHeight() ) );

        Map cellAttr = new HashMap(  );
        cellAttr.put( cell, attr );
        m_jgAdapter.edit(cellAttr, null, null, null);

    }

    private void drawTopology(){
        int y=50;
        int dx=50;
        int x=0;

        String temp="";
        //core switches
        positionVertexAt("sc0",150,y);
        positionVertexAt("sc1",350,y);
        positionVertexAt("sc2",500,y);
        positionVertexAt("sc3",650,y);
        //agg switches
        y=200;
        positionVertexAt("sa100",50,y);
        positionVertexAt("sa101",150,y);
        positionVertexAt("sa102",300,y);
        positionVertexAt("sa103",400,y);
        positionVertexAt("sa104",500,y);
        positionVertexAt("sa105",600,y);
        positionVertexAt("sa106",700,y);
        positionVertexAt("sa107",800,y);
        //access switches
        y=350;

        positionVertexAt("s0",50,y);
        positionVertexAt("s1",150,y+30);
        positionVertexAt("s2",300,y);
        positionVertexAt("s3",400,y+30);
        positionVertexAt("s4",500,y);
        positionVertexAt("s5",650,y+30);
        positionVertexAt("s6",750,y);
        positionVertexAt("s7",850,y+30);

        //hosts
        y=500;
        x=0;
        dx=30;
        int dy=30;
        for (int i=0;i<32;i++){
            y=y+dy;
            temp="h" + String.valueOf(i);
            positionVertexAt(temp,x+i*dx,y);
            dy=dy*-1;
        }

    }

    private SparseMatrix loadMatrix(){
        String filename=  "/home/amauri/mininet/scripts/trafficmatrix.txt";
        try{
            SparseMatrix m=new SparseMatrix();
            FileLoaderMatrix matrixLoader=new FileLoaderMatrix(m,filename);
            return m;
        }
        catch (Exception e){
            System.out.print (e);
            return null;
        }
    }
    private void reloadMatrix(){
        matrix.clear();
        //set all edges to 0
        int links=topo.edgeSet().size();
        int porta,portb;
        TopologyLink link=null;
        Iterator<TopologyLink> linkIterator=topo.edgeSet().iterator();
        while (linkIterator.hasNext()) {
            link=linkIterator.next();
            porta=link.getLocalPort();
            portb=link.getRemotePort();
            link.reconfigureLink(porta,portb,0);
        }

        matrix=loadMatrix();
        applyTrafficMatrix();

    }
    private ConcurrentHashMap<String,NetworkElement> loadNetworkDic(){
        String filename=  "/home/amauri/mininet/scripts/network_elements.txt";
        try{
            ConcurrentHashMap<String,NetworkElement> listNe;
            NetworkElementsLoader neLoader=new NetworkElementsLoader(filename);
            listNe=neLoader.Load();
            return listNe;
        }
        catch (Exception e){
            System.out.print (e);
            return null;
        }
    }

    private ListenableUndirectedGraph<NetworkElement,TopologyLink> loadTopology(ConcurrentHashMap<String,NetworkElement> networkElements){
        String filename=  "/home/amauri/mininet/scripts/topo.txt";
        TopologyLoader topologyLoader=new TopologyLoader(filename,networkElements);
        try{
            return topologyLoader.Load();
        }
        catch (Exception e){
            System.out.print (e);
            return null;
        }
    }

    private void applyTrafficMatrix(){

        String hosta="";
        String hostb="";
        NetworkElement elementa;
        NetworkElement elementb;
        String spliter=matrix.getKeysSeparator();
        List<String> keys=matrix.getRowColums();
        String temp="";
        TopologyLink link;
        List <TopologyLink> shortest_path;
        BellmanFordShortestPath bellmanFordShortestPath;

        PrintWriter writer= null;
        try {
            writer = new PrintWriter(outDump);
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        double weight=0;
        for (int i=0;i<keys.size();i++){
            temp=keys.get(i);
            String hosts[]=temp.split(spliter);
            elementa=networkElementList.get(hosts[0]);
            elementb=networkElementList.get(hosts[1]);
            weight=matrix.get(temp);
            //shortest_path = DijkstraShortestPath.findPathBetween(topo, elementa, elementb);
            bellmanFordShortestPath=new BellmanFordShortestPath(topo,elementa,6);
            shortest_path = bellmanFordShortestPath.getPathEdgeList(elementb);


            for (int j=0;j<shortest_path.size();j++){
                link=shortest_path.get(j);
                link.setWeight(link.getWeight()+weight);
            }

            writer.write("\n" + hosts[0] + " to " + hosts[1] + ";links:" + shortest_path.size() + ";weight:" + String.format("%.1f",weight) + ";");
            for (int j=0;j<shortest_path.size();j++){
                writer.write(shortest_path.get(j).getNetworkElementsName() + "(" + String.format("%.1f",shortest_path.get(j).getWeight())  +") ");
            }


            if (shortest_path.size()>6){
                System.out.println("Path " + hosts[0] + " to " + hosts[1] + " links:" + shortest_path.size());
                printPaths(shortest_path);
            }


            flowManager.createFlows(shortest_path,elementa,elementb);
        }
        writer.close();

    }
    private void printPaths(List <TopologyLink> links){
        for (int i=0;i<links.size();i++){
            System.out.print(topo.getEdgeSource(links.get(i)).getName());
            System.out.print(" to: " + topo.getEdgeTarget(links.get(i)).getName());
            System.out.println(" weight: " + String.valueOf(links.get(i).getWeight()));
        }
    }
}

