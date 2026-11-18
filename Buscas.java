
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.Queue;
import java.util.Stack;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Comparator;
import java.util.HashSet;

enum Direction {  LEFT, UP, RIGHT, DOWN } // up means: moving a piece up


enum Heuristic {  BADNUM, MANDIST, INVERSIONS, NONE;  // up means: moving a piece up
    @Override
    public String toString() {
        switch(this) {
            case BADNUM: return "Number of pieces out of place";
            case MANDIST: return "Manhattan distance";
            case INVERSIONS: return "Number of inversions";
            case NONE: return "None";
            default: throw new IllegalArgumentException();
        }
    }
}


//Estado do jogo (nó)
class Game implements Comparable<Game> {
    static int[] finalConfig;

    Game pai;

    int[] data;
    int i0;               //Localização do zero
    int h;                //Valor da heuristica
    int d;                //Profundidade do nó
    Heuristic heu;
    Direction lastMove;

    Game(int[] initialConfig, Heuristic heu) {
        data = new int[16];
        for(int i=0; i<16; i++) {
            int v = initialConfig[i];
            if(v == 0) {
                i0 = i;
            }
            data[i] = v;
        }
        this.heu = heu;
        UpdateHeuristicValue();
        d=0;
    }

    //Heuriticas---------------------------------------------------------
    private void UpdateHeuristicValue() {
        switch (heu) {
            case BADNUM:
            h = BadPiecesHeuristic();
            break;

            case MANDIST:
            h = ManDistHeuristic();
            break;

            case INVERSIONS:
            h = CalculateInversions();
            break;

            default:
        }
    }

    //Número de peças fora de sítio
    private int BadPiecesHeuristic() {
        int f = 0;
        for(int i=0; i<16; i++) if(data[i] != finalConfig[i]) f++;
        return f;
    }

    //Manhattan distance
    private int ManDistHeuristic() {
        int f = 0;
        for(int i=0; i<16; i++) {
            if(i == i0) continue;
            int j;
            for(j=0; finalConfig[j] != data[i]; j++) {
            }
            f += Math.abs(i%4 - j%4) + Math.abs(i/4 - j/4);
        }
        return f;
    }

    //Número de inversões
    private int CalculateInversions() {
        int n_inversoes;
        int soma_total=0;
        for(int i=0;i<16;i++){
            n_inversoes=0;
            if(data[i]!=0){
                for(int j=i+1;j<16;j++){
                    if(data[j]<data[i] && data[j]>0){
                        n_inversoes+=1;
                    }
                }
            }
            soma_total+=n_inversoes;
        }
        return soma_total;
    }

    @Override
    public String toString() {
        String str = "";
        for(int i=0; i<16; i++) {
            str += data[i] + "-";
        }
        return str;
    }
    //-------------------------------------------------------------------


    //Métodos------------------------------------------------------------

    //Guarda a configuração final numa variável static.
    public static void SetFinalConfig(int[] finalConfigToCopy) {
        finalConfig = new int[16];
        for(int i=0; i<16; i++) finalConfig[i] = finalConfigToCopy[i];
    }

    //Retorna um vetor com as direções possíveis (ou seja, as jogadas possíveis)
    public Vector<Direction> PossibleMoves() {
        Vector<Direction> moves = new Vector<Direction>();

        if(i0 % 4 < 3) moves.add(Direction.LEFT);
        if(i0 < 12) moves.add(Direction.UP);
        if(i0 % 4 > 0) moves.add(Direction.RIGHT);
        if(i0 > 4) moves.add(Direction.DOWN);

        return moves;
    }

    //Faz uma jogada mantendo a heuristica atualizada
    public void MakeMove(Direction d) {
        int swapIndex = -1;
        switch (d) {
            case RIGHT:
            swapIndex = i0-1;
            break;

            case LEFT:
            swapIndex = i0+1;
            break;

            case DOWN:
            swapIndex = i0-4;
            break;

            case UP:
            swapIndex = i0+4;
            break;

            default:
        }

        lastMove = d;
        data[i0] = data[swapIndex];
        data[swapIndex] = 0;
        i0 = swapIndex;

        UpdateHeuristicValue();
    }

    //Retorna a lista dos filhos
    public LinkedList<Game> MakeDescendants() {
        Vector<Direction> moves = PossibleMoves();
        LinkedList<Game> descendents = new LinkedList<Game>();
        for(Direction d : moves) {
            Game descendent = new Game(data, heu);
            descendent.MakeMove(d);
            descendents.add(descendent);
        }
        return descendents;
    }

    //Retorna o filho com menor heurística
    public Game GreedyDescendent() {
        Vector<Direction> moves = PossibleMoves();

        Game bestDescendent = new Game(data, heu);
        bestDescendent.MakeMove(moves.remove(0));

        for(Direction d : moves) {
            Game newDescendent = new Game(data, heu);
            newDescendent.MakeMove(d);
            if(newDescendent.h <= bestDescendent.h) {
                bestDescendent = newDescendent;
            }
        }
        return bestDescendent;
    }

    //Verifica se o estdo do jogo é igual ao estado final
    public boolean Solved() {
        for(int i=0; i<16; i++) {
            if(data[i] != finalConfig[i]) {
                return false;
            }
        }
        return true;
    }

    //Verifica se o é possível chegar ao estado final através do número de inversões
    public boolean ValidProblem() {
        int inversoes = CalculateInversions();
        if( (i0/4)%2 != inversoes % 2 ) {
            return true;
        }else{
            System.out.println(" Impossible.");
            return false;
        }
    }

    public void PrintBoard() {
        System.out.println("Heuristic: " + (h+d));
        for(int i=0; i<4; i++)System.out.print(data[i] + " ");
        System.out.println();
        for(int i=4; i<8; i++)System.out.print(data[i] + " ");
        System.out.println();
        for(int i=8; i<12; i++)System.out.print(data[i] + " ");
        System.out.println();
        for(int i=12; i<16; i++)System.out.print(data[i] + " ");
        System.out.println();
        System.out.println();
    }

    //Retorna a lista de jogadas feitas desde o nó inicial até este
    LinkedList<Direction> GetPath() {
        LinkedList<Direction> path = new LinkedList<Direction>();
        Game game = this;
        while(game.pai != null) {//Tá na ordem contrária
            path.addFirst(game.lastMove);
            game = game.pai;
        }
        return path;
    }
    //-------------------------------------------------------------------

    @Override
    public int compareTo(Game game) {
        int thisF = this.h + this.d;
        int gameF = game.h + game.d;
        if(thisF > gameF) {
            return 1;
        } else if (thisF < gameF) {
            return -1;
        } else {
            return 0;
        }
    }

}


public class Buscas {

    static void AnswerNotFound() {
        System.out.println(" Didn't find answer.\n");
    }

    public static void PrintPath(Game node, int maxMem, int totalMem) {
        LinkedList<Direction> path = node.GetPath();
        System.out.println(" Heuristic: " + node.heu);
        System.out.println(" Number of moves: " + path.size());
        System.out.println(" Path: " + path);
        System.out.println(" Maximum nodes in memory: " + maxMem+ "\tTotal nodes searched: " + totalMem);
        System.out.println();
    }

    //Buscas-------------------------------------------------------------------------------------------------------
    static void Pesquisa_em_Profundidade(int[] initialConfig, int[] finalConfig, Heuristic h){
        int maxMem=0, totalMem=0;
        Game g = new Game(initialConfig, h);
        HashSet<String> m = new HashSet<String>();

        System.out.println("DFS:");

        if(!g.ValidProblem()) {
            AnswerNotFound();
            return;
        }

        Stack<Game> s = new Stack<Game>();
        s.push(g);
        while(!s.isEmpty()) {
            maxMem = Math.max(maxMem, s.size());
            totalMem++;
            if(totalMem > 10000000) {
                System.out.println(" Time limit for search exceeded.\n");
                return;
            }
            Game node = s.pop();
            m.add(node.toString());
            if(node.Solved()) {
                PrintPath(node, maxMem, totalMem);;
                return;
            }

            LinkedList<Game> descendents = node.MakeDescendants();
            for(Game desc : descendents) {
                if(m.contains(desc.toString())) {
                    continue;
                }
                s.push(desc);
            }

        }
        AnswerNotFound();
        return;

    }

    static void Pesquisa_em_Largura(int[] initialConfig, int[] finalConfig, Heuristic h){
        int maxMem=0, totalMem=0;
        Game g = new Game(initialConfig, h);
        HashSet<String> m = new HashSet<String>();

        System.out.println("BFS:");
        if(!g.ValidProblem()) {
            AnswerNotFound();
            return;
        }

        LinkedList<Direction> path = new LinkedList<Direction>();
        Queue<Game> q = new LinkedList<Game>();
        q.add(g);
        while(!q.isEmpty()) {
            maxMem = Math.max(maxMem, q.size());
            totalMem++;
            if(totalMem > 10000000) {
                System.out.println(" Time limit for search exceeded.\n");
                return;
            }
            Game node = q.remove();
            m.add(node.toString());
            if(node.Solved()) {
                PrintPath(node, maxMem, totalMem);;
                return;
            }

            LinkedList<Game> descendents = node.MakeDescendants();
            for(Game desc : descendents) {
                if(m.contains(desc.toString())) {
                    continue;
                }
                desc.pai = node;
                q.add(desc);
            }
        }
        AnswerNotFound();
        return;
    }

    @SuppressWarnings("unchecked")
    static void Busca_iterativa_limitada_em_profundidade(int[] initialConfig, int[] finalConfig, Heuristic h){
        int maxMem=0, totalMem=0;
        int depth_lim = 3;
        Game g = new Game(initialConfig, h);
        System.out.println("DFSI:");
        HashSet<String> m = new HashSet<String>();

        if(!g.ValidProblem()) {
            AnswerNotFound();
            return;
        }
        Stack<Direction> path = new Stack<Direction>();
        Stack<Game> s = new Stack<Game>();
        Stack<Game> newS = new Stack<Game>();
        s.push(g);
        while(depth_lim <= 50) {

            while(!s.isEmpty()) {
                maxMem = Math.max(maxMem, s.size());
                totalMem++;
                if(totalMem > 10000000) {
                    System.out.println(" Time limit for search exceeded.\n");
                    return;
                }
                Game node = s.pop();
                m.add(node.toString());
                if(node.Solved()) {
                    PrintPath(node, maxMem, totalMem);;
                    return ;
                }
                if(node.d > depth_lim) {
                    newS.push(node);
                    continue;
                }

                LinkedList<Game> descendents = node.MakeDescendants();
                for(Game desc : descendents) {
                    if(m.contains(desc.toString())) {
                        continue;
                    }
                    desc.pai = node;
                    desc.d = node.d + 1;
                    s.push(desc);
                }
            }
            s = (Stack<Game>) newS.clone();
            newS.clear();
            depth_lim += 3;
        }
        AnswerNotFound();
        return;
    }

    private static void Gulosa(int[] initialConfig, int[] finalConfig, Heuristic h) {
        int maxMem=0, totalMem=0;
        Game g = new Game(initialConfig, h);
        System.out.println("Greedy:");
        HashSet<String> m = new HashSet<String>();

        if(!g.ValidProblem()) {
            AnswerNotFound();
            return;
        }
        LinkedList<Direction> path = new LinkedList<Direction>();
        PriorityQueue<Game> pq = new PriorityQueue<Game>();
        pq.add(g);
        while(!pq.isEmpty()) {
            maxMem = Math.max(maxMem, pq.size());
            totalMem++;
            if(totalMem > 10000000) {
                System.out.println(" Time limit for search exceeded.\n");
                return;
            }
            Game node = pq.poll();
            m.add(node.toString());
            if(node.Solved()) {
                PrintPath(node, maxMem, totalMem);;
                return;
            }

            LinkedList<Game> descendents = node.MakeDescendants();
            for(Game desc : descendents) {
                if(m.contains(desc.toString())) {
                    continue;
                }
                desc.pai = node;
                pq.add(desc);
            }
        }
        AnswerNotFound();
        return;
    }

    private static void A_Star(int[] initialConfig, int[] finalConfig, Heuristic h) {
        Game g = new Game(initialConfig, h);
        HashSet<String> m = new HashSet<String>();
        int maxMem=0, totalMem=0;


        System.out.println("A*:");

        if(!g.ValidProblem()) {
            AnswerNotFound();
            return;
        }
        PriorityQueue<Game> pq = new PriorityQueue<Game>();
        pq.add(g);
        while(!pq.isEmpty()) {
            maxMem = Math.max(maxMem, pq.size());
            Game node = pq.poll();
            totalMem++;
            if(totalMem > 10000000) {
                System.out.println(" Time limit for search exceeded.\n");
                return;
            }

            m.add(node.toString());
            if(node.Solved()) {
                PrintPath(node, maxMem, totalMem);
                return;
            }

            LinkedList<Game> descendents = node.MakeDescendants();
            for(Game desc : descendents) {
                if(m.contains(desc.toString())) {
                    continue;
                }
                desc.pai = node;
                desc.d = node.d + 1;
                pq.add(desc);
            }
        }

        AnswerNotFound();
        return;
    }

    //-------------------------------------------------------------------------------------------------------------


    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        int[] initialConfig = new int[16];
        int[] finalConfig = new int[16];

        for(int i=0; i<16; i++) initialConfig[i] = sc.nextInt();
        for(int i=0; i<16; i++) finalConfig[i] = sc.nextInt();
        Game.SetFinalConfig(finalConfig);


        Gulosa(initialConfig, finalConfig, Heuristic.MANDIST);
        Gulosa(initialConfig, finalConfig, Heuristic.BADNUM);
        Gulosa(initialConfig, finalConfig, Heuristic.INVERSIONS);
        A_Star(initialConfig, finalConfig, Heuristic.MANDIST);
        A_Star(initialConfig, finalConfig, Heuristic.BADNUM);
        A_Star(initialConfig, finalConfig, Heuristic.INVERSIONS);
        Busca_iterativa_limitada_em_profundidade(initialConfig, finalConfig, Heuristic.MANDIST);

        Pesquisa_em_Largura(initialConfig, finalConfig, Heuristic.NONE);
        Pesquisa_em_Profundidade(initialConfig, finalConfig, Heuristic.NONE);
    }
}
