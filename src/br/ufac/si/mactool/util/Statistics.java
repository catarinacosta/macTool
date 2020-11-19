/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufac.si.mactool.util;

import br.uff.ic.gems.tipmerge.model.Committer;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author j2cf, Catarina, Bruno Trindade
 */
public class Statistics {
	
	
	public static double getMedian(Integer[] values) {
		int tam = values.length;
		Integer[] valuesCopy = new Integer[tam];
		System.arraycopy(values, 0, valuesCopy, 0, tam);
		Arrays.sort(valuesCopy);

		if (tam % 2 == 0){ 
		   return ((double)valuesCopy[(tam / 2) - 1] + (double)valuesCopy[tam / 2]) / 2.0;
		}  else {
		   return (double)valuesCopy[tam / 2];
		}
    }
	
	public static double getMedian(double[] values) {
		int tam = values.length;
		double[] valuesCopy = new double[tam];
		System.arraycopy(values, 0, valuesCopy, 0, tam);
		Arrays.sort(valuesCopy);

		if (tam % 2 == 0){
		   return (valuesCopy[(valuesCopy.length / 2) - 1] + valuesCopy[valuesCopy.length / 2]) / 2.0;
		}  else {
		   return valuesCopy[valuesCopy.length / 2];
		}
    }
	
	public static double getMAD(Integer[] values ){
		double median = getMedian(values);
		double[] newValues = new double[values.length];
		int i =0 ;
		for (Integer listValue : values){
			newValues[i++] = Math.abs(listValue - median);
		}
		return getMedian(newValues);
	}
	
	public static double getMAD(double[] values ){
		double median = getMedian(values);
		double[] newValues = new double[values.length];
		int i =0 ;
		for (double listValue : values){
			newValues[i++] = Math.abs(listValue - median);
		}
		return getMedian(newValues);
	}
	
	
	
	public static List<Double> getMZScore(Integer[] values){
		
		double median = getMedian(values);
		double mad = getMAD(values);
		
		List<Double> zScoreValues = new ArrayList<>();
			
		for (Integer value : values){
			double zScoreValue = 0.6745 * (value - median)/mad;
			zScoreValues.add(zScoreValue);
		}
		
		return zScoreValues;
	}
	
	public static List<Double> getMZScore(double[] values){
		
		double median = getMedian(values);
		double mad = getMAD(values);
		
		List<Double> zScoreValues = new ArrayList<>();
			
		for (double value : values){
			double zScoreValue = 0.6745 * (value - median)/mad;
			zScoreValues.add(zScoreValue);
		}
		
		return zScoreValues;
	}
	
	public static List<Committer> getMZScoreCommitter(List<Committer> committer){
		Integer[] values = new Integer[committer.size()];

		for(int i = 0 ; i < committer.size() ; i++){
			values[i] = committer.get(i).getCommits();
		}
		List<Double> zScoreM = getMZScore(values);
		for (int i=0; i <= zScoreM.size(); i++){
			committer.get(i).setzScoreM(zScoreM.get(i));
		}
		return committer;
		
	}
        
    public static BigInteger combination(long n, long k) {
        BigInteger ret = BigInteger.ONE;
        for (long x = 0; x < k; x++) {
            ret = ret.multiply(BigInteger.valueOf(n-x))
                     .divide(BigInteger.valueOf(x+1));
        }
        return ret;
    }
	
	public static int[] unixConvert(long unixTs)
	{
		int time[] = new int[6], ind = 0;//0..5 => years, months, days, hours, minutes, seconds
		final double divisores[] = {31536000.0003456, 628000.0000288, 86400.0, 3600.0, 60.0};
		while(unixTs/divisores[ind] != 0)
		{
			time[ind] = (int)(unixTs/divisores[ind]);
			unixTs = (long)(unixTs % divisores[ind]);
			ind++;
			if(ind == 5)
			{
				time[ind] = (int)unixTs;
				break;
			}
		}
		return time;
	}
	
	public static String timeToString(int time[])
	{
		final String indexNames[] = {"years", "months", "days", "hours", "minutes", "seconds"};
		String output = "";
		for(int i = 0; i < indexNames.length; i++)
			if(time[i] != 0)
				output += String.format("%d %s, ", time[i], indexNames[i]);
		if(output.length() >= 2)
			output = output.substring(0, output.length()-2);
		else
			output = "0 seconds";
		return output;
	}
	
	public static double timeToDays(long time)
	{
		return time/86400.0;
	}
	
	
	public static String unixConvertedToString(long a, long b)
	{
		return timeToString(unixConvert(Math.abs(a-b)));
	}
}
