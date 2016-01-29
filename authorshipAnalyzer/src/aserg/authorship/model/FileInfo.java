package aserg.authorship.model;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import aserg.gtf.model.AbstractEntity;

public class FileInfo extends AbstractEntity{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	protected Long id;
	String path;
	
	public FileInfo(String path) {
		this.path = path;
	}
}
