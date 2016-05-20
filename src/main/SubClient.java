package main;

import java.io.Serializable;
import java.util.List;

import com.sun.org.apache.regexp.internal.recompile;

public class SubClient implements Serializable {

	private String ip;
	private String name;
	private String filepath;
	private List<String> list;
	
	public SubClient(String ip, String name, String filepath, List list) {
		this.ip = ip;
		this.name = name;
		this.filepath = filepath;
		this.list = list;
	}
	public String getIP() {
		return ip;
	}
	public void setIP(String ip) {
		this.ip = ip;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String> getList() {
		return list;
	}
	public void setList(List<String> list) {
		this.list = list;
	}
	public String getFilepath() {
		return this.filepath;
	}
	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}
}
