package pt.ist.socialsoftware.mono2micro.utils;

public class Pair<F, S> {

    private F first;
    private S second;

    public Pair() {

    }

    public  Pair( F f, S s ) {
        this.first = f;
        this.second = s;
    }

    public F getFirst() {
        return this.first;
    }

    public S getSecond() {
        return this.second;
    }

    public void setSecond(S second) {
        this.second = second;
    }
}