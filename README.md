# Baños — Simulación distribuida con sockets e hilos

Aplicación de escritorio en **Java + Swing** que simula personas moviéndose por un espacio con baños. Lo interesante: **dos ventanas (procesos) se conectan por red mediante sockets** y las personas **cruzan de una ventana a otra** cuando alcanzan el borde, como si compartieran el mismo escenario. Práctica de programación concurrente y de comunicaciones (DAM).

## Qué hace

- Simula 6 **personas** (puntos de colores) moviéndose con velocidad y dirección aleatorias dentro de un `Panel` Swing.
- Hay **baños** (`Banyo`) con un ocupante y una **cola** de espera por turnos.
- Cuando una persona sale por un borde, se **serializa y envía por socket** a la ventana remota, que la recrea y la sigue animando: el escenario se reparte entre las dos ventanas.

## Concurrencia y red

La clase `Comunicador` gestiona la comunicación P2P con tres hilos:

- **HiloServidor**: escucha en el puerto local para aceptar conexiones entrantes.
- **HiloCliente**: se conecta al host/puerto remoto para enviar personas.
- **HiloLector**: lee continuamente las personas que llegan del otro extremo.

Comunicación por texto sobre `Socket` (UTF-8, `BufferedReader`/`BufferedWriter`).

## Ejecución

Compilar y lanzar **dos** instancias que se apunten mutuamente:

```bash
javac -d out src/*.java

# Ventana A (puerto local 5000, envía a la 5001 de localhost)
java -cp out Main 5000 localhost 5001

# Ventana B (puerto local 5001, envía a la 5000 de localhost)
java -cp out Main 5001 localhost 5000
```

`Main <puertoLocal> [<hostRemoto> <puertoRemoto>]` — con un solo argumento arranca solo como servidor.

## Estructura

```
src/
├── Main.java         # arranque, crea ventana y personas
├── Panel.java        # render Swing y bucle de animación
├── Persona.java      # estado/movimiento de cada persona
├── Banyo.java        # baño con ocupante y cola
└── Comunicador.java  # sockets + hilos servidor/cliente/lector
```

---
Autor: **Javier Ibarra**
