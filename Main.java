import java.util.*;
import aima.search.framework.*;
import aima.search.informed.HillClimbingSearch;
import aima.search.informed.SimulatedAnnealingSearch;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Introduce numero de sensores: ");
        int nsensores = scanner.nextInt();

        System.out.print("Introduce numero de centros de datos: ");
        int ncentros = scanner.nextInt();

        System.out.print("¿Qué algoritmo quieres usar? [1 para Hill Climbing / 0 para Simulated Annealing]: ");
        boolean hillClimb = (scanner.nextInt() == 1);

        int steps = 10000, stiter = 1000, k = 25;
        double lambda = 0.01;
        if (!hillClimb) {
            System.out.println("Introduce los parámetros de Simulated Annealing:");
            System.out.print("Steps [10000 por defecto]: ");
            steps = scanner.nextInt();
            System.out.print("Stiter [1000 por defecto]: ");
            stiter = scanner.nextInt();
            System.out.print("K [25 por defecto]: ");
            k = scanner.nextInt();
            System.out.print("Lambda [0.01 por defecto]: ");
            lambda = scanner.nextDouble();
        }

        System.out.print("¿Qué estrategia para la solución inicial? [1 para avariciosa / 0 para aleatoria]: ");
        boolean greedy = (scanner.nextInt() == 1);

        Estado estadoInicial = new Estado(greedy);

        long startTime = System.nanoTime();

        if (hillClimb) {
            ejecutarHillClimbing(estadoInicial);
        } else {
            ejecutarSimulatedAnnealing(estadoInicial, steps, stiter, k, lambda);
        }

        long endTime = System.nanoTime();
        System.out.println("Duración del algoritmo: " + (endTime - startTime) / 1000000 + " ms");
    }

    private static void ejecutarHillClimbing(Estado estado) {
        System.out.println("\nEjecutando Hill Climbing...");
        try {
            RedesSuccessorFunction successorFunction = new RedesSuccessorFunction();
            RedesGoalTest goalTest = new RedesGoalTest();
            RedesHeuristicFunction heuristicFunction = new RedesHeuristicFunction();
            Problem problem = new Problem(estado, successorFunction, goalTest, heuristicFunction);
            Search search = new HillClimbingSearch();
            SearchAgent agent = new SearchAgent(problem, search);

            imprimirResultados(agent);
            Estado solucion = (Estado) search.getGoalState();
            System.out.println("\nSolución Final: " + solucion);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void ejecutarSimulatedAnnealing(Estado estado, int steps, int stiter, int k, double lambda) {
        System.out.println("\nEjecutando Simulated Annealing...");
        try {
            RedesSuccessorFunctionSA successorFunction = new RedesSuccessorFunctionSA();
            RedesGoalTest goalTest = new RedesGoalTest();
            RedesHeuristicFunction heuristicFunction = new RedesHeuristicFunction();
            Problem problem = new Problem(estado, successorFunction, goalTest, heuristicFunction);
            Search search = new SimulatedAnnealingSearch(steps, stiter, k, lambda);
            SearchAgent agent = new SearchAgent(problem, search);

            imprimirResultados(agent);
            Estado solucion = (Estado) search.getGoalState();
            System.out.println("\nSolución Final: " + solucion);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void imprimirResultados(SearchAgent agent) {
        System.out.println();
        for (Object action : agent.getActions()) {
            System.out.println(action);
        }
        Properties properties = agent.getInstrumentation();
        for (Object key : properties.keySet()) {
            System.out.println(key + " : " + properties.getProperty((String) key));
        }
    }
}