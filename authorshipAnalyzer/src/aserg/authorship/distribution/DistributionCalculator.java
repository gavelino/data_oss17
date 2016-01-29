package aserg.authorship.distribution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import aserg.gtf.model.authorship.Developer;
import aserg.gtf.model.authorship.File;

public class DistributionCalculator{
	Map<String, List<String>> moduleMap;
	public DistributionCalculator(Map<String, List<String>> moduleMap) {
		Map<String, List<String>> newModuleMap =  new HashMap<String, List<String>>(moduleMap);
		newModuleMap.remove("NO-MODULE");
		this.moduleMap = newModuleMap;
	}
	
	public DistributionMetrics getDistibutionMetrics(Developer dev){
		Map<String, List<String>> devModuleMap = new HashMap<String, List<String>>();
		for (File file : dev.getAuthorshipFiles()) {
			if (file.getModule().equals("NO-MODULE"))
				continue;
			if (!devModuleMap.containsKey(file.getModule()))
				devModuleMap.put(file.getModule(), new ArrayList<String>());
			devModuleMap.get(file.getModule()).add(file.getPath());				
		}
		int spread = devModuleMap.size();

		double focus = 0;
		for (Entry<String, List<String>> entry : devModuleMap.entrySet()) {
			int nDevFiles = dev.getAuthorshipFiles().size();
			int nModFiles = moduleMap.get(entry.getKey()).size();
			int nDevModFiles = entry.getValue().size();
			focus += ((double)nDevModFiles/nModFiles) * ((double)nDevModFiles/nDevFiles); 
		}
		return new DistributionMetrics(spread, focus);
	}
}
