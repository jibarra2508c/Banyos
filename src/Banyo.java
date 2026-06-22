import java.util.ArrayList;


public class Banyo {

    private int x;
    private int y;
    private int ancho;
    private int alto;

    // La persona que esta dentro ahora mismo (null si esta vacio).
    private Persona ocupante;

    // La cola de personas que esperan turno (la primera de la lista es la
    // siguiente en entrar).
    private ArrayList<Persona> cola;

    public Banyo(int x, int y, int ancho, int alto) {
        this.x = x;
        this.y = y;
        this.ancho = ancho;
        this.alto = alto;
        this.ocupante = null;
        this.cola = new ArrayList<Persona>();
    }

    /**
     * Una persona pide entrar al baño. Se pone la ultima en la cola. Luego espera (wait)
     */
    public synchronized void entrar(Persona p) throws InterruptedException {
        cola.add(p);

        while (ocupante != null || cola.get(0) != p) {
            wait();   // el hilo se duerme aqui hasta que alguien haga notifyAll()
        }

        cola.remove(0);   // ya es mi turno: me salgo de la cola
        ocupante = p;     // y entro
    }

    /**
     * La persona sale del baño y avisa a todas las que esperan
     */
    public synchronized void salir() {
        ocupante = null;
        notifyAll();
    }

    /** ¿Una persona en la posicion (px, py) esta dentro de la zona del baño? */
    public boolean contiene(int px, int py) {
        if (px >= x && px <= x + ancho && py >= y && py <= y + alto) {
            return true;
        }
        return false;
    }

    public synchronized boolean estaOcupado() {
        if (ocupante != null) {
            return true;
        }
        return false;
    }

    public synchronized int tamanoCola() {
        return cola.size();
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getAncho() {
        return ancho;
    }

    public int getAlto() {
        return alto;
    }

    public int getCentroX() {
        return x + ancho / 2;
    }

    public int getCentroY() {
        return y + alto / 2;
    }
}