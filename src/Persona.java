import java.awt.Color;


public class Persona implements Runnable {

    private int id;
    private int x;
    private int y;
    private int dx;     // velocidad horizontal
    private int dy;     // velocidad vertical
    private int radio;
    private Color color;

    // Referencia al panel donde vive esta persona y a su propio hilo.
    private Panel panel;
    private Thread hilo;
    private boolean activa;

    public Persona(int id, int x, int y, int dx, int dy, Color color) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.radio = 12;
        this.color = color;
    }

    /** Arranca el hilo de la persona dentro de un panel concreto. */
    public void iniciar(Panel panel) {
        this.panel = panel;
        this.activa = true;
        this.hilo = new Thread(this, "Persona-" + id);
        this.hilo.start();
    }

    /**
     * Mientras la persona este "activa", se mueve un poco y espera 30 milisegundos
     */
    @Override
    public void run() {
        while (activa) {
            try {
                mover();
                Thread.sleep(30);
            } catch (InterruptedException e) {
                return; // el hilo se interrumpio: salimos
            }
        }
    }

    /** Calcula el siguiente movimiento de la persona. */
    private void mover() throws InterruptedException {
        int ancho = panel.getWidth();
        int alto = panel.getHeight();

        // Si el panel todavia no tiene tamaño, no hacemos nada
        if (ancho == 0 || alto == 0) {
            return;
        }

        // 1) ¿He llegado a un baño?
        Banyo banyo = panel.banoEnPosicion(x, y);
        if (banyo != null) {
            // entrar() esta sincronizado: si el baño esta ocupado, este hilo
            // se queda esperando en la cola (wait) hasta que le toque.
            banyo.entrar(this);

            // Ya he entrado: me coloco en el centro del baño.
            this.x = banyo.getCentroX();
            this.y = banyo.getCentroY();

            // Me quedo dentro un rato (entre 2 y 5 segundos).
            int tiempo = 2000 + (int) (Math.random() * 3000);
            Thread.sleep(tiempo);

            // Salgo del baño y aviso a los que esperan.
            banyo.salir();

            // Me alejo un poco hacia abajo para no volver a entrar enseguida.
            this.y = this.y + 80;
            return;
        }

        // 2) Movimiento normal
        this.x = this.x + this.dx;
        this.y = this.y + this.dy;

        // 3) Rebote arriba / abajo (esto pasa siempre)
        if (this.y < 0) {
            this.dy = -this.dy;
        }
        if (this.y > alto) {
            this.dy = -this.dy;
        }

        // 4) Pared izquierda
        if (this.x < 0) {
            if (panel.estaConectado()) {
                // Hay otra ventana: me voy a ella.
                this.activa = false;
                panel.transferir(this);
                return;
            } else {
                // No hay otra ventana: reboto.
                this.dx = -this.dx;
            }
        }

        // 5) Pared derecha
        if (this.x > ancho) {
            if (panel.estaConectado()) {
                this.activa = false;
                panel.transferir(this);
                return;
            } else {
                this.dx = -this.dx;
            }
        }
    }

    // ---- Getters y setters ----
    public int getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }

    public int getRadio() {
        return radio;
    }

    public Color getColor() {
        return color;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "Persona#" + id + " (" + x + "," + y + ")";
    }
}