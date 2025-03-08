import IA.Red.*;
import java.util.*;

public class EstadoInicial {

    private Sensores sensor;
    private CentrosDatos centros;

    /**
     * Este vector tendrá tamaño sensor.size().
     * De 0 a sensor.size() - 1  serán los identificadores de los sensores
     * De sensor.size() hasta sensor.size()+centros.size()-1 serán los identificadores de los centros
     */
    private int[] conexiones;

    /*Constructor*/
    public EstadoInicial (int nsensores, int semilla, int ncentros) {
        this.sensor = new Sensores(nsensores, semilla);
        this.centros = new CentrosDatos(ncentros, semilla);
        this.conexiones = new int[nsensores] ;
    }

    /*Métodos privados*/
    private void dfs(int sensorId, boolean[] visitado) {
        if (visitado[sensorId]) return; // Si ya lo visitamos, terminamos

        visitado[sensorId] = true; // Marcamos como visitado

        // Revisar conexiones de este sensor
        int conectadoA = conexiones[sensorId];
        if (conectadoA != -1) { // Si tiene conexión
            if (conectadoA < sensor.size()) {
                // Si está conectado a otro sensor, seguimos explorando
                dfs(conectadoA, visitado);
            }
            // Si está conectado a un centro de datos, terminamos (se marcará como accesible)
        }

        // Revisamos si algún otro sensor apunta a este sensor (ruta indirecta)
        for (int i = 0; i < sensor.size(); i++) {
            if (conexiones[i] == sensorId) { // Si otro sensor se conecta a este
                dfs(i, visitado); // Exploramos también ese sensor
            }
        }
    }

    /*Métodos públicos*/
    public boolean es_valido_sensores() {
        boolean valido = true;
        for (int i = 0; i < sensor.size(); ++i) {
            int contador = 0;
            int j = 0;
            while (valido && j < conexiones.length) {
                if (conexiones[j] == i) ++contador;
                if (contador > 3) valido = false;
                ++j;
            }
        }
        return valido;
    }

    public boolean es_valido_centros() {
        boolean valido = true;
        for (int i = sensor.size(); i < sensor.size()+centros.size(); ++i) {
            int contador = 0;
            int j = 0;
            while (j < conexiones.length && valido) {
                if (conexiones[j] == i) ++contador;
                if (contador > 26) valido = false;
                ++j;
            }
        }
        return valido;
    }

    public boolean es_conexa() {
        int numSensores = sensor.size();
        boolean[] visitado = new boolean[numSensores];

        // Buscar un sensor conectado a un centro de datos como punto de inicio
        int inicio = -1;
        for (int i = 0; i < numSensores; i++) {
            if (conexiones[i] >= numSensores) { // Si está conectado directamente a un centro
                inicio = i;
                break;
            }
        }

        // Si ningún sensor tiene conexión directa a un centro, no es conexa
        if (inicio == -1) return false;

        // Realizar DFS recursivo para marcar sensores alcanzables
        dfs(inicio, visitado);

        // Verificar si todos los sensores fueron visitados
        for (boolean v : visitado) {
            if (!v) return false; // Si hay algún sensor no alcanzado, la red no es conexa
        }

        return true; // Todos los sensores pueden llegar a un centro de datos
    }

}
