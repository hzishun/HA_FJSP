package com.zll.FJSP.GA;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import com.zll.FJSP.Data.Problem;
import com.zll.FJSP.Data.Operation;
import com.zll.FJSP.Data.timeWindow;

public class CaculateFitness {

	/**
	 * @param MyProblem the problem description which has been arranged
	 * @return int[2] machineNoAndTimeArr machine index and time cost
	 */
	public static int[] getMachineNoAndTime(Problem input, int MS[], int jobNo, int operationNo) {
		int[][] proDesMatrix = input.getProDesMatrix();
		int operationToIndex[][] = input.getOperationToIndex();
		int tempCount = 0;
		int totaloperNo = operationToIndex[jobNo][operationNo];// 工序编号
		int machineTimeArr[] = proDesMatrix[totaloperNo];// 工序在备选机器上的加工时间
		int index = 0;
		int count = MS[totaloperNo];// 工序对应的机器编号（备选机器）

		while (tempCount < count) {
			if (machineTimeArr[index] != 0)// 如果对应加工时间为0，代表无法加工，跳过
				tempCount++;
			index++;
		}
		index--;

		int[] machineNoAndTimeArr = new int[2];
		machineNoAndTimeArr[0] = index;

//		if(index == -1){
//			System.out.println("totaloperNo " + totaloperNo + " index " + index);
//			for(int a: MS)
//				System.out.print(a + " ");
//			System.out.println();
//		}

		machineNoAndTimeArr[1] = proDesMatrix[totaloperNo][index];
		return machineNoAndTimeArr;
	}

	/**
	 * @param operationMatrix the operation description of the scheduling problem
	 */
	public static void initOperationMatrix(Operation[][] operationMatrix) {
		int i = 0, j = 0;
		for (i = 0; i < operationMatrix.length; i++) {
			for (j = 0; j < operationMatrix[i].length; j++)
				operationMatrix[i][j].initOperation();
		}
	}

	public class Time {
		int start;
		int end;
		int type;// 0为工作,1为空闲。

		Time(int s, int e, int t) {
			this.start = s;
			this.end = e;
			this.type = t;
		}
	}

	/**
	 * 计算一条染色体（一个可行的调度）所耗费的最大时间
	 *
	 * @param dna    the dna array,an element represents a procedure of a job
	 * @param length the DNA array length
	 * @param input  the time and order information of the problem
	 * @return the fitness of a sheduling
	 */
	public int[] evaluate(Chromosome chromosome, Problem input, Operation[][] operationMatrix) {
		int jobCount = input.getJobCount();
		int machineCount = input.getMachineCount();
		initOperationMatrix(operationMatrix);

		int[] operNoOfEachJob = new int[jobCount];// 当前处理到工件的工序No
		Arrays.fill(operNoOfEachJob, 0);

		ArrayList<Time>[] machTimes = new ArrayList[machineCount];// 机器的时间段
		for (int i = 0; i < machineCount; i++) {
			machTimes[i] = new ArrayList<>();
			machTimes[i].add(new Time(0, Integer.MAX_VALUE, 0));
		}

		int jobNo = 0;
		int operNo = 0;
		int operationTime = 0;
		int machineNo = 0;
		int[] machineNoAndTimeArr;
		int[] completeTime = new int[jobCount]; //记录每个工件的完工时间
		int[] pointer = new int[jobCount];//指针合集
		timeWindow[][] TWMatrix = input.getTWMatrix();//导入时间窗约束

		for (int i = 0; i < chromosome.gene_OS.length; i++) {
			jobNo = chromosome.gene_OS[i];// 工件名
			operNo = operNoOfEachJob[jobNo]++;// 当前工件操作所在的工序数 , 从1开始
			int lastOperNo = input.getOperationCountArr()[jobNo];// 当前工件的总工序数

			//找到这道工序对应的机器编号以及加工时间
			machineNoAndTimeArr = getMachineNoAndTime(input, chromosome.gene_MS, jobNo, operNo);
			machineNo = machineNoAndTimeArr[0];
			operationTime = machineNoAndTimeArr[1];

//			System.out.println("i=" + i + ",JobNo " + jobNo + ",OperNo " + operNo + ",machineNo " + machineNo
//					+ ",operationTime" + operationTime);

			// 如果是第一个，允许最早开始时间为0
			if (operNo == 0) {
				operationMatrix[jobNo][operNo].aStartTime = 0;
			} else {
				operationMatrix[jobNo][operNo].aStartTime = operationMatrix[jobNo][operNo - 1].endTime;
			}
			operationMatrix[jobNo][operNo].machineNo = machineNo;
			operationMatrix[jobNo][operNo].jobNo = jobNo;
			operationMatrix[jobNo][operNo].task = operNo;

			for (int j = 0; j < machTimes[machineNo].size(); j++) {
				int start = Math.max(operationMatrix[jobNo][operNo].aStartTime, machTimes[machineNo].get(j).start);
				int end = start + operationTime;
				// 对机器空闲的时间段，若可以加工，则加工，否则判断下一个空闲时间段
				if (machTimes[machineNo].get(j).type == 0 && end <= machTimes[machineNo].get(j).end) {
					if (operNo == lastOperNo - 1) {//判断是否为工件的最后一道工序
						completeTime[jobNo] = end;
					}
					// 设置工序开始结束时间
					operationMatrix[jobNo][operNo].startTime = start;
					operationMatrix[jobNo][operNo].endTime = end;
					// 更新机器时间段
					ArrayList<Time> t = new ArrayList<>();
					if (operationMatrix[jobNo][operNo].aStartTime > machTimes[machineNo].get(j).start) {
						t.add(new Time(machTimes[machineNo].get(j).start, operationMatrix[jobNo][operNo].aStartTime, 0));
						t.add(new Time(operationMatrix[jobNo][operNo].aStartTime, end, 1));
					} else {
						t.add(new Time(machTimes[machineNo].get(j).start, end, 1));
					}
					if (end < machTimes[machineNo].get(j).end) {
						t.add(new Time(end, machTimes[machineNo].get(j).end, 0));
					}
					machTimes[machineNo].remove(j);
					machTimes[machineNo].addAll(j, t);


//					System.out.println("startTime "+operationMatrix[jobNo][operNo].startTime+
//							",endTime "+operationMatrix[jobNo][operNo].endTime);

					break;
				}
			}
			//判断时间窗约束
			int currentPointer = pointer[jobNo];
			if (TWMatrix[jobNo].length != 0 && currentPointer < TWMatrix[jobNo].length) {//防空指针异常、指针超过索引长度
				if (TWMatrix[jobNo][currentPointer].endOperNo == operNo) {
					timeWindow tw = new timeWindow(TWMatrix[jobNo][currentPointer]);
					int startTime = operationMatrix[jobNo][tw.startOperNo].endTime;
					int endTime = operationMatrix[jobNo][tw.endOperNo].startTime;
					int rWaitingTime;//实际等待时长
					int sumProTime = 0; //中间工序的加工时长

					for (int j = tw.startOperNo + 1; j < tw.endOperNo; j++) {
						sumProTime += operationMatrix[jobNo][j].span;
					}

					rWaitingTime = endTime - startTime - sumProTime;
					if (rWaitingTime > tw.waitingTime) {

					}
					pointer[jobNo]++;
				}
			}
		}

		int totalWeightedTardiness = 0;

		for (int i = 0; i < jobCount; i++) {
			Random random = new Random(1);
			int sumMinProduceTime = 0;
			for (int j = 0; j < input.getOperationCountArr()[i]; j++) {
				int[] proDes = input.getProDesMatrix()[input.getOperationToIndex()[i][j]];
				int min = Integer.MAX_VALUE;
				for (int k = 0; k < proDes.length; k++) {
					if (proDes[k] != 0 && proDes[k] < min) {
						min = proDes[k];
					}
				}
				sumMinProduceTime += min;
			}
			double aCoefficient = 1 + (0.3 * jobCount) / machineCount;
			int dueTime = (int) Math.round(aCoefficient * sumMinProduceTime);//工件的交货期
			int jobWeight = 0;
			double randomNumber = random.nextDouble();
			if (randomNumber < 0.2) jobWeight = 1;
			else if (randomNumber < 0.4) jobWeight = 2;
			else if (randomNumber < 0.6) jobWeight = 3;
			else if (randomNumber < 0.8) jobWeight = 4;
			else jobWeight = 5;
			if (completeTime[i] > dueTime) totalWeightedTardiness += jobWeight * (completeTime[i] - dueTime); //拖期惩罚
		}

		int longestTime = 0;
		for(int i = 0; i < machineCount; i++)
			longestTime = Math.max(machTimes[i].get(machTimes[i].size() - 1).start , longestTime);
//
//		return longestTime;
		int[] result = new int[2];
		result[0] = longestTime;
		result[1] = totalWeightedTardiness + 1;
		return result;//加1避免出现0除
	}

	/**
	 * 计算一条染色体（一个可行的调度）所耗费的最大时间
	 *
	 * @param dna    the dna array,an element represents a procedure of a job
	 * @param length the DNA array length
	 * @param input  the time and order information of the problem
	 * @return the fitness of a sheduling
	 */
	public static int evaluate1(Chromosome chromosome, Problem input, Operation[][] operationMatrix) {
		int jobCount = input.getJobCount();
		int machineCount = input.getMachineCount();
		initOperationMatrix(operationMatrix);

		int span = -1;
		int[] operNoOfEachJob = new int[jobCount];// 当前处理到工件的工序No
		Arrays.fill(operNoOfEachJob, 0);

		int[] machFreeTime = new int[machineCount];// 机器最早空闲时间
		Arrays.fill(machFreeTime, 0);

		int jobNo = 0;
		int operNo = 0;
		int operationTime = 0;
		int machineNo = 0;
		int machineNoAndTimeArr[] = new int[2];

		for (int i = 0; i < chromosome.gene_OS.length; i++) {
			jobNo = chromosome.gene_OS[i];// 工件名
			operNo = operNoOfEachJob[jobNo]++;// 当前工件操作所在的工序数

			machineNoAndTimeArr = getMachineNoAndTime(input, chromosome.gene_MS, jobNo, operNo);
			machineNo = machineNoAndTimeArr[0];
			operationTime = machineNoAndTimeArr[1];

//			System.out.println("i=" + i + ",JobNo " + jobNo + ",OperNo " + operNo + ",machineNo " + machineNo
//					+ ",operationTime" + operationTime);

			if (operNo == 0) {
				// 如果是第一个，开始时间
				operationMatrix[jobNo][operNo].jobNo = jobNo;
				operationMatrix[jobNo][operNo].machineNo = machineNo;
				operationMatrix[jobNo][operNo].task = operNo;
				operationMatrix[jobNo][operNo].startTime = machFreeTime[machineNo];
				operationMatrix[jobNo][operNo].endTime = operationMatrix[jobNo][operNo].startTime + operationTime;
			} else {
				operationMatrix[jobNo][operNo].jobNo = jobNo;
				operationMatrix[jobNo][operNo].machineNo = machineNo;
				operationMatrix[jobNo][operNo].task = operNo;
				operationMatrix[jobNo][operNo].startTime = Math.max(operationMatrix[jobNo][operNo - 1].endTime,
						machFreeTime[machineNo]);
				operationMatrix[jobNo][operNo].endTime = operationMatrix[jobNo][operNo].startTime + operationTime;
			}

			machFreeTime[machineNo] = operationMatrix[jobNo][operNo].endTime;
			if (operationMatrix[jobNo][operNo].endTime > span) {
				span = operationMatrix[jobNo][operNo].endTime;
			}
		}

		return span;
	}

}
