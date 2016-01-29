package aserg.authorship.model;

import java.util.List;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import aserg.gtf.model.AbstractEntity;

public class AuthorshipInfo extends AbstractEntity{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	protected Long id;
	
	private String name;
	private String email;
	private String username;
	
	private List<FileInfo> files;	
	
	public AuthorshipInfo(String name, String email, String username,
			List<FileInfo> files) {
		super();
		this.name = name;
		this.email = email;
		this.username = username;
		this.files = files;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public List<FileInfo> getFiles() {
		return files;
	}

	public void setFiles(List<FileInfo> files) {
		this.files = files;
	}
	
	public int getNumFiles(){
		return files.size();
	}
}
