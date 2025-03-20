import aima.search.framework.HeuristicFunction;

public class RedesHeuristicFunction implements HeuristicFunction{
    public double getHeuristicValue(Object o) {
        Estado estado = (Estado) o;
        return estado.getHeuristica();
    }
}
