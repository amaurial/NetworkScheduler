package trafficmatrix;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: amauri
 * Date: 21/08/13
 * Time: 12:15
 * To change this template use File | Settings | File Templates.
 */
public class SparseMatrix {
    private List<Tuple> matrix;
    private Hashtable<String,Integer> index=new Hashtable<String,Integer>();
    private int maxRow;
    private int maxColumn;
    private String keysSeparator=";";

    public String getKeysSeparator() {
        return keysSeparator;
    }

    public void setKeysSeparator(String keysSeparator) {
        this.keysSeparator = keysSeparator;
    }


    public SparseMatrix(){
        matrix=new ArrayList<Tuple>();
    }

    public void add(String key,float value){
        Tuple t=new Tuple(key, value);
        matrix.add(t);
        index.put(key,matrix.size()-1);
    }

    public float get(String key){
        if (index.containsKey(key)){
            return matrix.get(index.get(key)).y;
        }
        else {
            return 0;
        }
    }
    public List<String> getRowColums(){
        List<String> list=new ArrayList<String>();
        for (int i=0;i<matrix.size();i++){
            list.add(matrix.get(i).x);
        }
        return list;
    }

    public void sort(boolean ascending){

        if (ascending){
            Collections.sort(matrix);
        }
        else {
            Collections.sort(matrix,Collections.reverseOrder());
        }

        //reorganize index
        index.clear();

        for (int i=0;i<matrix.size();i++){
            index.put(matrix.get(i).x,i);
        }
    }

    public void clear(){
        matrix.clear();
        index.clear();
    }



}
