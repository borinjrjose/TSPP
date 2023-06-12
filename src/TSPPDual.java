public class TSPPDual {
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

  TSPPDual(int[] coloredNodes, int[][] weightedEdges, int numberColors, int s, int t) {
    // Numbered edges
    int edgeNumber = -1;
    int[][] numberedEdges = new int[weightedEdges.length][weightedEdges[0].length];
    for (int i = 0; i < weightedEdges.length; i++)
      for (int j = 0; j < weightedEdges[i].length; j++)
        numberedEdges[i][j] = weightedEdges[i][j] > 0 ? ++edgeNumber : -1;

    // Normal problem variables
    this.initialEdgesPosition = 0;
    this.initialNodesPosition = 0;
    for (int i = 0; i < weightedEdges.length; i++)
      for (int j = 0; j < weightedEdges[i].length; j++)
        if (weightedEdges[i][j] > 0)
          this.initialNodesPosition++;

    this.initialLabelPosition = this.initialNodesPosition + coloredNodes.length;

    // Normal Problem constraints
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

    for (int i = 0; i < this.A.length; i++) {
      System.out.println();
      for (int j = 0; j < this.A[i].length; j++)
        System.out.print(this.A[i][j] + " ");
    }
  }
}
