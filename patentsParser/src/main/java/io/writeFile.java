/**
 * 
 */
package io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author mofeed
 *
 */
public class writeFile {
	public static void write(Map<String,Set<String>> data ,String fileName,String separator)
	{	
		FileWriter fw = null;
		BufferedWriter bw =null;
		try {
		
			File file = new File(fileName);
			if (!file.exists()) {
				file.createNewFile();
			}
			fw = new FileWriter(file.getAbsoluteFile());
			bw = new BufferedWriter(fw);
			String line= "";
			for (String type : data.keySet()) {
				line=type+separator;
				for (String code : data.get(type)) {
					line+=code+separator;
				}
				bw.write(line+"\n");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			try {
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
	
	public static void write(Map<String,String> data ,String fileName)
	{	
		FileWriter fw = null;
		BufferedWriter bw =null;
		try {
		
			File file = new File(fileName);
			if (!file.exists()) {
				file.createNewFile();
			}
			fw = new FileWriter(file.getAbsoluteFile());
			bw = new BufferedWriter(fw);
			for (String key : data.keySet()) {
				bw.write(key+":"+data.get(key)+"\n");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			try {
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
	public static void write(List<String> data ,String fileName)
	{	
		FileWriter fw = null;
		BufferedWriter bw =null;
		try {
		
			File file = new File(fileName);
			if (!file.exists()) {
				file.createNewFile();
			}
			fw = new FileWriter(file.getAbsoluteFile());
			bw = new BufferedWriter(fw);
			for (String line : data) {
				bw.write(line+"\n");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			try {
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
}
