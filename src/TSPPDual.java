import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

public class TSPPDual implements ITSPP {
  // Variables
  int initialEdgesPosition;
  int initialNodesPosition;
  int initialLabelPosition;

  // Constraints
  int initialColorPosition;
  int initialLeavingEdgePosition;
  int initialEnteringEdgePosition;
  int initialCiclePrevetionPosition;

  // Constraint matrix
  private final int[][] A;
  private final char[] relations;
  private final int[] b;

  // Objective matrix
  private final int[] c;

  // Dual problem model
  private final GRBEnv env;
  private final GRBModel model;
  private final GRBVar[] p;

  TSPPDual(int[] coloredNodes, int[][] weightedEdges, int numberColors, int s, int t) throws GRBException {
    // Numbered edges
    int edgeNumber = -1;
    int[][] numberedEdges = new int[weightedEdges.length][weightedEdges[0].length];
    for (int i = 0; i < weightedEdges.length; i++)
      for (int j = 0; j < weightedEdges[i].length; j++)
        numberedEdges[i][j] = weightedEdges[i][j] > 0 ? ++edgeNumber : -1;

    // Primal problem variables
    this.initialEdgesPosition = 0;
    this.initialNodesPosition = 0;
    for (int i = 0; i < weightedEdges.length; i++)
      for (int j = 0; j < weightedEdges[i].length; j++)
        if (weightedEdges[i][j] > 0)
          this.initialNodesPosition++;

    this.initialLabelPosition = this.initialNodesPosition + coloredNodes.length;

    // Primal Problem constraints
    this.initialColorPosition = 0;
    this.initialLeavingEdgePosition = this.initialColorPosition + numberColors;
    this.initialEnteringEdgePosition = this.initialLeavingEdgePosition + coloredNodes.length;
    this.initialCiclePrevetionPosition = this.initialEnteringEdgePosition + coloredNodes.length;

    this.A = new int[this.initialCiclePrevetionPosition + this.initialNodesPosition][this.initialLabelPosition
        + coloredNodes.length];

    // Color constraints
    for (int c = 0; c < numberColors; c++)
      for (int i = 0; i < coloredNodes.length; i++)
        if (coloredNodes[i] == c)
          this.A[c][i + this.initialNodesPosition] = 1;

    // Leaving edges constraints
    for (int i = 0; i < coloredNodes.length; i++) {
      this.A[i + this.initialLeavingEdgePosition][i + this.initialNodesPosition] = 1;

      for (int j = 0; j < weightedEdges[i].length; j++)
        if (weightedEdges[i][j] > 0)
          this.A[i + this.initialLeavingEdgePosition][numberedEdges[i][j]] = -1;

    }

    // Entering nodes constraints
    for (int j = 0; j < coloredNodes.length; j++) {
      this.A[j + this.initialEnteringEdgePosition][j + this.initialNodesPosition] = 1;

      for (int i = 0; i < weightedEdges.length; i++)
        if (weightedEdges[i][j] > 0)
          this.A[j + this.initialEnteringEdgePosition][numberedEdges[i][j]] = -1;
    }

    // Label cicle previnition constraints
    int N = 1000000;

    for (int i = 0; i < weightedEdges.length; i++) {
      for (int j = 0; j < weightedEdges[i].length; j++) {
        if (weightedEdges[i][j] <= 0)
          continue;

        this.A[numberedEdges[i][j] + this.initialCiclePrevetionPosition][j + this.initialLabelPosition] = 1;
        this.A[numberedEdges[i][j] + this.initialCiclePrevetionPosition][i + this.initialLabelPosition] = -1;
        this.A[numberedEdges[i][j] + this.initialCiclePrevetionPosition][numberedEdges[i][j]
            + this.initialEdgesPosition] = -N - 1;
      }
    }

    // b and relation matrices
    this.b = new int[this.A.length];
    this.relations = new char[this.A.length];
    for (int i = this.initialColorPosition; i < this.initialLeavingEdgePosition; i++) {
      this.relations[i] = GRB.GREATER_EQUAL;
      b[i] = 1;
    }

    for (int i = this.initialLeavingEdgePosition; i < this.initialEnteringEdgePosition; i++) {
      this.relations[i] = GRB.EQUAL;
      b[i] = (i - this.initialLeavingEdgePosition) != t ? 0 : 1;
    }

    for (int i = this.initialEnteringEdgePosition; i < this.initialCiclePrevetionPosition; i++) {
      this.relations[i] = GRB.EQUAL;
      b[i] = (i - this.initialEnteringEdgePosition) != s ? 0 : 1;
    }

    for (int i = this.initialCiclePrevetionPosition; i < this.b.length; i++) {
      this.relations[i] = GRB.GREATER_EQUAL;
      b[i] = -N;
    }

    // Objective matrix
    this.c = new int[this.A[0].length];
    for (int i = 0; i < weightedEdges.length; i++)
      for (int j = 0; j < weightedEdges[i].length; j++)
        if (weightedEdges[i][j] > 0)
          this.c[numberedEdges[i][j]] = weightedEdges[i][j];

    // Creating dual model
    this.env = new GRBEnv(true);
    this.env.set("logFile", "TSPPDual.log");
    this.env.start();

    this.model = new GRBModel(env);

    // Dual variables
    this.p = new GRBVar[this.A.length];

    // If the primal constraint is equal, p is free. If it's greater equal, then p
    // is greater equal.
    for (int i = 0; i < this.A.length; i++) {
      if (i >= this.initialLeavingEdgePosition && i < this.initialCiclePrevetionPosition)
        this.p[i] = this.model.addVar(-GRB.INFINITY, GRB.INFINITY, 0, GRB.INTEGER, "p[" + i + " ]");
      else
        this.p[i] = this.model.addVar(0, GRB.INFINITY, 0, GRB.INTEGER, "p[" + i + "]");
    }

    // Objective function
    GRBLinExpr objective = new GRBLinExpr();
    for (int i = 0; i < this.p.length; i++)
      objective.addTerm(this.b[i], this.p[i]);

    this.model.setObjective(objective, GRB.MAXIMIZE);

    // Constraints
    for (int j = 0; j < this.A[0].length; j++) {
      GRBLinExpr constraint = new GRBLinExpr();

      for (int i = 0; i < this.A.length; i++)
        constraint.addTerm(this.A[i][j], this.p[i]);

      this.model.addConstr(constraint, GRB.LESS_EQUAL, this.c[j], "constraint[" + j + "]");
    }

    this.model.optimize();
  }

  private String getAnsiCode(int variable) {
    String ANSI_RED = "\u001B[31m";
    String ANSI_BLUE = "\u001B[34m";
    String ANSI_YELLOW = "\033[0;33m";

    return variable < this.initialNodesPosition ? ANSI_RED
        : variable < this.initialLabelPosition ? ANSI_BLUE : ANSI_YELLOW;
  }

  private String getRelationAnsiCode() {
    return "\033[0;35m";
  }

  private String getAnsiResetCode() {
    return "\u001B[0m";
  }

  public void reportResults() throws GRBException {
    // Printing primal matrices
    for (int i = 0; i < this.c.length; i++)
      System.out.print(this.getAnsiCode(i) + this.c[i] + this.getAnsiResetCode() + " ");

    System.out.println();

    for (int i = 0; i < this.A.length; i++) {
      System.out.println();
      for (int j = 0; j < this.A[i].length; j++)
        System.out.print(this.getAnsiCode(j) + this.A[i][j] + this.getAnsiResetCode() + " ");

      System.out.print(this.getRelationAnsiCode() + this.relations[i] + " " + this.b[i] + this.getAnsiResetCode());
    }

    System.out.println("\n");

    // Printing results
    for (int i = 0; i < this.p.length; i++)
      System.out.println(p[i].get(GRB.StringAttr.VarName) + " " + p[i].get(GRB.DoubleAttr.X));

    System.out.println();

    System.out.println("Obj: " + this.model.get(GRB.DoubleAttr.ObjVal));
  }
}
