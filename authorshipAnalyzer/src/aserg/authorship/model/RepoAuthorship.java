package aserg.authorship.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import aserg.gtf.model.AbstractEntity;

public class RepoAuthorship  extends AbstractEntity{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	protected Long id;
	
	private String name;	
	
	@OneToMany(cascade = { CascadeType.ALL })
	private List<VersionAuthorshipInfo> versions;
	
	public RepoAuthorship(String name) {
		this.name = name;
		versions = new ArrayList<VersionAuthorshipInfo>();		
	}
	
	public void addVersionInfo(VersionAuthorshipInfo version){
		this.versions.add(version);
	}
	
}
