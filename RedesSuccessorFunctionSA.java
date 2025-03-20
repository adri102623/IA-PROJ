import IA.Red.CentrosDatos;
import IA.Red.Sensores;
import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;
import java.util.*;

public class RedesSuccessorFunctionSA implements SuccessorFunction {
    public List<Successor> getSuccessors(Object a) {
        ArrayList<Successor> retVal = new ArrayList();
        Estado hijo = (Estado) a;
        Random random = new Random();
        int nsensores = Estado.sensor.size();
        int ncentros = Estado.centros.size();

        int factorRamificacionMoverConexion = nsensores*ncentros;
        int factorRamificacionSwap = nsensores*nsensores;
        int factorRamificacionTotal = factorRamificacionMoverConexion + factorRamificacionSwap;

        int numRand = random.nextInt(factorRamificacionTotal);
        Estado newState = new Estado(hijo);
        if (numRand < factorRamificacionMoverConexion) { //operador1
            int sensorRandom = random.nextInt(nsensores);
            int nodoRandom = random.nextInt(nsensores + ncentros);
            while (!newState.moverConexion(sensorRandom, nodoRandom)) {
                sensorRandom = random.nextInt(nsensores);
                nodoRandom = random.nextInt(nsensores + ncentros);
                newState = new Estado(hijo);
            }
            String S;
            if (nodoRandom >= nsensores) {
                S = ("MOVIDA conexión: " + sensorRandom + " al centro: " + (nodoRandom - nsensores) + " | " + newState.toString() + "\n");
            }
            else {
                S = ("MOVIDA conexión: " + sensorRandom + " al sensor: " + nodoRandom + " | " + newState.toString() + "\n");
            }
            retVal.add(new Successor(S, newState));
        }

        else { //operador2
            int sensorRandom1 = random.nextInt(nsensores);
            int sensorRandom2 = random.nextInt(nsensores);
            while (!newState.swap(sensorRandom1, sensorRandom2)) {
                sensorRandom1 = random.nextInt(nsensores);
                sensorRandom2 = random.nextInt(nsensores);
                newState = new Estado(hijo);
            }
            String S;
            S = ("INTERCAMBIO " + " " + sensorRandom1 + " " + sensorRandom2 + " | " + newState.toString() + "\n");
            retVal.add(new Successor(S, newState));
        }
            return retVal;
        }
    }

