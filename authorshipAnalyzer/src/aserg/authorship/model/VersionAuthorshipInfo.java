package aserg.authorship.model;

import java.util.List;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import aserg.gtf.model.AbstractEntity;

public class VersionAuthorshipInfo extends AbstractEntity{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	protected Long id;
	
	private String tag;
	
	List<AuthorshipInfo> devs;
}
