import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class App {
	public static void main(String[] args) throws Exception {
		Scanner scanner = null;

		try {
			File file = new File(args[0]);
			scanner = new Scanner(file);

			// Read colored nodes
			if (!scanner.hasNextLine())
				throw new Error("No nodes informed");

			String[] nodes = scanner.nextLine().split(" ");
			int[] coloredNodes = new int[nodes.length];
			for (int i = 0; i < nodes.length; i++)
				coloredNodes[i] = Integer.parseInt(nodes[i]);

			int nColors = App.colorsLength(coloredNodes);

			// Read weighted edges
			if (!scanner.hasNextLine())
				throw new Error("No edges informed");

			String[] edges = scanner.nextLine().split(";");

			int[][] edgesMatrix = new int[coloredNodes.length][coloredNodes.length];
			for (int i = 0; i < edgesMatrix.length; i++)
				for (int j = 0; j < edgesMatrix[i].length; j++)
					edgesMatrix[i][j] = -1;

			Pattern regex = Pattern.compile("^\\((\\d+),(\\d+),(\\d+)\\)$");
			for (int i = 0; i < edges.length; i++) {
				Matcher m = regex.matcher(edges[i]);
				if (!m.matches())
					throw new Error("Arestas não seguem o padrão (nó1,nó2,peso);...;(nó1,nó2,peso)");

				edgesMatrix[Integer.parseInt(m.group(1))][Integer.parseInt(m.group(2))] = Integer.parseInt(m.group(3));
				edgesMatrix[Integer.parseInt(m.group(2))][Integer.parseInt(m.group(1))] = Integer.parseInt(m.group(3));
			}

			// Read s and t
			if (!scanner.hasNextLine())
				throw new Error("No s t vertices informed");

			String[] st = scanner.nextLine().split(" ");

			int s = Integer.parseInt(st[0]);
			int t = Integer.parseInt(st[1]);

			ITSPP tspp = !args[1].equals("dual")
					? new TSPP(coloredNodes, edgesMatrix, nColors, s, t, !args[2].equals("relaxed"))
					: new TSPPDual(coloredNodes, edgesMatrix, nColors, s, t, !args[2].equals("relaxed"));
			tspp.reportResults();
		} catch (FileNotFoundException e) {
			System.out.println("An error occured.");
			e.printStackTrace();
		} finally {
			if (scanner != null)
				scanner.close();
		}
	}

	public static int colorsLength(int[] coloredNodes) {
		Set<Integer> colors = new HashSet<Integer>();

		for (int color : coloredNodes)
			colors.add(color);

		return colors.size();
	}
}
