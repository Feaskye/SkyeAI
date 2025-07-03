package com.example.mcpdemo.common;


import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

//工具类 z
@Service
public class DateTimeTool {

    private static final Map<String,String> COUNTRY_MAP=Map.of(
            "c1","2020-02-01 12:00:00",
            "c2","2020-02-01 13:00:00"  );
 @Tool(description ="国家时间查询工具")
 public String getCurrentDateTimeByCountry(String country){
     return COUNTRY_MAP.getOrDefault(country,
             LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));  }

}
