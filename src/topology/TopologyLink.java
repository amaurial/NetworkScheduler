package topology;

import network.NetworkElement;
import org.jgrapht.graph.DefaultWeightedEdge;

/**
 * Created with IntelliJ IDEA.
 * User: amauri
 * Date: 26/08/13
 * Time: 11:55
 * To change this template use File | Settings | File Templates.
 */
public class TopologyLink extends DefaultWeightedEdge {
    private int localPort;
    private int remotePort;
    double weight;
    private NetworkElement elementA=null;
    private NetworkElement elementB=null;


    public TopologyLink(int localPort,int remotePort,double weight){

        this.localPort=localPort;
        this.remotePort=remotePort;
        this.weight=weight;
    }

    public int getLocalPort() {
        return localPort;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void reconfigureLink(int localPort,int remotePort,double weight){
        this.localPort=localPort;
        this.remotePort=remotePort;
        this.weight=weight;
    }

    public void setWeight(double weight){
        this.weight=weight;
    }


    @Override
    public double getWeight(){
        return this.weight;
    }

    @Override
    public String toString(){
        //String a= String.valueOf(this.weight);
        return String.format("%.1f",this.weight);
    }

    public String getNetworkElementsName(){
        if (elementB!=null && elementA!=null){
            return elementA.getName() + "<->" + elementB.getName();
        }
        return "";
    }

    public void setNetworkElements(NetworkElement elementA,NetworkElement elementB){
        this.elementA=elementA;
        this.elementB=elementB;
    }
    public NetworkElement getElementA(){
        return elementA;
    }

    public NetworkElement getElementB(){
        return elementB;
    }

}
