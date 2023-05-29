import gurobi.*;

public class TSPP {
  private final int[] coloredNodes;

  private final GRBEnv env;
  private final GRBModel model;
  private final GRBVar[] nodes;
  private final GRBVar[][] edges;

  TSPP(int[] coloredNodes, int[][] weightedEdges, int numberColors) throws GRBException {
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
  }
}
