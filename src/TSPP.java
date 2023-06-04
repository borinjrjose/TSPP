import gurobi.*;

public class TSPP {
  private final int[] coloredNodes;

  private final GRBEnv env;
  private final GRBModel model;
  private final GRBVar[] nodes;
  private final GRBVar[][] edges;

  private final GRBVar[] labels;

  TSPP(int[] coloredNodes, int[][] weightedEdges, int numberColors, int s, int t) throws GRBException {
    this.coloredNodes = coloredNodes;

    this.env = new GRBEnv(true);
    this.env.set("logFile", "TSPP.log");
    this.env.start();

    this.model = new GRBModel(env);

    // Create edges variables
    this.edges = new GRBVar[weightedEdges.length][weightedEdges[0].length];

    for (int i = 0; i < this.edges.length; i++)
      for (int j = 0; j < this.edges[i].length; j++)
        if (weightedEdges[i][j] > 0)
          this.edges[i][j] = this.model.addVar(0, 1, 0, GRB.BINARY, "edge[" + i + "," + j + "]");

    // Create nodes variables
    this.nodes = new GRBVar[this.coloredNodes.length];
    for (int i = 0; i < this.nodes.length; i++)
      this.nodes[i] = this.model.addVar(0, 1, 0, GRB.BINARY, "node[" + i + "]");

    // Add objective function
    GRBLinExpr objective = new GRBLinExpr();
    for (int i = 0; i < this.edges.length; i++)
      for (int j = 0; j < this.edges[i].length; j++)
        if (this.edges[i][j] != null)
          objective.addTerm(weightedEdges[i][j], this.edges[i][j]);

    this.model.setObjective(objective, GRB.MINIMIZE);

    // Choose at least one of each color constraint
    for (int c = 0; c < numberColors; c++) {
      GRBLinExpr colorConstraint = new GRBLinExpr();

      for (int i = 0; i < nodes.length; i++)
        if (this.coloredNodes[i] == c)
          colorConstraint.addTerm(1, this.nodes[i]);

      this.model.addConstr(colorConstraint, GRB.GREATER_EQUAL, 1, "color-" + c);
    }

    /**
     * All nodes need to have one edge leaving them with the exception of the t
     * node
     */
    for (int i = 0; i < nodes.length; i++) {
      GRBLinExpr leavingConstraing = new GRBLinExpr();
      leavingConstraing.addTerm(1, nodes[i]);

      for (int j = 0; j < edges[i].length; j++)
        if (edges[i][j] != null)
          leavingConstraing.addTerm(-1, edges[i][j]);

      if (i != t)
        this.model.addConstr(leavingConstraing, GRB.EQUAL, 0, "leaving-node-" + i);
      else
        this.model.addConstr(leavingConstraing, GRB.EQUAL, 1, "leaving-node-t");
    }

    /**
     * All nodes need to have one edge entering them with the exception of the s
     * node
     */
    for (int j = 0; j < nodes.length; j++) {
      GRBLinExpr enteringConstraint = new GRBLinExpr();
      enteringConstraint.addTerm(1, nodes[j]);

      for (int i = 0; i < edges.length; i++)
        if (edges[i][j] != null)
          enteringConstraint.addTerm(-1, edges[i][j]);

      if (j != s)
        this.model.addConstr(enteringConstraint, GRB.EQUAL, 0, "entering-node-" + j);
      else
        this.model.addConstr(enteringConstraint, GRB.EQUAL, 1, "entering-node-s");
    }

    // Labels for cicle prevention
    this.labels = new GRBVar[this.nodes.length];
    for (int i = 0; i < this.labels.length; i++)
      this.labels[i] = this.model.addVar(0, GRB.INFINITY, 0, GRB.INTEGER, "label[" + i + "]");

    int N = 1000000;

    for (int i = 0; i < this.edges.length; i++)
      for (int j = 0; j < this.edges[i].length; j++)
        if (this.edges[i][j] != null) {
          GRBLinExpr labelConstraint = new GRBLinExpr();

          // label[j] - label[i] >= edges[i][j] - (1 - edges[i][j])*N
          labelConstraint.addTerm(1, this.labels[j]);
          labelConstraint.addTerm(-1, labels[i]);

          labelConstraint.addTerm(-1, edges[i][j]);
          labelConstraint.addTerm(-N, edges[i][j]);

          this.model.addConstr(labelConstraint, GRB.GREATER_EQUAL, -N, "edge[" + i + "][" + j + "]-label");
        }

    this.model.optimize();
  }

  public void reportResults() throws GRBException {
    for (int i = 0; i < this.nodes.length; i++)
      System.out.println(nodes[i].get(GRB.StringAttr.VarName) + " " + nodes[i].get(GRB.DoubleAttr.X));

    System.out.println();

    for (int i = 0; i < this.edges.length; i++)
      for (int j = 0; j < this.edges[i].length; j++)
        if (edges[i][j] != null)
          System.out.println(edges[i][j].get(GRB.StringAttr.VarName) + " " + edges[i][j].get(GRB.DoubleAttr.X));

    System.out.println();

    for (int i = 0; i < this.labels.length; i++)
      System.out.println(this.labels[i].get(GRB.StringAttr.VarName) + " " + this.labels[i].get(GRB.DoubleAttr.X));

    System.out.println();

    System.out.println("Obj: " + this.model.get(GRB.DoubleAttr.ObjVal));
  }
}
