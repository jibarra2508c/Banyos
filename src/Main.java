import javax.swing.JFrame;
import java.awt.Color;

public class Main {

    public static void main(String[] args) {

        // Comprobamos los argumentos: o solo el puerto local (1), o el puerto
        // local mas el host y puerto remotos (3).
        if (args.length != 1 && args.length != 3) {
            System.out.println("Uso: java Main <puertoLocal> [<hostRemoto> <puertoRemoto>]");
            System.exit(1);
        }

        int puertoLocal = Integer.parseInt(args[0]);

        String hostRemoto;
        int puertoRemoto;
        if (args.length == 3) {
            hostRemoto = args[1];
            puertoRemoto = Integer.parseInt(args[2]);
        } else {
            hostRemoto = null;
            puertoRemoto = 0;
        }

        // Creamos la ventana.
        JFrame ventana = new JFrame("Banos - puerto " + puertoLocal);
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Panel panel = new Panel();
        ventana.add(panel);
        ventana.pack();
        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);

        // Preparamos la comunicacion con la otra ventana.
        Comunicador com = new Comunicador(panel, puertoLocal, hostRemoto, puertoRemoto);
        panel.setComunicador(com);
        com.iniciar();

        // colores para que las personas no sean todas iguales.
        Color[] colores = {
                Color.RED, Color.BLUE, Color.GREEN.darker(),
                Color.ORANGE, Color.MAGENTA, Color.CYAN.darker()
        };

        // Creamos 6 personas en posiciones y direcciones aleatorias.
        int numPersonas = 6;
        for (int i = 0; i < numPersonas; i++) {
            int x = 50 + (int) (Math.random() * 600);
            int y = 50 + (int) (Math.random() * 350);

            // Velocidad horizontal: entre -3 y -2, o entre 2 y 3.
            int dx;
            if (Math.random() > 0.5) {
                dx = 2 + (int) (Math.random() * 2);
            } else {
                dx = -(2 + (int) (Math.random() * 2));
            }

            // Velocidad vertical: entre -2 y -1, o entre 1 y 2.
            int dy;
            if (Math.random() > 0.5) {
                dy = 1 + (int) (Math.random() * 2);
            } else {
                dy = -(1 + (int) (Math.random() * 2));
            }

            Color color = colores[i % colores.length];

            // El id es distinto en cada ventana (usamos el puerto para ello).
            int id = puertoLocal * 100 + i;

            Persona p = new Persona(id, x, y, dx, dy, color);
            panel.agregarPersona(p);
        }
    }
}