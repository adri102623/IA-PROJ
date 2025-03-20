import IA.Red.*;
import java.util.*;
import java.util.Random;

/*
 * Concepto: A partir de un estado inicial, nosotros podemos aplicar una de entre un conjunto de operaciones disponibles a un sensor,
 * lo que nos generará un estado sucesor, es decir, un estado con la operación aplicada y las variables correspondientes actualizadas.
 * 
 * Por tanto, esta clase estará compuesta de un estado inicial sobre el cuál se aplicará la operación de operador sobre los nodos transmisor y receptor.
 */

public class GeneradorSucesores {
    private Estado estadoInicial;   //Contendrá el estado del cual se generará el estado sucesor
    private int idNodoTransmisor;   //
    private int idNodoReceptor;
    private operador op;

    public GeneradorSucesores(Estado estadoInicial, int transmisor, int receptor, operador operacion) {
        this.estadoInicial = estadoInicial;
        this.idNodoTransmisor = transmisor;
        this.idNodoReceptor = receptor;
        this.op = operacion;
    }

    /*
     * A ACABAR
     */
    public Estado generarSucesores() {

        switch (this.op.ordinal()) {
            case value:
                
                break;
            default:
                if (this.estadoInicial.es_centro(idNodoReceptor)) {

                }
                break;
        }
        return estadoSucesor;
    }

    /*
     * OPERACIONES PRIVADAS USADAS EN EL CÓDIGO
     */
}
