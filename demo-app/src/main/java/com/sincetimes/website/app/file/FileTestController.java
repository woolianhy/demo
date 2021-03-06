package com.sincetimes.website.app.file;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;

import com.sincetimes.website.app.file.function.MutlipartFileConsumer;
import com.sincetimes.website.core.common.support.LogCore;
import com.sincetimes.website.core.common.support.Sys;
import com.sincetimes.website.core.common.support.TimeTool;
import com.sincetimes.website.core.spring.controller.ControllerInterface;
import com.sincetimes.website.core.spring.manger.SpringManager;
@RestController
@Order(value = 6)
public class FileTestController implements ControllerInterface {
	
	@RequestMapping("/upload_file_simple2")
	Object upload_file_simple(@RequestParam Optional<StandardMultipartHttpServletRequest> freq){
		
		if(!freq.isPresent()){
			return "not a fileupload request";
		}
		return FileManager.inst().upload_file_simple(freq.get(), "simple");
	}
	@RequestMapping("/upload_file_simple")
	Object upload_file_simple2(StandardMultipartHttpServletRequest req, HttpServletResponse resp){
		String dirName = Objects.toString(req.getParameter("dir"), "image");
		String ymdPath = TimeTool.formatTime(System.currentTimeMillis(), "yyyy_MM_dd")+"/";
		String dirPath = "upload/";
		String rootPath = req.getServletContext().getRealPath("/");
		if(Sys.isLinux()){
			rootPath = SpringManager.inst().upload_path;
		}
		String filePath = dirPath + dirName+ "/"+ ymdPath;
		String saveUrl = req.getContextPath() + "/" + filePath;
		
		String realpath = rootPath + filePath;
		
		List<String> names = new ArrayList<>();
		MutlipartFileConsumer consume = (m)->{
			FileManager.inst().save(realpath, m).ifPresent(f->names.add(f.getName()));
		};
		
		List<String> codes = new ArrayList<>();
		MutlipartFileConsumer consume2 = (m)->{
			List<String> rst = FileManager.inst().readFileLines(m);
			codes.addAll(rst);
		};
		
		Map<String, MutlipartFileConsumer> comsumeMap = new HashMap<>();
		comsumeMap.put("code_file", consume2);
		comsumeMap.put("file", consume);
		
		FileManager.inst().handle_multi_file(comsumeMap, req);
		saveUrl += names.get(0);
		LogCore.BASE.info("{},{}",names,codes.stream().findFirst());
		return saveUrl + names.toString() + codes.toString();
	}
}
