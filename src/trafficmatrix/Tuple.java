package trafficmatrix;

import java.util.*;


/**
 * Created with IntelliJ IDEA.
 * User: amauri
 * Date: 03/12/13
 * Time: 12:41
 * To change this template use File | Settings | File Templates.
 */
public class Tuple implements Comparable {
    public final String x;
    public final Float y;
    public Tuple(String x, Float y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public java.lang.String toString() {
        return "Tuple{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if (!(other instanceof Tuple)){
            return false;
        }
        Tuple other_ = (Tuple) other;
        return other_.x == this.x && other_.y == this.y;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((x == null) ? 0 : x.hashCode());
        result = prime * result + ((y == null) ? 0 : y.hashCode());
        return result;
    }


    @Override
    public int compareTo(Object o) {
        if (!(o instanceof Tuple)){
            return 1;
        }
        Tuple t=(Tuple)o;

        if (t.y>this.y){
            return -1;
        }
        if (t.y==this.y){
            return 0;
        }
        if (t.y<this.y){
            return 1;
        }
        return 0;
    }
}
