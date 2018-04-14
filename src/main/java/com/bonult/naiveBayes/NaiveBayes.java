package com.bonult.naiveBayes;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by bonult on 2018/4/7.
 */
public class NaiveBayes implements Serializable {

	private static final long serialVersionUID = -1L;

	private Map<String,Integer> features;
	private Map<String,Integer> typeIndexMap;
	private Map<String,Integer> typeCountMap;
	private List<String> typesWithIndex;
	private Map<String,int[]> featureMap;
	private int trainNum;
	private int typeNum;

	public NaiveBayes(){

	}

	public NaiveBayes(int typeNum){
		this.typeNum = typeNum;
	}

	public void train(String featuresDataPath, String trainDataPath, String trainModelSavedPath){
		initFeatures(featuresDataPath);
		typeIndexMap = new HashMap<>();
		typesWithIndex = new ArrayList<>();
		buildFeatureMap(trainDataPath);
		saveTrainModel(trainModelSavedPath);
	}

	private void initFeatures(String featuresDataPath){
		features = new HashMap<>();
		try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(featuresDataPath)))){
			String line = null;
			while((line = br.readLine()) != null){
				Integer count = features.computeIfAbsent(line, a -> 0);
				features.put(line, count + 1);
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	private void buildFeatureMap(String trainDataPath){
		featureMap = new HashMap<>();
		typeCountMap = new HashMap<>();
		try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(trainDataPath)))){
			String line = null;
			Set<String> words = new HashSet<>();
			trainNum = 0;
			while((line = br.readLine()) != null){
				trainNum++;
				String[] tmp = line.split(" ");
				Integer index = typeIndexMap.get(tmp[0]);
				if(index == null){
					index = typesWithIndex.size();
					typeIndexMap.put(tmp[0], index);
					typesWithIndex.add(tmp[0]);
					typeCountMap.put(tmp[0], 0);
				}
				typeCountMap.put(tmp[0], typeCountMap.get(tmp[0]) + 1);
				int i = index;
				for(int j = 1; j < tmp.length; j++){
					String word = tmp[j];
					if(features.containsKey(word)){
						if(words.contains(word)){
							continue;
						}
						words.add(tmp[j]);
						int[] count = featureMap.computeIfAbsent(tmp[j], k -> new int[typeNum]);
						count[i]++;
					}
				}
				words.clear();
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	private void saveTrainModel(String trainModelSavedPath){
		try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(trainModelSavedPath))){
			oos.writeObject(this);
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	public void test(String trainModelSavedPath, String testDataPath){
		try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(trainModelSavedPath)); BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(testDataPath)))){
			NaiveBayes nb = (NaiveBayes)ois.readObject();
			features = nb.features;
			typeIndexMap = nb.typeIndexMap;
			typeCountMap = nb.typeCountMap;
			typesWithIndex = nb.typesWithIndex;
			featureMap = nb.featureMap;
			trainNum = nb.trainNum;
			typeNum = nb.typeNum;

			String line = null;
			double[] p = new double[typesWithIndex.size()];
			int[] pc = new int[typesWithIndex.size()];
			Set<String> wordSet = new HashSet<>();
			while((line = br.readLine()) != null){
				String[] tmp = line.split(" ");
				for(String word : tmp){
					if(features.containsKey(word)){
						wordSet.add(word);
					}
				}
				for(int i = 0; i < p.length; i++){
					int typeCount = typeCountMap.get(typesWithIndex.get(i));
					p[i] = typeCount * 1.0 / trainNum;
					typeCount += wordSet.size();
					for(String word : wordSet){
						int[] count = featureMap.get(word);
						if(count == null){
							p[i] *= 1 / typeCount;
						}else{
							p[i] *= (count[i] + 1.0) / typeCount;
						}
					}
				}
				wordSet.clear();
				double max = -1;
				int maxIndex = -1;
				for(int i = 0; i < p.length; i++){
					if(p[i] > max){
						max = p[i];
						maxIndex = i;
					}
				}
				System.out.println(typesWithIndex.get(maxIndex) + " | " + line);
				pc[maxIndex]++;
			}
			System.out.println(typesWithIndex);
			System.out.println(Arrays.toString(pc));
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException{
		String path = NaiveBayes.class.getClassLoader().getResource("").getPath();
		NaiveBayes nb = new NaiveBayes(2);
		nb.train(path + "情感词汇.txt", path + "训练.txt", path + "train_model");
		nb.test(path + "train_model", path + "分词.txt");
	}
}
