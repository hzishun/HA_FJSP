package com.zll.FJSP.Data;

/**
* Description:读取算例数据
* @author zll-hust E-mail:zh20010728@126.com
* @date 创建时间：2020年5月28日 下午2:18:10
*/
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Input {
	private File file;

	public Input(File file) {
		this.file = file;
	}

	/**
	 * @return the problem description which has been arranged
	 */
	public Problem getProblemDesFromFile() {
		Problem input = new Problem();
		BufferedReader reader = getBufferedReader(file);
		String prodesStrArr[] = null;
		int proDesMatrix[][] = null;
		String proDesString;
		int[] operationCountArr = null;
		int[] machineCountArr = null;
		List<Integer> operationCountList = new ArrayList<Integer>();// 存储每个工序的备选机器数目

		try {
			proDesString = reader.readLine();
			String proDesArr[] = proDesString.split("\\s+");
			int jobNum = Integer.valueOf(proDesArr[0]);// 工件数
			int machineNum = Integer.valueOf(proDesArr[1]);// 机器数

			operationCountArr = new int[jobNum];
			input.setJobCount(jobNum);
			input.setMachineCount(machineNum);

			prodesStrArr = new String[jobNum];
			int count = 0;// Calculate how many orders in the problem 总工序数
			int index = 0;// store the index of first blank 标记第一个空格位置
			int maxOperationCount = 0, tempCount = 0;
			// find the max operation count of the job arrays 找出工序最多者
			for (int i = 0; i < jobNum; i++) {
				prodesStrArr[i] = reader.readLine().trim();
				index = prodesStrArr[i].indexOf(' ');
				tempCount = Integer.valueOf(prodesStrArr[i].substring(0, index));//每个工件的工序数
				count += tempCount;
				if (maxOperationCount < tempCount)
					maxOperationCount = tempCount;
			}

			int[][] operationToIndex = new int[jobNum][maxOperationCount];// 用来存储i工件j工序所对应的problemDesMatrix[][]的index
			input.setMaxOperationCount(maxOperationCount);
			proDesMatrix = new int[count][];
			String opeationDesArr[];
			int operationCount = 0;
			int operationTotalIndex = 0;
			int selectedMachineCount = 0;
			int machineNo = 0, operationTime = 0;
			proDesMatrix[0] = new int[machineNum];
			for (int i = 0; i < jobNum; i++) {
				opeationDesArr = prodesStrArr[i].split("\\s+");
				// the opeartion count of every job 每个工件的工序数
				operationCount = Integer.valueOf(opeationDesArr[0]);
				operationCountArr[i] = operationCount;
				int k = 1;
				for (int j = 0; j < operationCount; j++) {
					if (k < opeationDesArr.length) {
						selectedMachineCount = Integer.valueOf(opeationDesArr[k++]);
						// 存储每个工序的备选机器数目
						for (int m = 0; m < selectedMachineCount; m++) {
							machineNo = Integer.valueOf(opeationDesArr[k++]);// 机器编号
							operationTime = Integer.valueOf(opeationDesArr[k++]);// 加工时间
							proDesMatrix[operationTotalIndex][machineNo - 1] = operationTime;// 保存在matrix中
						}
						operationCountList.add(selectedMachineCount);
					}
					// 用来存储i工件j工序所对应的problemDesMatrix[][]的index
					operationToIndex[i][j] = operationTotalIndex;
					operationTotalIndex++;
					if (operationTotalIndex < count) {
						proDesMatrix[operationTotalIndex] = new int[machineNum];
					}
				}
			}

			int listSize = operationCountList.size();
			machineCountArr = new int[listSize];
			for (int i = 0; i < listSize; i++)
				machineCountArr[i] = operationCountList.get(i);

			input.setMachineCountArr(machineCountArr);
			input.setProDesMatrix(proDesMatrix);
			input.setTotalOperationCount(proDesMatrix.length);
			input.setOperationToIndex(operationToIndex);

			input.setOperationCountArr(operationCountArr);

			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		//随机生成算例的时间窗约束

		int sum = 0;
		int count = 0;
		for (int i = 0; i < proDesMatrix.length; i++) {
			int[] productionMatrix = proDesMatrix[i];
			for (int j = 0; j < productionMatrix.length && productionMatrix[j] != 0; j++) {
				sum += productionMatrix[j];
				count++;
			}
		}
		int averageProduceTime = sum / count;//计算平均加工时间长

		Random r = new Random(114514);
//		int totalOperationCount = input.getTotalOperationCount();
//		int upper = (int) Math.round(((double) totalOperationCount / 3) * 0.6);
//		int lowwer = (int) Math.round(((double) totalOperationCount / 5) * 0.7);
//		int twNumber = r.nextInt(upper - lowwer) + lowwer;
//		int count = 0;//计数防止死循环
//
//		List<Integer> tabuNumber = new ArrayList<>();//记录选择的工序
//		while (twNumber > 0 && count <= 100) {
//
//		}
		int jobCount = input.getJobCount();
		timeWindow[][] TWMatrix = new timeWindow[jobCount][];
		for (int i = 0; i < jobCount; i++) {
			int operNum = operationCountArr[i];
			//类似分配过程将工序分段
			List<int[]> totalList = new ArrayList<>();
			int[] initMatrix = new int[operNum];
			List<timeWindow> TWList = new ArrayList<>();

			for (int j = 0; j < operNum; j++) {
				initMatrix[j] = j;
			}
			totalList.add(initMatrix);

			while (totalList.size() > 0) {

				int startNode;
				int endNode;
				int length = totalList.get(0).length;
				if (length == 2) {//直接指定起始和结束点
					startNode = 0;
					endNode = 1;
				} else {
					do {
						startNode = r.nextInt(length - 1);//不能选最后一个工序作为时间窗起始节点
						endNode = r.nextInt(length - 1) + 1;//不能选第一个工序作为时间窗结束节点
					} while (startNode == endNode);
				}

				if (startNode > endNode) {//调整大小
					int temp = startNode;
					startNode = endNode;
					endNode = temp;
				}

				int startOperNo;
				int endOperNo;
				int waitingTime = 0;

				startOperNo = totalList.get(0)[startNode];
				endOperNo = totalList.get(0)[endNode];

				for (int j = 0; j < endOperNo - startOperNo; j++) {
					waitingTime += r.nextInt(averageProduceTime) + averageProduceTime;
				}

				TWList.add(new timeWindow(i, startOperNo, endOperNo, waitingTime));
				//把工序分为左右两部分
				int[] leftMatrix = new int[startNode + 1];
				int[] rightMatrix = new int[length - endNode];

				if (startNode > 0) {
					System.arraycopy(totalList.get(0), 0, leftMatrix, 0, startNode + 1);
					totalList.add(leftMatrix);
				}

				if (length - endNode > 1) {
					System.arraycopy(totalList.get(0), endNode, rightMatrix, 0, length - endNode);
					totalList.add(rightMatrix);
				}

				totalList.remove(0);
			}
			//copy一下
			TWMatrix[i] = new timeWindow[TWList.size()];
			Collections.sort(TWList);
			for (int j = 0; j < TWList.size(); j++) {
				TWMatrix[i][j] = TWList.get(j);
			}
			//分成起始和结尾节点直接指定时间窗
//			List<Integer> startNodeList = new ArrayList<>();
//			List<Integer> endNodeList = new ArrayList<>();
//			List<timeWindow> TWList = new ArrayList<>();
//			int startNode;
//			int endNode;
//
//			for (int j = 0; j < operNum; j++) {
//				startNodeList.add(j);
//				endNodeList.add(j);
//			}
//
//			do {
//				startNode = r.nextInt(startNodeList.size() - 1);//不能选最后一个工序作为时间窗起始节点
//				endNode = r.nextInt(endNodeList.size()) + 1;//不能选第一个工序作为时间窗结束节点
//			} while (startNode == endNode);
//
//			int startOperNo;
//			int endOperNo;
//			int waitingTime = 0;
//
//			startOperNo = startNodeList.get(startNode);
//			endOperNo = endNodeList.get(endNode);
//			int countNum = endOperNo - startOperNo;
//			int countNumber = countNum;
//			for (int j = 0; j < countNum; j++) {
//				waitingTime += r.nextInt(averageProduceTime) + averageProduceTime;
//			}
//
//			while (countNum > 0) {
//				startNodeList.remove(startNode);
//				endNodeList.remove(endNode - countNumber + countNum);
//				countNum--;
//			}
//
//			TWList.add(new timeWindow(jobNo, startOperNo, endOperNo, waitingTime));


//			int pointNum = r.nextInt(operNum) * 2;
//			List<Integer> chosenNum = new ArrayList<>();//记录被选中的点
//			int randomNum;
//			//获得节点序列
//			while (pointNum > 0) {
//				do {
//					randomNum = r.nextInt(operNum * 2);
//				} while (chosenNum.contains(randomNum));
//				chosenNum.add(randomNum);
//				pointNum--;
//			}
//			if (chosenNum.size() != 0) {
//				Collections.sort(chosenNum);
//			} else {
//				continue;
//			}
//			//首尾节点数不能为2
//			if (chosenNum.get(0) / 2 == chosenNum.get(1) / 2) {
//				int startNum = chosenNum.get(0) / 2;
//				int endNum = chosenNum.get(chosenNum.size() - 1) / 2;
//				chosenNum.remove(0);
//				do {
//					randomNum = r.nextInt(operNum * 2) + 1;
//				} while (chosenNum.contains(randomNum) || randomNum == startNum || randomNum == endNum);
//				chosenNum.add(randomNum);
//				Collections.sort(chosenNum);
//			}
//			if (chosenNum.get(chosenNum.size() - 1) / 2 == chosenNum.get(chosenNum.size() - 2) / 2) {
//				int startNum = chosenNum.get(0) / 2;
//				int endNum = chosenNum.get(chosenNum.size() - 1) / 2;
//				chosenNum.remove(chosenNum.size() - 1);
//				do {
//					randomNum = r.nextInt(operNum * 2) + 1;
//				} while (chosenNum.contains(randomNum) || randomNum == startNum || randomNum == endNum);
//				chosenNum.add(randomNum);
//				Collections.sort(chosenNum);
//			}

			//随机生成时间窗时间并记录时间窗序列
//			int size = chosenNum.size() / 2;
//			timeWindow[] TWs = new timeWindow[size];
//			for (int j = 0; j < size; j++) {
//				int jobNo = i;
//				int startOperNo = chosenNum.get(2 * j) / 2;//工序从0开始
//				int endOperNo = chosenNum.get(2 * j + 1) / 2;
//				int waitingTime = 0;
//				for (int k = 0; k < endOperNo - startOperNo; k++) {
//					waitingTime += r.nextInt(averageProduceTime * 2) + averageProduceTime;
//				}
//				TWs[j] = new timeWindow(jobNo, startOperNo, endOperNo, waitingTime);
//			}
//			TWMatrix[i] = TWs;


		}
		input.setTWMatrix(TWMatrix);
		return input;
	}

	/**
	 * @param file a .txt file which contains the time and order information
	 * @return BufferedReader of the file
	 */
	private BufferedReader getBufferedReader(File file) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return reader;
	}

	/**
	 * @param input        the problem description which has been arranged
	 * @param prodesMatrix the problem description which has been arranged
	 */
	public void storeProdesInfoToDisk(Problem input, int prodesMatrix[][]) {
		int operationCountofEveryJobArr[] = input.getOperationCountArr();
		int len = operationCountofEveryJobArr.length;
		int sum = 0;
		for (int num : operationCountofEveryJobArr)
			sum += num;
		System.out.println(sum);
		File file = new File("proDesMatrixPro1.txt");
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			int index = 0, j = 0, i = 0;
			for (i = 0; i < len; i++) {
				for (j = 0; j < operationCountofEveryJobArr[i]; j++)
					writer.write((index + 1) + "-(" + i + "," + (j + 1) + "):" + Arrays.toString(prodesMatrix[index++])
							+ "\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
