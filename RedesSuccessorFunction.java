import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;
import IA.Red.*;

import java.util.*;

public class RedesSuccessorFunction implements SuccessorFunction{
    public List<Successor> getSuccessors(Object a) {
        ArrayList<Successor> retVal = new ArrayList();
        Estado estadoActual = (Estado) a;
        Sensores sensor = Estado.sensor;
        CentrosDatos centros = Estado.centros;

        for (int i = 0; i < sensor.size(); i++) {
            for (int j =  1; j < sensor.size(); ++j) {
                Estado newState = new Estado(estadoActual);
                if (newState.swap(i, j)) {
                    String S = ("INTERCAMBIO " + " " + i + " " + j + " " + newState.toString() + "\n");
                    retVal.add(new Successor(S, newState));
                }
            }
        }

        for (int i = 0; i < sensor.size(); i++) {
            for (int j = 0; j < sensor.size() + centros.size(); ++j) { // Puede ser otro sensor o un centro
                if (i != j) { // No puede moverse a sí mismo
                    Estado newState = new Estado(estadoActual);
                    if (newState.moverConexion(i, j)) {
                        String S;
                        if (j >= sensor.size()) {
                            S = "MOVIDA conexión: " + i + " al centro: " + (j - sensor.size()) + " " + newState.toString() + "\n";
                        }
                        else S = "MOVIDA conexión: " + i + " al sensor: " + j + " " + newState.toString() + "\n";
                        retVal.add(new Successor(S, newState));
                    }
                }
            }
        }
        return retVal;
    }
}

