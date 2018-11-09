import java.util.ArrayList;
import java.util.Random;


public class Network {
	
//	private Layer[] layers;
	private double[][][] weights;
	private double[][] biases;
	private int size;
	
	
	public Network(Integer[] sizes) {
		
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
		double[][][][] batches = new double[trainingData.length/batchSize][batchSize][][];
		for(int j = 0; j<batches.length; j++) {
			for(int k = 0; k<batchSize; k++) {
				batches[j][k]=trainingData[k+j*batchSize];
			}
		}
		for(double[][][] batch: batches) {
			updateMiniBatch(batch, etha);
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
			System.out.println(String.format("Epoch %d: %d / %d", i, evaluateTest(testData), testData.length));
		}
	}
	
	private void updateMiniBatch(double[][][] miniBatch, double etha) {
		double coef=etha/(double)(miniBatch.length);
		double[][][] nablaW;
		double[][] nablaB;
		
		nablaW = Utils.zeros3D(this.weights); 
		nablaB = Utils.zeros2D(this.biases);
		
		for (int i=0;i<miniBatch.length;i++) {
			backProp(miniBatch[i][0], miniBatch[i][1],nablaW,nablaB);
		}
		
		
		for(int i = 0; i < this.weights.length; i++) {
			for(int j = 0; j < this.weights[i].length; j++) {
				this.biases[i][j]-=coef*nablaB[i][j];
				for(int k = 0; k < this.weights[i][j].length; k++) {
					this.weights[i][j][k]-=coef *nablaW[i][j][k];
				}
			}
		}
	}

	private void backProp(double[] x, double[] y, double[][][] nablaW, double[][] nablaB) {
		double[] yTransf = new double[10];
		yTransf=Utils.transformToArrIntY(y);
		
		double[][][] deltaNablaW;
		double[][] deltaNablaB, wTrans;
		//se reemplazo deltaW y deltaB de este metodo por deltaNablaW deltaNablaB, para poder pasar por referencia nablaW y nablaB de update_mini_batch
		deltaNablaW = Utils.zeros3D(this.weights); 
		deltaNablaB = Utils.zeros2D(this.biases);
		
		ArrayList<double[]> activations = new ArrayList<>();
		ArrayList<double[]> zs = new ArrayList<>();
		double[] z,
				 delta,
				 sp,
				 activation;
		
		activation=x;
		activations.add(activation);
		
		for(int i = 0; i < this.weights.length; i++) {
			z=Utils.sumArray(Utils.dotProduct(activation,this.weights[i]),this.biases[i]);
			zs.add(z);
			activation=Utils.sigmoid(z);
			activations.add(activation);
		}
		delta=Utils.elementWiseMultipArr(
				costDerivative(activations.get(activations.size()-1), yTransf),
				Utils.sigmoidPrime(zs.get(zs.size()-1)));
		deltaNablaB[deltaNablaB.length-1] = delta;
		deltaNablaW[deltaNablaW.length-1] = Utils.vectorMultTo2D(delta,activations.get(activations.size()-2));
		
		for(int ll =2;ll<this.size;ll++) {
			z = zs.get(zs.size()-ll);
			sp = Utils.sigmoidPrime(z);
			wTrans=Utils.trasponerMatrix(this.weights[this.weights.length+1-ll]);
			delta = Utils.elementWiseMultipArr(Utils.dotProduct(delta, wTrans), sp);
			deltaNablaB[deltaNablaB.length-ll] = delta;
			deltaNablaW[deltaNablaW.length-ll] = Utils.vectorMultTo2D(delta, activations.get(activations.size()-ll-1));
			//en vez de retornar nablaB y nablaW aqui, los modifico en este metodo.
			//el procesamiento es secuencial.
			// from update_mini_batch
			//	        delta_nabla_b, delta_nabla_w = self.backprop(x, y)
			//	        nabla_b = [nb+dnb for nb, dnb in zip(nabla_b, delta_nabla_b)]
			//	        nabla_w = [nw+dnw for nw, dnw in zip(nabla_w, delta_nabla_w)]
			
			for(int i=0;i<nablaB.length;i++) {
				nablaB[i] = Utils.sumArray(nablaB[i], deltaNablaB[i]);
			}
			for(int i=0;i<nablaW.length;i++) {
				for(int j=0;j<nablaW[i].length;j++) {
					nablaW[i][j]=Utils.sumArray(nablaW[i][j], deltaNablaW[i][j]);
				}
			}
			
			
		}
	}
	private double [] costDerivative(double[] outputActivations,double[] y) {
		return Utils.substractArray(outputActivations, y);
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
		Network net = new Network(new Integer[]{784,100,10});
		double[] labels = MnistReader.getLabels("train-labels.idx1-ubyte");
		double[][] images = MnistReader.getDoubleImages("train-images.idx3-ubyte");
		double[][][] trainingData = new double[labels.length][2][];
		for(int i = 0; i<labels.length; i++) {
			trainingData[i][0]=images[i];
			trainingData[i][1]= new double[1];
			trainingData[i][1][0] = labels[i];			
		}
		labels = MnistReader.getLabels("t10k-labels.idx1-ubyte");
		images = MnistReader.getDoubleImages("t10k-images.idx3-ubyte");
		double[][][] testData = new double[labels.length][2][];
		for(int i = 0; i<labels.length; i++) {
			testData[i][0]=images[i];
			testData[i][1]= new double[1];
			testData[i][1][0] = labels[i];			
		}
		net.SGD(trainingData, 30, 10, 100, testData);
	}
	
	private static class Utils {
		public static double[] transformToArrIntY(double[] y) {
			//This is a kind of strange function at first sight. 
			//why transform an array of doubles into an array of doubles?
			//because this is not a real array, es an integer casted to a double inside an array.
			// that means, in reality, this "double array" is an integer.
			// We'll tansform this integer into a array of doubles representation :for instance
			//from 5.0 to --> [0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0]
			int yInt=(int)y[0];
			double[] res= new double[10];
			for(int i=0;i<res.length;i++) {
				if(i==yInt) {
					res[i] = 1.0;
				}else {
					res[i] = 0.0;
				}
			}
			return res;
		}
		
		public static double[] sigmoid(double[] z) {
			double answer[] = new double[z.length];
			for(int i =0; i<z.length; i++) {
				answer[i] = sigmoid(z[i]);
			}
			return answer;
		}
		public static double [][] vectorMultTo2D(double[] a, double[] b) {
			double[][] c;
			c= new double[a.length][];
			for(int i=0;i<a.length;i++) {
				c[i] = new double[b.length];
				for(int j=0;j<b.length;j++) {
					c[i][j] = a[i]*b[j];
				}
			}
			return c;
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
		public static double[][] trasponerMatrix(double[][] matrix){
			double[][] res;
			res= new double[matrix[0].length][];
			for(int j=0;j<matrix[0].length;j++) {
				res[j] = new double[matrix.length];
				for(int i=0;i<matrix.length;i++) {
					res[j][i] = matrix[i][j];
				}
			}
			return res;
		}
		public static double[] multiplyConstant2arr(double[] a, double b) {
			for(int i = 0; i<a.length; i++) {
				a[i]*=b;
			}
			return a;
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
		public static double[] substractArray(double[] a, double[] b) throws IllegalArgumentException {
			if(a.length!=b.length)
				throw new IllegalArgumentException("Please provide arrays of the same size");
			for(int i = 0; i<a.length; i++) {
				a[i]-=b[i];
			}
			return a;
		}
		public static double[] elementWiseMultipArr(double[] a, double[] b) throws IllegalArgumentException {
			if(a.length!=b.length)
				throw new IllegalArgumentException("Please provide arrays of the same size");
			for(int i = 0; i<a.length; i++) {
				a[i]*=b[i];
			}
			return a;
		}
		public static double[][][] zeros3D(double[][][] size){
			double[][][] res = new double[size.length][][];
			for(int i = 0; i < size.length; i++) {
				res[i]= new double[size[i].length][];
				for(int j = 0; j<size[i].length; j++) {
					res[i][j]= new double[size[i][j].length];
				}
			}
			return res;
		}
		
		public static double[][] zeros2D(double[][] size){
			double[][] res = new double[size.length][];
			for(int i = 0; i < size.length; i++) {
				res[i]= new double[size[i].length];
			}
			return res;
		}
		
		public static double [] zeros1D(int iMax) {
			double[] res= new double[iMax];
			for(int i = 0; i < iMax; i++) {
				res[i]=0;
			}
			return res;
		}
		public static int maxPos(double[] data) {
			int max = 0;
			for(int i = 1; i < data.length; i++) {
				if(data[i]>data[max])
					max = i;
			}
			return max;
		}
		
		// Fisher�Yates shuffle
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
