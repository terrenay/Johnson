package johnson;

import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.lang.Math;

class Johnson {
	public static void main(String[] args) {

		// Uncomment the following two lines if you want to read from a file
		// System.In.open("public/test1.in");
		// Out.compareTo("public/test1.out");
		Scanner scanner = new Scanner(System.in);

		int n = scanner.nextInt(); // number of vertices
		int m = scanner.nextInt(); // number of edges
		// int start = scanner.nextInt();
		// int end = scanner.nextInt();

		// The following two arrays stores the information of edges
		int[][] edge_array = new int[m][3];

		// Read edges
		for (int i = 0; i < m; i++) {
			edge_array[i][0] = scanner.nextInt(); // one endpoint
			edge_array[i][1] = scanner.nextInt(); // the other endpoint
			edge_array[i][2] = scanner.nextInt(); // weight
		}

		Graph G = new Graph(n, m, edge_array);
		/*
		 * System.out.println("Dijkstra: Distanz zu " + end + " ist " + dijkstra(G,
		 * start, end)); Arrays.stream(bellmanFord(G, start)) .forEach(p ->
		 * System.out.println("BF: Distanz zu " + p.index + " ist " + p.value));
		 */
		Pair[][] result = G.johnson();
		for (int i = 0; i < result.length; i++) {
			System.out.println("From node " + i + ": ");
			for (int j = 0; j < result[i].length; j++) {
				System.out.println("to node " + j + ": " + (result[i][j] == null
						? "-" : result[i][j].value));
			}
			System.out.println();
		}

		// Uncomment the following line if you want to read from a file
		// In.close();
	}

}

class Graph {

	public int n; // number of vertices
	public int m; // number of edges
	public int[] degrees; // degrees[i]: the degree of vertex i
	public int[][] edges; // edges[i][j]: the endpoint of the j-th edge of vertex i
	public int[][] weights; // weights[i][j]: the weight of the j-th edge of vertex i

	Graph(int n, int m, int[][] edge_array) {
		this.n = n;
		this.m = m;
		degrees = new int[n];

		for (int i = 0; i < n; i++) {
			degrees[i] = 0;
		}

		for (int i = 0; i < m; i++) {
			degrees[edge_array[i][0]]++;
			// degrees[edge_array[i][1]]++;
		}

		edges = new int[n][];
		weights = new int[n][];

		for (int i = 0; i < n; i++) {
			if (degrees[i] != 0) {
				edges[i] = new int[degrees[i]];
				weights[i] = new int[degrees[i]];
				degrees[i] = 0;
			} else {
				edges[i] = null;
				weights[i] = null;
			}
		}

		for (int i = 0; i < m; i++) {
			edges[edge_array[i][0]][degrees[edge_array[i][0]]] = edge_array[i][1];
			// edges[edge_array[i][1]][degrees[edge_array[i][1]]] = edge_array[i][0];
			weights[edge_array[i][0]][degrees[edge_array[i][0]]] = edge_array[i][2];
			// weights[edge_array[i][1]][degrees[edge_array[i][1]]] = edge_array[i][2];
			degrees[edge_array[i][0]]++;
			// degrees[edge_array[i][1]]++;
		}
	}

	public int[][] deepCopy(int[][] arr) {
		int[][] output = new int[arr.length][];
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] == null)
				continue;
			output[i] = new int[arr[i].length];
			for (int j = 0; j < arr[i].length; j++)
				output[i][j] = arr[i][j];
		}
		return output;
	}

	public void reweight(int[][] newWeights, Pair[] heights) {
		for (int i = 0; i < newWeights.length; i++) {
			for (int j = 0; j < degrees[i]; j++) {
				newWeights[i][j] += heights[i].value - heights[edges[i][j]].value;
			}
		}
	}

	public Pair[][] johnson() {
		Pair[][] apspModified = new Pair[n][n];
		Pair[][] apspOutput = new Pair[n][n];
		Pair[] heights = bellmanFord();
		int[][] newWeights = deepCopy(weights);
		reweight(newWeights, heights);
		for (int i = 0; i < n; i++) {
			apspModified[i] = dijkstra(newWeights, i);
		}
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < degrees[i]; j++) {
				// Reverse reweighting
				int endIndex = edges[i][j];
				int val = heights[endIndex].value + 
						apspModified[i][endIndex].value - heights[i].value;
				apspOutput[i][endIndex] = new Pair(i, val);
			}
		}
		return apspOutput;
	}

	public Pair[] bellmanFord() { // Kürzester Weg von einem imaginären Startknoten mit Gewicht 0 zu allen anderen
		Pair[] pairs = new Pair[n];
		for (int i = 0; i < n; i++) {
			pairs[i] = new Pair(i, 0);
		}
		for (int k = 0; k < n - 1; k++) { // Für alle Knoten
			for (int i = 0; i < edges.length; i++) { // Startpunkt
				for (int j = 0; j < degrees[i]; j++) {
					int endPoint = edges[i][j];
					pairs[endPoint].value = Math.min(pairs[endPoint].value, pairs[i].value + weights[i][j]);
				}
			}
		}
		// Checking negative cycle
		for (int i = 0; i < edges.length; i++) {
			for (int j = 0; j < degrees[i]; j++) {
				int endPoint = edges[i][j];
				if (pairs[i].value + weights[i][j] < pairs[endPoint].value) {
					System.out.println("Negative cycle detected involving node " + endPoint);
					throw new AssertionError();
				}
			}
		}
		return pairs;
	}

	public Pair[] dijkstra(int[][] weights, int start) {
		PriorityQueue<Pair> heap = new PriorityQueue<Pair>();
		Pair startPair = new Pair(start, 0); // index, value (distance)
		Pair[] pairs = new Pair[n];
		boolean[] closed = new boolean[n];
		heap.add(startPair);
		for (int i = 0; i < n; i++) {
			if (i == start)
				pairs[i] = startPair;
			else
				pairs[i] = new Pair(i, Integer.MAX_VALUE);
		}
		while (!heap.isEmpty()) {
			Pair cur = heap.poll();
			for (int i = 0; i < degrees[cur.index]; i++) {
				int neighbourIndex = edges[cur.index][i];
				if (!closed[neighbourIndex]) {
					Pair neighbourPair = pairs[neighbourIndex];
					int newVal = cur.value + weights[cur.index][i];
					if (newVal < neighbourPair.value) {
						neighbourPair.value = cur.value + weights[cur.index][i];
						heap.add(neighbourPair);
					}
				}
			}
			closed[cur.index] = true;
		}
		return pairs;
	}
}

class Pair implements Comparable<Pair> {

	public int index;
	public int value;

	public Pair(int index, int value) {
		this.index = index;
		this.value = value;
	}

	@Override
	public int compareTo(Pair other) {
		if (this.value < other.value) {
			return -1;
		} else if (this.value == other.value) {
			return 0;
		} else {
			return 1;
		}
	}
}