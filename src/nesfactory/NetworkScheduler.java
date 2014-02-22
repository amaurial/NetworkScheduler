package nesfactory;

import flowmanager.FlowManager;
import network.NetworkElementsLoader;
import network.NetworkElement;
import org.jgrapht.alg.BellmanFordShortestPath;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.ListenableUndirectedGraph;
import topology.TopologyLink;
import topology.TopologyLoader;
import trafficmatrix.*;

import java.io.*;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: amauri
 * Date: 15/10/13
 * Time: 13:01
 * To change this template use File | Settings | File Templates.
 */
public class NetworkScheduler {


    static ConcurrentHashMap<String,NetworkElement> networkElementList;
    static SparseMatrix matrix;
    static ListenableUndirectedGraph<NetworkElement,TopologyLink> topo;
    static FlowManager flowManager;//=new FlowManager(networkElementList,topo);
    static String outDump="/home/amauri/mininet/log/sp.log";
    static String flowsDump="/home/amauri/mininet/log/flows.log";


    public static void main(String args[]){

        String IP=args[0];
        int PORT=Integer.parseInt(args[1]);

        initGraph();
        flowManager.setControlerIP(IP);
        flowManager.setControlerPort(PORT);
        applyTrafficMatrix();



        List<String> flows=flowManager.getAllFlows();

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String option;

        while(true){


            System.out.println("Option <applyflows,delflows,reloadmatrix,reloadflows,<flows:hosta;hostb>,quit> : ");
            try {
                option = br.readLine();
                if (option.contains("applyflows")){
                    flowManager.applyAllFlows();

                }
                else if (option.contains("delflows")){
                    flowManager.deleteAllFlows();

                }
                else if (option.contains("reloadmatrix")){
                    reloadMatrix();
                }
                else if (option.contains("reloadflows")){
                    flowManager.deleteAllFlows();
                    reloadMatrix();
                    flowManager.applyAllFlows();
                }
                else if (option.contains("flows:")){
                    if ((option.contains("all"))){
                        dumpAllFlows();
                    }
                    else {
                        String tmpStr[]=option.split(":");
                        getPairFlows(tmpStr[1],null);
                        getPairPath(tmpStr[1],null);
                    }

                }

                else if (option.contains("quit")){
                    break;

                }
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

    }
    public static void initGraph(){
        matrix=loadMatrix();
        networkElementList=loadNetworkDic();
        topo=loadTopology(networkElementList);
        flowManager=new FlowManager(networkElementList,topo);

    }

    private static void dumpAllFlows(){
        PrintWriter writer= null; //dump the flows
        try {
            writer = new PrintWriter(flowsDump);
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        List<String> hosts=flowManager.getAllPairs();//pair of hosts
        if (hosts ==null){
            return;
        }
        List<String> flows;//flows per hosts
        List<TopologyLink> links;//links per hosts
        String pair="";
        for(int i=0;i<hosts.size();i++){
            pair=hosts.get(i);
            writer.write(pair+"\n");
            getPairFlows(pair,writer);
            getPairPath(pair,writer);
        }
        writer.close();


    }

    private static void getPairFlows(String arg,PrintWriter writer){

       List <String> tmpList=null;

        try {
            //String tmpStr[]=arg.split(":");
            //String skey= tmpStr[1];
            tmpList=flowManager.getFlowsPerPair(arg);
            if (tmpList!=null){
                for (int i=0;i<tmpList.size();i++){
                    if (writer==null){
                        System.out.println(tmpList.get(i));
                    }
                    else{
                        writer.write(tmpList.get(i)+"\n");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    private static void getPairPath(String arg,PrintWriter writer){

        List <TopologyLink> tmpListTopo=null;
        try {
            //String tmpStr[]=arg.split(":");
            //String skey= tmpStr[1];
            tmpListTopo=flowManager.getPathsPerPair(arg);
            if (tmpListTopo!=null){
                for (int i=0;i<tmpListTopo.size();i++){
                    if (writer==null){
                        System.out.println(tmpListTopo.get(i).getNetworkElementsName());
                    }
                    else{
                        writer.write(tmpListTopo.get(i).getNetworkElementsName()+"\n");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    private static SparseMatrix loadMatrix(){
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

    private static void reloadMatrix(){
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
        flowManager.clearFlows();
        matrix=loadMatrix();
        applyTrafficMatrix();

    }

    private static ConcurrentHashMap<String,NetworkElement> loadNetworkDic(){
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

    private static ListenableUndirectedGraph<NetworkElement,TopologyLink> loadTopology(ConcurrentHashMap<String,NetworkElement> networkElements){
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

    private static void applyTrafficMatrix(){

        //String hosta="";
        //String hostb="";
        NetworkElement elementa;
        NetworkElement elementb;
        String spliter=matrix.getKeysSeparator();
        List<String> keys=matrix.getRowColums();
        String temp="";
        TopologyLink link;
        List <TopologyLink> shortest_path;
        BellmanFordShortestPath bellmanFordShortestPath;

        PrintWriter writer= null; //dump the flows
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
    private static void printPaths(List <TopologyLink> links){
        for (int i=0;i<links.size();i++){
            System.out.print(topo.getEdgeSource(links.get(i)).getName());
            System.out.print(" to: " + topo.getEdgeTarget(links.get(i)).getName());
            System.out.println(" weight: " + String.valueOf(links.get(i).getWeight()));
        }
    }

}
