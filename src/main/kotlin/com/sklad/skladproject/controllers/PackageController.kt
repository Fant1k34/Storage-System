package com.sklad.skladproject.controllers

import com.sklad.skladproject.dto.PackageDto
import com.sklad.skladproject.dto.PackageRequestDto
import com.sklad.skladproject.services.PackageService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
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

    @ResponseBody
    @GetMapping("/v1/get-all-packages")
    fun getAllPackages(): List<PackageDto> {
        logger.info("Handling get all packages request")

        return packageService.getAllPackages()
    }

    // Package name is not unique (unique key is name + weight + unit), so a list is returned
    @ResponseBody
    @GetMapping("/v1/get-package")
    fun getPackages(@RequestParam("name") name: String): List<PackageDto> {
        logger.info("Handling get packages request for name $name")

        return packageService.getPackagesByName(name)
    }
}
