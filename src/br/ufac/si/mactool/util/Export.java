/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufac.si.mactool.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import br.uff.ic.gems.tipmerge.model.Repository;

/**
 *
 * @author Bruno Trindade
 */
 
public class Export {
 
	public static void toCSV(Repository repos, String description, String header[], List<String> lines)
	{
		try
		{
			String fileName = repos.getName() + (description != null ? "-"+description : "")   + ".csv";
			File f = new File(fileName);
			FileWriter fw = new FileWriter(f);
			System.out.println("File created: " + f.getAbsolutePath());
			
			//Header:
			fw.write(String.join(",", header)+"\n");
			
			//Lines
			for(String line : lines)
				fw.write(line+"\n");
			
			fw.close();
		}
		catch(IOException ioe)
		{
			System.out.println("Export failed!");
		}
	}
}

 
     
