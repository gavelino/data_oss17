package aserg.authorship;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import aserg.authorship.distribution.DistributionCalculator;
import aserg.authorship.distribution.DistributionMetrics;
import aserg.gtf.model.LogCommitFileInfo;
import aserg.gtf.model.LogCommitInfo;
import aserg.gtf.model.NewFileInfo;
import aserg.gtf.model.authorship.AuthorshipInfo;
import aserg.gtf.model.authorship.Developer;
import aserg.gtf.model.authorship.File;
import aserg.gtf.model.authorship.Repository;
import aserg.gtf.task.DOACalculator;
import aserg.gtf.task.NewAliasHandler;
import aserg.gtf.task.extractor.FileInfoExtractor;
import aserg.gtf.task.extractor.GitLogExtractor;
import aserg.gtf.task.extractor.LinguistExtractor;
import aserg.gtf.util.FileInfoReader;
import aserg.gtf.util.LineInfo;
import aserg.gtf.util.LinuxModulesUtility;

public class AuthorshipAnalyzer {
	private static final Logger LOGGER = Logger.getLogger(AuthorshipAnalyzer.class);
	static String release = "NO-Release";
	public static void main(String[] args) {
		LOGGER.trace("AuthorshipAnalyser starts");
		String repositoryPath = "";
		String repositoryName = "";
		if (args.length>0)
			repositoryPath = args[0];
		if (args.length>1)
			repositoryName = args[1];
		if (args.length>2)
			release = args[2];
		
		repositoryPath = (repositoryPath.charAt(repositoryPath.length()-1) == '/') ? repositoryPath : (repositoryPath + "/");
		if (repositoryName.isEmpty())
			repositoryName = repositoryPath.split("/")[repositoryPath.split("/").length-1];
		

		Map<String, List<LineInfo>> filesInfo;
		Map<String, List<LineInfo>> aliasInfo;
		Map<String, List<LineInfo>> modulesInfo;
		try {
			filesInfo = FileInfoReader.getFileInfo("repo_info/filtered-files.txt");
		} catch (IOException e) {
			LOGGER.warn("Not possible to read repo_info/filtered-files.txt file. File filter step will not be executed!");
			filesInfo = null;
		}		
		try {
			aliasInfo = FileInfoReader.getFileInfo("repo_info/alias.txt");
		} catch (IOException e) {
			LOGGER.warn("Not possible to read repo_info/alias.txt file. Aliases treating step will not be executed!");
			aliasInfo = null;
		}
		try {
			modulesInfo = FileInfoReader.getFileInfo("repo_info/modules.txt");
		} catch (IOException e) {
			LOGGER.warn("Not possible to read repo_info/modules.txt file. No modules info will be setted!");
			modulesInfo = null;
		}
		
		
		GitLogExtractor gitLogExtractor = new GitLogExtractor(repositoryPath, repositoryName);	
		NewAliasHandler aliasHandler =  aliasInfo == null ? null : new NewAliasHandler(aliasInfo.get(repositoryName));
		FileInfoExtractor fileExtractor = new FileInfoExtractor(repositoryPath, repositoryName);
		LinguistExtractor linguistExtractor =  new LinguistExtractor(repositoryPath, repositoryName);
		
		
		
		//gitLogExtractor.persist(commits);
		
		//printCoverageInTime(repositoryPath, repositoryName, filesInfo,	fileExtractor, linguistExtractor, gitLogExtractor,aliasHandler);
		
		try {
			calculateAuthorship(repositoryPath, repositoryName, filesInfo, modulesInfo,	fileExtractor, linguistExtractor, gitLogExtractor,aliasHandler);
		} catch (Exception e) {
			LOGGER.error("TF calculation aborted!",e);
		}
		

		
		
		LOGGER.trace("GitTruckFactor end");
	}

	//Test to calculate the authorship variation on time 
	private static void printCoverageInTime(String repositoryPath,
			String repositoryName, Map<String, List<LineInfo>> filesInfo,
			FileInfoExtractor fileExtractor,
			LinguistExtractor linguistExtractor,
			GitLogExtractor gitLogExtractor, NewAliasHandler aliasHandler) throws Exception {
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2015);
		cal.set(Calendar.MONTH, Calendar.FEBRUARY);
		cal.set(Calendar.DAY_OF_MONTH, 25);
		String devsA[] = {"Yoann Delouis", "yDelouis"};
		//String devsA[] = {"WonderCsabo", "Damien"};
 		List<String> devs = Arrays.asList(devsA);
 		

		//cal.add(Calendar.MONTH, -24);

 		for (int i = 0; i < 24; i++) {
 			Map<String, LogCommitInfo> commits = gitLogExtractor.execute();
 			commits = aliasHandler.execute(repositoryName, commits);
 			
 			List<NewFileInfo> files = fileExtractor.execute();
 			files = linguistExtractor.setNotLinguist(files);
 			if (filesInfo != null)
 				applyFilterFiles(filesInfo.get(repositoryName), files);
 			
 			Map<String, LogCommitInfo> newCommits = filterCommitsByDate(commits, cal.getTime());
			DOACalculator doaCalculator = new DOACalculator(repositoryPath,	repositoryName, newCommits.values(), files);
			Repository repository = doaCalculator.execute();
			printCoverage(repository, devs, cal);
			
			cal.add(Calendar.MONTH, -1);
		}
	}
	
	private static void calculateAuthorship(String repositoryPath,
			String repositoryName, 
			Map<String, List<LineInfo>> filesInfo, 
			Map<String, List<LineInfo>> modulesInfo,
			FileInfoExtractor fileExtractor,
			LinguistExtractor linguistExtractor,
			GitLogExtractor gitLogExtractor, NewAliasHandler aliasHandler) throws Exception {
			Map<String, List<String>> moduleMap = null;
			Map<String, LogCommitInfo> commits = gitLogExtractor.execute();
 			if (aliasHandler != null)
 					commits = aliasHandler.execute(repositoryName, commits);
 			 				
			List<NewFileInfo> files = fileExtractor.execute();
			files = linguistExtractor.setNotLinguist(files);	
			if(filesInfo != null && filesInfo.size()>0) 
				if(filesInfo.containsKey(repositoryName))
					applyFilterFiles(filesInfo.get(repositoryName), files);
				else
					LOGGER.warn("No filesInfo for " + repositoryName);
			
			if (repositoryName.equals("torvalds/linux")){
				moduleMap = setLinuxModules(files); 
//				printModuleSize(moduleMap);
			}
			else if(modulesInfo != null && modulesInfo.containsKey(repositoryName))
				moduleMap = setModules(modulesInfo.get(repositoryName), files);

			filterModule(files, "firmware");
			

//			printDevsInfo(files, commits);
			
			
//			*** repository-info.jar ***
//			printReleaseInfo(files, commits);
			
			
			
//			printFilteredFiles(files);
			
			//applyRegexFilter(files, "^Library/Formula/.*");
			//applyRegexSelect(files, "^kernel/.*");
			
			
			//fileExtractor.persist(files);
			
			DOACalculator doaCalculator = new DOACalculator(repositoryPath, repositoryName, commits.values(), files);
			Repository repository = doaCalculator.execute();
			//doaCalculator.persist(repository);

			
			
//			*** file-authorship.jar ***
			printFilesAuthorship(repository, modulesInfo);
			
//			*** author-workload.jar ***
//			*** print authorship workload, spread and focus ***
//			List<Developer> devs = getSortDevs(repository);
//			if (moduleMap == null)
//				printDevs(devs, repository.getDevelopers());
//			else
//				printDevs(devs, repository.getDevelopers(), moduleMap);
			
//			*** new-authors-ratio.jar *** Print authors ratio by module
//			printAuthorRatio(release, repository, files);
	}

	


	private static void printAuthorRatio(String release, Repository repository, List<NewFileInfo> files) {
		//Set<String> selectedFiles = new
		List<Developer> devs = repository.getDevelopers();
		Set<Developer> authors = new HashSet<Developer>();
		Map<String,Set<Developer>> authorsMap = new HashMap<String, Set<Developer>>();
		Map<String,Set<Developer>> devMap = new HashMap<String, Set<Developer>>();
		
		for (Developer developer : devs) {
			List<File> devFiles = developer.getAuthorshipFiles();
			for (AuthorshipInfo authorship : developer.getAuthorshipInfos()) {
				File file = authorship.getFile();
				String module = file.getModule();
				if (authorship.isDOAAuthor()){					
					if (!authorsMap.containsKey(module))
						authorsMap.put(module, new HashSet<Developer>());
					authorsMap.get(module).add(developer);
				}
				else{
					if (!devMap.containsKey(module))
						devMap.put(module, new HashSet<Developer>());
					devMap.get(module).add(developer);
				}
			}
			if (devFiles != null && devFiles.size()>0){
				for (File file : devFiles) {
					String module = file.getModule();
//					if (!authorsMap.containsKey(module))
//						authorsMap.put(module, new HashSet<Developer>());
//					authorsMap.get(module).add(developer);
				}
				authors.add(developer);
			}
		}
		for (Entry<String, Set<Developer>> entry : devMap.entrySet()) {
			int nDevs = entry.getValue().size();
			int nAuthors = authorsMap.get(entry.getKey()).size();
			System.out.println(release+";"+entry.getKey()+";"+nDevs+";"+nAuthors+";"+((float)nAuthors/nDevs));
		}
		System.out.println(release+";"+"all"+";"+devs.size()+";"+authors.size()+";"+((float)authors.size()/devs.size()));
	}

	private static void printDevsInfo(List<NewFileInfo> files,
			Map<String, LogCommitInfo> commits) {
		Set<String> fileNames = new HashSet<String>();
		for (NewFileInfo file : files) {
			if (!file.getFiltered())
				fileNames.add(file.getPath());
		}
		Map<String, Set<String>> devMaps = new HashMap<String, Set<String>>();
		devMaps.put("total", new HashSet<String>());
		for (Entry<String, LogCommitInfo> entry : commits.entrySet()) {
			List<LogCommitFileInfo> commitFiles = entry.getValue().getLogCommitFiles();
			String developer = entry.getValue().getUserName();
			if (commitFiles!=null) {
				for (LogCommitFileInfo commitFile : commitFiles) {
					if (fileNames.contains(commitFile.getNewFileName())||fileNames.contains(commitFile.getOldFileName())) {
						String moduleName = LinuxModulesUtility.getModule(commitFile.getNewFileName());
						if (!devMaps.containsKey(moduleName))
							devMaps.put(moduleName, new HashSet<String>());
						devMaps.get(moduleName).add(developer);
						devMaps.get("total").add(developer);
					}
				}
			}
			
		}
		for (Entry<String, Set<String>> entry : devMaps.entrySet()) {
			System.out.println(entry.getKey() + ":" + entry.getValue().size());
		}
		
	}

	private static void printReleaseInfo(List<NewFileInfo> files,
			Map<String, LogCommitInfo> commits) {
		int nCommits = commits.size();
		int nAllFiles = files.size();
		int nSelectedFiles = 0;
		int nDevelopers = getDevelopers(commits).size();
		for(NewFileInfo fileInfo : files){
			if (!fileInfo.getFiltered())
				nSelectedFiles++;
		}
		System.out.format("%s;%d;%d;%d;%d\n", release, nCommits, nAllFiles, nSelectedFiles, nDevelopers);
		// TODO Auto-generated method stub
		
	}

	private static Set<String> getDevelopers(
			Map<String, LogCommitInfo> commits) {
		Set<String> developers = new HashSet<String>();
		for(Entry<String, LogCommitInfo> entry : commits.entrySet()){
			developers.add(entry.getValue().getUserName());
		}
		return developers;
	}

	private static void printFilteredFiles(List<NewFileInfo> files) {
		for (NewFileInfo file : files){
			if (file.getFiltered())
				System.out.println(file.getPath() + ";" + file.getModule());
		}
		
	}

	private static void printModuleSize(Map<String, List<String>> moduleMap) {
		int total = 0;
		for (Entry<String, List<String>> entry : moduleMap.entrySet()) {
			System.out.println(entry.getKey() + ": "+ entry.getValue().size());
			total += entry.getValue().size();
		}
		System.out.println("Total: "+total);
		
	}
	private static void printFilesAuthorship(Repository repository, Map<String, List<LineInfo>> modulesInfo) {
		System.out.println("release;fullname;path;module;usernameId;name;email");
		int devId = 1;
		Map<String, Integer> mapDevId = new HashMap<String, Integer>(); 
		for (Developer dev : repository.getDevelopers()) {
			if (!mapDevId.containsKey(dev.getNewUserName()))
				mapDevId.put(dev.getNewUserName(), devId++);
			for (File file : dev.getAuthorshipFiles()) {
				
				String moduleName = null; 
				if (repository.getFullName().equals("torvalds/linux"))
					moduleName = LinuxModulesUtility.getModule(file.getPath());
				System.out.format("%s;%s;%s;%s;%s;%s;%s\n", release, repository.getFullName(), file.getPath(), moduleName, mapDevId.get(dev.getNewUserName()), dev.getName(), dev.getEmail());
			}
		}
	}

	private static void printDevs(List<Developer> mainDevs,
			List<Developer> allDevs, Map<String, List<String>> moduleMap) {
		int totalFiles = getTotalFiles(allDevs);
		//System.out.println("release;devName;devEmail;devFiles;allFiles;spread;focus");
		DistributionCalculator dc = new DistributionCalculator(moduleMap);
		for (Developer developer : mainDevs) {
			if (developer.getAuthorshipFiles().size()>0){
				DistributionMetrics dMetrics = dc.getDistibutionMetrics(developer);
				System.out.format("%s;%s;%s;%d;%d;%d;%f\n", release, developer.getName(), developer.getEmail(), 
						developer.getAuthorshipFiles().size(), totalFiles, dMetrics.getSpread(), dMetrics.getFocus());
			}
		}
		
	}

	private static void printDevs(List<Developer> mainDevs, List<Developer> allDevs) {
		int totalFiles = getTotalFiles(allDevs);
		//System.out.println("release;devName;devEmail;devFiles;allFiles");
		for (Developer developer : mainDevs) {
			if (developer.getAuthorshipFiles().size()>0)
				System.out.format("%s;%s;%s;%d;%d\n", release, developer.getName(), developer.getEmail(), developer.getAuthorshipFiles().size(), totalFiles);
		}
	}

	private static List<Developer> getMainDevs(Repository repository, int i) {
		//List<Developer> mainDevs = new ArrayList<Developer>();
		List<Developer> allDevs = repository.getDevelopers();
		Collections.sort(allDevs);
		Collections.reverse(allDevs);
		return allDevs.subList(0, i);
	}

	private static List<Developer> getSortDevs(Repository repository) {
		List<Developer> allDevs = repository.getDevelopers();
		Collections.sort(allDevs);
		Collections.reverse(allDevs);
		return allDevs;
	}
	
	private static List<Developer> getSortMainDevs(Repository repository, int nDevs) {
		List<Developer> allDevs = repository.getDevelopers();
		Collections.sort(allDevs);
		Collections.reverse(allDevs);
		return allDevs.subList(0, nDevs);
	}

	private static void filterModule(List<NewFileInfo> files, String module) {
		int count = 0;
		for (NewFileInfo file : files) {
			if (file.getModule().equals(module)){
				file.setFiltered(true);
				file.setFilterInfo("Module-filter");
			}
			else if (!file.getFiltered())
				count++;
		}
		LOGGER.info("Files without "+module + ": "+count);
	}
	private static void filterByModule(List<NewFileInfo> files, String module) {
		int count = 0;
		for (NewFileInfo file : files) {
			if (!file.getModule().equals(module)){
				file.setFiltered(true);
				file.setFilterInfo("Module-filter");
			}
			else if (!file.getFiltered())
				count++;
		}
		LOGGER.info(module + " - files: " +count);
	}

	private static Map<String, List<String>> setModules(List<LineInfo> modulesInfo,
			List<NewFileInfo> files) {
		Map<String, String> fileModuleMap =  new HashMap<String, String>();
		Map<String, List<String>> moduleMap =  new HashMap<String, List<String>>();
		for (LineInfo lineInfo : modulesInfo) {
			String filePath = lineInfo.getValues().get(0);
			String moduleName =lineInfo.getValues().get(1);
			fileModuleMap.put(filePath, moduleName);
			if (!moduleMap.containsKey(moduleName))
				moduleMap.put(moduleName, new ArrayList<String>());
			moduleMap.get(moduleName).add(filePath);
		}
		for (NewFileInfo newFileInfo : files) {
			if (fileModuleMap.containsKey(newFileInfo.getPath()))
				newFileInfo.setModule(fileModuleMap.get(newFileInfo.getPath()));
			else{
				LOGGER.warn("Alert: module not found for file "+newFileInfo.getPath());
				if (!moduleMap.containsKey("NO-MODULE"))
					moduleMap.put("NO-MODULE", new ArrayList<String>());
				moduleMap.get("NO-MODULE").add(newFileInfo.getPath());
				newFileInfo.setModule("NO-MODULE");
				
			}
		}
		return moduleMap;
	}
	
	private static Map<String, List<String>> setLinuxModules(List<NewFileInfo> files) {
		Map<String, String> fileModuleMap =  new HashMap<String, String>();
		Map<String, List<String>> moduleMap =  new HashMap<String, List<String>>();
		for (NewFileInfo newFileInfo : files) {
			String moduleName = LinuxModulesUtility.getModule(newFileInfo.getPath());
			newFileInfo.setModule(moduleName);
			if (moduleName.equals("NO-MODULE")){
				newFileInfo.setFiltered(true);
				newFileInfo.setFilterInfo("NO-MODULE-INFO");
			}
			if (!moduleMap.containsKey(moduleName))
				moduleMap.put(moduleName, new ArrayList<String>());
			moduleMap.get(moduleName).add(newFileInfo.getPath());
			
		
		}
		return moduleMap;
	}

	private static void printCoverage(Repository repository, List<String> devsName, Calendar cal) {
		int nFiles = getTotalFiles(repository.getDevelopers());
		Set<String> devsFiles =  new HashSet<String>();
		for (String devName : devsName) {
			for (Developer developer : repository.getDevelopers()) {
				if (developer.getName().equalsIgnoreCase(devName)){
					devsFiles.addAll(getAuthorFiles(developer));
					
				}
					
			}
		}
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		System.out.println("Coverage;"+ format.format(cal.getTime()) +";"+ devsFiles.size() +";"+ nFiles +";"+ (float)devsFiles.size()/nFiles*100);
	}

	private static int getTotalFiles(List<Developer> developers) {
		Set<String> files = new HashSet<String>();
		for (Developer developer : developers) {
			for (AuthorshipInfo authorship : developer.getAuthorshipInfos()) {
				files.add(authorship.getFile().getPath());
			}
		}
		return files.size();
	}

	private static Set<String> getAuthorFiles(Developer developer) {
		Set<String> paths = new HashSet<String>();
		for (AuthorshipInfo authorship : developer.getAuthorshipInfos()) {
			if (authorship.isDOAAuthor())
				paths.add(authorship.getFile().getPath());
		}		
		return paths;
	}

	private static Map<String, LogCommitInfo> filterCommitsByDate(
			Map<String, LogCommitInfo> commits, Date endDate) {
		Map<String, LogCommitInfo> newCommits =  new HashMap<String, LogCommitInfo>(commits);
		for (Entry<String, LogCommitInfo> entry : commits.entrySet()) {
			if (entry.getValue().getAuthorDate().after(endDate))
				newCommits.remove(entry.getKey());
		}
		return newCommits;
	}

	private static void applyRegexFilter(List<NewFileInfo> files, String exp) {
		int count = 0;
		for (NewFileInfo newFileInfo : files) {
			if (!newFileInfo.getFiltered()){
				if (newFileInfo.getPath().matches(exp)){
					count++;
					newFileInfo.setFiltered(true);
					newFileInfo.setFilterInfo("REGEX: "+ exp);
				}			
			}
		}
		LOGGER.info("REGEX FILTER = " + count);
	}
	
	private static void applyRegexSelect(List<NewFileInfo> files, String exp) {
		int count = 0;
		for (NewFileInfo newFileInfo : files) {
			if (!newFileInfo.getFiltered()){
				if (!newFileInfo.getPath().matches(exp)){
					count++;
					newFileInfo.setFiltered(true);
					newFileInfo.setFilterInfo("REGEX: "+ exp);
				}			
			}
		}
		LOGGER.info("REGEX FILTER = " + count);
	}

	private static void applyFilterFiles(List<LineInfo> filteredFilesInfo, List<NewFileInfo> files) {
		if (filteredFilesInfo != null ){
			for (LineInfo lineInfo : filteredFilesInfo) {
				String path = lineInfo.getValues().get(0);
				for (NewFileInfo newFileInfo : files) {
					if (newFileInfo.getPath().equals(path)){
						newFileInfo.setFiltered(true);
						newFileInfo.setFilterInfo(lineInfo.getValues().get(1));
					}
					
				}
			}
		}
	}

}
