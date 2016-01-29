package aserg.gtf.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

public class LinuxModulesUtility {
	private static final Logger LOGGER = Logger.getLogger(LinuxModulesUtility.class);
	
	static private List<ModulePattern> linuxModules = new ArrayList<LinuxModulesUtility.ModulePattern>();
	static String core[] = {"init", "block", "ipc", "kernel", "lib", "mm", "virt",    
					 "include/linux", "include/keys", "include/trace", "include/uapi", "include/Kbuild"};
	static String fs[] = {"fs"};
	static String driver[] = {"crypto", "drivers", "sound", "security",
					   "include/acpi", "include/clocksource", "include/crypto", "include/drm", "include/media", "include/mtd", "include/pcmcia", "include/target", "include/rdma", "include/rxrpc", "include/scsi", "include/ras", "include/sound", "include/kvm", "include/soc", "include/video"};
	static String net[] = {"net",   
					"include/net"};
	static String arch[] = {"arch",
					 "include/xen", "include/math-emu", "include/dt-bindings","include/asm-"};
	static String firmware[] = {"firmware"};
	static String misc[] = {"Documentation", "scripts", "samples", "usr", "MAINTAINERS", "CREDITS", "README", ".gitignore", "Kbuild", "Makefile", "REPORTING-BUGS", ".mailmap", "COPYING", "tools", "Kconfig"};
	
	static private Map<String, String> moduleMapPattern = null;
	
	static public void initLinuxModulesUtility() {
		moduleMapPattern = new HashMap<String, String>();
//		linuxModules.add(new ModulePattern("include", include));
//		linuxModules.add(new ModulePattern("core", core));
//		linuxModules.add(new ModulePattern("fs", fs));
//		linuxModules.add(new ModulePattern("driver", driver));
//		linuxModules.add(new ModulePattern("net", net));
//		linuxModules.add(new ModulePattern("arch", arch));
//		linuxModules.add(new ModulePattern("firmware", firmware));
//		linuxModules.add(new ModulePattern("misc", misc));
		addToMap("core", core);
		addToMap("fs", fs);
		addToMap("driver", driver);
		addToMap("net", net);
		addToMap("arch", arch);
		addToMap("firmware", firmware);
		addToMap("misc", misc);
	}
	
	static private void addToMap(String moduleName, String[] patterns) {
		for (String pattern : patterns) {
			moduleMapPattern.put(pattern, moduleName);
		}
		
	}
	
	static public String getModule(String path){
		if (moduleMapPattern == null)
			initLinuxModulesUtility();
		for (Entry<String, String> entry : moduleMapPattern.entrySet()) {
			if (path.indexOf(entry.getKey())==0)
				return entry.getValue();
		}
		LOGGER.warn("Alert: module not found for file "+path);
		return "NO-MODULE";
	}

	class ModulePattern{
		String name;
		List<String> patterns;
		public ModulePattern(String name, String[] patterns) {
			super();
			this.name = name;
			this.patterns = Arrays.asList(patterns);
		}
		
	}
	

}
