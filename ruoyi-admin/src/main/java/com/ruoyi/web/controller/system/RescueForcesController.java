package com.ruoyi.web.controller.system;


import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.domain.entity.RescueForces;
import com.ruoyi.system.service.RescueForcesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/system")
public class RescueForcesController {
    @Autowired
    private RescueForcesService rescueForcesService;



    @GetMapping("/rescueforces")
    public List<RescueForces> rescueforces(@RequestParam String eqid) {
        return rescueForcesService.RescueForcesByEqId(eqid);
    }

    @GetMapping("/fromRescueForces")
    public AjaxResult fromRescueForces(@RequestParam("eqid") String eqid,
                                       @RequestParam("time") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime time) {
        List<RescueForces> rescueForcesList = rescueForcesService.fromRescueForces(eqid, time);
        return AjaxResult.success(rescueForcesList);
    }

}
