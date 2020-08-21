package br.ufac.si.mactool;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import br.ufac.si.mactool.experiment.RevisionAnalyzer;
import br.ufac.si.mactool.model.ConflictedFile;
import br.ufac.si.mactool.util.Export;
import br.ufac.si.mactool.util.RunGit;
import br.ufac.si.mactool.util.Statistics;
import br.uff.ic.gems.tipmerge.dao.EditedFilesDao;
import br.uff.ic.gems.tipmerge.dao.MergeCommitsDao;
import br.uff.ic.gems.tipmerge.dao.MergeFilesDao;
import br.uff.ic.gems.tipmerge.dao.RepositoryDao;
import br.uff.ic.gems.tipmerge.model.Committer;
import br.uff.ic.gems.tipmerge.model.EditedFile;
import br.uff.ic.gems.tipmerge.model.MergeCommits;
import br.uff.ic.gems.tipmerge.model.MergeFiles;
import br.uff.ic.gems.tipmerge.model.Repository;

/**
 * @author Bruno Trindade
 * */

public class MACTool
{
	public static Repository repos = null;
	
	public static final String CONFLICT_HEADER[] = {"Hash", "Devs intersection", "Conflicted files", 
    		"Conflicted files caused by same dev", "Null files", "Conflicted files caused by same dev / Conflicted files"};
    
	public static final String GENERAL_HEADER[] = {"Hash", "Merge timestamp", "Branching time", "Merge isolation time", 
    		"Devs 1", "Devs 2", "Different devs", "Same devs", "Devs intersection", 
    		"Commits 1", "Commits 2", "Changed files 1", "Changed files 2", "Changed files intersection", "Has conflict?", "Conflicted Files", "Chunks"};
	
	public static void main(String[] args)
	{
		for(int i = 0; i < args.length; i++)
		{
			System.out.println("Repository: " + args[i]);
			try {
				repos = new RepositoryDao(new File(args[i])).getRepository();
				RepositoryDao rdao = new RepositoryDao(repos.getProject());
				rdao.setGeneralBasicDatas(repos);
				runAnalysis();
			}
			catch(NullPointerException ex) {
				System.out.println("\tInvalid repository!");
			}
			System.out.println("Finished\n");
		}
	}
	
	public static void runAnalysis()
	{	
		MergeFilesDao mergeFilesDao = new MergeFilesDao();
		MergeCommitsDao mergeCommitsDao = new MergeCommitsDao(repos.getProject());

        ArrayList<String> linesConflito = new ArrayList<String>(), linesGeral = new ArrayList<String>();
        List<String> merges = repos.getListOfMerges();
        int max = merges.size(), cont = 0;
        
        for(String hashMerge : merges)  {
        	
            hashMerge = hashMerge.split(" ")[0];
            MergeFiles merge = mergeFilesDao.getMerge(hashMerge, repos.getProject());
            
            try {
	            if(merge.getHashBase().equals(merge.getParents()[0]) || merge.getHashBase().equals(merge.getParents()[1]))
	            {
	            	System.out.printf("\t-> Merge (%s) %02d/%02d\n", hashMerge.substring(0, 5), ++cont, max);
	            	continue;
	            }
	            else
	            	System.out.printf("\t-> Merge (%s) %02d/%02d - Collected\n", hashMerge.substring(0, 5), ++cont, max);
            }
            catch(NullPointerException npe) {
            	continue;
            }
            
            merge.setFilesOnBranchOne(new EditedFilesDao().getFiles(merge.getHashBase(), merge.getParents()[0], repos.getProject(), "All Files"));
            merge.setFilesOnBranchTwo(new EditedFilesDao().getFiles(merge.getHashBase(), merge.getParents()[1], repos.getProject(), "All Files"));

            //=================// Data do merge //=================//
            String mergeTimestamp = getMergeTimestamp(hashMerge);
            mergeTimestamp = convertToDefaultTimeZone(mergeTimestamp);
            
            //=================// Tempo de isolamento //=================//

            /**
             * Tempo de isolamento Ancestor: tempo do commit Base+1 até cada Parent.
             * Tempo de isolamento Parent: tempo do commit Base até cada Parent;
             * Tempo de isolamento Merge: tempo do commit Base até o Merge.
             **/
            
            String commitNext1 = getAncestorNextHash(merge.getHashBase(), merge.getParents()[0]), commitNext2 = getAncestorNextHash(merge.getHashBase(), merge.getParents()[1]);
			long bTime = 0;

			long timeNext1 = getCommitUnixTime(commitNext1), timeNext2 = getCommitUnixTime(commitNext2);
			if(timeNext1 > timeNext2)
				bTime = timeNext2;
			else
				bTime = timeNext1;

			long timeParent1 = getCommitUnixTime(merge.getParents()[0]), timeParent2 = getCommitUnixTime(merge.getParents()[1]);
			if(timeParent1 > timeParent2)
				bTime = timeParent1 - bTime;
			else
				bTime = timeParent2 - bTime;
			
			String branchingTime = String.format("%.7f", Statistics.timeToDays(bTime)).replace(",", ".");
			String isolamentoMerge = getIsolationTimeBetweenCommits(merge.getHashBase(), hashMerge);
            
            //=================// Qtde. de desenvolvedores //=================//
            MergeCommits mergeCommits = new MergeCommits(hashMerge, repos.getProject());
            mergeCommits.setHashBase(merge.getHashBase());
            mergeCommits.setParents(merge.getParents()[0], merge.getParents()[1]);
            mergeCommitsDao.setCommittersOnBranch(mergeCommits);
            int committers1 = 0, commits1 = 0;
            int committers2 = 0, commits2 = 0;
            int sameCommitters = 0, diffCommitters = 0;
            int numIntersec = 0;
            
            mergeCommitsDao.setCommittersOnBranch(mergeCommits);
            try {
            	
	            List<String> committersR1 = new ArrayList<String>();
	            
	            for(Committer c : mergeCommits.getCommittersBranchOne()) {
	            	
	            	commits1 += c.getCommits();
	            	++committers1;
	            	
	            	committersR1.add(c.getNameEmail());
	            }
	            
	            for(Committer c : mergeCommits.getCommittersBranchTwo()) {
	            	
	            	commits2 += c.getCommits();
	            	++committers2;
	            	
	            	if(committersR1.contains(c.getNameEmail()))
	            		numIntersec += 1;
	            }
	            
	            diffCommitters = committersR1.size()+mergeCommits.getCommittersBranchTwo().size()-sameCommitters*2;
	            sameCommitters = numIntersec;
	            
	            if(committers1 == committers2 && committers1 == numIntersec)
	            	numIntersec = 2;
	            else if(numIntersec > 0)
	            	numIntersec = 1;
	        }
	        catch(NullPointerException npe)
	        {}
            
            //=================// Qtde. de arquivos alterados //=================//
            int arquivosAlterados1 = 0, arquivosAlterados2 = 0, arqIntersec = 0;
            try {
            	arquivosAlterados1 = merge.getFilesOnBranchOne().size();   
            	arquivosAlterados2 = merge.getFilesOnBranchTwo().size();
            	
                List<String> arqsR1 = new ArrayList<String>();
                
                for(EditedFile c : merge.getFilesOnBranchOne())
                	arqsR1.add(c.getFileName());
                
                for(EditedFile c : merge.getFilesOnBranchTwo())
                	if(arqsR1.contains(c.getFileName()))
                		arqIntersec += 1;
                
                if(arquivosAlterados1 == arquivosAlterados2 && arquivosAlterados1 == arqIntersec)
                	arqIntersec = 2;
                else if(arqIntersec > 0)
                	arqIntersec = 1;
            }
            catch(NullPointerException npe)
            {}
            
            //================// Conflitos //==============//
            int arquivos = RevisionAnalyzer.hasConflictNum(repos.getProject().toString(), merge.getParents()[0], merge.getParents()[1]);
            int contAmbos = 0, contNull = 0;
            int chunks = 0;
            boolean conflito = false;
        	String descricaoArquivos = "";
        	
        	//Removendo untracked files
            cleanUntrackedFiles();
            if(arquivos > 0) //Se o número de arquivos em conflito for positivo 
            {
            	//=======// Chunks //=======//
                conflito = true;                
                for(String line : RunGit.getListOfResult("git diff", repos.getProject()))
                	if(line.replace("+","").startsWith("======="))
                		chunks++;
            	
            	//=======// Arquivos //=======//
            	//System.out.println("git diff --name-only --diff-filter=U");
            	List<String> arquivosList = RunGit.getListOfResult("git diff --name-only --diff-filter=U", repos.getProject());
            	
                if(numIntersec > 0)
                {
                	//Gerando a lista de arquivos em conflito
                	HashMap<String, ConflictedFile> ac = new HashMap<String, ConflictedFile>();
                	for(String arquivo : arquivosList)
                	{
                		ConflictedFile aux = new ConflictedFile(arquivo);
                		aux.setDeleted(checkDeletedFile(aux.getFileName()));                		
                		ac.put(arquivo, aux);
                	}
                	
                	if(ac.size() > 0)
                	{
                		checkWhoCausedConflict(merge.getHashBase(), merge.getParents()[0], ac, 0);
                		checkWhoCausedConflict(merge.getHashBase(), merge.getParents()[1], ac, 1);
                		
                		//Verificando se o mesmo causou o conflito dos dois lados
                		for (Entry<String, ConflictedFile> entry : ac.entrySet()) 
                		{
                			if(entry.getValue().getAuthor(0) != null && entry.getValue().getAuthor(0).equals(entry.getValue().getAuthor(1)))
                				contAmbos += 1;
                			else if(entry.getValue().getAuthor(0) == null || entry.getValue().getAuthor(1) == null)
                				contNull += 1;
                			else
                				descricaoArquivos += String.format("%s,%s,%s;", entry.getValue().getFileName(), entry.getValue().getAuthor(0), entry.getValue().getAuthor(1));
                		}
                	}
                }
                linesConflito.add(merge.getHash()+","+numIntersec+","+arquivos+","+contAmbos+","+contNull+","+((double)contAmbos/arquivos)+","+descricaoArquivos);
            }
            
            linesGeral.add(merge.getHash()+","+mergeTimestamp+","+branchingTime+","+isolamentoMerge+","+committers1+","+committers2+","+diffCommitters+","+sameCommitters+","+
            		numIntersec+","+commits1+","+commits2+","+arquivosAlterados1+","+arquivosAlterados2+","+arqIntersec+","+ (conflito ? "YES" : "NO") +","+arquivos+","+chunks);
        }
        try
        {
	        Export.toCSV(repos, "conflict", CONFLICT_HEADER, linesConflito);
	        Export.toCSV(repos, "general", GENERAL_HEADER, linesGeral);
        }
        catch(StringIndexOutOfBoundsException ex)
        {
        	System.out.println("\tNo merge was analyzed.");
        }
    }
	
	/*
	 * git diff HASH arquivo
	 * Quando os marcadores de conflito são criados e é feito o diff com o commit, se a linha foi modificada
	 * no commit do diff, a linha fica sem o +
	 * */
	
	//Identificar o causador do conflito de cada arquivo
	public static void checkWhoCausedConflict(String hashBase, String hashParent, HashMap<String, ConflictedFile> ac, int ramoID)
	{
		List<String> linhasLog = RunGit.getListOfResult("git log -m --name-only --pretty=format:%H " + hashBase + "^.." + hashParent, repos.getProject());
		String hash = null;
		for(String linha : linhasLog) //Linha = arquivo modificado
		{
			if(hash == null)
				hash = linha;
			else if(linha.length() == 0)
				hash = null;
			else if(ac.get(linha) != null && ac.get(linha).getAuthor(ramoID) == null)
			{
				//Abrir o arquivo nesse commit pra ver
				List<String> linhasDiff = RunGit.getListOfResultSpecial(new String[]{"git", "diff", hash, linha}, repos.getProject());
				int tagStatus = 0;
				String tags[] = {"", "+<<<<<<<", "+=======", "+>>>>>>>"};
				ConflictedFile arq = ac.get(linha);
				if(ac.get(linha).isDeleted() == false)
				{
					for(String lD : linhasDiff)
					{
						//Verificando se a linha é algum marcador
						for(int i = 1; i < 4; i++)
							if(lD.startsWith(tags[i]))
							{
								tagStatus = i;
								break;
							}
						
						//Se estiver nas tags do ramo analisado e uma das linhas entre as tags não começar com +, então a modificação foi nesse commit
						if(ramoID == tagStatus && !linha.startsWith("+"))
						{
							arq.setAuthor(ramoID, getCommitAuthor(hash));
							arq.setCommit(ramoID, hash);
							break;
						}
					}
				}
				else
				{
					arq.setAuthor(ramoID, getCommitAuthor(hash));
					arq.setCommit(ramoID, hash);
					String arquivoDeletado = RunGit.getResultSpecial(new String[]{"git", "diff", "--summary" ,"HEAD.." + hash, linha}, repos.getProject());
					if(arquivoDeletado != null && arquivoDeletado.contains("delete mode")) //Encontra o commit onde foi deletado
						arq.setDeleted(false);
					else
						arq.setDeleted(true);
				}
			}
		}
	}
	
	//Verificar se um arquivo foi removido
	public static boolean checkDeletedFile(String file)
	{
		try
		{
			return RunGit.getResultSpecial(new String[]{"git", "diff", file}, repos.getProject()).contains("* Unmerged path " + file);
		}
		catch(NullPointerException npe)
		{
			return RunGit.getResultSpecial(new String[]{"git", "status", file, "-s"}, repos.getProject()).startsWith("DD ");
		}
	}
	
	//Recuperar o autor de um commit
	public static String getCommitAuthor(String hash)
	{
		return RunGit.getResult("git log --pretty=format:%an -1 " + hash, repos.getProject());
	}
	
	//Recuperar timestamp do merge
	public static String getMergeTimestamp(String hashMerge)
	{
		return RunGit.getResult("git log -1 --pretty=format:%ci " + hashMerge, repos.getProject())/*.substring(0, 19)*/;
	}
	
	//Recuperar o tempo de isolamento do ancestral
	public static String getAncestorIsolationTime(String hashBase, String hashParent)
	{
		List<String> beginLineTotal = RunGit.getListOfResult("git log --first-parent --pretty=format:%HX%ciX%ct " + hashBase + ".." + hashParent, repos.getProject());
		if(beginLineTotal.size() > 0)
        {
			long parentUnixTs = getCommitUnixTime(hashParent);
			
			String parts[] = beginLineTotal.get(beginLineTotal.size()-1).split("X");
        	long ancestorUnixTs = Long.parseLong(parts[2]);
        	
        	for(int i = beginLineTotal.size()-2; i >= 0 && ancestorUnixTs > parentUnixTs; i--) //Evitando que o tempo do ancestral seja maior que o do pai (tempo negativo)
        	{
        		parts = beginLineTotal.get(i).split("X");
        		ancestorUnixTs = Long.parseLong(parts[2]);
        	}
        	return String.format("%.7f", Statistics.timeToDays(parentUnixTs - ancestorUnixTs)).replace(",", ".");
        }
		return "0.00000";
	}

	//Recuperar o hash do commit após o base
	public static String getAncestorNextHash(String hashBase, String hashParent)
	{
		List<String> commits = RunGit.getListOfResult("git rev-list --ancestry-path --reverse " + hashBase + ".." + hashParent, repos.getProject());
		if(commits.size() > 0)
        {
			long parentUnixTs = getCommitUnixTime(hashParent);
			long baseUnixTs = getCommitUnixTime(hashBase);
			long ancestorUnixTs = getCommitUnixTime(commits.get(0));
			String ancestor = commits.get(0);
			
			for(String hash : commits) {
				if(ancestorUnixTs <= parentUnixTs && ancestorUnixTs > baseUnixTs)
					break;
				
				ancestorUnixTs = getCommitUnixTime(hash);
				ancestor = hash;
			}
			
			return ancestor;
        }
		return "null";
	}
	
	//Recuperar tempo de isolamento entre dois hashes
	public static String getIsolationTimeBetweenCommits(String hashA, String hashB)
	{
		long commitAtime = getCommitUnixTime(hashA), commitBtime = getCommitUnixTime(hashB);
        return String.format("%.7f", Statistics.timeToDays(commitBtime-commitAtime)).replace(",", ".");
	}
	
	//Converter para GMT-00 ou UTC
	public static String convertToDefaultTimeZone(String Date) 
	{
		//Removido: não faz sentido analisar tudo em um mesmo fuso, já que as pessoas geralmente fazem commit no horário da cidade delas
        String converted_date = Date;
        try {

            /*DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            utcFormat.setTimeZone(TimeZone.getTimeZone("GMT-05:00"));

            Date date = utcFormat.parse(Date);

            DateFormat currentTFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            currentTFormat.setTimeZone(TimeZone.getTimeZone("GMT-00:00"));

            converted_date =  currentTFormat.format(date);*/
        }catch (Exception e){ e.printStackTrace();}

        return converted_date;
	}
	
	//Recuperar horário do commit em Unix
	public static long getCommitUnixTime(String hash)
	{
		String parentData = RunGit.getResult("git log -1 --pretty=format:%ciX%ct " + hash, repos.getProject()),  parts2[] = parentData.split("X");
    	return Long.parseLong(parts2[1]);
	}
	
	//Remover arquivos não gerenciados pelo Git
	public static void cleanUntrackedFiles()
	{
		RunGit.getResult("git clean -df", repos.getProject());
	}
}

/*Alguns comandos Git:
git log --reverse --ancestry-path HASH^..origin/BRANCH --pretty=format:%ci
git log --reverse --ancestry-path f12b46a0ceaf2925a75fc3efd45ff2d0e60640d6^..refs/remotes/origin/Ramo2 --pretty=format:%ci
git show-ref --head
git rev-list ANCESTOR_HASH ^..PARENT_HASH
git log --pretty=format:%HX%ci ANCESTOR_HASH PARENT_HASH*/
//=================================================//
