package network;

import topology.TopologyLink;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: amauri
 * Date: 26/08/13
 * Time: 11:48
 * To change this template use File | Settings | File Templates.
 */
public class NetworkElement {
    private String name;
    private Integer id;

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    private String mac;
    private List<TopologyLink> links;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString(){
          return this.name;
    }

    /**
     *
     * @param remoteElement
     * @return
     * Get the port if there is a connection to the remoteElement
     * return -1 if there is no connection
     */
   /* public int getPort (NetworkElement remoteElement){
        while (links.iterator().hasNext()) {
            TopologyLink link=links.iterator().next();
            if (link.getRemoteElement().getId()== remoteElement.getId() &&
                    link.getRemoteElement().getName().compareTo(remoteElement.getName())==0){
                return link.getLocalPort();
            }
        }
        return -1;
    }*/

    public void addLink(TopologyLink link){
        links.add(link);
    }
}
