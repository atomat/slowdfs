package com.hcb168.slowdfs.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hcb168.slowdfs.util.MyUtil;

@Controller
public class Health {
	@ResponseBody
	@RequestMapping(value = "/healthcheck", method = { RequestMethod.GET,
			RequestMethod.POST }, produces = "text/html;charset=UTF-8")
	public String healthCheck() throws Exception {
		return MyUtil.getReturnSucc("ok");
	}
}
