import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

public class NQueens implements Comparable<NQueens>{

    private static final int N = 20;
    private static final int MAX_GENERATIONS = 1;
    private static final int POPULATION_SIZE = 2;
    private static final int CROSSOVER_RATE = 50;

    private static Random r = new Random();
    private static int totalSearchCost = 0;
    private static long totalTimeElapsed = 0;
    private static int generationCount = 0;

    private byte[] board;
    private PriorityQueue<NQueens> kids;

    public static void main(String[] args){
        if(args.length != 2){
            try {
                throw new Exception("Two command-line args required: [hill|genetic] [number_of_instances]");
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        int solved = 0;
        int numOfInstances = Integer.parseInt(args[1]);
        switch (args[0]){
            case"hill":
                break;
            case"genetic":
                break;
            default:
        }
        //GENETIC----------------------
        for (int i = 0; i < numOfInstances; i++) {
            long start = System.currentTimeMillis();
            if(geneticSolve(POPULATION_SIZE)){
                solved++;
            }
            totalTimeElapsed += System.currentTimeMillis() - start;
        }

/*        
        //HILL CLIMB-----------------------
        Set<NQueens> setOfInstances = generateUniqueInstances(numOfInstances);
        
        for(NQueens instance : setOfInstances){
            long start = System.currentTimeMillis();
            if(hillClimbSolve(instance)){
                solved++;
            }
            totalTimeElapsed += System.currentTimeMillis() - start;
        }
*/
        System.out.println("Percent solved: " + solved/(double)numOfInstances * 100);
        System.out.println("Average search cost: " + totalSearchCost/(double)numOfInstances + " nodes");
        System.out.println("Average time elapsed: " + totalTimeElapsed/(double)numOfInstances + " ms");
/*
        //STANDALONE----------------------------
        byte[] standalone = {2, 0, 3, 1};
        totalSearchCost = 0;
        NQueens alone = new NQueens(standalone);
        System.out.println(alone);
        totalTimeElapsed = System.currentTimeMillis();
        boolean singleSolved = hillClimbSolve(alone);
        totalTimeElapsed = System.currentTimeMillis() - totalTimeElapsed;
        System.out.println("Solved? " + singleSolved);
        System.out.println("Search cost: " + totalSearchCost);
        System.out.println("Time elapsed: " + totalTimeElapsed + "ms");
*/
    }

    private static Set<NQueens> generateUniqueInstances(int n){
        Set<NQueens> result = new HashSet<>();
        while(result.size() < n){
            result.add(new NQueens(N));
        }
        return result;
    }

    public NQueens(byte[] board){
        this.board = board.clone();
    }

    public NQueens(int size){
        List<Byte> digits = new ArrayList<>();
        for(byte i = 0; i < size; i++){
            digits.add(i);
        }
        Collections.shuffle(digits);
        board = new byte[size];
        for(int i = 0; i < board.length; i++){
            board[i] = digits.get(i);
        }
    }

    public static boolean hillClimbSolve(NQueens instance){
        NQueens curr = instance;
        while(curr.compareTo(curr.getKids().peek()) > 0){
            curr = curr.getKids().peek();
        }
        return curr.isSolved();
    }

    public static boolean geneticSolve(int populationSize){
        Queue<NQueens> population = new PriorityQueue<>(generateUniqueInstances(populationSize));
        System.out.println("INITIAL---------");
        for (NQueens instance:
             population) {
            System.out.println(Arrays.toString(instance.board) + " " + instance.fitness());
        }
        while(!population.peek().isSolved() && generationCount < MAX_GENERATIONS){
            Queue<NQueens> nextPopulation = new PriorityQueue<>();
            while(!population.isEmpty()) {
                NQueens mommy = population.poll();
                NQueens daddy = population.poll();  //in an odd parity population, daddy could be null
                nextPopulation.addAll(makeBabiesFrom(mommy, daddy));
            }
            totalSearchCost += nextPopulation.size();
            generationCount++;
            population = nextPopulation;
        }
        System.out.println("FINAL---------------------");
        for (NQueens instance:
                population) {
            System.out.println(Arrays.toString(instance.board) + " " + instance.fitness());
        }
        return population.peek().isSolved();
    }

    public static Queue<NQueens> makeBabiesFrom(NQueens mommy, NQueens daddy){
        Queue<NQueens> babies = new PriorityQueue<>();
        babies.add(mommy);
        if(daddy != null){
            if(r.nextInt(100) < CROSSOVER_RATE) {
                int crossoverPoint = r.nextInt(N - 1) + 1;
                //in situ crossover with XOR
                for (int i = 0; i < crossoverPoint; i++) {
                    swap(mommy, daddy, i);
                }
            }
            babies.add(daddy);
        }
        for (NQueens instance : babies) {
            instance.mutate();
        }
        return babies;
    }
    
    public static void swap(NQueens mommy, NQueens daddy, int i){
        mommy.setBoard(i, (byte)(mommy.getBoard()[i] ^ daddy.getBoard()[i]));
        daddy.setBoard(i, (byte)(mommy.getBoard()[i] ^ daddy.getBoard()[i]));
        mommy.setBoard(i, (byte)(mommy.getBoard()[i] ^ daddy.getBoard()[i]));
    }

    public void mutate(){
        for (int i = 0; i < board.length; i++) {
            if(r.nextInt(N) < 1){
                board[i] = (byte)r.nextInt(N);
            }
        }
    }

    public PriorityQueue<NQueens> getKids(){
        if(kids == null){
            kids = new PriorityQueue<>();
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board.length; j++) {
                    if(this.board[i] != j) {
                        NQueens kid = new NQueens(this.board);
                        kid.setBoard(i, (byte) j);
                        kids.add(kid);
                    }
                }
            }
            totalSearchCost += kids.size();
        }
        return kids;
    }
    
    public byte[] getBoard(){
        return board.clone();
    }

    public void setBoard(int i, byte b){
        this.board[i] = b;
    }

    public int fitness(){
        int result = 0;
        for(int i = 0; i < board.length; i++){
            for(int j = i + 1; j < board.length; j++){
                if(board[i] == board[j]){
                    result++;
                }
                if((board[i] + (j - i) == board[j]) ||
                    (board[i] - (j - i) == board[j])){
                    result++;
                }
            }
        }
        return result;
    }

    public boolean isSolved(){
        return fitness() == 0;
    }

    @Override
    public int compareTo(NQueens that){
        return this.fitness() - that.fitness();
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < board.length; i++){
            for(byte j = 0; j < board.length; j++){
                sb.append((board[i] == j)? 'O': ' ');
                sb.append((j != board.length - 1)? '|': '\n');
            }
            if(i < board.length - 1){
                for (byte j = 0; j < board.length; j++) {
                    sb.append('-');
                    sb.append((j != board.length - 1) ? '+' : '\n');
                }
            }
        }
        return sb.toString();
    }

}