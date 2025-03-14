import IA.Red.*;
import java.util.*;
import java.util.Random;

public class Estado {

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

    /**
     * Este vector tendrá tamaño sensor.size()+centros.size().
     * Indica el número de conexiones que tiene cada sensor/centro
     */
    private int[] contador_conexiones;

    /*Constructor*/
    public Estado (int nsensores, int semilla, int ncentros) {
        this.sensor = new Sensores(nsensores, semilla);
        this.centros = new CentrosDatos(ncentros, semilla);
        this.conexiones = new int[nsensores] ;
        Arrays.fill(conexiones, -1);
        this.contador_conexiones = new int[nsensores+ncentros];
        Arrays.fill(contador_conexiones, 0);
    }

    /*Métodos privados*/
    private boolean dfs(int sensorId, boolean[] visitado) {
        if (es_centro(sensorId)) return true;

        if (!visitado[sensorId]) {
            visitado[sensorId] = true;
            int v = conexiones[sensorId];
            dfs(v, visitado);
        }
        return false;
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
        return Math.sqrt((Math.pow(2,(x1-x2))) + (Math.pow(2,(x2-y2))));
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

    /* esta función genera el estado inicial aleatoriamente */
    public void estado_inicial_random () {
        Random rand = new Random();

        for (int i = 0; i < sensor.size(); i++) {
            int centroId = sensor.size() + rand.nextInt(centros.size());
            if (es_valido_centros(centroId)) {
                conexiones[i] = centroId;
                ++contador_conexiones[centroId];
            } else {
                int sensorId = rand.nextInt(sensor.size());
                while (!es_valido_sensor(sensorId)) {
                    sensorId = rand.nextInt(sensor.size());
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

            else { // si no se puede conectar a un centro entonces se conecta a un sensor random
                Random rand = new Random();
                int sensorId = rand.nextInt(sensor.size());
                while (!es_valido_sensor(sensorId)) {
                    sensorId = rand.nextInt(sensor.size());
                }
                conexiones[i] = sensorId;
                ++contador_conexiones[sensorId];
            }
        }
    }

    /* mover estos métodos a la clase del algoritmo
    public double cantidad_a_transmitir(int sensorId) {
        double capacidad = sensor.get(sensorId).getCapacidad();
        return capacidad*3;
    }

    public double cantidad_a_recibir(int sensorId) {
        return sensor.get(sensorId).getCapacidad();
    }

    public double cantidad_a_almacenar(int sensorId) {
        double capacidad = sensor.get(sensorId).getCapacidad();
        return capacidad*2;
    }*/

}
