import IA.Red.*;
import java.util.*;

public class Estado {
    /* ===================== ATRIBUTOS ===================== */
    // Constantes para movimientos en un tablero 2D (no se usan en el código actual, pero pueden ser útiles)
    private static final int[] X = {-1, 0, 1, 0};
    private static final int[] Y = {0, 1, 0, -1};

    // Sensores y Centros de Datos
    public static Sensores sensor;
    public static CentrosDatos centros;

    // Información de las conexiones
    private int[] conexiones;                // Indica a qué nodo está conectado cada sensor
    private double[] capacidadRestante;      // Capacidad restante de cada nodo (sensor o centro)
    private int[] contador_conexiones;       // Número de conexiones de cada sensor/centro

    // Matriz de posicionamiento de sensores y centros en el tablero 100x100
    private int[][] tablero;

    // Métricas de coste e información transmitida
    private double coste = 0;
    private double info = 0;
    public static double a, b;

    /* ===================== CONSTRUCTORES ===================== */
    /**
     * Constructor de copia.
     */
    Estado(Estado a) {
        this.conexiones = Arrays.copyOf(a.conexiones, a.conexiones.length);
        this.capacidadRestante = Arrays.copyOf(a.capacidadRestante, a.capacidadRestante.length);
        this.contador_conexiones = Arrays.copyOf(a.contador_conexiones, a.contador_conexiones.length);
        this.tablero = new int[100][100];
        for (int i = 0; i < 100; i++) {
            System.arraycopy(a.tablero[i], 0, this.tablero[i], 0, 100);
        }
        this.coste = a.coste;
        this.info = a.info;
    }

    Estado(boolean greedy) {
        conexiones = new int[sensor.size()];
        Arrays.fill(conexiones, -1);

        this.contador_conexiones = new int[sensor.size()+centros.size()];
        Arrays.fill(contador_conexiones, 0);

        this.capacidadRestante = new double[sensor.size()+centros.size()];
        for(int i = 0; i < sensor.size(); i++){
            capacidadRestante[i] = sensor.get(i).getCapacidad()*2;
        }
        for(int i = sensor.size(); i < sensor.size() + centros.size(); ++i) {
            capacidadRestante[i] = 125;
        }

        this.tablero = new int[100][100];
        for (int i = 0; i < 100; i++) {
            Arrays.fill(tablero[i], -1); //inicializamos matriz a -1
        }
        inicializar_tablero();

        if (greedy) estado_inicial_cercania();
        else estado_inicial_random();

        if (!solucionInicialValida()) {
            System.out.println("Solucion invalida!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.exit(0);
        }
    }

    /* ===================== MÉTODOS DE INICIALIZACIÓN ===================== */
    /**
     * Inicializa el tablero con la posición de sensores y centros.
     */
    private void inicializar_tablero() {
        for (int i = 0; i < sensor.size(); i++) {
            tablero[sensor.get(i).getCoordX()][sensor.get(i).getCoordY()] = i;
        }
        for (int j = 0; j < centros.size(); j++) {
            tablero[centros.get(j).getCoordX()][centros.get(j).getCoordY()] = j + sensor.size();
        }
    }

    /* ===================== VERIFICACIONES Y UTILIDADES ===================== */
    private boolean es_sensor(int sensorId) {
        return sensorId >= 0 && sensorId < sensor.size();
    }

    private boolean es_centro(int centroId) {
        return centroId >= sensor.size() && centroId < conexiones.length;
    }

    private boolean es_valido_sensor(int sensorId) {
        return contador_conexiones[sensorId] <= 2 && conexion_con_centro(sensorId);
    }

    private boolean es_valido_centros(int centroId) {
        return contador_conexiones[centroId] <= 24;
    }

    public boolean conexion_con_centro(int sensorId) {
        boolean[] visitado = new boolean[sensor.size()];
        Arrays.fill(visitado, false);
        return dfs(sensorId, visitado);
    }

    private boolean dfs(int sensorId, boolean[] visitado) {
        if (es_centro(sensorId)) return true;
        if (!visitado[sensorId]) {
            visitado[sensorId] = true;
            int v = conexiones[sensorId];
            return v != -1 && dfs(v, visitado); //si v = -1 puede que ese sensor no tenga ninguna conexión
        }
        return false;
    }

    private double dist(int id1, int id2, boolean escentro) {
        double x1, y1, x2, y2;

        /* id1 siempre será sensor */

        x1 = sensor.get(id1).getCoordX();
        y1 = sensor.get(id1).getCoordY();

        if (escentro){
            x2 = centros.get(id2).getCoordX();
            y2 = centros.get(id2).getCoordY();
        }
        else {
            x2 = sensor.get(id2).getCoordX();
            y2 = sensor.get(id2).getCoordY();
        }
        return Math.sqrt((Math.pow(x1-x2, 2)) + (Math.pow(y1-y2,2)));
    }

    private boolean pos_valida(int[][]tablero, int x, int y) {
        int filas = tablero.length;
        int columnas = tablero[0].length;
        return x >= 0 && x < filas && y >= 0 && y < columnas;
    }

    private ArrayList<Integer> bfs(int sensorId, boolean[][] visitado) {
        Queue<Pair<Integer, Integer>> cola = new LinkedList<>();
        cola.add(new Pair<>(sensor.get(sensorId).getCoordX(), sensor.get(sensorId).getCoordY()));
        visitado[sensor.get(sensorId).getCoordX()][sensor.get(sensorId).getCoordY()] = true;
        ArrayList<Integer> cercanos = new ArrayList<>();

        while (!cola.isEmpty()) {
            Pair<Integer, Integer> actual = cola.poll();
            int posX = actual.first;
            int posY = actual.second;
            for (int i = 0; i < 4; ++i) {
                int x_next = posX + X[i];
                int y_next = posY + Y[i];
                if (pos_valida(tablero, x_next, y_next)) {
                    if (tablero[x_next][y_next] != -1 ) {
                        cercanos.add(tablero[x_next][y_next]);
                    }
                    if (!visitado[x_next][y_next]) {
                        cola.add(new Pair<>(x_next, y_next));
                        visitado[x_next][y_next] = true;
                    }
                }
            }
        }
        return cercanos;
    }

    private void actualizar_capacidadRestante_centro(int sensorId, int centroId) {
        if (sensor.get(sensorId).getCapacidad() > capacidadRestante[centroId]) {
            //info_perdida += (int) sensor.get(sensorId).getCapacidad() - (int) capacidadRestante[centroId];
            capacidadRestante[centroId] = 0;
        }
        else capacidadRestante[centroId] -= sensor.get(sensorId).getCapacidad();
    }

    //el 1 le envía al 2
    private void actualizar_capacidadRestante_sensor(int sensorId1, int sensorId2) {
        if (sensor.get(sensorId1).getCapacidad() > capacidadRestante[sensorId2]) {
            //info_perdida += (int) sensor.get(sensorId1).getCapacidad() - (int) capacidadRestante[sensorId2];
            capacidadRestante[sensorId2] = 0;
            sensor.get(sensorId2).setCapacidad((int) sensor.get(sensorId2).getCapacidad() + (int) capacidadRestante[sensorId2]);
        }
        else {
            capacidadRestante[sensorId2] -= sensor.get(sensorId1).getCapacidad();
            sensor.get(sensorId2).setCapacidad((int) sensor.get(sensorId2).getCapacidad() + (int) sensor.get(sensorId1).getCapacidad());
        }
    }

    private void actualizar_conexiones_sensor(int sensorId1, int sensorId2) {
        conexiones[sensorId1] = sensorId2;
        ++contador_conexiones[sensorId2];
    }

    private void actualizar_conexiones_centro(int sensorId, int centroId) {
        conexiones[sensorId] = centroId;
        ++contador_conexiones[centroId];
    }

    public void crear_conexion (int id1, int id2) {
        if (es_centro(id1) || conexiones[id1] == id2 || !es_valido_sensor(id2)) return;
        if (conexiones[id1] != -1) romper_conexion(id1);

        conexiones[id1] = id2;
        contador_conexiones[id2]++;
    }

    public void romper_conexion (int id1) {
        if (es_centro(id1) || conexiones[id1] == -1) return;
        contador_conexiones[conexiones[id1]]--;
        conexiones[id1] = -1;
    }

    /* ===================== SOLUCIÓN VÁLIDA ===================== */
    private boolean solucionInicialValida() {
        // Verificar que cada sensor tenga una conexión válida
        for (int i = 0; i < sensor.size(); i++) {
            if (conexiones[i] == -1) { // Si un sensor no tiene conexión
                System.out.println("Error: Sensor " + i + " no tiene conexión.");
                return false;
            }
        }

        // Verificar que ningún sensor exceda el número máximo de conexiones permitidas
        for (int i = 0; i < sensor.size(); i++) {
            if (contador_conexiones[i] > 3) {
                System.out.println("Error: Sensor " + i + " tiene más de 3 conexiones.");
                return false;
            }
        }

        // Verificar que ningún centro de datos tenga más de 24 conexiones
        for (int i = sensor.size(); i < sensor.size() + centros.size(); i++) {
            if (contador_conexiones[i] > 25) {
                System.out.println("Error: Centro " + (i - sensor.size()) + " tiene más de 25 conexiones.");
                return false;
            }
        }

        // Verificar que la capacidad restante de sensores y centros no sea negativa
        for (int i = 0; i < sensor.size() + centros.size(); i++) {
            if (capacidadRestante[i] < 0) {
                System.out.println("Error: Nodo " + i + " tiene capacidad restante negativa.");
                return false;
            }
        }

        // Verificar que cada sensor pueda llegar a un centro de datos
        for (int i = 0; i < sensor.size(); i++) {
            if (!conexion_con_centro(i)) {
                System.out.println("Error: Sensor " + i + " no puede transmitir su información a un centro.");
                return false;
            }
        }

        return true; // Si todas las verificaciones se cumplen, la solución inicial es válida.
    }

    /* ===================== OPERADORES DE BÚSQUEDA LOCAL ===================== */
    //operador1: intercambio de conexiones entre dos sensores
    public boolean swap(int id1, int id2) {
        if (!es_sensor(id1) || !es_sensor(id2) || conexiones[id1] == -1 || conexiones[id2] == -1) return false;

        int conexion1 = conexiones[id1];
        int conexion2 = conexiones[id2];

        if (conexion1 == id2 || conexion2 == id1 || !es_valido_sensor(conexion1) || !es_valido_sensor(conexion2) ) return false;

        capacidadRestante[conexion1] += sensor.get(id1).getCapacidad();
        sensor.get(conexion1).setCapacidad((int) (sensor.get(conexion1).getCapacidad() - (int) sensor.get(id1).getCapacidad()));

        capacidadRestante[conexion2] += sensor.get(id2).getCapacidad();
        sensor.get(conexion2).setCapacidad((int) (sensor.get(conexion2).getCapacidad() - (int) sensor.get(id2).getCapacidad()));

        romper_conexion(id1);
        romper_conexion(id2);

        crear_conexion(id1, conexion2);
        crear_conexion(id2, conexion1);

        actualizar_capacidadRestante_sensor(id1, conexion2);
        actualizar_capacidadRestante_sensor(id2, conexion1);

        return true;
    }

    //operador2: mueve la conexión de un sensor a otro sensor o centro
    public boolean moverConexion(int sensorId, int nuevoDestino) {
        if (!es_sensor(sensorId) || conexiones[sensorId] == -1 || sensorId == nuevoDestino) return false;
        if ((es_centro(nuevoDestino) && !es_valido_centros(nuevoDestino)) || (es_sensor(nuevoDestino) && !es_valido_sensor(nuevoDestino))) return false;

        int conexionAntigua = conexiones[sensorId];
        capacidadRestante[conexionAntigua] += sensor.get(sensorId).getCapacidad();
        sensor.get(conexionAntigua).setCapacidad((int) (sensor.get(conexionAntigua).getCapacidad() - (int) sensor.get(sensorId).getCapacidad()));

        romper_conexion(sensorId);
        crear_conexion(sensorId, nuevoDestino);

        if (es_centro(nuevoDestino)) {
            actualizar_capacidadRestante_centro(sensorId, nuevoDestino);
        } else {
            actualizar_capacidadRestante_sensor(sensorId, nuevoDestino);
        }
        return true;
    }

    /* ===================== ESTADOS INICIALES ===================== */

    /* esta función genera el estado inicial aleatoriamente */
    public void estado_inicial_random () {
        Random rand = new Random();

        for (int i = 0; i < sensor.size(); i++) {
            int centroId = sensor.size() + rand.nextInt(centros.size());
            if (es_valido_centros(centroId)) {
                actualizar_conexiones_centro(i, centroId);
                actualizar_capacidadRestante_centro(i, centroId);
            } else {
                //si no hay un centro valido me conecto a un sensor random pero tengo que mirar que sea válido y que no sea el mismo
                //sensor porque no me puedo conectar a mi mismo
                int intentos = 0; //evitamos quedarnos en un bucle infinito
                int sensorId = rand.nextInt(sensor.size());
                while ((!es_valido_sensor(sensorId) || sensorId == i) && intentos < sensor.size()) {
                    sensorId = rand.nextInt(sensor.size());
                    ++intentos;
                }
                if (intentos == sensor.size()) {
                    System.out.println("Advertencia: No se encontró conexión válida para el sensor " + i);
                    continue;
                }
                actualizar_conexiones_sensor(i, sensorId);
                actualizar_capacidadRestante_sensor(i, sensorId);
            }

        }
    }

    /* esta función genera el estado inicial conectando al centro más cercano y teniendo en cuenta las restricciones */
    public void estado_inicial_cercania () {
        for (int i = 0; i < sensor.size(); ++i) {
            double min = Double.POSITIVE_INFINITY;
            int id_min = -1;

            for (int j = 0; j < centros.size(); ++j) {
                if (es_valido_centros(j + sensor.size())) {
                    double distancia = dist(i, j, true);
                    if (distancia < min) {
                        min = distancia;
                        id_min = j + sensor.size();
                    }
                }
            }
            if (id_min != -1) {
                actualizar_conexiones_centro(i, id_min);
                actualizar_capacidadRestante_centro(i, id_min);
            }

            else { // si no se puede conectar a un centro entonces se conecta al sensor o al centro más cercano
                boolean[][] visitado = new boolean[100][100];
                for (boolean[] fila : visitado) {
                    Arrays.fill(fila, false);
                }
                ArrayList<Integer> cercanos = bfs(i, visitado);
                boolean conexionEstablecida = false;


                for (int vecino : cercanos) { //vecino = cercanos.get(i)
                    if (es_centro(vecino) && es_valido_centros(vecino)) {
                        actualizar_conexiones_centro(i, vecino);
                        actualizar_capacidadRestante_centro(i, vecino);
                        conexionEstablecida = true;
                        break; // Salimos del bucle al encontrar una conexión válida
                    } else if (es_sensor(vecino) && es_valido_sensor(vecino)) {
                        actualizar_conexiones_sensor(i, vecino);
                        actualizar_capacidadRestante_sensor(i, vecino);
                        conexionEstablecida = true;
                        break; // Salimos al encontrar una conexión válida
                    }
                }

                if (!conexionEstablecida) {
                    System.out.println("Advertencia: No se encontró conexión válida para el sensor " + i);
                }
            }
        }
    }

    /* ===================== HEURÍSTICA ===================== */
    public double getHeuristica() {
        double costeTotal = 0;
        double infoTotal = 0;

        for (int i = 0; i < conexiones.length; i++) {
            if (conexiones[i] != -1) { // Si el sensor i tiene conexión
                double distancia = dist(i, conexiones[i], es_centro(conexiones[i]));
                double volumenCapturado = sensor.get(i).getCapacidad();
                double volumenTransmitido = Math.min(volumenCapturado * 3, capacidadRestante[i]); // Un sensor transmite como máximo 3 veces su capacidad
                double capacidadDestino;
                if (es_centro(conexiones[i])) capacidadDestino = 125;
                else capacidadDestino = sensor.get(conexiones[i]).getCapacidad() * 2;

                // Si el volumen transmitido supera la capacidad del destino, se pierde info
                if (volumenTransmitido > capacidadDestino) {
                    volumenTransmitido = capacidadDestino;
                }

                costeTotal += Math.pow(distancia, 2) * volumenTransmitido;
                infoTotal += volumenTransmitido;
            }
        }

        this.coste = costeTotal;
        this.info = infoTotal;

        return a*coste - b*info;

    }

    @Override
    public String toString() {
        return "Sensores: " + sensor.size() +
                ", Centros: " + centros.size() +
                ", Conexiones: " + Arrays.toString(conexiones) +
                ", Coste: " + coste +
                ", Información transmitida: " + info;
    }

}
