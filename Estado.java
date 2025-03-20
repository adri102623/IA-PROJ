import IA.Red.*;
import java.util.*;

public class Estado implements Cloneable {
    //arrays constantes para bfs
    private static final int[] X = {-1, 0, 1, 0};
    private static final int[] Y = {0, 1, 0, -1};

    /* array lists */
    private Sensores sensor;
    private CentrosDatos centros;

    /**
     * Este vector indica a que está conectado cada sensor
     * Este vector tendrá tamaño sensor.size().
     * De 0 a sensor.size() - 1  serán los identificadores de los sensores
     * De sensor.size() hasta sensor.size()+centros.size()-1 serán los identificadores de los centros
     */
    private int[] conexiones;
    private double[] capacidadRestante;
    /**
     * Este vector tendrá tamaño sensor.size()+centros.size().
     * Indica el número de conexiones que tiene cada sensor/centro
     */
    private int[] contador_conexiones;

    /**
     * Tablero 100 x 100
     */
    int[][] tablero;

    int info_perdida;

    /*Constructor*/
    public Estado (int nsensores, int semilla, int ncentros) {
        this.sensor = new Sensores(nsensores, semilla);
        this.centros = new CentrosDatos(ncentros, semilla);
        this.conexiones = new int[nsensores] ;
        Arrays.fill(conexiones, -1);
        this.capacidadRestante = new double[nsensores + ncentros];
        for(int i = 0; i < sensor.size(); i++){
            capacidadRestante[i] = sensor.get(i).getCapacidad()*2;
        }
        for(int i = sensor.size(); i < sensor.size() + centros.size(); ++i) {
            capacidadRestante[i] = 125;
        }
        this.contador_conexiones = new int[nsensores+ncentros];
        Arrays.fill(contador_conexiones, 0);
        this.tablero = new int[100][100];
        for (int i = 0; i < 100; i++) {
            Arrays.fill(tablero[i], -1); //inicializamos matriz a -1
        }
        int posX, posY;
        for (int i = 0; i < nsensores; ++i) {
            posX = sensor.get(i).getCoordX();
            posY = sensor.get(i).getCoordY();
            tablero[posX][posY] = i;
        }
        for (int j = 0; j < ncentros; ++j) {
            posX = centros.get(j).getCoordX();
            posY = centros.get(j).getCoordY();
            tablero[posX][posY] = j+nsensores;
        }
        this.info_perdida = 0;
    }

    /*Métodos privados*/
    private boolean dfs(int sensorId, boolean[] visitado) {
        if (es_centro(sensorId)) return true;

        if (!visitado[sensorId]) {
            visitado[sensorId] = true;
            int v = conexiones[sensorId];
            //dfs(v, visitado);ç
            if (v != -1 && dfs(v, visitado)) {
                return true;
            }
        }
        return false;
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

    private boolean es_sensor(int sensorId) {
        return (0 <= sensorId && sensorId < sensor.size());
    }

    private boolean es_centro(int centroId) {
        return (sensor.size() <= centroId  && centroId < conexiones.length);
    }

    /**
     * Si el sensor tiene 2 conexiones, es válido, porque se le puede añadir una tercera conexión
     */
    private boolean es_valido_sensor(int sensorId) {
        return contador_conexiones[sensorId] <= 2;
    }

    /*
      Si el centro tiene 24 conexiones, es válido, porque se le puede añadir una 25 conexión
     */
    private boolean es_valido_centros(int centroId) {
        return contador_conexiones[centroId] <= 24;
    }

    private boolean esta_transmitiendo(int sensorId) {
        return conexiones[sensorId] != -1;
    }

    private void actualizar_capacidadRestante_centro(int sensorId, int centroId) {
        if (sensor.get(sensorId).getCapacidad() > capacidadRestante[centroId]) {
            info_perdida += (int) sensor.get(sensorId).getCapacidad() - (int) capacidadRestante[centroId];
            capacidadRestante[centroId] = 0;
        }
        else capacidadRestante[centroId] -= sensor.get(sensorId).getCapacidad();
    }

    //el 1 le envía al 2
    private void actualizar_capacidadRestante_sensor(int sensorId1, int sensorId2) {
        if (sensor.get(sensorId1).getCapacidad() > capacidadRestante[sensorId2]) {
            info_perdida += (int) sensor.get(sensorId1).getCapacidad() - (int) capacidadRestante[sensorId2];
            capacidadRestante[sensorId2] = 0;
            sensor.get(sensorId2).setCapacidad((int) sensor.get(sensorId2).getCapacidad() + (int) capacidadRestante[sensorId2]);
        }
        else {
            capacidadRestante[sensorId2] -= sensor.get(sensorId1).getCapacidad();
            sensor.get(sensorId2).setCapacidad((int) sensor.get(sensorId2).getCapacidad() + (int) sensor.get(sensorId1).getCapacidad());
        }
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

    /*Métodos públicos*/
    /**
     * Conectamos id1 (origen) con id2 (destino)
     * El id1 no puede ser un centro, porque los centros no se conectan a otros
     * Si la conexión ya existe no se crea una nueva
     * No se permite que un sensor tenga más de 3 conexiones
     */
    public void crear_conexion (int id1, int id2) {
        if (es_centro(id1)) {
            System.out.println("Error: El centro " + id1 + "solo recibe información.");
            return;
        }

        /*if (esta_transmitiendo(id1)) {
            System.out.println("Error: El sensor " + id1 + "ya está transmitiendo información.");
            return;
        }*/

        if (conexiones[id1] == id2) {
            System.out.println("Error: La conexión entre " + id1 + " y " + id2 + " ya está establecida.");
            return;
        }

        if (!es_valido_sensor(id2)) {
            System.out.println("Error: El nodo " + id2 + " no acepta más conexiones.");
            return;
        }

        if (esta_transmitiendo(id1)) {
            romper_conexion(id1);
        }

        ++contador_conexiones[id2];
        conexiones[id1] = id2;

        System.out.println("Se ha creado la conexión entre " + id1 + " y " + id2);
    }

    /*
    Si no tiene conexión no se puede romper
    Si id1 es un centro, no puede tener una conexión
     */
    public void romper_conexion (int id1) {
        if (es_centro(id1)) {
            System.out.println("Error: El nodo " + id1 + " es un centro.");
            return;
        }
        if (conexiones[id1] == -1) {
            System.out.println("Error: El sensor " + id1 + " no tiene ninguna conexión establecida.");
            return;
        }

        int id2 = conexiones[id1];
        --contador_conexiones[id2];
        conexiones[id1] = -1;

        System.out.println("Se ha roto la conexión entre " + id1 + " y " + id2);
    }

    public boolean conexion_con_centro(int sensorId) {
        boolean[] visitado = new boolean[sensor.size()];
        Arrays.fill(visitado, false);
        return dfs(sensorId, visitado);
    }

    public int getInfo_perdida() {
        return this.info_perdida;
    }

    /* esta función genera el estado inicial aleatoriamente */
    public void estado_inicial_random () {
        Random rand = new Random();

        for (int i = 0; i < sensor.size(); i++) {
            int centroId = sensor.size() + rand.nextInt(centros.size());
            if (es_valido_centros(centroId)) {
                conexiones[i] = centroId;
                ++contador_conexiones[centroId];
                actualizar_capacidadRestante_centro(i, centroId);
            } else {
                int sensorId = rand.nextInt(sensor.size());
                while (!es_valido_sensor(sensorId)) {
                    sensorId = rand.nextInt(sensor.size());
                    actualizar_capacidadRestante_sensor(i, sensorId);
                }
                conexiones[i] = sensorId;
                ++contador_conexiones[sensorId];
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
                conexiones[i] = id_min;
                ++contador_conexiones[id_min];
            }

            else { // si no se puede conectar a un centro entonces se conecta al sensor o al centro más cercano
                boolean[][] visitado = new boolean[100][100];
                for (int l = 0; l < 100; l++) {
                    Arrays.fill(visitado[l], false);
                }
                ArrayList<Integer> cercanos = bfs(i, visitado);
                boolean encontrado = false;
                int k = 0;
                while (!encontrado && k < cercanos.size()) {
                    if (es_centro(cercanos.get(k))) {
                        if(es_valido_centros(cercanos.get(k))) {
                            encontrado = true;
                            conexiones[i] = cercanos.get(k);
                            ++contador_conexiones[cercanos.get(k)];
                            actualizar_capacidadRestante_centro(i, cercanos.get(k));
                        }
                    }
                    else if (es_valido_sensor(cercanos.get(k))) {
                        conexiones[i] = cercanos.get(k);
                        ++contador_conexiones[cercanos.get(k)];
                        actualizar_capacidadRestante_sensor(i, cercanos.get(k));
                    }
                }
            }
        }
    }

    /*
     * A ACABAR
     */
    /*public Estado clone() {

    }*/

}
