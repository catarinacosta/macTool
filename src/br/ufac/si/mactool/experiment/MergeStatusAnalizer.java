/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufac.si.mactool.experiment;

import java.util.List;

/**
 *
 * @author j2cf
 */
public class MergeStatusAnalizer {
	public static boolean isConflict(List<String> mergeMessage) {
        String MERGE_FAIL_MESSAGE = "Automatic merge failed";
        return mergeMessage.stream().anyMatch((line) -> (line.contains(MERGE_FAIL_MESSAGE)));
    }

    public static boolean isFastForward(List<String> mergeMessage) {
        String MERGE_FAST_FORWARD_MESSAGE = "Fast-forward";
        return mergeMessage.stream().anyMatch((line) -> (line.contains(MERGE_FAST_FORWARD_MESSAGE)));
    }
    
    public static int countConflictedFiles(List<String> mergeMessage)
    {
    	String MERGE_CONFLICT_MERGE = "CONFLICT (";
    	int conflictedFiles = 0;
    	for(String line : mergeMessage)
    		if(line.contains(MERGE_CONFLICT_MERGE))
    			conflictedFiles += 1;
    	
    	return conflictedFiles;
    }
}