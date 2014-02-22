package network;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: amauri
 * Date: 15/10/13
 * Time: 13:23
 * To change this template use File | Settings | File Templates.
 */
public class NetworkElementsLoader {

    private String filename;
    private ConcurrentHashMap<String,NetworkElement> networkElements;
    public NetworkElementsLoader(String filename){
        this.filename=filename;
    }
    public ConcurrentHashMap<String,NetworkElement> Load(){
        InputStream ips=null;
        InputStreamReader ipsr=null;
        BufferedReader file=null;
        networkElements=new ConcurrentHashMap<String, NetworkElement>();
        try {
            ips=new FileInputStream(this.filename);
            ipsr=new InputStreamReader(ips);
            file=new BufferedReader(ipsr);
            String text = null;

            while ((text = file.readLine()) != null) {
                String line=text.toLowerCase();
                String column[] = line.split(";");
                //check host
                if (column[0].compareTo("host")==0)  {
                    Host host=new Host();
                    host.setName(column[1]);
                    host.setIp(column[2]);
                    host.setMac(column[3]);
                    networkElements.put(column[1],host);
                }
                //check host
                if (column[0].compareTo("switch")==0)  {
                    Switch sw=new Switch();
                    sw.setName(column[1]);
                    sw.setPorts(Integer.parseInt(column[2]));
                    sw.setMac(column[3]);
                    networkElements.put(column[1],sw);
                }

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
        return networkElements;
    }
    public void setFilename(String filename){
        this.filename=filename;
    }
}
