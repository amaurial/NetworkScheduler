package flowmanager;

import network.Host;
import network.NetworkElement;
import network.Switch;
import org.jgrapht.graph.ListenableUndirectedGraph;
import topology.TopologyLink;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: amauri
 * Date: 02/11/13
 * Time: 08:21
 * To change this template use File | Settings | File Templates.
 */
public class FlowManager{
    /**
     * keeps the flows for each pair host;host ->flow
     */
    private Hashtable<String,List<String>> flowsPerPair;
    private Hashtable<String,List<TopologyLink>> pathsPerPair;
    private Hashtable<String,List<String>> hostsPerLink;
    private List<String> flows;
    /**
     * keeps the flows for each switch ->flow
     */
    private Hashtable<String,List<String>> flowsPerSwitch;

    private List<String> flowsToDelete;

    ConcurrentHashMap<String,NetworkElement> networkElementList;
    ListenableUndirectedGraph<NetworkElement,TopologyLink> topo;
    private String controlerIP="127.0.0.1";
    private int controlerPort=6699;

    public String getControlerIP() {
        return controlerIP;
    }

    public void setControlerIP(String controlerIP) {
        this.controlerIP = controlerIP;
    }

    public int getControlerPort() {
        return controlerPort;
    }

    public void setControlerPort(int controlerPort) {
        this.controlerPort = controlerPort;
    }

    public void clearFlows(){
        flows.clear();
        flowsPerPair.clear();
        flowsPerSwitch.clear();
        pathsPerPair.clear();
        hostsPerLink.clear();
    }

    public FlowManager(ConcurrentHashMap<String,NetworkElement> networkElementList,
                       ListenableUndirectedGraph<NetworkElement,TopologyLink> topo){
        this.networkElementList=networkElementList;
        this.topo=topo;
        this.flows=new ArrayList<String>();
        this.flowsToDelete=new ArrayList<String>();
        this.flowsPerPair=new Hashtable<String, List<String>>();
        this.flowsPerSwitch=new Hashtable<String, List<String>>();
        this.hostsPerLink=new Hashtable<String, List<String>>();
        this.pathsPerPair=new Hashtable<String, List<TopologyLink>>();

    }

    /**
     * create flow and return then
     * @param links
     * @return
     */
    public List<String> createListFlows(List<TopologyLink> links,NetworkElement origin,NetworkElement destiny){
        List<String> list=new ArrayList<String>();
        TopologyLink link;
        NetworkElement left;
        NetworkElement right;
        NetworkElement lastRight=null;
        NetworkElement lastLeft=null;
        NetworkElement tempNe;
        String flow="";
        String flowcommand="setflow";
        String dpid="";
        String port="";
        int cases=0;

        //sanity check
        //origin and destiny must be hosts
        if (!(origin instanceof network.Host || destiny instanceof network.Host)){
            System.out.println("origin and destiny must be Hosts");
            System.out.println("origin:" + origin.getClass());
            System.out.println("destiny:" + destiny.getClass());
        }

        String IPorigin=((Host)origin).getIp();
        String IPdestiny=((Host)destiny).getIp();


        for (int i=0;i<links.size();i++){
            link=links.get(i);
            left=topo.getEdgeSource(link);
            right=topo.getEdgeTarget(link);
            if (origin.getName().compareTo(left.getName())==0){
                cases=0;
                //first link
            }
            if (origin.getName().compareTo(right.getName())==0){
                //first link
                cases=1;
            }
            if (destiny.getName().compareTo(left.getName())==0){
                cases=2;
                //last link
            }
            if (destiny.getName().compareTo(right.getName())==0){
                //last link
                cases=3;
            }

            if (left instanceof network.Switch && right instanceof network.Switch){
                cases=4;
            }

            switch (cases){
                case 0://first link host->switch
                    dpid=right.getMac();
                    port=String.valueOf(link.getRemotePort());
                    flow=setFlowMessage(flowcommand, dpid, IPorigin, port) ;
                    list.add(flow);
                    break;
                case 1://first link switch->host
                    dpid=left.getMac();
                    port=String.valueOf(link.getLocalPort());
                    flow=setFlowMessage(flowcommand, dpid, IPorigin, port) ;
                    list.add(flow);
                    break;
                case 2://last link host->switch
                    dpid=right.getMac();
                    port=String.valueOf(link.getRemotePort());
                    flow=setFlowMessage(flowcommand, dpid, IPdestiny, port) ;
                    list.add(flow);
                    break;
                case 3://last link switch->host
                    dpid=left.getMac();
                    port=String.valueOf(link.getLocalPort());
                    flow=setFlowMessage(flowcommand, dpid, IPdestiny, port) ;
                    list.add(flow);
                    break;
                case 4://switch ->switch
                    //we have several cases:
                    //must route packages to and from both origin and destiny

                    if (lastRight!=left){
                        if (!(lastLeft instanceof Host || lastRight instanceof Host)){
                            tempNe=left;
                            left=right;
                            right=tempNe;

                            //packages to destiny must go to left local port
                            dpid=left.getMac();
                            port=String.valueOf(link.getRemotePort());
                            flow=setFlowMessage(flowcommand, dpid, IPdestiny, port) ;
                            list.add(flow);

                            //packages to origin must go to local right local port
                            dpid= right.getMac();
                            port=String.valueOf(link.getLocalPort());
                            flow=setFlowMessage(flowcommand, dpid, IPorigin, port) ;
                            list.add(flow);
                            break;
                        }
                    }

                    //packages to destiny must go to left local port
                    dpid=left.getMac();
                    port=String.valueOf(link.getLocalPort());
                    flow=setFlowMessage(flowcommand, dpid, IPdestiny, port) ;
                    list.add(flow);

                    //packages to origin must go to local right local port
                    dpid= right.getMac();
                    port=String.valueOf(link.getRemotePort());
                    flow=setFlowMessage(flowcommand, dpid, IPorigin, port) ;
                    list.add(flow);
                    break;

                default:
                    System.out.println("Fail on identify network elements:");
                    System.out.println("Left:" + left.getClass() + " " + left.getName());
                    System.out.println("Right:" + right.getClass() + " " + right.getName());

            }

            lastRight=right;
            lastLeft=left;

        }
        return list;
    }
    public List<String> getAllFlows(){
        List<String> list=new ArrayList<String>(flows);
        return list;
    }
    private String setFlowMessage(String message,String dpid,String IP, String port){
        //setflow 00-00-00-10-00-00 10.0.0.11 1
        String temp=message + " " + dpid + " " + IP + " " + port;
        return temp;
    }

    private String removeFlowMessage(String message,String dpid,String IP, String port){
        String temp=message + " " + dpid + " " + IP ;
        return temp;
    }
    /**
     * create flows and add in the internal list
     * @param links
     */
    public void createFlows(List<TopologyLink> links,NetworkElement origin,NetworkElement destiny){
        String skey=origin.getName()+ ';' + destiny.getName();
        List<String> list=createListFlows(links, origin, destiny);
        List<String> tempList;
        List<TopologyLink> linksTemp=new ArrayList<TopologyLink>(links);

        //add to general list

        for(int i=0;i<list.size();i++){
            flows.add(list.get(i));
        }

        //add flows to list per host pair
        if (flowsPerPair.containsKey(skey)){
            flowsPerPair.remove(skey);
        }
        tempList=new ArrayList<String>(list);
        flowsPerPair.put(skey,tempList);

        //add paths to list per host pair
        if (pathsPerPair.containsKey(skey)){
            pathsPerPair.remove(skey);
        }
        pathsPerPair.put(skey,linksTemp);

        //TODO put the list of hosts to each link

    }

    /**
     * return a list of all pairs of hosts
     * @return
     */
    public List<String> getAllPairs(){
        List<String> tempList=null;

        Object[] objects = flowsPerPair.keySet().toArray();
        if (objects.length>0){
            tempList=new ArrayList<String>();
        }
        for (int i=0;i<objects.length;i++){
            String tmp=(String) objects[i];
            tempList.add(tmp);
        }
        return tempList;
    }

    /**
     * Get the path for a specific pair of hosts  skey="h0;h10"
     * @param skey
     * @return
     */


    public List<String> getFlowsPerPair(String skey){
        List<String> tempList=null;

        if (flowsPerPair.containsKey(skey)){
            tempList=new ArrayList<String>(flowsPerPair.get(skey)) ;
        }
        else{
            //lets invert the key
            //skey="h0;h10"
            String stemp[]= skey.split(";");
            String newsKey=stemp[1]+";"+stemp[0];
            if (flowsPerPair.containsKey(newsKey)){
                tempList=new ArrayList<String>(flowsPerPair.get(newsKey)) ;
            }
        }
        return tempList;
    }

    /**
     * Get paths per pair  skey="h0;h10"
     * @param skey
     * @return
     */
    public List<TopologyLink> getPathsPerPair(String skey){
        List<TopologyLink> tempList=null;

        if (pathsPerPair.containsKey(skey)){
            tempList=new ArrayList<TopologyLink>(pathsPerPair.get(skey)) ;
        }
        else{
            //lets invert the key
            //skey="h0;h10"
            String stemp[]= skey.split(";");
            String newsKey=stemp[1]+";"+stemp[0];
            if (pathsPerPair.containsKey(newsKey)){
                tempList=new ArrayList<TopologyLink>(pathsPerPair.get(newsKey)) ;
            }
        }
        return tempList;
    }

    public boolean applyAllFlows(){


        //sanity check
        if (flows.size()<1){
            return false;
        }
        return sendListCommands(flows);


    }
    public boolean applyFlows(String hostPair){
        return false;
    }

    public boolean applyFlowsSwitch(String sswitch){
        return false;
    }

    @Override
    public String toString(){
        return flows.toString();
    }
    private boolean sendListCommands(List<String> commands){

        String answer="";
        String flow="";
        int total=0;

        if (commands.size()<1){
            return true;
        }

        ConnectionController conn=new ConnectionController(controlerIP,controlerPort);
        if (conn.connect()){
            answer=conn.read();
            for (int i=0;i<commands.size();i++){
                flow=commands.get(i);
                if (conn.send(flow)==false){
                    //delete applied flows
                    System.out.println("flow not applied " + flow);
                }
                answer=conn.read();
                System.out.print(String.valueOf(i) + " from " + String.valueOf(total) + ":");
                System.out.println(answer);
                //validate answer
            }

            conn.send("quit");
            System.out.println(conn.read());
            conn.close();
            return true;
        }

        return false;

    }
    public boolean deleteAllFlows(){
        //prepare the list
        if (flows.size()<1){
            return false;
        }

        flowsToDelete.clear();
        String flow="";
        String[] temp;
        for (int i=0;i<flows.size();i++)
        {
            flow=flows.get(i);
            temp=flow.split(" ");
            flowsToDelete.add(removeFlowMessage("removeflow ",temp[1],temp[2],""));
        }
        return sendListCommands(flowsToDelete);
    }

    public boolean deleteFlows(String hostPair){
        return false;
    }
    public boolean deleteFlowsFromSwitch(String sswitch){
        return false;
    }

}
