package trafficmatrix;

import java.io.*;


/**
 * Created with IntelliJ IDEA.
 * User: amauri
 * Date: 24/09/13
 * Time: 10:55
 * To change this template use File | Settings | File Templates.
 */
public class FileLoaderMatrix {
    private String filename;
    private SparseMatrix matrix;
    private String spliter=";";

    public FileLoaderMatrix(SparseMatrix matrix,String filename){
        this.filename=filename;
        this.matrix=matrix;
        matrix.setKeysSeparator(spliter);
        LoadMatrix();
        matrix.sort(false);
    }
    public FileLoaderMatrix(SparseMatrix matrix){
        this.matrix=matrix;
    }
    public void LoadMatrix(){
        InputStream ips=null;
        InputStreamReader ipsr=null;
        BufferedReader file=null;
        try {
            ips=new FileInputStream(this.filename);
            ipsr=new InputStreamReader(ips);
            file=new BufferedReader(ipsr);
            String text = null;

            while ((text = file.readLine()) != null) {
                //list.add(Integer.parseInt(text));
                String column[] = text.split(";");
                if (column.length==3){
                    if (!column[0].contains("#")){
                        String key=column[0] + spliter + column[1];
                        float val=Float.parseFloat(column[2]);
                        matrix.add(key,val);
                    }

                }
                else{
                    System.out.print("Incorrect row value:" + text);
                    System.out.print("Expected:string;string;float");
                }
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();

        }
        finally {
            try {
                if (file != null) {
                    file.close();
                    ipsr.close();
                    ips.close();
                }
            } catch (IOException e) {
                System.out.print(e);
            }
        }
    }
    public void setFileName(String filename){
        this.filename=filename;
    }
}
