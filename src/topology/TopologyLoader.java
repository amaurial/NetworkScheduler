package topology;

import network.Host;
import network.NetworkElement;
import network.Switch;
import org.jgrapht.graph.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: amauri
 * Date: 15/10/13
 * Time: 13:22
 * To change this template use File | Settings | File Templates.
 */
public class TopologyLoader {
    private String filename;//topology filename. output from command <net> in mininet
    private ConcurrentHashMap<String,NetworkElement> networkElementList;// dictionary of elements.
    private List<String> tuples;

    public TopologyLoader(String filename,ConcurrentHashMap<String,NetworkElement> networkElementList){
        this.filename=filename;
        this.networkElementList=networkElementList;
    }

    public ListenableUndirectedWeightedGraph<NetworkElement,TopologyLink> Load(){
        tuples=loadTuples();
        List<Switch> switches=new ArrayList<Switch>();
        List<Host> hosts=new ArrayList<Host>();
        String item="";


        //s0 lo:  s0-eth1:sa100-eth1 s0-eth2:sa101-eth2 s0-eth3:h0-eth0 s0-eth4:h1-eth0 s0-eth5:h2-eth0 s0-eth6:h3-eth0
        //h0 h0-eth0:s0-eth3
        //check if all elements in the topology are in the dictionary
        String ret=checkElements();
        if (ret.length()>0){
            System.out.println("Elements missing in dictionary: " + ret);
            return null;
        }

        ListenableUndirectedWeightedGraph<NetworkElement,TopologyLink> topo=new ListenableUndirectedWeightedGraph<NetworkElement, TopologyLink>(TopologyLink.class);
        //add all vertex
        for (int i=0;i<tuples.size();i++) {

            String tuple=tuples.get(i);
            String items[]=tuple.split(" +");

            if (items.length>0){
                NetworkElement element=  this.networkElementList.get(items[0]);
                topo.addVertex(element);
                for (int k=1;k<items.length;k++){
                    item= items[k];
                    if (!item.contains("lo")){
                        String links[]=item.split(":");

                        //get port first edge
                        int port1=getPort(links[0]);
                        //get element and port second edge
                        NetworkElement element2=getElement(links[1]);
                        int port2=getPort(links[1]);

                        TopologyLink link=new TopologyLink(port1,port2,0);
                        link.setNetworkElements(element,element2);
                        //check if element2 is already in the graph
                        if (!topo.containsVertex(element2)){
                            topo.addVertex(element2);
                        }
                        //in our case edges are bidirectional
                        if (!(topo.containsEdge(element,element2)||topo.containsEdge(element2,element))){
                            topo.addEdge(element,element2,link);
                        }
                    }
                }

            }
        }
        return topo;
    }

    /**
     * Get port from an element specification
     * @param item : expect something like this: sa100-eth1
     * @return
     */
    private int getPort(String item){

        String temp[]=item.split("-");
        int port=0;
        if (temp[1].startsWith("eth")){
            String sport=temp[1].substring(3,temp[1].length());
            port=Integer.parseInt(sport);
            return port;
        }
        return -1;
    }

    /**
     * Get element from an element specification
     * @param item :expect something like this: sa100-eth1
     * @return
     */
    private NetworkElement getElement(String item){
        String temp[]=item.split("-");
        return this.networkElementList.get(temp[0]);
    }

    /**
     * Check if every element on topology has an entry in dictionary
     * @return elements that are not in the dictionary
     */
    private String checkElements(){
        String retElements="";
        for (int i=0;i<tuples.size();i++){
        //while (tuples.iterator().hasNext())  {
            String tuple=tuples.get(i);
            String items[]=tuple.split(" ");

            if (items.length>0){
                String elementname=items[0];
                NetworkElement element=  this.networkElementList.get(elementname);
                if (element==null){
                    retElements=retElements+ "," + elementname;
                }
            }
        }
        return retElements;
    }
    /**
     * Load the topology lines from <filename> in a list of Strings to be parsed
     * @return
     */
    private List<String> loadTuples(){
        InputStream ips=null;
        InputStreamReader ipsr=null;
        BufferedReader file=null;
        List<String> tuples=new ArrayList<String>();
        try {
            ips=new FileInputStream(this.filename);
            ipsr=new InputStreamReader(ips);
            file=new BufferedReader(ipsr);
            String text = null;

            while ((text = file.readLine()) != null) {
                String line=text.toLowerCase();
                tuples.add(line);
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();

        }
        finally {
            try {
                if (file != null) {
                    file.close();
                    ipsr.close();
                    ips.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return tuples;
    }
}
