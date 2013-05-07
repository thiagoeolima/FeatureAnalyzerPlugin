package br.ufal.ic.featureanalyzer.activator;

import java.util.LinkedList;
import java.util.List;

public class ProjectErrorLogger {

	private static ProjectErrorLogger INSTANCE;
	private List<String> projectsName;
	private ProjectErrorLogger() {
		projectsName = new LinkedList<String>();
	}
	
	public void clearLogList(){
		projectsName.clear();
	}
	
	public List<String> getProjectsList(){
		return new LinkedList<String>(projectsName);
	}
	
	public void addProject(String projectName){
		if(!projectsName.contains(projectName)){
			projectsName.add(projectName);
		}
	}
	
	
	public static ProjectErrorLogger getInstance(){
		if(INSTANCE == null){
			INSTANCE = new ProjectErrorLogger();
		}
		return INSTANCE;
	}
}
