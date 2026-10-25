package com.sklad.skladproject.controllers

import com.sklad.skladproject.dto.PackageRequestDto
import com.sklad.skladproject.services.PackageService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/")
class PackageController(
    val packageService: PackageService
) {
    private val logger = LoggerFactory.getLogger("PackageController")

    @ResponseBody
    @PostMapping("/v1/add-package")
    fun addPackage(@RequestBody packageRequest: PackageRequestDto): Boolean {
        logger.info("Handling add package request $packageRequest")

        val packageDomain = packageService.convertToPackage(packageRequest)
        val result = packageService.savePackage(packageDomain)

        return result.isSuccess
    }
}
