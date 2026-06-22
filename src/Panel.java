import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;


public class Panel extends JPanel implements ActionListener {

    // Lista de personas que hay ahora mismo en este panel.
    // Como varios hilos (las personas) la leen y la modifican a la vez,
    // protegemos su acceso con bloques "synchronized (personas)".
    private ArrayList<Persona> personas;

    // Lista de baños (no cambia, asi que no hace falta sincronizarla).
    private ArrayList<Banyo> banyos;

    private Comunicador comunicador;

    public Panel() {
        setBackground(new Color(245, 245, 245));
        setPreferredSize(new Dimension(700, 450));

        personas = new ArrayList<Persona>();
        banyos = new ArrayList<Banyo>();

        // Creamos tres baños en posiciones fijas.
        banyos.add(new Banyo(80, 60, 80, 70));
        banyos.add(new Banyo(540, 60, 80, 70));
        banyos.add(new Banyo(300, 320, 80, 70));

        // Timer de Swing: cada 30 milisegundos llamara a actionPerformed(),
        // que repinta el panel. Asi se ve el movimiento.
        Timer temporizador = new Timer(30, this);
        temporizador.start();
    }

    /** Lo llama el Timer cada 30 ms: simplemente repintamos. */
    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }

    // Gestion de personas

    public void agregarPersona(Persona p) {
        synchronized (personas) {
            personas.add(p);
        }
        // Arrancamos su hilo fuera del bloque synchronized.
        p.iniciar(this);
    }

    public void quitarPersona(Persona p) {
        synchronized (personas) {
            personas.remove(p);
        }
    }

    /** Devuelve el baño que hay en la posicion (x,y), o null si no hay ninguno. */
    public Banyo banoEnPosicion(int x, int y) {
        for (int i = 0; i < banyos.size(); i++) {
            Banyo b = banyos.get(i);
            if (b.contiene(x, y)) {
                return b;
            }
        }
        return null;
    }

    // Comunicacion
    public void setComunicador(Comunicador c) {
        this.comunicador = c;
    }

    public boolean estaConectado() {
        if (comunicador != null && comunicador.estaConectado()) {
            return true;
        }
        return false;
    }

    /** Quita la persona de este panel y la envia a la otra ventana. */
    public void transferir(Persona p) {
        quitarPersona(p);
        if (comunicador != null) {
            comunicador.enviar(p);
        }
    }

    // Dibujo

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Dibujamos los baños.
        for (int i = 0; i < banyos.size(); i++) {
            Banyo b = banyos.get(i);

            // Verde si esta libre, rojo si esta ocupado.
            if (b.estaOcupado()) {
                g2.setColor(new Color(220, 80, 80));
            } else {
                g2.setColor(new Color(120, 200, 120));
            }
            g2.fillRoundRect(b.getX(), b.getY(), b.getAncho(), b.getAlto(), 10, 10);

            g2.setColor(Color.DARK_GRAY);
            g2.drawRoundRect(b.getX(), b.getY(), b.getAncho(), b.getAlto(), 10, 10);

            // Texto "WC" y numero de personas en cola.
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2.drawString("WC", b.getX() + b.getAncho() / 2 - 10,
                    b.getY() + b.getAlto() / 2 - 4);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g2.drawString("Cola: " + b.tamanoCola(),
                    b.getX() + 8, b.getY() + b.getAlto() - 8);
        }

        // Dibujamos las personas. Sincronizamos porque otros hilos pueden
        // estar añadiendo o quitando personas de la lista a la vez.
        synchronized (personas) {
            for (int i = 0; i < personas.size(); i++) {
                Persona p = personas.get(i);
                int r = p.getRadio();
                g2.setColor(p.getColor());
                g2.fillOval(p.getX() - r, p.getY() - r, r * 2, r * 2);
                g2.setColor(Color.BLACK);
                g2.drawOval(p.getX() - r, p.getY() - r, r * 2, r * 2);
            }
        }

        // Indicador de conexion (circulo verde/rojo arriba a la derecha).
        String texto;
        if (estaConectado()) {
            g2.setColor(new Color(40, 160, 40));
            texto = "Conectado";
        } else {
            g2.setColor(new Color(180, 60, 60));
            texto = "Sin conexion (rebote)";
        }
        g2.fillOval(getWidth() - 22, 8, 12, 12);
        g2.setColor(Color.BLACK);
        g2.drawOval(getWidth() - 22, 8, 12, 12);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        int anchoTexto = g2.getFontMetrics().stringWidth(texto);
        g2.drawString(texto, getWidth() - 30 - anchoTexto, 19);
    }
}