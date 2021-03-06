// Luis Iv�n Morett Ar�valo		   A01634417
// Jes�s Alejandro Gonz�lez S�nchez A00820225 
// Network
// Profesor: Gerardo Salinas

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.imageio.ImageIO;


public class Network {
	
	private double[][][] weights;
	private double[][] biases;
	private int size;
	private int[] netShape;
	private static Network network=null;
	private boolean isBusy;
	
	/**
	 * Private constructor to preserve the singleton principle
	 * 
	 */
	private Network() {
	}
	
	/**
	 * This method always returns the unique instance of the network
	 * @return the network
	 */
	public static Network getInstance() 
    { 
        if (network == null) {
        	network = new Network(); 
        	String fileName = "cnn02.json";
    		network.start( 1, 10, .2, new int[] {784,100,10}, fileName);
    		network.saveNetwork(fileName);
        }
        return network; 
    } 
	
	/**
	 * This method creates the Network based on the layer sizes	
	 * @param sizes the number of layers and number of neurons in  each layer
	 */
	private void createNetwork(int[] sizes) {
		this.size = sizes.length;
		this.netShape=sizes;
		// Initializing biases
		// Creates an array that stores the biases (an array of doubles)
		// for each layer of the network starting in the second one
		
		this.size=sizes.length;
		
		this.biases = new double[this.size-1][];
		for(int i = 0; i < this.size-1; i++) {
			this.biases[i] = new double[sizes[i+1]];
			for(int j = 0; j < sizes[i+1]; j++) {
				this.biases[i][j]=Math.random()*2-1;
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
					this.weights[i][j][k]=Math.random()*2-1;
				}
			}
		}
		this.isBusy=false;
	}

	/**
	 * Return the output of the network
	 * @param activation the input of the layer
	 * @return the output
	 */
	private double[] feedforward(double[] activation) {
		for(int i = 0; i<this.size-1; i++) {
			activation = Utils.sigmoid(Utils.sumArray(Utils.dotProduct(activation, weights[i]),this.biases[i]));
		}
		return activation;
	}
	
	/**
	 * Stochastic Gradient Descent function that trains the network
	 * @param trainingData the data used to train the network
	 * @param batchSize the size of the mini batch used to train the network
	 * @param etha the learning rate of the network
	 */
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
	
	/**
	 * Stochastic Gradient Descent function that trains the network
	 * @param trainingData the data used to train the network
	 * @param epochs the number of times that the network is trained with the same data
	 * @param batchSize the size of the mini batch used to train the network
	 * @param etha the learning rate of the network
	 */
	private void SGD(double[][][] trainingData, int epochs, int batchSize, double etha) {
		for(int i=0; i<epochs; i++) {
			SGD(trainingData, batchSize, etha);
			System.out.println(String.format("Epoch %i completed", i));
		}
	}
	
	/**
	 * Stochastic Gradient Descent function that trains the network
	 * @param trainingData the data used to train the network
	 * @param epochs the number of times that the network is trained with the same data
	 * @param batchSize the size of the mini batch used to train the network
	 * @param etha the learning rate of the network
	 * @param testData the data used to test the data in each epoch
	 */
	private void SGD(double[][][] trainingData, int epochs, int batchSize, double etha, double[][][] testData) {
		for(int i=0; i<epochs; i++) {
			SGD(trainingData, batchSize, etha);
			System.out.println(String.format("Epoch %d: %d / %d", i, evaluateTest(testData), testData.length));
		}
	}
	
	/**
	 * Updates the weights and biases based on the values changed in the backprop function
	 * @param miniBatch the minibatch used to train the network
	 * @param etha the learning rate of the network
	 */
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

	/**
	 * Function used to calculate nablaW and nablaB
	 * @param x input to the network
	 * @param y expected value
	 * @param nablaW the rate of change of the weight, this is expected to be an empty multidimensional array to be changed by reference
	 * @param nablaB the rate of change of the biases, this is expected to be an empty multidimensional array to be changed by reference
	 */
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
	
	/**
	 * Calculates the partial cost derivative 
	 * @param outputActivations the output of the layer
	 * @param y the desired output
	 * @return the cost derivative vector
	 */
	private double [] costDerivative(double[] outputActivations,double[] y) {
		return Utils.substractArray(outputActivations, y);
	}
	
	private int evaluateTest(double[][][] testData) {
		int cont = 0;
		for(int i = 0; i < testData.length; i++) {
			if((int)testData[i][1][0]==evaluate(testData[i][0])){
				cont++;
			}
		}
		return cont;
	}
	
	/**
	 * evaluates the input
	 * @param data input
	 * @return the answer from the network
	 */
	public int evaluate(double[] data) {
		return Utils.maxPos(this.feedforward(data));
	}
	
	/**
	 * Saves the network in an external file
	 * @param nameFile
	 */
	public void saveNetwork(String nameFile) {
		saveNetwork(nameFile,"");
	}
	
	/**
	 * Saves the network in an external file
	 * @param nameFile
	 * @param path
	 */
	public void saveNetwork(String nameFile,String path) {
		this.isBusy=true;
		JsonObject netJson = createJsonNet();
		try {
			FileWriter fr;
			if(path.equals(""))
				fr = new FileWriter(nameFile);
			else
				fr = new FileWriter(path+"\\"+nameFile);
			PrintWriter pw= new PrintWriter (fr);
			pw.print(netJson);
			pw.close();
			fr.close();
		}catch(IOException e){
			System.out.println("Algo paso. No se pudo guardar la red neuronal entrenada.");
		}
		this.isBusy=false;
	}
	
	/**
	 * Starts the network and trains it based in the params
	 * @param epochs the number of times that the network is trained with the same data
	 * @param batchSize the size of the mini batch used to train the network
	 * @param etha the learning rate of the network
	 * @param altShape the number of layers (implicit as the size of the array) and number of neurons in  each layer
	 */
	public void start( int epochs, int batchSize, double etha,  int[] altShape ) {
		this.createNetwork(altShape);
		this.isBusy=true;
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
		
		this.SGD(trainingData, epochs, batchSize, etha, testData);
		this.isBusy=false;
	}
	
	/**
	 * Starts the network in case it didn't find the desired file it starts to train it with the rest of the params
	 * @param epochs the number of times that the network is trained with the same data
	 * @param batchSize the size of the mini batch used to train the network
	 * @param etha the learning rate of the network
	 * @param altShape the number of layers (implicit as the size of the array) and number of neurons in  each layer
	 * @param path where the saved network file is supposed to be
	 */
	public void start( int epochs, int batchSize, double etha,int[] altShape ,String path) {
		String line;
		this.isBusy=true;
		
		try {
			FileReader fr = new FileReader(path);
			BufferedReader br = new BufferedReader(fr);
			line=br.readLine();
			br.close();
			fr.close();
			
			JsonParser jsPar = new JsonParser();
			JsonObject jsNet= (JsonObject) jsPar.parse(line);
			JsonArray jsShape=(JsonArray)jsNet.get("shape");
			int[] shape= new int[jsShape.size()];
			for(int i=0;i<jsShape.size();i++) {
				shape[i] = jsShape.get(i).getAsInt();
			}
			this.createNetwork(shape);
			JsonArray jsWeights= (JsonArray) jsNet.get("weights");
			JsonArray jsBiases=  (JsonArray) jsNet.get("biases");
			this.fillB(jsBiases);
			this.fillW(jsWeights);
			System.out.println("Vaya, se encontro el archivo con la red entrenada.");
			
		}catch(FileNotFoundException e) {
			System.out.println("No se encontro el archivo. Se procedera a entrenar a la red.");
			this.start(  epochs,  batchSize,  etha,  altShape);
			this.saveNetwork("cnn.json");
		}catch (Exception e) {
			System.out.println("Hay un problema con el archivo que contiene la red entrenada. Se procedera a entrenar a la red.");
			this.start(  epochs,  batchSize,  etha,  altShape);	
			this.saveNetwork("cnn.json");
		}
		this.isBusy=false;
	}
	
	/**
	 * fills the weights with the JSONArray
	 * @param jsArr the JSONArray
	 */
	private void fillW( JsonArray jsArr) {
		JsonArray jsW2D, jsW1D;
		for(int i=0;i<jsArr.size();i++) {
			jsW2D= (JsonArray) jsArr.get(i);
			for(int j=0;j<jsW2D.size();j++) {
				jsW1D= (JsonArray) jsW2D.get(j);
				for(int k=0;k<jsW1D.size();k++) {
					this.weights[i][j][k] = jsW1D.get(k).getAsDouble();
				}
			}
		}
	}
	
	/**
	 * fills the biases with the JSONArray
	 * @param jsArr the JSONArray
	 */
	private void fillB( JsonArray jsArr) {
		JsonArray jsW1D;
		for(int i=0;i<jsArr.size();i++) {
			jsW1D= (JsonArray) jsArr.get(i);
			for(int j=0;j<jsW1D.size();j++) {
				this.biases[i][j] = jsW1D.get(j).getAsDouble();
			}
		}
	}
	
	/**
	 * Creates a JSONObject based in the data in the network
	 * @return
	 */
 	private JsonObject createJsonNet() {
		JsonObject jsonObj = new JsonObject();
		JsonArray wJson,bJson, wJson2D,
				  wJson3D,bJson2D, netShapeJson;
		netShapeJson= new JsonArray();
		wJson= new JsonArray();
		bJson= new JsonArray();
		
		for(int i = 0; i < this.netShape.length; i++) {
			netShapeJson.add(netShape[i]);
		}
		for (int i = 0; i < this.weights.length; i++) {
			wJson2D = new JsonArray();
			for(int j=0;j<this.weights[i].length;j++) {
				wJson3D = new JsonArray();
				for(int k=0;k<this.weights[i][j].length;k++) {
					wJson3D.add(this.weights[i][j][k]);
				}
				wJson2D.add(wJson3D);
			}
			wJson.add(wJson2D);
		}
		
		for (int i = 0; i < this.biases.length; i++) {
			bJson2D = new JsonArray();
			for(int j=0;j<this.biases[i].length;j++) {
				bJson2D.add(this.biases[i][j]);
			}
			bJson.add(bJson2D);
		}
		//Add someMoreInformationAboutNet
		jsonObj.add("shape", netShapeJson);
		jsonObj.add("weights", wJson);
		jsonObj.add("biases", bJson);
		
		return jsonObj;
	}
 	
 	/**
 	 * Loads an image to test the network
 	 * @param file the image path
 	 * @return the output from the network
 	 */
 	public int loadImage(String file) {
 		int pixel;
 		this.isBusy=true;
		try {//LOAD EXAMPLE.
			java.io.File imgFile = new java.io.File(file);
			BufferedImage image = ImageIO.read(imgFile);
			
			double[] entrada = new double[image.getHeight()*image.getWidth()];
			for(int y=0;y<image.getHeight();y++) {
				for(int x=0;x<image.getWidth();x++){
						pixel=255-(image.getRGB(x, y)& 0x000000FF);
						entrada[y*image.getWidth()+x] = (double) pixel/255;
				}
			}
			
			return this.evaluate(entrada);
		}catch(NullPointerException e) {
			System.out.println("No se encotro la imagen 4");
		}catch(IOException e) {
			e.printStackTrace();
		}catch(IllegalArgumentException e) {
			System.out.println("ALGO ES NULO");
		}
		this.isBusy=false;
		return -1;
 	}
	
	public boolean isBusy() {
		return isBusy;
	}
	
	public void setBusy(boolean isBusy) {
		this.isBusy = isBusy;
	}
	
	public static void main(String... args) {
		Network net= Network.getInstance();
		System.out.println(net.loadImage("testImages\\000.png"));
		System.out.println(net.loadImage("testImages\\444.png"));
		System.out.println(net.loadImage("testImages\\222.png"));
		System.out.println(net.loadImage("testImages\\333.png"));
		System.out.println(net.loadImage("testImages\\666.png"));
		System.out.println(net.loadImage("testImages\\888.png"));
		System.out.println(net.loadImage("testImages\\999.png"));
		
	}
	
	/**
	 * Auxiliary class used by the network to calculate the vector operations
	 * @author Luis Ivan Morett Arevalo & Jesus Alejandro Gonzalez Sanchez
	 *
	 */
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
			double[] sum = new double[a.length];
			for(int i = 0; i<a.length; i++) {
				sum[i]=a[i]+b[i];
			}
			return sum;
		}
		public static double[] substractArray(double[] a, double[] b) throws IllegalArgumentException {
			if(a.length!=b.length)
				throw new IllegalArgumentException("Please provide arrays of the same size");
			double[] sub = new double[a.length];
			for(int i = 0; i<a.length; i++) {
				sub[i]=a[i]-b[i];
			}
			return sub;
		}
		public static double[] elementWiseMultipArr(double[] a, double[] b) throws IllegalArgumentException {
			if(a.length!=b.length)
				throw new IllegalArgumentException("Please provide arrays of the same size");
			double[] mult = new double[a.length];
			for(int i = 0; i<a.length; i++) {
				mult[i]=a[i]*b[i];
			}
			return mult;
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
