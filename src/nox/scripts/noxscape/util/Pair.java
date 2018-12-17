package nox.scripts.noxscape.util;

public class Pair<k, v> {

    public final k a;
    public final v b;

    public Pair(k a, v b) {
        this.a = a;
        this.b = b;
    }

    public k getA() { return a; }
    public v getB() { return b; }
}
