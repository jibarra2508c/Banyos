import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.awt.Color;


public class Comunicador {

    private static final String COD_TEXTO = "UTF-8";

    private Panel panel;
    private int puertoLocal;
    private String hostRemoto;
    private int puertoRemoto;

    // Conexion por la que enviamos personas
    private Socket socketSaliente;
    private BufferedWriter escritor;

    // Conexion por la que recibimos personas
    private Socket socketEntrante;
    private BufferedReader lector;

    public Comunicador(Panel panel, int puertoLocal, String hostRemoto, int puertoRemoto) {
        this.panel = panel;
        this.puertoLocal = puertoLocal;
        this.hostRemoto = hostRemoto;
        this.puertoRemoto = puertoRemoto;
    }

    /** Arranca los dos hilos: el servidor y (si procede) el cliente. */
    public void iniciar() {
        // Hilo servidor: siempre, para poder recibir.
        Thread hiloServidor = new Thread(new HiloServidor());
        hiloServidor.setDaemon(true);
        hiloServidor.start();

        // Hilo cliente: solo si nos han dado una ventana a la que conectarnos.
        if (hostRemoto != null) {
            Thread hiloCliente = new Thread(new HiloCliente());
            hiloCliente.setDaemon(true);
            hiloCliente.start();
        }
    }

    //  lado servidor

    /**
     * Clase que implementa Runnable (como la clase "Hilo" del tema).
     * Crea el ServerSocket y espera conexiones.
     */
    private class HiloServidor implements Runnable {
        @Override
        public void run() {
            try {
                ServerSocket serverSocket = new ServerSocket(puertoLocal);
                System.out.println("[Servidor] Escuchando en el puerto " + puertoLocal);

                while (true) {
                    Socket s = serverSocket.accept();
                    System.out.println("[Servidor] Otra ventana se ha conectado.");
                    prepararEntrada(s);
                }
            } catch (IOException e) {
                System.out.println("[Servidor] Error: " + e.getMessage());
            }
        }
    }

    /** Prepara el BufferedReader y lanza un hilo que va leyendo personas. */
    private void prepararEntrada(Socket s) {
        try {
            socketEntrante = s;
            InputStream is = s.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, COD_TEXTO);
            lector = new BufferedReader(isr);

            Thread hiloLector = new Thread(new HiloLector());
            hiloLector.setDaemon(true);
            hiloLector.start();
        } catch (IOException e) {
            System.out.println("[Servidor] No se pudo abrir la entrada: " + e.getMessage());
        }
    }

    /** Va leyendo lineas de texto (una por persona) y reconstruye cada persona en esta ventana.*/
    private class HiloLector implements Runnable {
        @Override
        public void run() {
            try {
                String linea = lector.readLine();
                while (linea != null && !linea.isEmpty()) {

                    // Separamos la linea por comas: "id,x,y,dx,dy,rgb"
                    String[] partes = linea.split(",");
                    int id = Integer.parseInt(partes[0]);
                    int x = Integer.parseInt(partes[1]);
                    int y = Integer.parseInt(partes[2]);
                    int dx = Integer.parseInt(partes[3]);
                    int dy = Integer.parseInt(partes[4]);
                    int rgb = Integer.parseInt(partes[5]);

                    // Creamos la persona de nuevo en esta ventana.
                    Color color = new Color(rgb);
                    Persona p = new Persona(id, x, y, dx, dy, color);

                    // La colocamos pegada al borde por el que debe entrar.
                    int ancho = panel.getWidth();
                    if (p.getDx() > 0) {
                        p.setX(p.getRadio() + 1);          // entra por la izquierda
                    } else {
                        p.setX(ancho - p.getRadio() - 1);  // entra por la derecha
                    }

                    System.out.println("[Lector] Persona recibida: " + p);
                    panel.agregarPersona(p);

                    linea = lector.readLine();
                }
            } catch (Exception e) {
                System.out.println("[Lector] Conexion cerrada: " + e.getMessage());
            }
        }
    }

    //  lado cliente
    /** Intenta conectarse a la otra ventana. Lo reintenta cada 2 segundos hasta que lo consigue. */
    private class HiloCliente implements Runnable {
        @Override
        public void run() {
            while (true) {
                if (escritor == null) {
                    try {
                        Socket s = new Socket(hostRemoto, puertoRemoto);
                        socketSaliente = s;
                        OutputStream os = s.getOutputStream();
                        OutputStreamWriter osw = new OutputStreamWriter(os, COD_TEXTO);
                        escritor = new BufferedWriter(osw);
                        System.out.println("[Cliente] Conectado con la otra ventana.");
                    } catch (IOException e) {
                        // La otra ventana aun no esta lista: reintentamos.
                    }
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }

    /**
     * Convierte la persona en una linea de texto y la envia por el socket.
     * Lo hacemos "synchronized" para que dos personas no escriban a la vez
     * y se mezclen sus datos.
     */
    public synchronized void enviar(Persona p) {
        if (escritor == null) {
            return;
        }
        try {
            String linea = p.getId() + "," + p.getX() + "," + p.getY() + ","
                    + p.getDx() + "," + p.getDy() + "," + p.getColor().getRGB();

            escritor.write(linea);
            escritor.newLine();
            escritor.flush();
            System.out.println("[Cliente] Persona enviada: " + linea);
        } catch (IOException e) {
            System.out.println("[Cliente] Error al enviar: " + e.getMessage());
            escritor = null;
        }
    }

    /** Hay conexion para enviar si ya tenemos el escritor preparado. */
    public synchronized boolean estaConectado() {
        if (escritor != null) {
            return true;
        }
        return false;
    }
}