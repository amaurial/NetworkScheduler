package network;

import network.NetworkElement;

/**
 * Created with IntelliJ IDEA.
 * User: amauri
 * Date: 26/08/13
 * Time: 11:48
 * To change this template use File | Settings | File Templates.
 */
public class Switch extends NetworkElement {
    private int ports;

    public int getPorts() {
        return ports;
    }

    public void setPorts(int ports) {
        this.ports = ports;
    }

}
