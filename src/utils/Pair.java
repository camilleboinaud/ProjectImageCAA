package utils;

/**
 * Created by camille on 29/03/15.
 */
public class Pair<A,B> {
    private A first;
    private B second;

    public Pair(A first, B second){
        this.first = first;
        this.second = second;
    }

    public A getFirst(){
        return this.first;
    }

    public B getSecond(){
        return this.second;
    }

    public void setFirst(A first){
        this.first = first;
    }

    public void setSecond(B second){
        this.second = second;
    }

    public void set(A first, B second){
        this.setFirst(first);
        this.setSecond(second);
    }

}
