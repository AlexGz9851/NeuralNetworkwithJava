import java.util.ArrayList;
import java.util.Random;

public class Network {
	
//	private Layer[] layers;
	private double[][][] weights;
	private double[][] biases;
	private int size;
	
	
	public Network(Integer[] sizes) {
		// TODO: try to change the code to a layer class
//		this.layers = new Layer[sizes.length];
//		for(int i =0; i<sizes.length; i++)
//		{
//			layers[i] = new Layer(sizes[i]);
//		}
		
		
		// Initializing biases
		// Creates an array that stores the biases (an array of doubles)
		// for each layer of the network starting in the second one
		
		this.size=sizes.length;
		
		this.biases = new double[this.size-1][];
		for(int i = 0; i < this.size-1; i++) {
			this.biases[i] = new double[sizes[i+1]];
			for(int j = 0; j < sizes[i+1]; j++) {
				this.biases[i][j]=Math.random();
			}
		}
		
		
		// i: no. connection between layer
		// j: list of weights corresponding the jth neuron
		// k: individual weight
		this.weights = new double[this.size-1][][];
		for(int i = 0; i < this.weights.length; i++) {
			this.weights[i]=new double[sizes[i+1]][];
			for(int j = 0; j < sizes[i+1]; j++) {
				this.weights[i][j] = new double[sizes[i]];
				for(int k = 0; k < sizes[i]; k++) {
					this.weights[i][j][k]=Math.random();
				}
			}
		}
	}
	
	private double[] feedforward(double[] activation) {
		for(int i = 0; i<this.size-1; i++) {
			activation = Utils.sigmoid(Utils.sumArray(this.biases[i],Utils.dotProduct(activation, weights[i])));
		}
		return activation;
	}
	
	private void SGD(double[][][] trainingData, int batchSize, double etha) {
		Utils.randomShuffle(trainingData);
		double[][][][] batches = new double[trainingData.length/batchSize][][][];
		for(int j = 0; j<batches.length; j+=batchSize) {
			batches[j] = new double[batchSize][][];
			for(int k = 0; k<batchSize; k++) {
				batches[j][k]=trainingData[k+j*batchSize];
			}
		}
		for(double[][][] batch: batches) {
			updateBatch(batch, etha);
		}
	}
	
	public void SGD(double[][][] trainingData, int epochs, int batchSize, double etha) {
		for(int i=0; i<epochs; i++) {
			SGD(trainingData, batchSize, etha);
			System.out.println(String.format("Epoch %i completed", i));
		}
	}
	
	public void SGD(double[][][] trainingData, int epochs, int batchSize, double etha, double[][][] testData) {
		for(int i=0; i<epochs; i++) {
			SGD(trainingData, batchSize, etha);
			System.out.println(String.format("Epoch %i: %i / %i", i, evaluateTest(testData), testData.length));
		}
	}
	
	private void updateBatch(double[][][] batch, double etha) {
		// TODO
	}
	
	private void backProp(double[] x, double[] y) {
		ArrayList<double[]> activations = new ArrayList<>();
		ArrayList<double[]> zs = new ArrayList<>();
		double[] activation = x;
		
		activations.add(x);
		// TODO
		
	}
	
	private int evaluateTest(double[][][] testData) {
		int cont = 0;
		for(int i = 0; i < testData.length; i++) {
			if((int)testData[i][1][0]==Utils.maxPos(this.feedforward(testData[i][0]))){
				cont++;
			}
		}
		return cont;
	}
	
	public int evaluate(double[] data) {
		return Utils.maxPos(this.feedforward(data));
	}
	
	public static void main(String... args) {
		Network net = new Network(new Integer[]{2,3,10});
		double[] a = new double[] {.5,.4};
		a = net.feedforward(a);
		System.out.println("a");
	}
	
	private static class Utils {
		public static double[] sigmoid(double[] z) {
			double answer[] = new double[z.length];
			for(int i =0; i<z.length; i++) {
				answer[i] = sigmoid(z[i]);
			}
			return answer;
		}
		
		public static double[] sigmoidPrime(double[] z) {
			double answer[] = new double[z.length];
			for(int i =0; i<z.length; i++) {
				answer[i] = sigmoidPrime(z[i]);
			}
			return answer;
		}
		
		private static double sigmoid(double z) {
			return 1.0/(1.0+Math.exp(-z));
		}
		
		private static double sigmoidPrime(double z) {
			return sigmoid(z)*(1-sigmoid(z));
		}
		
		public static double dotProduct(double[] a, double[] b) throws IllegalArgumentException {
			if(a.length!=b.length)
				throw new IllegalArgumentException("Please provide arrays of the same size");
			double sum = 0;
			for(int i = 0; i<a.length; i++) {
				sum+=(a[i]*b[i]);
			}
			return sum;
		}
		
		public static double[] dotProduct(double[] a, double[][] b) throws IllegalArgumentException{
			double[] data = new double[b.length];
			for(int i = 0; i<b.length; i++) {
				data[i]=dotProduct(a,b[i]);
			}
			return data;
		}
		
		public static double[] sumArray(double[] a, double b) {
			for(int i = 0; i<a.length; i++) {
				a[i]+=b;
			}
			return a;
		}

		public static double[] sumArray(double[] a, double[] b) throws IllegalArgumentException {
			if(a.length!=b.length)
				throw new IllegalArgumentException("Please provide arrays of the same size");
			for(int i = 0; i<a.length; i++) {
				a[i]+=b[i];
			}
			return a;
		}

		public static int maxPos(double[] data) {
			int max = 0;
			for(int i = 1; i < data.length; i++) {
				if(data[i]>data[max])
					max = i;
			}
			return max;
		}
		
		// Fisher–Yates shuffle
		public static <E> void randomShuffle(E[] arr) {
		    Random rnd = new Random();
		    for (int i = arr.length - 1; i > 0; i--)
		    {
		      int index = rnd.nextInt(i + 1);
		      // Simple swap
		      E temp = arr[index];
		      arr[index] = arr[i];
		      arr[i] = temp;
		    }
		}
		
	}
}
